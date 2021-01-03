package org.csu.kmeans;

import org.csu.mcs.Agent;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Point implements Serializable {

    public Point deepClone() throws IOException, OptionalDataException, ClassNotFoundException {
        // 将对象写到流里
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream(bo);
        oo.writeObject(this);
        // 从流里读出来
        ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
        ObjectInputStream oi = new ObjectInputStream(bi);
        return (Point) oi.readObject();
    }

    // id
    private int id;

    // the reward after completing this task. DA
    private double reward;

    private double value;

    // the quality of this task
    private double quality;

    // The number of times the task was performed
    private int times;

    // the longest distance of current cycle
    private double distance;

    // the agent who select this task
    private List<Agent> agentList;

    // this task has completed by who
    private Agent agent;

    // current location of task
    private Float x;     // x 轴
    private Float y;    // y 轴

    public Point(Float x, Float y) {
        this.x = x;
        this.y = y;
        this.times = 0;
        this.agentList = new ArrayList<>();
    }

    public int hasCompleted() {
        return getAgent() == null ? 0 : 1;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public double getQuality() {
        return quality;
    }

    public void setQuality(double quality) {
        this.quality = quality;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public double getReward() {
        return reward;
    }

    public void setReward(double reward) {
        this.reward = reward;
    }

    public Float getX() {
        return x;
    }

    public void setX(Float x) {
        this.x = x;
    }

    public Float getY() {
        return y;
    }

    public void setY(Float y) {
        this.y = y;
    }

    public List<Agent> getAgentList() {
        return agentList;
    }

    public void setAgentList(List<Agent> agentList) {
        this.agentList = agentList;
    }

    @Override
    public String toString() {
        return getValue() + " " + getQuality();
    }

    /**
     * 计算距离
     *
     * @param centroid 质心点
     * @param type
     * @return
     */
    public Double calculateDistance(Point centroid, int type) {
        // TODO
        Double result = null;
        switch (type) {
            case 1:
                result = calcL1Distance(centroid);
                break;
            case 2:
                result = calcCanberraDistance(centroid);
                break;
            case 3:
                result = calcEuclidianDistance(centroid);
                break;
            case 4:
                result = calcL1Distance4PV(centroid);
                break;
        }
        return result;
    }



    /*
            计算距离公式
     */

    private Double calcL1Distance(Point centroid) {
        double res = 0;
        res = Math.abs(getX() - centroid.getX()) + Math.abs(getY() - centroid.getY());
        return res / (double) 2;
    }

        /*
            计算距离公式
     */

    private Double calcL1Distance4PV(Point centroid) {
        double res = 0;
        res = Math.abs(getValue() - centroid.getValue()) + Math.abs(getQuality() - centroid.getQuality());
        return res / (double) 2;
    }

    private double calcEuclidianDistance(Point centroid) {
        return Math.sqrt(Math.pow((centroid.getX() - getX()), 2) + Math.pow((centroid.getY() - getY()), 2));
    }

    private double calcCanberraDistance(Point centroid) {
        double res = 0;
        res = Math.abs(getX() - centroid.getX()) / (Math.abs(getX()) + Math.abs(centroid.getX()))
                + Math.abs(getY() - centroid.getY()) / (Math.abs(getY()) + Math.abs(centroid.getY()));
        return res / (double) 2;
    }

    @Override
    public boolean equals(Object obj) {
        Point other = (Point) obj;
        if (getX().equals(other.getX()) && getY().equals(other.getY())) {
            return true;
        }
        return false;
    }
}