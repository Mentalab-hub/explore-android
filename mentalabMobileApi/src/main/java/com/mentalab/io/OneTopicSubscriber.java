package com.mentalab.io;

import com.mentalab.io.constants.Topic;
import com.mentalab.packets.Packet;

public class OneTopicSubscriber extends AbstractSubscriber {

    public OneTopicSubscriber(Topic t) {
        super(t);
    }


    @Override
    public void receiveMessage(Topic t, Packet m) {
        messagesReceived.add(m);
    }
}
