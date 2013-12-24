package com.gdiama.client.enhancer;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ExponentialBackOffTest {

    @Test
    public void correctValues() throws Exception {
        ExponentialBackOff backOff = new ExponentialBackOff(1000, 0.5);
        assertThat(backOff.next()).isEqualTo(1500);
        assertThat(backOff.next()).isEqualTo(3750);
        assertThat(backOff.next()).isEqualTo(13125);
    }
}
