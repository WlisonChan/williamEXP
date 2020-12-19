package org.csu.mcs;

import lombok.extern.slf4j.Slf4j;
import org.csu.kmeans.Point;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Slf4j
public class DetectiveAlgorithm {

    // cost coefficient - ai
    public static final double COST_COEFFICIENT = 1.0;
    // distance coefficient - bi
    public static final double DISTANCE_COEFFICIENT = 1.0;

    public static void taskSelection(List<Agent> agentList, List<Point> taskList) {
        while (true) {
            boolean flag = true;
            initAgentState(agentList);
            for (int i = 0; i < agentList.size(); i++) {
                Agent agent = agentList.get(i);
                int cur = -1;
                double maxProfit = Double.MIN_VALUE;
                for (int j = 0; j < taskList.size(); j++) {
                    Point task = taskList.get(j);
                    if (task.getAgent() != null) {
                        continue;
                    }
                    double tempProfit = task.getValue() - agent.getCost(task);
                    if (tempProfit > maxProfit) {
                        maxProfit = tempProfit;
                        cur = j;
                    }
                }
                if (cur < 0) {
                    continue;
                }
                Point task = taskList.get(cur);
                if (maxProfit < 0) {
                    Point anotherTask = findAnotherTask(task, taskList);
                    double anotherProfit = anotherTask.getValue() - calCost4Task(task, anotherTask);
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
                    agent.setProfit(maxProfit);
                }
            }
            if (flag) {
                break;
            }
            selectWinner(taskList);
        }
    }

    public static void selectWinner(List<Point> taskList) {
        for (int i = 0; i < taskList.size(); i++) {
            Point task = taskList.get(i);
            if (task.getAgentList().size() == 0 || task.getAgent() != null) {
                continue;
            }
            List<Agent> agentList = task.getAgentList();
            int cur = 0;
            double maxProfit = agentList.get(0).getProfit();
            for (int j = 1; j < agentList.size(); j++) {
                Agent agent = agentList.get(j);
                if (agent.getProfit() > maxProfit) {
                    maxProfit = agent.getProfit();
                    cur = j;
                }
            }
            Agent winner = agentList.get(cur);
            completeTask(winner);
            updateLocation(winner);
        }
    }

    public static void completeTask(Agent agent) {
        List<Point> taskSet = agent.getTaskSet();
        taskSet.stream().forEach(e -> e.setAgent(agent));
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
            if (task.getId() == taskB.getId()) {
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

}
