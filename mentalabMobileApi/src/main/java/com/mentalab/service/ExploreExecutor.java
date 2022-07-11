package com.mentalab.service;

import java.util.concurrent.*;

public class ExploreExecutor {

  private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(5);
  private static final ScheduledExecutorService SCHEDULED_EXECUTOR =
      Executors.newScheduledThreadPool(2);

  public static Future<Boolean> submitTask(Callable<Boolean> task) {
    return EXECUTOR_SERVICE.submit(task);
  }

  public static Future<Boolean> submitTimeoutTask(
      Callable<Boolean> task, int millis, Runnable cleanup) {
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
    EXECUTOR_SERVICE.shutdown();
    SCHEDULED_EXECUTOR.shutdown();
  }
}
