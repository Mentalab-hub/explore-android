package com.mentalab.io;

import com.mentalab.packets.Packet;
import com.mentalab.utils.constants.Topic;

import java.util.function.Consumer;

public abstract class Subscriber implements Consumer<Packet> {

  Topic t;

  public Topic getTopic() {
    return t;
  }
}
