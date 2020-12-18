package org.csu.mcs;

import lombok.extern.slf4j.Slf4j;
import org.csu.kmeans.Cluster;
import org.csu.kmeans.Kmeans;
import org.csu.kmeans.KmeansModel;
import org.csu.kmeans.Point;
import sun.management.resources.agent;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
public class Algorithm {
    //parameter setting

    // PSRD's parameter
    public static final double VAL_I = 1.03;
    public static final double QUL_I = 0.81;
    public static final double D_QUALITY = 5;

    public static final double PSRD_A = 0.3;
    public static final double PSRD_B = 1-PSRD_A;


    // cal avg quality of tasks
    public static double avgQuality = 0;
    // cal avg value of tasks
    public static double avgValue = 0;

    // the round of mcs
    public static int z = 1;
    public static final double Z_LIMIT = 6;

    // the advice price of platform.
    public static double r = 0;
    public static double sumWinner = 0;

    // agents' gamma
    public static double gamma = 0.5;

    // ITSI's time
    public static final double ITSI_T = 10;
    public static final double ITSI_GAMMA = 0.03;
    public static final double ITSI_TS = 100;

    // cal avg quality of tasks
    public static void calAvgQuality(List<Point> taskList){
        avgQuality = taskList.stream().mapToDouble(Point::getQuality).average().orElse(0D);
        log.info("The avgQuality's value is [{}]",avgQuality);
    }

    public static void calAvgValue(List<Point> taskList){
        avgValue = taskList.stream().mapToDouble(Point::getValue).average().orElse(0D);
        log.info("The avgValue's value is [{}]",avgValue);
    }

    public static void selectTasks(List<Point> taskList,List<Agent> agentList) throws IOException, ClassNotFoundException {
        while (true) {
            int k = 0;
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
                    if (point.getAgent() != null){
                        // this task has been completed.
                        continue;
                    }
                    double fi = calTaskFi(agent, point);
                    //System.out.println(point.getId()+" "+fi);
                    if (fi > maxFi && point.getValue() - agent.getCost(point) > 0){
                        //System.out.print( " r- "+agent.getId()+" "+point.getId()+" ");
                        maxFi = fi;
                        cur = point;
                    }
                }
                if (cur!=null){
                    cur.getAgentList().add(agent);
                    double cost = agent.getCost(cur);
                    Random random = new Random();
                    agent.setBid((1+random.nextDouble()*0.1)*cost);

                    k=1;
                }
                //System.out.println();
            }

/*            for (int i = 0; i < taskList.size(); i++) {
                Point point = taskList.get(i);
                int size = point.getAgentList().size();
                System.out.println("taskId :"+point.getId()+" az:"+size);
            }*/

            //printTask(taskList);
            calR(agentList);
            selectWinner(taskList);
/*            try {
                System.out.println("-------------------sleep 1s-------------------");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            if (k==0){
                break;
            }
        }
    }

    public static void selectWinner(List<Point> taskList){
        for (int i = 0; i < taskList.size(); i++) {
            Point task = taskList.get(i);
            List<Agent> agentList = task.getAgentList();
            if (agentList.size() == 0 || task.getAgent() != null){
                continue;
            }
            int cur = 0;
            double cost = agentList.get(0).getCost(task);
            for (int j = 1; j < agentList.size(); j++) {
                Agent agent = agentList.get(j);
                if (agent.getCost(task) < cost && agent.getBid() < task.getValue()){
                    cur = j;
                }
            }
            Agent winner = agentList.get(cur);
            task.setAgent(winner);
            updateLocation(winner,task);

            log.info("The task id is [{}] which is completed by agent [{}]", task.getId(),winner.getId());
        }
    }

    public static void updateReward(Point task,Agent agent){
        double res = 0;
        if (agent.getBid() > r ){
            double cost = agent.getCost(task);
            double xigema = Math.max(0.8,cost/agent.getBid());
            double bSum = 0;
            List<Double> bidSet = agent.getBidSet();
            for (int i = 0; i < bidSet.size(); i++) {
                bSum += Math.pow(agent.getGamma(),z-1-i)  * bidSet.get(i);
            }
            res = xigema * (cost+bSum)+2*(1-xigema) * bSum *Math.atan(agent.getBid()-r) /Math.PI;
        }else {
            res = agent.getBid();
        }

        agent.setCost(agent.getCost(task)+agent.getCost());
        agent.setBid(res);
    }

    public static void payForAgent(Agent agent){
        Random random = new Random();
        List<Double> bidSet = agent.getBidSet();
        double sumBid = 0;
        for (int i = 0; i < bidSet.size(); i++) {
            Double temp = bidSet.get(i);
            sumBid+=temp;
        }
        double pExtra = agent.getBid() * (itsiCal(ITSI_TS) - itsiCal(ITSI_TS+ITSI_T));
        double pCeil = Math.min(sumBid - agent.getCost(),sumBid*(1-itsiCal(ITSI_T)));
        double p = pExtra+(pCeil-pExtra)*random.nextDouble();
        if (random.nextBoolean()) {

        }else{

        }
    }

    public static double itsiCal(double x){
        return Math.pow(Math.E,-ITSI_GAMMA*x);
    }

    public static void calR(List<Agent> agentList){
        double avg = agentList.stream()
                .filter(e->e.getBid()!=0)
                .mapToDouble(Agent::getBid)
                .average().orElse(0D);
        double wins = agentList.stream()
                .filter(e->e.getBid()!=0)
                .count();
        if (z == 1){
            r = avg;
        }else {
            r = (r * sumWinner +avg)/(sumWinner+wins);
        }
        sumWinner+=wins;
    }

    public static void updateLocation(Agent agent,Point task){
        agent.setX(task.getX());
        agent.setY(task.getY());
    }

    public static double calTaskFi(Agent agent, Point task){
        double v = task.getValue();
        //System.out.printf(" v:%.3f ",v);
        if (task.getQuality() >= avgQuality){
            double e = Z_LIMIT * Math.log(1+(Main.QUALITY_LIMIT + 5 - avgQuality)*Z_LIMIT);
            //System.out.printf(" e :%.3f",e );
            v += e * agent.getHd()/D_QUALITY;
            //System.out.printf(" refresh v:%.3f ",v);
        }
        double res = PSRD_A * (Math.pow(agent.getValI(),task.getValue()-avgValue) /* + agent.getKi()*/ ) * v
                + PSRD_B * (Math.pow(agent.getQuaI(),Math.abs(task.getQuality()-avgQuality)) /* + agent.getLi() */ ) * task.getQuality();
        //System.out.printf("res %.3f %.3f\n",
        // (Math.pow(VAL_I,task.getReward()-avgValue) * v),(Math.pow(QUL_I,Math.abs(task.getQuality()-avgQuality)) * task.getQuality()));
        //System.out.println();
        return res;
    }

    public static void initAgentState(List<Agent> agentList){
        //init bid
        agentList.stream().forEach(e->e.setBid(0));
    }

    public static void printTask(List<Point> taskList){
        taskList.stream().forEach(e-> System.out.println(e.getId()+" "+e.getQuality()+"     "+e.getValue()));
    }

    public static void printAgent(List<Agent> agentList){
        agentList.stream().forEach(e-> System.out.println(e));
    }

}
