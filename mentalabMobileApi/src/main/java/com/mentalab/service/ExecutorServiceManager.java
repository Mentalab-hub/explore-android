package com.mentalab.service;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExecutorServiceManager {

    private final static ExecutorService EXECUTOR = Executors.newFixedThreadPool(20);
    private final static ExecutorService DECODE_EXECUTOR = Executors.newSingleThreadExecutor();
    private final static Queue<Callable<?>> TASKS = new LinkedList<>();

    static Future<Void> decodeTaskResult;


    public static Future<Void> submitDecoderTask(ParseRawDataTask decodeTask) {
        if (DECODE_EXECUTOR.isTerminated()) {
            decodeTaskResult = DECODE_EXECUTOR.submit(decodeTask);
            return DECODE_EXECUTOR.submit(decodeTask);
        }
        return decodeTaskResult;
    }


    public static Future<Boolean> submitTask(Callable<Boolean> task) {
        if (TASKS.contains(task)) {
            return ;
        }
        TASKS.add(task);
        Future<Boolean> result = EXECUTOR.submit(task);
        return result;
    }


    public static void shutDownHook() {
        EXECUTOR.shutdown();
    }
}
