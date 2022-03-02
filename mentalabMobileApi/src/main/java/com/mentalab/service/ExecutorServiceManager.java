package com.mentalab.service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceManager {
  private static final int NTHREADPOOL = 100;
  private static final ExecutorService executor = new ExploreThreadPoolExecutor(NTHREADPOOL, NTHREADPOOL,
      0L, TimeUnit.MILLISECONDS,
       new LinkedBlockingDeque<Runnable>());

  public static synchronized ExecutorService getExecutorService() {
    return executor;
  }


}
