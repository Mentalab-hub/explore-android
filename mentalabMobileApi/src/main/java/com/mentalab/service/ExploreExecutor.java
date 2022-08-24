package com.mentalab.service;

import java.util.concurrent.*;

public final class ExploreExecutor {

  private ExploreExecutor() { // Static class
  }

  private static ExecutorService BLOCKING_EXECUTOR = Executors.newSingleThreadExecutor();
  private static final ScheduledExecutorService SCHEDULED_EXECUTOR =
      Executors.newScheduledThreadPool(2);

  public static Future<Boolean> submitTask(Callable<Boolean> task) {
    return BLOCKING_EXECUTOR.submit(task);
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

  public static Future<Boolean> submitImpedanceTask(Callable<Boolean> impedanceTask) {
    BLOCKING_EXECUTOR.shutdownNow();
    BLOCKING_EXECUTOR = Executors.newSingleThreadExecutor();
    return BLOCKING_EXECUTOR.submit(impedanceTask);
  }

  public static ExecutorService getExecutorInstance() {
    return BLOCKING_EXECUTOR;
  }

  public static void shutDown() {
    BLOCKING_EXECUTOR.shutdown();
    SCHEDULED_EXECUTOR.shutdown();
  }
}
