package com.mentalab;

import android.util.Log;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;

public class LslPacketSubscriber {

  private static final String TAG = "EXPLORE_LSL_DEV";

  public LslPacketSubscriber() {

    Log.d(TAG, "Subscribing!!");
    PubSubManager.getInstance().subscribe("ExG", this::packetCallback);
    System.loadLibrary("lsl");
  }

  public void packetCallback(Packet packet) {
    Log.d(TAG, "Getting data in LSL callback!!");
  }
}
