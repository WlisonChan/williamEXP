package org.csu.mcs;

public class Test {

    public static final double QUA_I = 0.9;
    public static final double VAL_I = 1.03;
    public static final double AVG_QUA = 65;
    public static final double AVG_VAL = 65;
    public static final double C_K = 0;
    public static final double C_L = 0;
    public static final double V_j = 30;
    public static final double Q_j = 30;
    public static final double COMPLETE_TASK_NUM = 100;
    public static final double C_E = 0.7;
    public static final double Q_MAX = 100;
    public static final double TIME = 10;
    public static final double A = 0.1;
    public static final double B = 1-A;

    @org.junit.jupiter.api.Test
    public void testFi() {
            //for (double COMPLETE_TASK_NUM =1;COMPLETE_TASK_NUM<=100;COMPLETE_TASK_NUM++) {
        for (double Q_j = 30; Q_j <= 100; Q_j++) {

                double ei = 0;
                if (Q_j > AVG_QUA) {
                    ei = C_E * COMPLETE_TASK_NUM * Math.log(1 + TIME * (Q_j - AVG_QUA));
                }

                double val = A * (Math.pow(VAL_I, V_j - AVG_VAL) + C_K) * (V_j + ei);
                double val2 = A * (Math.pow(VAL_I, V_j - AVG_VAL) + C_K) * (V_j);
                double qua = B * (Math.pow(QUA_I, Math.abs(Q_j - AVG_QUA)) + C_L) * (Q_j);
                double fi = qua + val;
                double fi2 = qua + val2;
/*            System.out.printf("%.3f\t%.3f\t%.3f\t%.3f\t%.3f\t%.3f\t%.3f\n",Q_j,ei,val,qua,fi,
                    (Math.pow(VAL_I, V_j - AVG_VAL)),Math.pow(QUA_I, Math.abs(Q_j - AVG_QUA)));*/
                System.out.printf("%.3f\t%.3f\t", fi,fi2);
                System.out.println();
        }
    }

    @org.junit.jupiter.api.Test
    public void testITSI(){
        double p = 70;
        double r = 1.03;
        double a = 1.05;
        for (double  x = 1; x <=4; x+=0.1) {
            for (double y = -5; y <= 0; y+=0.1) {
                double res = (70+y)*Math.pow(1+r/a,-a * x);
                System.out.printf("%.3f\t",res);
            }
            System.out.println();
        }
    }

    @org.junit.jupiter.api.Test
    public void testPV(){
        for (int i = 30; i <= 100; i++) {
            System.out.printf("%d\t",i);
        }
        System.out.println();
        for (double  V_j = 30; V_j <= 100; V_j++) {

            for (double Q_j = 30; Q_j <= 100; Q_j++) {

                double val = A * (Math.pow(VAL_I, V_j - AVG_VAL) + C_K) * (V_j);
                double qua = B * (Math.pow(QUA_I, Math.abs(Q_j - AVG_QUA)) + C_L) * (Q_j);
                double fi = qua + val;
                System.out.printf("%.3f\t", fi);
            }
                System.out.println();

        }

    }

}