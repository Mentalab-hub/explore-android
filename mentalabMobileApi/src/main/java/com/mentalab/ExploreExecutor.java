package com.mentalab;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ExploreExecutor {

  final AtomicBoolean isLocked = new AtomicBoolean(true);

  private ExecutorService serialExecutor = Executors.newSingleThreadExecutor();
  private ExecutorService parallelExecutor = Executors.newFixedThreadPool(5);
  private ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(2);

  public static ExploreExecutor getInstance() {
    return ExploreExecutor.InstanceHolder.INSTANCE;
  }

  ExecutorService getExecutor() {
    testAvailablity();
    return parallelExecutor;
  }

  ExecutorService getSerialExecutor() {
    testAvailablity();
    return serialExecutor;
  }

  ScheduledExecutorService getScheduledExecutor() {
    testAvailablity();
    return scheduledExecutor;
  }

  void resetExecutorServices() {
    shutDownNow();
    serialExecutor = Executors.newSingleThreadExecutor();
    parallelExecutor = Executors.newFixedThreadPool(5);
    scheduledExecutor = Executors.newScheduledThreadPool(2);
  }

  void shutDownNow() {
    serialExecutor.shutdownNow();
    parallelExecutor.shutdownNow();
    scheduledExecutor.shutdownNow();
  }

  void shutDown() {
    serialExecutor.shutdown();
    parallelExecutor.shutdown();
    scheduledExecutor.shutdown();
  }

  private ExploreExecutor() {}

  private static class InstanceHolder { // Initialization-on-demand synchronization
    private static final ExploreExecutor INSTANCE = new ExploreExecutor();
  }

  private synchronized void testAvailablity() {
    if (!isLocked.get()) {
      throw new RejectedExecutionException(
              "Cannot proceed with task. This is most likely because the impedance task is runnning.");
    }
  }

  public AtomicBoolean getLock() {
    return isLocked;
  }
}
