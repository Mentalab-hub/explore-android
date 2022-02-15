package com.mentalab.io;

import com.mentalab.utils.constants.Topic;
import com.mentalab.packets.Packet;

import java.util.concurrent.LinkedBlockingQueue;

public interface Subscriber {

    LinkedBlockingQueue<Packet> messagesReceived = new LinkedBlockingQueue<>();

    void receiveMessage(Topic t, Packet m);

    LinkedBlockingQueue<Packet> getMessagesReceived();
}
