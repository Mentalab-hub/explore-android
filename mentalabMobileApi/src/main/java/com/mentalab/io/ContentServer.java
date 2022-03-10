package com.mentalab.io;

import com.mentalab.packets.Packet;
import com.mentalab.utils.constants.Topic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ContentServer {

  private static ContentServer INSTANCE;
  private final Map<Topic, ArrayList<Subscriber>> topicSubscribers = new HashMap<>();

  private ContentServer() {}

  public static ContentServer getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ContentServer();
    }
    return INSTANCE;
  }

  public synchronized void publish(Topic topic, Packet message) {
    final ArrayList<Subscriber> subscribers = this.topicSubscribers.get(topic);
    if (subscribers == null) {
      return;
    }

    for (Subscriber s : subscribers) {
      s.accept(message);
    }
  }

  public synchronized void registerSubscriber(Subscriber sub) {
    this.topicSubscribers.computeIfAbsent(sub.getTopic(), k -> new ArrayList<>()).add(sub);
  }
}
