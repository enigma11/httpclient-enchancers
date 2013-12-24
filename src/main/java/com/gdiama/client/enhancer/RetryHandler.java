package com.gdiama.client.enhancer;

import com.gdiama.client.Config;
import org.apache.http.conn.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InterruptedIOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;


//todo retry socketTimeoutExc? should only retry 500&503?
public class RetryHandler implements InvocationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryHandler.class);
    private final Object client;
    private final Config config;

    public RetryHandler(Object client, Config config) {
        this.client = client;
        this.config = config;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;
        boolean success = false;
        boolean stopRetry = false;
        int count = 0;
        final ExponentialBackOff exponentialBackOff = new ExponentialBackOff(config.sleepTime(), config.backoffMultiplier());

        Throwable capturedException = null;

        do {
            try {
                result = method.invoke(client, args);
                success = true;
            } catch (InvocationTargetException e) {
                count++;
                capturedException = e.getTargetException();

                if (shouldRetry(capturedException)) {
                    long next = exponentialBackOff.next();
                    TimeUnit timeUnit = config.retrySleepTimeUnit();
                    LOGGER.info("Attempt {}. Backing off for {} {}", count, next, timeUnit.name().toLowerCase());
                    try {
                        timeUnit.sleep(next);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new InterruptedIOException();
                    }
                } else {
                    stopRetry = true;
                }
            } catch (Exception e) {
                capturedException = e;
                stopRetry = true;
            }
        } while ((!stopRetry && count < config.maxReties() && !success));

        if (capturedException != null && !success) {
            throw capturedException;
        }

        return result;
    }

    private boolean shouldRetry(Throwable capturedException) {
        return capturedException instanceof ConnectTimeoutException ||
                capturedException instanceof SocketTimeoutException;
    }

}
