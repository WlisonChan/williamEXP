package org.csu.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestEXP {
    public static void main(String[] args) {
        /*double a = 1.03;
        double b = 0.81;
        for (int q = 30; q <= 100; q++) {
            double v = q;
            //double q = v;
            double a1 = Math.pow(a, v - 65);
            double b1 = Math.pow(b,Math.abs(q-65));
            double res = (a1+2)*(v)+(b1+3)*q;
            System.out.println(res);
            System.out.println((a1 + 2) * (v));
            System.out.println((b1 + 3) * q);
            System.out.println();
        }*/
        /*List<Integer> list  = new ArrayList<>();
        list.add(1);
        list.add(2);
        List<Integer> l2 = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            l2.add(list.get(i));
        }
        System.out.println(l2);
        l2.clear();
        System.out.println(list);*/
        System.out.println(Math.pow(27, 1.0 / 3.0));
    }
}
