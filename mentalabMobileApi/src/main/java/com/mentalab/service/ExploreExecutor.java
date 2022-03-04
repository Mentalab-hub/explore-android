package com.mentalab.service;

import java.util.concurrent.*;

public class ExploreExecutor {

    private final static ExecutorService CONFIG_EXECUTOR = Executors.newSingleThreadExecutor();
    private final static ExecutorService DECODE_EXECUTOR = Executors.newSingleThreadExecutor();

    // only one decode submission at a time. Semaphore in case we want to re-execute at a later date
    private final static Semaphore decodeSemaphore = new Semaphore(1);


    public static void submitDecoderTask() {
        if (decodeSemaphore.tryAcquire()) {
            DECODE_EXECUTOR.submit(ParseRawDataTask.getInstance());
        }
    }


    public static Future<Boolean> submitTask(Callable<Boolean> task) {
        return CONFIG_EXECUTOR.submit(task); // one at a time
    }


    public static void shutDownHook() {
        DECODE_EXECUTOR.shutdown();
        CONFIG_EXECUTOR.shutdown();
    }


    public static void terminateDecoderTask() {
        // todo: gracefully exit and release semaphore
    }
}
