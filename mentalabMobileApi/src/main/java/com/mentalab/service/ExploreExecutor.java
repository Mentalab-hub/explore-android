package com.mentalab.service;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExploreExecutor {

  private static final ExecutorService TASK_EXECUTOR = Executors.newSingleThreadExecutor();
  private static final ExecutorService SIMULTANEOUS_TASK_EXECUTOR = Executors.newFixedThreadPool(5);

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
