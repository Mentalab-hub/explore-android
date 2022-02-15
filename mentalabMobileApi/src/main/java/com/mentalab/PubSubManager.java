package com.mentalab;

import com.mentalab.io.constants.Topic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

class PubSubManager {

  private static final PubSubManager pubSubSingleton = new PubSubManager();

  private final Map<Topic, ArrayList<Consumer<?>>> topicsSubscribers = new HashMap<>();

  public static PubSubManager getInstance() {
    return pubSubSingleton;
  }

  public synchronized <T> void publish(String topic, T message) {
    ArrayList<Consumer<?>> subscribers = this.topicsSubscribers.get(topic);
    if (subscribers == null) return;

    for (Consumer subscriberConsumer : subscribers) {
      subscriberConsumer.accept(message);
    }
  }

  public synchronized <T> void subscribe(Topic topic, Consumer<T> subscriberCallback) {
    ArrayList<Consumer<?>> subscribers = this.topicsSubscribers.get(topic);
    if (subscribers == null) {
      subscribers = new ArrayList<Consumer<?>>();
      topicsSubscribers.put(topic, subscribers);
    }
    subscribers.add(subscriberCallback);
  }
}
