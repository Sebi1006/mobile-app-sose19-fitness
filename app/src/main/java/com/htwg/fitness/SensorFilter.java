package com.htwg.fitness;

public class SensorFilter {

    public static double sum(double[] array) {
        double retVal = 0;

        for (int i = 0; i < array.length; i++) {
            retVal += array[i];
        }

        return retVal;
    }

    public static double[] cross(double[] arrayA, double[] arrayB) {
        double[] retArray = new double[3];

        retArray[0] = arrayA[1] * arrayB[2] - arrayA[2] * arrayB[1];
        retArray[1] = arrayA[2] * arrayB[0] - arrayA[0] * arrayB[2];
        retArray[2] = arrayA[0] * arrayB[1] - arrayA[1] * arrayB[0];

        return retArray;
    }

    public static double norm(double[] array) {
        double retVal = 0;

        for (int i = 0; i < array.length; i++) {
            retVal += array[i] * array[i];
        }

        return Math.sqrt(retVal);
    }

    public static double dot(double[] arrayA, double[] arrayB) {
        return arrayA[0] * arrayB[0] + arrayA[1] * arrayB[1] + arrayA[2] * arrayB[2];
    }

    public static double[] normalize(double[] array) {
        double[] retVal = new double[array.length];
        double norm = norm(array);

        for (int i = 0; i < array.length; i++) {
            retVal[i] = array[i] / norm;
        }

        return retVal;
    }

}
