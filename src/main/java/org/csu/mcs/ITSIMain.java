package org.csu.mcs;

import lombok.extern.slf4j.Slf4j;
import org.csu.kmeans.Point;
import org.csu.util.WriterUtil;

import java.io.IOException;
import java.util.List;
import java.util.Random;

@Slf4j
public class ITSIMain {

    public static WriterUtil writerUtil;

    static{
        //writerUtil = new WriterUtil("agentUnity0-"+Main.BUDGET+"-"+Main.WORKER_NUM);
        //writerUtil = new WriterUtil("platformUnity0-"+Main.BUDGET+"-"+Main.WORKER_NUM);
        //writerUtil = new WriterUtil("itsi-r-"+Algorithm.ITSI_GAMMA+"-"+Main.BUDGET+"-"+Main.WORKER_NUM);
        //writerUtil = new WriterUtil("itsi-r"+"-"+Main.BUDGET+"-"+Main.WORKER_NUM+" "+Main.TASK_NUM);
        //writerUtil = new WriterUtil("agentRate"+"-"+Main.BUDGET+"-"+Main.WORKER_NUM+" "+Main.TASK_NUM);

    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        for (int i = 0; i < 10; i++) {
            List<Point> taskList = Main.initTaskSet();
            List<Agent> agentList = Main.initAgents();
            List<Point> points = Main.copyTasks(taskList);
            List<Agent> agents = Main.copyAgents(agentList);

            Main.z=1;
            //param setting
            ITSIAlgorithm.budget = Main.BUDGET;
            DetectiveAlgorithm.budget = Main.BUDGET;
            for (; Main.z <= Main.Z_LIMIT; Main.z++) {
                //log.info("---------- The round of [{}] is start ----------", z);
                ITSIAlgorithmRun(taskList, agentList);
                //System.out.println("~~~~~~~~~~~~                   ~~~~~~~~~~~~~");
                DetectiveAlgorithmRun(points, agents);

                taskList = Main.initTaskSet();
                points = Main.copyTasks(taskList);
                //System.out.println("PSRD budget less " + Algorithm.budget);
                //System.out.println("DetectiveAlgorithm budget less " + DetectiveAlgorithm.budget);
            }

            agentList.stream().forEach(e -> {
                double sumPay = e.getBidSet().stream().mapToDouble(Double::doubleValue).sum();
                //log.info("agent id is [{}] get final payment [{}]", e.getId(), sumPay);
            });
            agentList.stream().forEach(e -> Algorithm.payForAgent(e));

            // print info
            //printTaskInfo(agentList,"ITSI");
            //printTaskInfo(agents,"DA");

            calPlatformUnity(agentList,"ITSI");
            calPlatformUnity(agents,"DA");
            //System.out.println(ITSIAlgorithm.budget);
            //System.out.println(DetectiveAlgorithm.budget);

        }
        // close resource
        //writerUtil.close();
    }

    public static void DetectiveAlgorithmRun(List<Point> taskList, List<Agent> agentList) throws IOException {

        DetectiveAlgorithm.taskSelection(agentList, taskList);

        //printInfo(taskList);
        //printQualityInfo(taskList);
    }

    public static void ITSIAlgorithmRun(List<Point> taskList, List<Agent> agentList) throws IOException {

        ITSIAlgorithm.taskSelection(agentList, taskList);

        //printInfo(taskList);
        //printQualityInfo(taskList);
    }

    public static void printTaskInfo(List<Agent> agentList,String str){

        /*double sum = agentList.stream().mapToDouble(e -> e.getTaskDASet().size()).sum();
        log.info("[{}] - Tasks have been completed nums is [{}]",str,sum);*/
        System.out.println(str);
        agentList.stream().forEach(
                e -> {
                    double sumPay = e.getTaskDASet().stream().mapToDouble(Point::getReward).sum();
                    calReward(e,sumPay);
                }
        );

    }

    public static void calPlatformUnity(List<Agent> agentList,String str){
        double sum = 0;
        double winner = 0;
        for (int i = 0; i < agentList.size(); i++) {
            Agent agent = agentList.get(i);
            double agentSum = agent.getTaskDASet().stream().mapToDouble(Point::getValue).sum();
            sum+=agentSum;
            //System.out.println(agent.getId()+" "+agentSum);
            if (agent.getTaskDASet().size()>0){
                winner++;
            }
        }
        System.out.println(str+" "+winner);
        //writerUtil.outputData(str+"\t"+winner);
    }

    public static void calReward(Agent agent,double reward){
        double pExtra = reward * (ITSIAlgorithm.itsiCal(Algorithm.ITSI_TS) - ITSIAlgorithm.itsiCal(Algorithm.ITSI_TS + Algorithm.ITSI_T));
        Random random = new Random();
        double pCeil = Math.min(reward - agent.getCost(), reward * (1 - ITSIAlgorithm.itsiCal(Algorithm.ITSI_T)));
        double p = pExtra + (pCeil - pExtra) * random.nextDouble();
        double instanceUnity = reward + pExtra - p;
        double postUnity = (reward + pExtra)*ITSIAlgorithm.itsiCal(Algorithm.ITSI_T);
        String output = agent.getId()+"\t"+instanceUnity+"\t"+postUnity;
        writerUtil.outputData(output);
        System.out.println(output);
    }

}
