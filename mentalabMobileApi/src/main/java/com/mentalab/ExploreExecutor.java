package com.mentalab;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class ExploreExecutor {

  private ExploreExecutor() { // Static class
  }

  private static ExecutorService SERIAL_EXECUTOR = Executors.newSingleThreadExecutor();
  private static ExecutorService PARALLEL_EXECUTOR = Executors.newFixedThreadPool(5);
  private static ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newScheduledThreadPool(2);

  static ExecutorService getExecutorInstance() {
    return PARALLEL_EXECUTOR;
  }

  static ExecutorService getSerialExecutorInstance() {
    return SERIAL_EXECUTOR;
  }

  static ScheduledExecutorService getScheduledExecutorInstance() {
    return SCHEDULED_EXECUTOR;
  }

  static void resetExecutorServices() {
    SERIAL_EXECUTOR.shutdownNow();
    SERIAL_EXECUTOR = Executors.newSingleThreadExecutor();

    PARALLEL_EXECUTOR.shutdownNow();
    PARALLEL_EXECUTOR = Executors.newFixedThreadPool(5);

    SCHEDULED_EXECUTOR.shutdownNow();
    SCHEDULED_EXECUTOR = Executors.newScheduledThreadPool(2);
  }

  static void blockExecutorServices() throws InterruptedException {
    PARALLEL_EXECUTOR.wait();
    SCHEDULED_EXECUTOR.wait();
  }

  static void unblockExecutorServices() {
    PARALLEL_EXECUTOR.notifyAll();
    SCHEDULED_EXECUTOR.notifyAll();
  }

  static void shutDown() {
    SERIAL_EXECUTOR.shutdown();
    SCHEDULED_EXECUTOR.shutdown();
  }
}
