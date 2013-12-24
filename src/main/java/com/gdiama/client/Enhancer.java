package com.gdiama.client;

import com.gdiama.client.enhancer.RetryHandler;

import java.lang.reflect.Proxy;

public class Enhancer {

    private final Object object;
    private Config config;

    private Enhancer(Object object) {
        this.object = object;
    }

    public static Enhancer enhance(Object object) {
        return new Enhancer(object);
    }

    public Enhancer using(Config config) {
        this.config = config;
        return this;
    }

    public <T> T withRetry() {
        return (T) Proxy.newProxyInstance(
                object.getClass().getClassLoader(),
                object.getClass().getInterfaces(),
                new RetryHandler(object, config)
        );
    }

}
