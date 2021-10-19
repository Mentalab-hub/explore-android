package com.mentalab;

import android.util.Log;

import com.mentalab.LslLoader.StreamInfo;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import java.io.IOException;
import java.util.ArrayList;

public class LslPacketSubscriber{

  private static final String TAG = "EXPLORE_LSL_DEV";
  static LslLoader.StreamOutlet lslStreamOutlet;
  private LslLoader.StreamInfo lslStreamInfo;
  static LslLoader lslLoader = new LslLoader();


  public LslPacketSubscriber() throws IOException {


    lslStreamInfo = new StreamInfo("Explore_ExG", "ExG", 8, 250, LslLoader.ChannelFormat.float32, "ExG");
    if (lslStreamInfo == null) {
      throw new IOException("Stream Info is Null!!");
    }
    lslStreamOutlet = new LslLoader.StreamOutlet(lslStreamInfo);

    Log.d(TAG, "Subscribing!!");
    PubSubManager.getInstance().subscribe("ExG", this::packetCallback);


  }


  public void packetCallback(Packet packet) {
    Log.d(TAG, "Getting data in LSL callback!!");
    if (packet instanceof DataPacket){
      ArrayList<Float> packetVoltageValues = ((DataPacket)packet).getVoltageValues();
      lslStreamOutlet.push_sample(packetVoltageValues.stream().mapToDouble(i-> i).toArray());
    }

  }
}
