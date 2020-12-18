package org.csu.mcs;


import lombok.Data;
import org.csu.kmeans.Point;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class Agent implements Serializable{

    public Agent deepClone() throws IOException, OptionalDataException, ClassNotFoundException {
        // 将对象写到流里
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream(bo);
        oo.writeObject(this);
        // 从流里读出来
        ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
        ObjectInputStream oi = new ObjectInputStream(bi);
        return (Agent) oi.readObject();
    }

    // agent id
    private int id;
    // Accumulated travel distance
    private double runDistance;
    // Cumulative costs
    private double cost;
    // Accumulated income
    private double pay;

    // complete task set
    private List<Point> taskSet;
    // record the task seq
    private List<List<Point>> taskSeq;
    // bid set
    private List<Double> bidSet;

    // DA the profit of one round
    private double profit;

    private double cost4r;

    private double bid4r;

    private double bid;

    private double sumCost;

    // the num of tasks which have completed in high quality
    private double hd;

    // current location of participant
    private float x;
    private float y;

    // sensitive param
    private double valI;
    private double quaI;
    private double ki;
    private double li;

    private double gamma;

    public Agent(float x, float y){
        this.x = x;
        this.y = y;
        this.taskSet = new ArrayList<>();
        this.taskSeq = new ArrayList<>();
        this.bidSet = new ArrayList<>();
    }

    // The cost of performing the target task
    public double getCost(Point point){
        double d = getDistance(point);
        return DetectiveAlgorithm.COST_COEFFICIENT + DetectiveAlgorithm.DISTANCE_COEFFICIENT * d;
    }

    /**
     * Calculate the distance from the participant to the task
     * @param point
     * @return
     */
    public double getDistance(Point point){
        double d = Math.pow(this.x - point.getX(), 2) + Math.pow(this.y - point.getY(), 2);
        d = Math.sqrt(d);
        return d;
    }

}
