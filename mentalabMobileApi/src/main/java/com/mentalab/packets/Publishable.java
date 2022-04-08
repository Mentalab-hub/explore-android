package com.mentalab.packets;

import com.mentalab.utils.constants.Topic;

public interface Publishable {

  Topic getTopic();

  void publish();
}
