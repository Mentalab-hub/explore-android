package com.mentalab.packets;

import com.mentalab.utils.constants.Topic;

public abstract class PublishablePacket extends Packet implements Publishable {

  protected PublishablePacket(double timeStamp) {
    super(timeStamp);
  }

  @Override
  public Topic getTopic() {
    return null;
  }
}
