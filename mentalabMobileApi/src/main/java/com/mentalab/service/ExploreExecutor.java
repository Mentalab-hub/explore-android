package com.mentalab.service;

import java.util.concurrent.*;

public class ExploreExecutor {

    private final static ExecutorService TASK_EXECUTOR = Executors.newSingleThreadExecutor();


    public static Future<Boolean> submitTask(Callable<Boolean> task) {
        return TASK_EXECUTOR.submit(task); // one at a time
    }


    public static void shutDownHook() {
        TASK_EXECUTOR.shutdown();
    }
}
