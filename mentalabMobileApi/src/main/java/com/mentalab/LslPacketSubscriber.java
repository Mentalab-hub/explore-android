package com.mentalab;

import android.util.Log;

public class LslPacketSubscriber {

  private static final String TAG = "EXPLORE_LSL_DEV";

  public LslPacketSubscriber() {
    Log.d(TAG, "Subscribing!!");
    PubSubManager.getInstance().subscribe("ExG", this::packetCallback);
  }

  public void packetCallback(Packet packet) {
    Log.d(TAG, "Getting data in LSL callback!!");
  }
}
