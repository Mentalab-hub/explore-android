package com.mentalab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class PubSubManager {
  private static final PubSubManager pubSubSingleton = new PubSubManager();

  private Map<String, ArrayList<Consumer<?>>> topicsSubscribers =
      new HashMap<String, ArrayList<Consumer<?>>>();

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

  public synchronized <T> void subscribe(String topicName, Consumer<T> subscriberCallback) {
    ArrayList<Consumer<?>> subscribers = this.topicsSubscribers.get(topicName);
    if (subscribers == null) {
      subscribers = new ArrayList<Consumer<?>>();
      topicsSubscribers.put(topicName, subscribers);
    }
    subscribers.add(subscriberCallback);
  }
}
