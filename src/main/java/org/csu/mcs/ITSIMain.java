package org.csu.mcs;

import lombok.extern.slf4j.Slf4j;
import org.csu.kmeans.Point;

import java.io.IOException;
import java.util.List;

@Slf4j
public class ITSIMain {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        for (int i = 0; i < 1; i++) {
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
            printTaskInfo(agentList);
            printTaskInfo(agents);
            System.out.println(ITSIAlgorithm.budget);
            System.out.println(DetectiveAlgorithm.budget);

        }
        // close resource
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

    public static void printTaskInfo(List<Agent> agentList){
        double sum = agentList.stream().mapToDouble(e -> e.getTaskDASet().size()).sum();
        log.info("Tasks have been completed nums is [{}]",sum);
    }
}
