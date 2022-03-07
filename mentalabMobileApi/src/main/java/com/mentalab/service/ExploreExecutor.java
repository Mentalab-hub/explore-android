package com.mentalab.service;

import java.util.concurrent.*;

public class ExploreExecutor {

    private final static ExecutorService TASK_EXECUTOR = Executors.newSingleThreadExecutor();
    private final static ExecutorService SIMULTANEOUS_TASK_EXECUTOR = Executors.newFixedThreadPool(5);


    public static Future<Boolean> submitTask(Callable<Boolean> task) {
        if (task instanceof DeviceConfigurationTask) {
            return TASK_EXECUTOR.submit(task); // one at a time
        } else {
            return SIMULTANEOUS_TASK_EXECUTOR.submit(task); // can happen simultaneously
        }
    }


    public static void shutDown() {
        TASK_EXECUTOR.shutdown();
    }
}
