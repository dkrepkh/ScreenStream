package com.javabender.screenstream.stream.management;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

public class StreamThreadPool {
    private final ExecutorService executor;
    private final AtomicBoolean hasStopped;

    public StreamThreadPool(AtomicBoolean hasStopped) {
        executor = Executors.newFixedThreadPool(2, new ThreadPool());
        this.hasStopped = hasStopped;
    }

    public void submit(Runnable task) {
        executor.submit(task);
    }

    public void shutdown() {
        executor.shutdown();
    }
    private class ThreadPool implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setPriority(Thread.NORM_PRIORITY);
            thread.setDaemon(false);
            thread.setUncaughtExceptionHandler((t, e) -> {
                System.err.println("Error in thread " + t.getName() + ": " + e.getMessage());
                hasStopped.set(true);
                shutdown();
            });
            return thread;
        }
    }
}
