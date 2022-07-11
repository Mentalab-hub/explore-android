package com.mentalab.service;

import java.util.concurrent.*;

public class ExploreExecutor {

  private static final ExecutorService BLOCKING_EXECUTOR = Executors.newSingleThreadExecutor();
  private static final ExecutorService PARALLEL_EXECUTOR = Executors.newFixedThreadPool(5);
  private static final ScheduledExecutorService SCHEDULED_EXECUTOR =
      Executors.newScheduledThreadPool(2);

  public static Future<Boolean> submitTask(Callable<Boolean> task) {
    if (task instanceof DeviceConfigurationTask) {
      return BLOCKING_EXECUTOR.submit(task); // one at a time
    } else {
      return PARALLEL_EXECUTOR.submit(task); // can happen in parallel
    }
  }

  public static Future<Boolean> submitTimeoutTask(Callable<Boolean> task, int millis, Runnable cleanup) {
    final Future<Boolean> handler = SCHEDULED_EXECUTOR.submit(task);
    SCHEDULED_EXECUTOR.schedule(
        () -> {
          handler.cancel(true);
          cleanup.run();
        },
        millis,
        TimeUnit.MILLISECONDS);
    return handler;
  }

  public static void shutDown() {
    BLOCKING_EXECUTOR.shutdown();
    PARALLEL_EXECUTOR.shutdown();
  }
}
