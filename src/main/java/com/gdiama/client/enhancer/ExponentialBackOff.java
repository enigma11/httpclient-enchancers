package com.gdiama.client.enhancer;

public class ExponentialBackOff {

    private final double backoffMultiplier;
    private final int initialSleepTime;
    private int count;
    private long previousSleepTime;

    public ExponentialBackOff(int initialSleepTime, double backoffMultiplier) {
        this.backoffMultiplier = backoffMultiplier;
        this.previousSleepTime = initialSleepTime;
        this.initialSleepTime = initialSleepTime;
        this.count = 0;
    }

    public long next() {
        long sleepTime = (long) Math.max(previousSleepTime * (1 + backoffMultiplier + count), initialSleepTime);
        previousSleepTime = sleepTime;
        count++;
        return sleepTime;
    }

}
