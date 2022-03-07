package com.mentalab.io;

import com.mentalab.packets.Packet;
import com.mentalab.packets.command.CommandStatus;
import com.mentalab.utils.constants.Topic;

public class CommandAcknowledgeSubscriber extends Subscriber {

    volatile Boolean result;


    public CommandAcknowledgeSubscriber() {
        this.t = Topic.COMMAND;
    }


    @Override
    public void accept(Packet p) {
        if (p instanceof CommandStatus) {
            result = ((CommandStatus) p).getResult();
        } else {
            result = false;
        }
        synchronized (this) {
            result.notify();
        }
    }


    public boolean getAcknowledgement() throws InterruptedException {
        synchronized (this) {
            result.wait(3_000);
        }
        return result;
    }
}
