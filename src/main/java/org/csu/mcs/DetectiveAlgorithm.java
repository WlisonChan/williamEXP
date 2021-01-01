package org.csu.mcs;

import lombok.extern.slf4j.Slf4j;
import org.csu.kmeans.Point;

import java.util.List;

@Slf4j
public class DetectiveAlgorithm {

    //parameter setting
    public static double budget = 0;
    // cost coefficient - ai
    public static final double COST_COEFFICIENT = 0.5;
    // distance coefficient - bi
    public static final double DISTANCE_COEFFICIENT = 1.0;

    public static void taskSelection(List<Agent> agentList, List<Point> taskList) {
        while (true) {
            boolean flag = true;
            initAgentState(agentList);
            initTaskStatus(taskList);
            for (int i = 0; i < agentList.size(); i++) {
                Agent agent = agentList.get(i);
                int cur = -1;
                double maxProfit = Integer.MIN_VALUE;
                for (int j = 0; j < taskList.size(); j++) {
                    Point task = taskList.get(j);
                    if (task.getAgent() != null) {
                        continue;
                    }
                    double tempProfit = task.getReward() - agent.getCost(task);
                    //System.out.println(maxProfit+"====================");
                    if (tempProfit > maxProfit) {
                        maxProfit = tempProfit;
                        cur = j;
                    }
                }
                if (cur < 0) {
                    continue;
                }
                Point task = taskList.get(cur);
                //System.out.println(maxProfit);
                if (maxProfit < 0) {
                    Point anotherTask = findAnotherTask(task, taskList);
                    double anotherProfit = anotherTask.getReward() - calCost4Task(task, anotherTask);
                    if (maxProfit + anotherProfit > 0) {
                        flag = false;
                        task.getAgentList().add(agent);
                        anotherTask.getAgentList().add(agent);
                        agent.getTaskSet().add(task);
                        agent.getTaskSet().add(anotherTask);
                        agent.setProfit(maxProfit + anotherProfit);
                    }
                } else {
                    flag = false;
                    task.getAgentList().add(agent);
                    agent.getTaskSet().add(task);
                    //System.out.println(agent.getId()+" 2 "+task.getId()+" ");
                    agent.setProfit(maxProfit);
                }
            }
           /* for (int i = 0; i < agentList.size(); i++) {
                Agent agent = agentList.get(i);
                System.out.println("aid: "+agent.getId());
                for (int j = 0; j < agent.getTaskSet().size(); j++) {
                    System.out.println(agent.getTaskSet().get(j).getId());
                }
            }
            for (int i = 0; i < taskList.size(); i++) {
                Point task = taskList.get(i);
                System.out.println("task id : "+ task.getId());
                for (int j = 0; j < task.getAgentList().size(); j++) {
                    System.out.println(task.getAgentList().get(j).getId());
                }
            }*/
            flag = selectWinner(taskList);
            if (!flag) {
                break;
            }
        }
    }

    public static boolean selectWinner(List<Point> taskList) {
        boolean flag = false;
        for (int i = 0; i < taskList.size(); i++) {
            Point task = taskList.get(i);
            if (task.getAgentList().size() == 0 || task.getAgent() != null) {
                continue;
            }
            List<Agent> agentList = task.getAgentList();
            int cur = -1;
            double maxProfit = Integer.MIN_VALUE;
            for (int j = 0; j < agentList.size(); j++) {
                Agent agent = agentList.get(j);
                double reward = agent.getTaskSet().stream().mapToDouble(Point::getReward).sum();
                if (agent.getProfit() > maxProfit && budget >= reward) {
                    maxProfit = agent.getProfit();
                    cur = j;
                }
            }
            if (cur >= 0) {
                flag = true;
                Agent winner = agentList.get(cur);
                completeTask(winner);
                updateLocation(winner);
            }
        }
        return flag;
    }

    public static void completeTask(Agent agent) {
        List<Point> taskSet = agent.getTaskSet();
        //System.out.println(taskSet.size());
        //taskSet.stream().forEach(e -> e.setAgent(agent));
        double reward = 0;
        for (int i = 0; i < taskSet.size(); i++) {
            Point task = taskSet.get(i);
            task.setAgent(agent);
            reward+=task.getReward();
            agent.getBidSet().add(task.getReward());
            agent.getCostSet().add(agent.getCost(task));
            agent.getTaskDASet().add(task);
            //System.out.println(agent.getTaskDASet());

            budget-=task.getReward();
        }
        agent.setPay(agent.getPay()+reward);
        if (taskSet.size() > 1) {
            log.info("The tasks' id are [{}] and [{}] which is completed by agent [{}]",
                    taskSet.get(0).getId(), taskSet.get(1).getId(), agent.getId());
        } else {
            log.info("The task id is [{}] which is completed by agent [{}]",
                    taskSet.get(0).getId(), agent.getId());
        }
    }

    public static void updateLocation(Agent agent) {
        List<Point> taskSet = agent.getTaskSet();
        Point task = taskSet.get(taskSet.size() - 1);
        agent.setX(task.getX());
        agent.setY(task.getY());
    }

    public static Point findAnotherTask(Point task, List<Point> taskList) {
        double minCost = Double.MAX_VALUE;
        Point cur = null;
        for (int i = 0; i < taskList.size(); i++) {
            Point taskB = taskList.get(i);
            if (task.getId() == taskB.getId() || taskB.getAgent() != null) {
                continue;
            }
            double v = calCost4Task(task, taskB);
            if (v < minCost) {
                minCost = v;
                cur = taskB;
            }
        }
        return cur;
    }

    public static double calCost4Task(Point taskA, Point taskB) {
        // calculate the distance between two tasks;
        double d = Math.pow(taskA.getX() - taskB.getX(), 2) + Math.pow(taskA.getY() - taskB.getY(), 2);
        d = Math.sqrt(d);
        return COST_COEFFICIENT + DISTANCE_COEFFICIENT * d;
    }

    public static void initAgentState(List<Agent> agentList) {
        agentList.stream()
                .forEach(e ->
                {
                    e.getTaskSet().clear();
                    e.setProfit(0);
                });
    }

    public static void initTaskStatus(List<Point> taskList){
        taskList.stream().forEach(e->e.getAgentList().clear());
    }

}
