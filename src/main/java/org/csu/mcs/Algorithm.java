package org.csu.mcs;

import lombok.extern.slf4j.Slf4j;
import org.csu.kmeans.Point;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
public class Algorithm {
    //parameter setting
    public static double budget = 0;

    // PSRD's parameter
    public static final double VAL_I = 1.03;
    public static final double QUL_I = 0.81;
    public static final double D_QUALITY = Main.Z_LIMIT * 2;

    public static final double PSRD_A = 0.3;
    public static final double PSRD_B = 1 - PSRD_A;
    public static final double PSRD_E = 0.05;


    // cal avg quality of tasks
    public static double avgQuality = 0;
    // cal avg value of tasks
    public static double avgValue = 0;
    // history's avgQuality
    public static List<Double> hAvgQ;

    // the advice price of platform.
    public static double r = 0;
    public static double sumWinner = 0;

    // agents' gamma
    public static double gamma = 0.5;

    // ITSI's time
    public static final double ITSI_T = 10;
    public static final double ITSI_GAMMA = 0.03;
    public static final double ITSI_TS = 100;

    static {
        hAvgQ = new ArrayList<>();
    }

    // cal avg quality of tasks
    public static void calAvgQuality(List<Point> taskList) {
        avgQuality = taskList.stream().mapToDouble(Point::getQuality).average().orElse(0D);
        hAvgQ.add(avgQuality);
        log.info("The avgQuality's value is [{}]", avgQuality);
    }

    public static void calAvgValue(List<Point> taskList) {
        avgValue = taskList.stream().mapToDouble(Point::getValue).average().orElse(0D);
        log.info("The avgValue's value is [{}]", avgValue);
    }

    public static void selectTasks(List<Point> taskList, List<Agent> agentList) throws IOException, ClassNotFoundException {
        while (true) {
            boolean flag = false;
            initAgentState(agentList);
            //System.out.println(agentList.size());
            for (int i = 0; i < agentList.size(); i++) {
                Agent agent = agentList.get(i);
                //System.out.println("id :"+agent.getId()+" ");
                double maxFi = 0;
                Point cur = null;
                //List<Point> temp = copyTask(taskList);
                for (int j = 0; j < taskList.size(); j++) {
                    Point point = taskList.get(j);
                    //System.out.print(point.getId()+" ");
                    if (point.getAgent() != null) {
                        // this task has been completed.
                        continue;
                    }
                    double fi = calTaskFi(agent, point);
                    //System.out.println(point.getId()+" "+fi);
                    if (fi > maxFi && point.getValue() - agent.getCost(point) > 0) {
                        //System.out.print( " r- "+agent.getId()+" "+point.getId()+" ");
                        maxFi = fi;
                        cur = point;
                    }
                }
                if (cur != null) {
                    cur.getAgentList().add(agent);
                    double cost = agent.getCost(cur);
                    Random random = new Random();

                    double bid = (1 + random.nextDouble() * 0.1) * cost;
                    if (Main.z == 1 && agent.getForeBidSet().size() != 0) {
                        double bSum = 0;
                        List<List<Double>> bidSet = agent.getForeBidSet();
                        for (int j = bidSet.size() - 1; j >= 0; j--) {
                            double curSum = bidSet.get(j).stream().mapToDouble(Double::doubleValue).sum();
                            bSum += Math.pow(agent.getGamma(), bidSet.size() - j) * curSum;
                        }
                        /*
                        for (int j = 0; j < bidSet.size(); j++) {
                            double curSum = bidSet.get(j).stream().mapToDouble(Double::doubleValue).sum();
                            bSum += Math.pow(agent.getGamma(), Main.z - 1 + j) * curSum;
                            System.out.println(" bsum " + bSum);
                        }*/
                        bid = cost + bSum;
                        //System.out.println("bsum    " + bSum + "   cost   " + cost);
                        agent.setBid(bid);
                    } else {
                        agent.setBid(bid);
                    }

                }
            }

            /*
            for (int i = 0; i < taskList.size(); i++) {
                Point point = taskList.get(i);
                int size = point.getAgentList().size();
                System.out.println("taskId :"+point.getId()+" az:"+size);
            }
            */

            //printTask(taskList);
            calR(agentList);
            flag = selectWinner(taskList);
            if (!flag) {
                break;
            }
        }
    }

    public static Boolean selectWinner(List<Point> taskList) {
        boolean flag = false;
        for (int i = 0; i < taskList.size(); i++) {
            Point task = taskList.get(i);
            List<Agent> agentList = task.getAgentList();
            if (agentList.size() == 0 || task.getAgent() != null) {
                continue;
            }
            int cur = -1;
            double cost = Double.MAX_VALUE;
            for (int j = 0; j < agentList.size(); j++) {
                Agent agent = agentList.get(j);
                if (agent.getCost(task) < cost && agent.getBid() < task.getValue() && budget - agent.getBid() >=0) {
                    cur = j;
                    cost = agent.getCost(task);
                }
            }
            if (cur >= 0) {
                Agent winner = agentList.get(cur);
                budget-=winner.getBid();
                task.setAgent(winner);
                winner.getTaskSet().add(task);
                winner.getCostSet().add(cost);
                winner.setHd(winner.getHd() + 1);
                updateReward(task, winner);
                if (task.getQuality() >= avgQuality) {
                    winner.getHdTask().add(task.getValue());
                }
                updateLocation(winner, task);

                log.info("The task id is [{}] which is completed by agent [{}]", task.getId(), winner.getId());
                flag = true;
            }
        }
        return flag;
    }

    public static void refreshBidSet(List<Agent> agentList) {
        agentList.stream().forEach(e -> e.getForeBidSet().add(e.getThisRoundBidSet()));
    }

    public static void updateReward(Point task, Agent agent) {
        double res = 0;
        //System.out.println(" distance " + task.getX() + " " + task.getY() + " " + agent.getX() + " " + agent.getY());
        //System.out.println("origin    " + agent.getBid() + "      cost     " + agent.getCost(task) + "     r     " + r);
        if (agent.getBid() > r) {
            double cost = agent.getCost(task);
            double sigma = Math.max(0.8, cost / agent.getBid());
            double bSum = agent.getBid() - cost;
            res = sigma * (cost + bSum) + 2 * (1 - sigma) * bSum * Math.atan(agent.getBid() - r) / Math.PI;
            //System.out.println("res1 " + res);
        } else {
            res = agent.getBid();
            //System.out.println("res2 " + res);
        }
        //System.out.println("res      ====================================================================================" + res);
        agent.setCost(agent.getCost(task) + agent.getCost());
        agent.setBid(res);
        agent.getBidSet().add(res);
        agent.getThisRoundBidSet().add(res);
    }

    public static void payForAgent(Agent agent) {
        Random random = new Random();
        List<Double> bidSet = agent.getBidSet();
        double sumBid = 0;
        for (int i = 0; i < bidSet.size(); i++) {
            Double temp = bidSet.get(i);
            sumBid += temp;
        }
        double pExtra = sumBid * (itsiCal(ITSI_TS) - itsiCal(ITSI_TS + ITSI_T));
        //System.out.println(" --------- "+agent.getBid()+"    "+itsiCal(ITSI_TS)+"    "+itsiCal(ITSI_TS + ITSI_T));
        double pCeil = Math.min(sumBid - agent.getCost(), sumBid * (1 - itsiCal(ITSI_T)));
        double p = pExtra + (pCeil - pExtra) * random.nextDouble();
        log.info("id:[{}] extra:[{}] pCeil:[{}] p:[{}]", agent.getId(), pExtra, pCeil, p);
        if (random.nextBoolean()) {
            agent.setPay(sumBid + pExtra - p);
        } else {
            agent.setPay(sumBid + pExtra);
        }
        getExtraEi(agent);
    }

    public static void getExtraEi(Agent agent) {
        if (agent.getHd() > D_QUALITY) {
            double culQ = agent.getHdTask().stream().mapToDouble(Double::doubleValue).sum();
            double res = PSRD_E * agent.getHd() * Math.log(1 + culQ);
            agent.setEi(res);
            //log.info("agent's id [{}] get Ei [{}]",agent.getId(),res);
        }
    }

    public static double itsiCal(double x) {
        double res = Math.pow(Math.E, -ITSI_GAMMA * x);
        //System.out.println("res  = "+res);
        return res;
    }

    public static void calR(List<Agent> agentList) {
        double avg = agentList.stream()
                .filter(e -> e.getBid() != 0)
                .mapToDouble(Agent::getBid)
                .average().orElse(0D);
        double wins = agentList.stream()
                .filter(e -> e.getBid() != 0)
                .count();
        r = avg;
        /*if (Main.z == 1) {
            r = avg;
        } else {
            r = (r * sumWinner + avg) / (sumWinner + wins);
        }
        sumWinner += wins;*/
    }

    public static void updateLocation(Agent agent, Point task) {
        agent.setX(task.getX());
        agent.setY(task.getY());
    }

    public static double calTaskFi(Agent agent, Point task) {
        double v = task.getValue();
        //System.out.printf(" v:%.3f ",v);
        if (task.getQuality() >= avgQuality) {
            double e = Main.Z_LIMIT * Math.log(1 + (Main.QUALITY_LIMIT + 5 - avgQuality) * Main.Z_LIMIT);
            //System.out.printf(" e :%.3f",e );
            v += e * agent.getHd() / D_QUALITY;
            //System.out.printf(" refresh v:%.3f ",v);
        }
        double res = PSRD_A * (Math.pow(agent.getValI(), task.getValue() - avgValue) /* + agent.getKi()*/) * v
                + PSRD_B * (Math.pow(agent.getQuaI(), Math.abs(task.getQuality() - avgQuality)) /* + agent.getLi() */) * task.getQuality();
        //System.out.printf("res %.3f %.3f\n",
        // (Math.pow(VAL_I,task.getReward()-avgValue) * v),(Math.pow(QUL_I,Math.abs(task.getQuality()-avgQuality)) * task.getQuality()));
        //System.out.println();
        return res;
    }

    public static void initAgentState(List<Agent> agentList) {
        //init bid
        agentList.stream().forEach(e -> e.setBid(0));
        agentList.stream().forEach(e -> e.getThisRoundBidSet().clear());
    }

    public static void printTask(List<Point> taskList) {
        taskList.stream().forEach(e -> System.out.println(e.getId() + " " + e.getQuality() + " " + e.getValue()));
    }

    public static void printAgent(List<Agent> agentList) {
        agentList.stream().forEach(e -> System.out.println(e));
    }

}
