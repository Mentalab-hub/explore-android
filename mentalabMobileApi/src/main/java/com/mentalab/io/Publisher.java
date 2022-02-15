package com.mentalab.io;

import com.mentalab.io.constants.Topic;
import com.mentalab.packets.Packet;

public class Publisher {

    private final Topic topic;

    public Publisher(Topic topic) {
        this.topic = topic;
    }

    public void publish(Packet m) {
        ContentServer.getInstance().sendMessage(this.topic, m);
    }
}
