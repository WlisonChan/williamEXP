package org.csu.mcs;

import lombok.extern.slf4j.Slf4j;
import org.csu.kmeans.Cluster;
import org.csu.kmeans.Kmeans;
import org.csu.kmeans.KmeansModel;
import org.csu.kmeans.Point;
import org.csu.util.WriterUtil;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
public class Main {

    // x size for map
    public static final int X_SIZE = 1000;
    // y size for map
    public static final int Y_SIZE = 1000;
    // the number of tasks.
    public static final int TASK_NUM = 100;
    // the number of workers.
    public static final int WORKER_NUM = 20;
    // budget
    public static final double BUDGET = 1000;
    public static final double GAMMA = 1.00;
    // the upper limit of moving.
    public static final double MOVE_LIMIT = 1000;

    // the upper of one task's quality
    public static final double QUALITY_LIMIT = 100;
    // the upper of one task's value
    public static final double VALUE_LIMIT = 100;
    // the upper of one agent's cost
    public static final double COST_LIMIT = 5;

    // PSRD's parameter
    public static final double L_I = 3;
    public static final double K_I = 2;

    // kmeans - k
    public static final int KMEANS_K = 5;
    // kmeans - type, the calculation distance formula type.
    public static final int KMEANS_TYPE = 1;

    // the round of mcs
    public static int z = 1;
    public static final double Z_LIMIT = 6;

    public static WriterUtil writerUtil;

    static {
        //writerUtil = new WriterUtil("quality");
        writerUtil = new WriterUtil("platformUtility-"+BUDGET+"-"+GAMMA);
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        for (int i = 0; i < 100; i++) {
            List<Point> taskList = initTaskSet();
            List<Agent> agentList = initAgents();
            List<Point> points = copyTasks(taskList);
            List<Agent> agents = copyAgents(agentList);

            z=1;
            //param setting
            Algorithm.budget = BUDGET;
            DetectiveAlgorithm.budget = BUDGET;
            for (; z <= Z_LIMIT; z++) {
                //log.info("---------- The round of [{}] is start ----------", z);
                AlgorithmRun(taskList, agentList);
                //System.out.println("~~~~~~~~~~~~                   ~~~~~~~~~~~~~");
                DetectiveAlgorithmRun(points, agents);
                taskList = initTaskSet();
                points = copyTasks(taskList);
                //System.out.println("PSRD budget less " + Algorithm.budget);
                //System.out.println("DetectiveAlgorithm budget less " + DetectiveAlgorithm.budget);
            }

            agentList.stream().forEach(e -> {
                double sumPay = e.getBidSet().stream().mapToDouble(Double::doubleValue).sum();
                //log.info("agent id is [{}] get final payment [{}]", e.getId(), sumPay);
            });
            agentList.stream().forEach(e -> Algorithm.payForAgent(e));

            // print info
            printAgentInfo(agentList);
            printDAAgent(agents);

        }
        // close resource
        writerUtil.close();
    }


    public static void AlgorithmRun(List<Point> taskList, List<Agent> agentList)
            throws IOException, ClassNotFoundException {

        Algorithm.calAvgQuality(taskList);
        Algorithm.calAvgValue(taskList);

        Algorithm.selectTasks(taskList, agentList);

        //Algorithm.refreshBidSet(agentList);

        //Algorithm.printAgent(agentList);
        //Algorithm.printTask(taskList);
        //printInfo(taskList);
        //System.out.println("-------- --------- -------- --------- ------------ ------- ------------ ---------- -------");
        //printQualityInfo(taskList);
    }

    public static void DetectiveAlgorithmRun(List<Point> taskList, List<Agent> agentList) throws IOException {

        DetectiveAlgorithm.taskSelection(agentList, taskList);

        //printInfo(taskList);
        //printQualityInfo(taskList);
    }


    public static List<Point> initTaskSet() throws IOException {
        return kMeansForTasks();
    }

    /**
     * 均匀生成任务，并且使用kmeans聚类
     *
     * @return
     * @throws IOException
     */
    public static List<Point> kMeansForTasks() throws IOException {
        //数据输出路径
        //FileWriter writer = new FileWriter("src/main/resources/out.txt");
        List<Point> points = new ArrayList<>(); // 任务点集

        Random random = new Random();
        for (int i = 0; i < TASK_NUM; i++) {
            float curX = random.nextFloat() * X_SIZE;
            float curY = random.nextFloat() * Y_SIZE;
            //System.out.println(curX+" "+curY);
            Point point = new Point(curX, curY);
            double quality = random.nextDouble() * QUALITY_LIMIT * 0.7 + QUALITY_LIMIT * 0.3;
            double val = quality;
            point.setQuality(quality);
            point.setValue(val);

            point.setReward(val * (0.85 + random.nextDouble() / 10));
            point.setId(i);
            points.add(point);
        }
        KmeansModel model = Kmeans.run(points, KMEANS_K, KMEANS_TYPE);
        // 将任务聚类
//        writer.write("====================   K is " + model.getK() + " ,  Object Funcion Value is " + model.getOfv() + " ,  calc_distance_type is " + model.getCalc_distance_type() + "   ====================\n");
//        int i = 0;
//        for (Cluster cluster : model.getClusters()) {
//            i++;
//            writer.write("====================   classification " + i + "   ====================\n");
//            for (Point point : cluster.getPoints()) {
//                writer.write(point.toString() + "\n");
//            }
//            writer.write("\n");
//            writer.write("centroid is " + cluster.getCentroid().toString());
//            writer.write("\n\n");
//        }
//        writer.close();
        return points;
    }

    /**
     * Initialize participants' location
     *
     * @return
     */
    public static List<Agent> initAgents() {
        // 参与者位置初始化
        List<Agent> agents = new ArrayList<>(WORKER_NUM);
        Random random = new Random();
        for (int i = 0; i < WORKER_NUM; i++) {
            float curX = random.nextFloat() * X_SIZE;
            float curY = random.nextFloat() * Y_SIZE;
            Agent agent = new Agent(curX, curY);
            //agent.setCost4r(random.nextDouble()*COST_LIMIT);
            agent.setId(i);
            agent.setValI(random.nextDouble() / 2 + 1);
            agent.setQuaI(0.3 + random.nextDouble() / 2.0);
            agent.setKi(random.nextDouble() * K_I);
            agent.setLi(random.nextDouble() * L_I);
            //agent.setGamma(0.85 + random.nextDouble() / 10.0);
            agent.setGamma(GAMMA);

            agents.add(agent);
        }

        return agents;
    }

    public static List<Point> copyTasks(List<Point> tasks) throws IOException, ClassNotFoundException {
        List<Point> temp = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            Point point = tasks.get(i);
            Point cur = point.deepClone();
            //System.out.println( point.getAgentList().size()+" "+cur.getAgentList().size());
            temp.add(cur);
        }
        return temp;
    }

    public static List<Agent> copyAgents(List<Agent> agentList) throws IOException, ClassNotFoundException {
        List<Agent> temp = new ArrayList<>();
        for (int i = 0; i < agentList.size(); i++) {
            Agent agent = agentList.get(i);
            Agent cur = agent.deepClone();
            temp.add(cur);
        }
        return temp;
    }

    /**
     * 任务完成状态打印
     *
     * @param taskList
     */
    public static void printInfo(List<Point> taskList) {
        int sum = taskList.stream().mapToInt(Point::hasCompleted).sum();
        log.info("The sum of tasks which has completed is [{}]", sum);
    }

    public static void printQualityInfo(List<Point> taskList) {
        double avg = taskList.stream()
                .filter(e -> e.getAgent() != null)
                .mapToDouble(Point::getQuality)
                .average().orElse(0D);
        double sum = taskList.stream()
                .filter(e -> e.getAgent() != null)
                .mapToDouble(Point::getQuality)
                .sum();
        //log.info("The sum of tasks' quality is [{}]", sum);
        //log.info("The avg of tasks' quality is [{}]", avg);
    }

    public static void printAgentInfo(List<Agent> agentList) {
        double sumCompTask = agentList.stream()
                .mapToDouble(e -> e.getCostSet().size())
                .sum();
        //log.info("The total sum of tasks which have completed is [{}]", sumCompTask);
        /*agentList.stream()
                .forEach(e -> {
                    //double payment = e.getBidSet().stream().mapToDouble(Double::doubleValue).sum();
                    double sumCost = e.getCostSet().stream().mapToDouble(Double::doubleValue).sum();
                    log.info("Agent's id [{}] has completed [{}], get payment [{}], cost is [{}],get Ei [{}]",
                            e.getId(), e.getBidSet().size(), e.getPay(), sumCost, e.getEi());
                    //System.out.println(e.getBidSet());
                });*/

/*        double sum = 0;
        double num = 0;
        for (int i = 0; i < agentList.size(); i++) {
            Agent agent = agentList.get(i);
            List<Point> taskDASet = agent.getTaskSet();
            for (int j = 0; j < taskDASet.size(); j++) {
                Point task = taskDASet.get(j);
                sum+=task.getQuality();
                num++;
            }
        }

        writerUtil.outputData("MY\t"+sum+"\t\t"+(sum/num)+"\t\t"+num);*/

        double platform = 0;

        // 平台效用 输出
        for (int i = 0; i < agentList.size(); i++) {
            Agent agent = agentList.get(i);
            List<Point> taskSet = agent.getTaskSet();
            double taskSumValue = taskSet.stream().mapToDouble(Point::getValue).sum();
            double bidSum = agent.getBidSet().stream().mapToDouble(Double::doubleValue).sum();
            platform+=taskSumValue - bidSum;
        }

        writerUtil.outputData("MY\t"+platform+"\t\t");

/*        double sw = 0;
        for (int i = 0; i < agentList.size(); i++) {
            Agent agent = agentList.get(i);
            List<Point> taskSet = agent.getTaskSet();
            double sumTaskVal = taskSet.stream().mapToDouble(Point::getValue).sum();
            double sumCost = agent.getCostSet().stream().mapToDouble(Double::doubleValue).sum();
            sw+=sumTaskVal-sumCost;
        }
        writerUtil.outputData("MY\t"+sw+"\t\t");*/

        //log.info("The sum quality of tasks is [{}],avg is [{}]",sum,sum/num);
//        agentList.stream()
//                .forEach(e -> {
//                    System.out.println(e.getId() + " " + e.getEi());
//                    //System.out.println(e.getHdTask());
//                    //System.out.println(e.getCostSet());
//                    //System.out.println(e.getBidSet());
//                });
    }

    public static void printDATask(List<Point> taskList) {
        double sum = taskList.stream()
                .filter(e -> e.getAgent() != null)
                .mapToDouble(Point::getQuality)
                .sum();
        double avg = taskList.stream()
                .filter(e -> e.getAgent() != null)
                .mapToDouble(Point::getQuality)
                .average().orElse(0D);
        log.info("DA Task info");
        log.info("The sum of tasks' quality is [{}]", sum);
        log.info("The avg of tasks' quality is [{}]", avg);
    }

    public static void printDAAgent(List<Agent> agentList) {
       /* double sumCompTask = agentList.stream()
                .mapToDouble(e -> e.getCostSet().size())
                .sum();
        log.info("The total sum of tasks which have completed is [{}]", sumCompTask);

        agentList.stream().forEach(e -> {
            double costSum = e.getCostSet().stream().mapToDouble(Double::doubleValue).sum();
            log.info("Agent's id [{}] has completed [{}], get payment [{}], cost is [{}]",
                    e.getId(), e.getBidSet().size(), e.getPay(), costSum);
        });
*/

        /*double sum = 0;
        double num = 0;
        for (int i = 0; i < agentList.size(); i++) {
            Agent agent = agentList.get(i);
            List<Point> taskDASet = agent.getTaskDASet();
            for (int j = 0; j < taskDASet.size(); j++) {
                Point task = taskDASet.get(j);
                sum+=task.getQuality();
                num++;
            }
        }
        writerUtil.outputData("DA\t"+sum+"\t\t"+(sum/num)+"\t\t"+num);*/

        double platform = 0;

        // 平台效用 输出
        for (int i = 0; i < agentList.size(); i++) {
            Agent agent = agentList.get(i);
            List<Point> taskSet = agent.getTaskDASet();
            double taskSumValue = taskSet.stream().mapToDouble(Point::getValue).sum();
            double bidSum = agent.getBidSet().stream().mapToDouble(Double::doubleValue).sum();
            platform+=taskSumValue - bidSum;
        }

        writerUtil.outputData("DA\t"+platform+"\t\t");

/*        double sw = 0;
        for (int i = 0; i < agentList.size(); i++) {
            Agent agent = agentList.get(i);
            List<Point> taskSet = agent.getTaskDASet();
            double sumTaskVal = taskSet.stream().mapToDouble(Point::getValue).sum();
            double sumCost = agent.getCostSet().stream().mapToDouble(Double::doubleValue).sum();
            sw+=sumTaskVal-sumCost;
        }
        writerUtil.outputData("DA\t"+sw+"\t\t");*/

        //log.info("The sum quality of tasks is [{}],avg is [{}]",sum,sum/num);
    }

}
