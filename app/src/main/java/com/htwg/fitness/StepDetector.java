package com.htwg.fitness;

public class StepDetector {

    private static final int ACCEL_RING_SIZE = 50;
    private static final int VEL_RING_SIZE = 10;
    private static final double STEP_THRESHOLD = 25;
    private static final int STEP_DELAY_NS = 250000000;

    private int accelRingCounter = 0;
    private double[] accelRingX = new double[ACCEL_RING_SIZE];
    private double[] accelRingY = new double[ACCEL_RING_SIZE];
    private double[] accelRingZ = new double[ACCEL_RING_SIZE];
    private int velRingCounter = 0;
    private double[] velRing = new double[VEL_RING_SIZE];
    private long lastStepTimeNs = 0;
    private double oldVelocityEstimate = 0;

    private StepListener listener;

    public void registerListener(StepListener listener) {
        this.listener = listener;
    }

    public void updateAccel(long timeNs, double x, double y, double z) {
        double[] currentAccel = new double[3];
        currentAccel[0] = x;
        currentAccel[1] = y;
        currentAccel[2] = z;

        accelRingCounter++;
        accelRingX[accelRingCounter % ACCEL_RING_SIZE] = currentAccel[0];
        accelRingY[accelRingCounter % ACCEL_RING_SIZE] = currentAccel[1];
        accelRingZ[accelRingCounter % ACCEL_RING_SIZE] = currentAccel[2];

        double[] worldZ = new double[3];
        worldZ[0] = SensorFilter.sum(accelRingX) / Math.min(accelRingCounter, ACCEL_RING_SIZE);
        worldZ[1] = SensorFilter.sum(accelRingY) / Math.min(accelRingCounter, ACCEL_RING_SIZE);
        worldZ[2] = SensorFilter.sum(accelRingZ) / Math.min(accelRingCounter, ACCEL_RING_SIZE);

        double normalizationFactor = SensorFilter.norm(worldZ);

        worldZ[0] = worldZ[0] / normalizationFactor;
        worldZ[1] = worldZ[1] / normalizationFactor;
        worldZ[2] = worldZ[2] / normalizationFactor;

        double currentZ = SensorFilter.dot(worldZ, currentAccel) - normalizationFactor;
        velRingCounter++;
        velRing[velRingCounter % VEL_RING_SIZE] = currentZ;

        double velocityEstimate = SensorFilter.sum(velRing);

        if (velocityEstimate > STEP_THRESHOLD && oldVelocityEstimate <= STEP_THRESHOLD
                && (timeNs - lastStepTimeNs > STEP_DELAY_NS)) {
            listener.step(timeNs);
            lastStepTimeNs = timeNs;
        }

        oldVelocityEstimate = velocityEstimate;
    }

}
