package com.gdiama.client.enhancer;

import com.gdiama.client.Config;
import org.apache.http.conn.ConnectTimeoutException;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RetryHandlerTest {

    private final Object[] args = new Object[]{new Arg()};
    private Client client = new Client();
    private Config config = mock(Config.class);

    Method method;

    @Before
    public void setup() throws NoSuchMethodException {
        when(config.maxReties()).thenReturn(3);
        when(config.sleepTime()).thenReturn(10);
        when(config.backoffMultiplier()).thenReturn(3.0);
        when(config.retrySleepTimeUnit()).thenReturn(TimeUnit.MICROSECONDS);

        method = client.getClass().getMethod("doWork", Arg.class);
    }

    @Test
    public void returnResultOnSuccessWhenNoExceptionOccurred() throws Throwable {
        RetryHandler retryHandler = new RetryHandler(client, config);
        Object result = retryHandler.invoke(null, method, args);

        assertThat(result).isNotNull();
        assertThat(client.noOfCalls()).isEqualTo(1);
    }

    @Test
    public void returnSuccessfulResultAfterConnectTimeoutExceptionThrownOnce() throws Throwable {
        client.doThrow(new SocketTimeoutException());

        RetryHandler retryHandler = new RetryHandler(client, config);
        Object result = retryHandler.invoke(null, method, args);

        assertThat(result).isNotNull();
        assertThat(client.noOfCalls()).isEqualTo(2);
    }

    @Test
    public void returnSuccessfulResultAfterSocketTimeoutExceptionThrownOnce() throws Throwable {
        client.doThrow(new ConnectTimeoutException());

        RetryHandler retryHandler = new RetryHandler(client, config);
        Object result = retryHandler.invoke(null, method, args);

        assertThat(result).isNotNull();
        assertThat(client.noOfCalls()).isEqualTo(2);
    }

    @Test
    public void rethrowSocketTimeoutExceptionWhenRetryAttemptsExhausted() throws Throwable {
        client.doThrow(new SocketTimeoutException()).doThrow(new SocketTimeoutException()).doThrow(new SocketTimeoutException());
        RetryHandler retryHandler = new RetryHandler(client, config);

        try {
            retryHandler.invoke(null, method, args);
            fail("should have failed when SocketTimeoutException thrown");
        } catch (SocketTimeoutException throwable) {
            //expected
        }

        assertThat(client.noOfCalls()).isEqualTo(3);
    }

    @Test
    public void rethrowConnectTimeoutExceptionWhenRetryAttemptsExhausted() throws Throwable {
        client.doThrow(new ConnectTimeoutException()).doThrow(new ConnectTimeoutException()).doThrow(new ConnectTimeoutException());

        RetryHandler retryHandler = new RetryHandler(client, config);

        try {
            retryHandler.invoke(null, method, args);
            fail("should have failed when SocketTimeoutException thrown");
        } catch (ConnectTimeoutException throwable) {
            //expected
        }

        assertThat(client.noOfCalls()).isEqualTo(3);
    }

    @Test
    public void returnSuccessfulResultAfterConnectExceptionThrownOnce() throws Throwable {
        client.doThrow(new SocketTimeoutException());

        RetryHandler retryHandler = new RetryHandler(client, config);
        Object result = retryHandler.invoke(null, method, args);

        assertThat(result).isNotNull();
        assertThat(client.noOfCalls()).isEqualTo(2);
    }

    @Test
    public void rethrowConnectExceptionWhenRetryAttemptsExhausted() throws Throwable {
        client.doThrow(new SocketTimeoutException()).doThrow(new SocketTimeoutException()).doThrow(new SocketTimeoutException());
        RetryHandler retryHandler = new RetryHandler(client, config);

        try {
            retryHandler.invoke(null, method, args);
            fail("should have failed when SocketTimeoutException thrown");
        } catch (SocketTimeoutException throwable) {
            //expected
        }

        assertThat(client.noOfCalls()).isEqualTo(3);
    }

    @Test
    public void returnImmediatelyWhenNonRetryableExceptionThrownByUnderlyingClient() throws Throwable {
        RuntimeException exception = new RuntimeException();
        client.doThrow(exception);

        int retries = 100;
        when(config.maxReties()).thenReturn(retries);
        when(config.sleepTime()).thenReturn(10);
        when(config.backoffMultiplier()).thenReturn(3.0);
        when(config.retrySleepTimeUnit()).thenReturn(TimeUnit.MICROSECONDS);

        method = client.getClass().getMethod("doWork", Arg.class);

        for (int i = 0; i < retries; i++) {
            client.doThrow(new RuntimeException());
        }

        RetryHandler retryHandler = new RetryHandler(client, config);
        try {
            retryHandler.invoke(null, method, args);
            fail("underlying client should have thrown an exception");
        } catch (RuntimeException throwable) {
            assertThat(throwable).isSameAs(exception);
        }

        assertThat(client.noOfCalls()).isEqualTo(1);
    }

    @Test
    public void setInterruptedFlagWhileHandlerSleepingWhenHandlerThreadInterrupted() throws Throwable {
        ConnectTimeoutException exception = new ConnectTimeoutException();
        client.doThrow(exception);

        int retries = 100;
        when(config.maxReties()).thenReturn(retries);
        when(config.sleepTime()).thenReturn(10);
        when(config.backoffMultiplier()).thenReturn(3.0);
        when(config.retrySleepTimeUnit()).thenReturn(TimeUnit.MINUTES);

        method = client.getClass().getMethod("doWork", Arg.class);


        final RetryHandler retryHandler = new RetryHandler(client, config);

        final CountDownLatch wait = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(1);

        final Thread runner = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    wait.countDown();
                    retryHandler.invoke(null, method, args);
                } catch (Throwable throwable) {
                    done.countDown();
                    assertThat(Thread.currentThread().isInterrupted()).isTrue();

                }

            }
        });
        runner.start();

        ensureRetryHandlerHasInvokedClientAndSleeping(wait);
        interruptRetryHandler(runner);
        done.await(5000, TimeUnit.MILLISECONDS);

        assertThat(client.noOfCalls()).isEqualTo(1);
    }

    private void interruptRetryHandler(final Thread runner) throws InterruptedException {
        Thread interruptor = new Thread(new Runnable() {
            @Override
            public void run() {
                runner.interrupt();
            }
        });
        interruptor.start();
        interruptor.join();
    }

    private void ensureRetryHandlerHasInvokedClientAndSleeping(CountDownLatch wait) throws InterruptedException {
        wait.await(10, TimeUnit.SECONDS);
        TimeUnit.MILLISECONDS.sleep(1000);
    }

    public static class Client {

        private List<Exception> exceptions = new ArrayList<>();
        private int count = 0;

        public Response doWork(Arg arg) throws Exception {
            count++;
            if (!exceptions.isEmpty()) {
                throw exceptions.remove(0);
            }
            return new Response();
        }

        public Client doThrow(Exception exception) {
            this.exceptions.add(exception);
            return this;
        }

        public int noOfCalls() {
            return count;
        }
    }

    public static class Arg {}

    public static class Response {}
}
