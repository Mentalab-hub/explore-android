package com.mentalab.io;

import com.mentalab.io.constants.Topic;
import com.mentalab.packets.Packet;

import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractSubscriber implements Subscriber {

    public AbstractSubscriber(Topic... topics) {
        for (Topic t : topics) {
            ContentServer.getInstance().registerSubscriber(this, t);
        }
    }


    public LinkedBlockingQueue<Packet> getMessagesReceived() {
        return messagesReceived;
    }
}
