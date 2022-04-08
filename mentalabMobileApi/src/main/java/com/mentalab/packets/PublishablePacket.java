package com.mentalab.packets;

import com.mentalab.io.ContentServer;
import com.mentalab.utils.constants.Topic;

public abstract class PublishablePacket extends Packet implements Publishable {

    protected PublishablePacket(double timeStamp) {
        super(timeStamp);
    }

    @Override
    public Topic getTopic() {
        return null;
    }

    public void publish() {
        ContentServer.getInstance().publish(this.getTopic(), this);
    }
}
