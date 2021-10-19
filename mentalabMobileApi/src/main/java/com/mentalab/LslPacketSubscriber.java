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
  static LslLoader.StreamOutlet lslStreamOutletExg;
  private LslLoader.StreamInfo lslStreamInfoExg;
  static LslLoader.StreamOutlet lslStreamOutletOrn;
  private LslLoader.StreamInfo lslStreamInfoOrn;
  static LslLoader lslLoader = new LslLoader();


  public LslPacketSubscriber() throws IOException {

    lslStreamInfoExg = new StreamInfo("Explore_ExG", "ExG", 8, 250, LslLoader.ChannelFormat.float32, "ExG");
    if (lslStreamInfoExg == null) {
      throw new IOException("Stream Info is Null!!");
    }
    lslStreamOutletExg = new LslLoader.StreamOutlet(lslStreamInfoExg);

    lslStreamInfoOrn = new StreamInfo("Explore_Orn", "Orn", 9, 20, LslLoader.ChannelFormat.float32, "Orn");
    if (lslStreamInfoOrn == null) {
      throw new IOException("Stream Info is Null!!");
    }
    lslStreamOutletOrn = new LslLoader.StreamOutlet(lslStreamInfoOrn);

    Log.d(TAG, "Subscribing!!");
    PubSubManager.getInstance().subscribe("ExG", this::packetCallback);
    PubSubManager.getInstance().subscribe("Orn", this::packetCallback);


  }


  public void packetCallback(Packet packet) {
    Log.d(TAG, "Getting data in LSL callback!!");
    if (packet instanceof DataPacket){
      lslStreamOutletExg.push_sample(convertArraylistToFloatArray(packet));
    }
    else if (packet instanceof Orientation){

      lslStreamOutletOrn.push_sample(convertArraylistToFloatArray(packet));
    }

  }

  float[] convertArraylistToFloatArray(Packet packet){
    ArrayList<Float> packetVoltageValues= new ArrayList<Float>();
    if (packet instanceof DataPacket){
    packetVoltageValues = ((DataPacket)packet).getVoltageValues();
    float[] floatArray = new float[packetVoltageValues.size()];
    Object[] array = packetVoltageValues.toArray();
    for (int index = 0; index < packetVoltageValues.size(); index ++){
      floatArray[index] = ((Float) packetVoltageValues.get(index)).floatValue();
    }
    return floatArray;
  }
    else if(packet instanceof Orientation){
      packetVoltageValues = ((Orientation)packet).listValues;
      float[] floatArray = new float[packetVoltageValues.size()];
      Object[] array = packetVoltageValues.toArray();
      for (int index = 0; index < packetVoltageValues.size(); index ++){
        floatArray[index] = ((Float) packetVoltageValues.get(index)).floatValue();
      }
      return floatArray;
    }
    return null;
  }
}
