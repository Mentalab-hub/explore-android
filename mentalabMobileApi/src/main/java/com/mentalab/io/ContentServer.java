package com.mentalab.io;

import com.mentalab.utils.constants.Topic;
import com.mentalab.packets.Packet;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ContentServer {

    private final Map<Topic, List<Subscriber>> subscribers;

    private static final ContentServer serverInstance = new ContentServer();


    private ContentServer() {
        this.subscribers = new HashMap<>();
        for (Topic t : Topic.values()) {
            this.subscribers.put(t, new LinkedList<>());
        }
    }


    public static ContentServer getInstance() {
        return serverInstance;
    }


    public void sendMessage(Topic t, Packet m) {
        List<Subscriber> subs = subscribers.get(t);
        for (Subscriber s : subs) {
            s.receiveMessage(t, m);
        }
    }


    public void registerSubscriber(Subscriber s, Topic t) {
        subscribers.get(t).add(s);
    }
}
