package com.mentalab.service.io;

import com.mentalab.packets.Packet;
import com.mentalab.utils.constants.Topic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ContentServer {

  private final Map<Topic, Set<Subscriber>> topicSubscribers = new HashMap<>();

  public static ContentServer getInstance() {
    return InstanceHolder.INSTANCE;
  }

  public void publish(Topic topic, Packet message) {
    final Set<Subscriber> subscribers = this.topicSubscribers.get(topic);
    if (subscribers == null) {
      return;
    }

    for (Subscriber s : subscribers) {
      s.accept(message);
    }
  }

  public void registerSubscriber(Subscriber sub) {
    this.topicSubscribers.computeIfAbsent(sub.getTopic(), k -> new HashSet<>()).add(sub);
  }

  public void deRegisterSubscriber(Subscriber sub) {
    final Set<Subscriber> topicSubscribers = this.topicSubscribers.get(sub.getTopic());
    if (topicSubscribers != null) {
      topicSubscribers.remove(sub);
    }
  }

  private ContentServer() {}

  private static class InstanceHolder { // Initialization-on-demand synchronization
    static final ContentServer INSTANCE = new ContentServer();
  }
}
