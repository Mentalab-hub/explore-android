package com.mentalab.service;

import com.mentalab.exception.InvalidCommandException;
import com.mentalab.tasks.DecoderTask;
import com.mentalab.tasks.DeviceConfigurationTask;
import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ExploreThreadPoolExecutor extends ThreadPoolExecutor {
  ExecutorService decoderTaskService = Executors.newSingleThreadExecutor();



  /**
   * Creates a new {@code ThreadPoolExecutor} with the given initial parameters and default thread
   * factory and rejected execution handler. It may be more convenient to use one of the {@link
   * Executors} factory methods instead of this general purpose constructor.
   *
   * @param corePoolSize    the number of threads to keep in the pool, even if they are idle, unless
   *                        {@code allowCoreThreadTimeOut} is set
   * @param maximumPoolSize the maximum number of threads to allow in the pool
   * @param keepAliveTime   when the number of threads is greater than the core, this is the maximum
   *                        time that excess idle threads will wait for new tasks before terminating.
   * @param unit            the time unit for the {@code keepAliveTime} argument
   * @param workQueue       the queue to use for holding tasks before they are executed.  This queue
   *                        will hold only the {@code Runnable} tasks submitted by the {@code execute}
   *                        method.
   * @throws IllegalArgumentException if one of the following holds:<br> {@code corePoolSize < 0}<br>
   *                                  {@code keepAliveTime < 0}<br> {@code maximumPoolSize <= 0}<br>
   *                                  {@code maximumPoolSize < corePoolSize}
   * @throws NullPointerException     if {@code workQueue} is null
   */
  public ExploreThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
      TimeUnit unit, BlockingQueue<Runnable> workQueue) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
  }



  public void startDecoderTask(Callable<Void> task) {
    decoderTaskService.submit(task);
  }

  public void stopDecoderTask(Callable<Void> task) {
    decoderTaskService.submit(task);
  }
  /**
   * Initiates an orderly shutdown in which previously submitted tasks are executed, but no new
   * tasks will be accepted. Invocation has no additional effect if already shut down.
   *
   * <p>This method does not wait for previously submitted tasks to
   * complete execution.  Use {@link #awaitTermination awaitTermination} to do that.
   */
  @Override
  public void shutdown() {
    decoderTaskService.shutdown();
    super.shutdown();
  }


  /**
   * @param task
   * @throws RejectedExecutionException {@inheritDoc}
   * @throws NullPointerException       {@inheritDoc}
   */
  @Override
  public <T> Future<T> submit(Callable<T> task) {
    if (((LinkedBlockingDeque)getQueue()).peekFirst() instanceof DeviceConfigurationTask){
      throw new RejectedExecutionException("Can not execute multiple config tasks in parallel", null);
    }

    Future<T> submittedConfigurationTask = super.submit(task);
    try {
      submittedConfigurationTask.get();
    } catch (ExecutionException | InterruptedException e) {
      return submittedConfigurationTask;
    }
    return submittedConfigurationTask;
  }
}
