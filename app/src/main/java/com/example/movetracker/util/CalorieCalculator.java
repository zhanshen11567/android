package com.example.movetracker.util;

public class CalorieCalculator {

    private static final double DEFAULT_WEIGHT_KG = 70.0;

    /**
     * 按 MET 公式计算消耗卡路里。
     * 配速 = 分钟每公里，MET 取值：快跑 <6min → 10，中速 6-8min → 8，慢跑 >8min → 6。
     */
    public static double calculate(double distanceMeters, long durationSeconds) {
        if (durationSeconds == 0 || distanceMeters == 0) return 0;
        double paceMinPerKm = (durationSeconds / 60.0) / (distanceMeters / 1000.0);
        double met;
        if (paceMinPerKm < 6) {
            met = 10.0;
        } else if (paceMinPerKm < 8) {
            met = 8.0;
        } else {
            met = 6.0;
        }
        return met * DEFAULT_WEIGHT_KG * (durationSeconds / 3600.0);
    }
}
