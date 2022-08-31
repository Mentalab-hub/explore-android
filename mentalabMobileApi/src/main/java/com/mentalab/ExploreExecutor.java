package com.mentalab;

import com.mentalab.exception.MentalabException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ExploreExecutor {

  final AtomicBoolean isLocked = new AtomicBoolean(true);

  private ExecutorService serialExecutor = Executors.newSingleThreadExecutor();
  private ExecutorService parallelExecutor = Executors.newFixedThreadPool(5);
  private ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(2);

  public static ExploreExecutor getInstance() {
    return ExploreExecutor.InstanceHolder.INSTANCE;
  }

  ExecutorService getExecutor() throws MentalabException {
    testAvailablity();
    return parallelExecutor;
  }

  ExecutorService getSerialExecutor() throws MentalabException {
    testAvailablity();
    return serialExecutor;
  }

  ScheduledExecutorService getScheduledExecutor() throws MentalabException {
    testAvailablity();
    return scheduledExecutor;
  }

  void resetExecutorServices() {
    shutDownNow();
    this.serialExecutor = Executors.newSingleThreadExecutor();
    this.parallelExecutor = Executors.newFixedThreadPool(5);
    this.scheduledExecutor = Executors.newScheduledThreadPool(2);
  }

  void shutDownNow() {
    this.serialExecutor.shutdownNow();
    this.parallelExecutor.shutdownNow();
    this.scheduledExecutor.shutdownNow();
  }

  void shutDown() {
    this.serialExecutor.shutdown();
    this.parallelExecutor.shutdown();
    this.scheduledExecutor.shutdown();
  }

  private ExploreExecutor() {}

  private static class InstanceHolder { // Initialization-on-demand synchronization
    private static final ExploreExecutor INSTANCE = new ExploreExecutor();
  }

  private synchronized void testAvailablity() throws MentalabException {
    if (!isLocked.get()) {
      throw new MentalabException(
          "Cannot proceed with task. This is most likely because impedance is runnning.");
    }
  }

  public AtomicBoolean getLock() {
    return isLocked;
  }
}
