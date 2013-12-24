package com.gdiama.client;

import java.util.concurrent.TimeUnit;

public interface Config {
    int sleepTime();

    double backoffMultiplier();

    TimeUnit retrySleepTimeUnit();

    int maxReties();
}
