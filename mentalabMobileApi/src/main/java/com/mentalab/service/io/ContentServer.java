package com.mentalab.service.io;

import com.mentalab.packets.Packet;
import com.mentalab.utils.constants.Topic;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ContentServer {

  private volatile static ContentServer INSTANCE;
  private final Map<Topic, Set<Subscriber>> topicSubscribers = new ConcurrentHashMap<>();

  private ContentServer() {}

  public static ContentServer getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ContentServer();
    }
    return INSTANCE;
  }

  public synchronized void publish(Topic topic, Packet message) {
    final Set<Subscriber> subscribers = this.topicSubscribers.get(topic);
    if (subscribers == null) {
      return;
    }

    for (Subscriber s : subscribers) {
      s.accept(message);
    }
  }

  public synchronized void registerSubscriber(Subscriber sub) {
    this.topicSubscribers.computeIfAbsent(sub.getTopic(), k -> new HashSet<>()).add(sub);
  }

  public synchronized void deRegisterSubscriber(Subscriber sub) {
    this.topicSubscribers.get(sub.getTopic()).remove(sub);
  }
}

