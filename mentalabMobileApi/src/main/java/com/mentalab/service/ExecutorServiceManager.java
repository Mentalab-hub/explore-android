package com.mentalab.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServiceManager {

    private static final int NTHREADPOOL = 100;
    private static final ExecutorService executor = Executors.newFixedThreadPool(NTHREADPOOL);


    public static synchronized ExecutorService getExecutorService() {
        return executor;
    }


    public static void shutDownHook() {
        executor.shutdown();
    }
}
