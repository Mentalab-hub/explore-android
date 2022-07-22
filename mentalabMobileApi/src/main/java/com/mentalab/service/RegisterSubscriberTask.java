package com.mentalab.service;

import com.mentalab.service.io.ContentServer;
import com.mentalab.service.io.CountDownSubscriber;
import com.mentalab.utils.CheckedExceptionSupplier;

import java.util.concurrent.Callable;

public abstract class RegisterSubscriberTask<T> implements CheckedExceptionSupplier<Boolean> {

  protected T getResultOf(CountDownSubscriber<T> sub) throws InterruptedException {
    ContentServer.getInstance().registerSubscriber(sub);
    T result = sub.awaitResult();
    ContentServer.getInstance().deRegisterSubscriber(sub);
    return result;
  }

  T registerTimeoutSub(CountDownSubscriber<T> sub) throws InterruptedException {
    ContentServer.getInstance().registerSubscriber(sub);
    T result = sub.awaitResultWithTimeout(3000);
    ContentServer.getInstance().deRegisterSubscriber(sub);
    return result;
  }

  T registerTimeoutSubAndThen(CountDownSubscriber<T> sub, Callable<Void> andThen)
      throws Exception {
    ContentServer.getInstance().registerSubscriber(sub);
    andThen.call();
    T result = sub.awaitResultWithTimeout(3000);
    ContentServer.getInstance().deRegisterSubscriber(sub);
    return result;
  }
}
