package com.mentalab.service;

import com.mentalab.service.io.ContentServer;
import com.mentalab.service.io.CountDownSubscriber;

import java.util.concurrent.Callable;

public abstract class RegisterSubscriberTask<T> implements Callable<Boolean> {

  T getResultOf(CountDownSubscriber<T> sub) throws InterruptedException {
    ContentServer.getInstance().registerSubscriber(sub);
    T result = sub.awaitResultWithTimeout(3000);
    ContentServer.getInstance().deRegisterSubscriber(sub);
    return result;
  }

  T getResultOfSubscriberAfterTask(CountDownSubscriber<T> sub, Callable<Void> task)
      throws Exception {
    ContentServer.getInstance().registerSubscriber(sub);
    task.call();
    T result = sub.awaitResultWithTimeout(3000);
    ContentServer.getInstance().deRegisterSubscriber(sub);
    return result;
  }
}
