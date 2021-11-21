package com.mentalab;

import android.util.Log;
import com.mentalab.LslLoader.ChannelFormat;
import com.mentalab.LslLoader.StreamInfo;
import java.io.IOException;
import java.util.ArrayList;

public class LslPacketSubscriber extends Thread {

  private static final String TAG = "EXPLORE_LSL_DEV";
  private static final int nominalSamplingRateOrientation = 20;
  private static final int dataCountOrientation = 9;
  static LslLoader.StreamOutlet lslStreamOutletExg;
  static LslLoader.StreamOutlet lslStreamOutletOrn;
  static LslLoader.StreamOutlet lslStreamOutletMarker;
  static LslLoader lslLoader = new LslLoader();
  private LslLoader.StreamInfo lslStreamInfoExg;
  private LslLoader.StreamInfo lslStreamInfoOrn;
  private LslLoader.StreamInfo lslStreamInfoMarker;
  private static String connectedDeviceName = null;

  public LslPacketSubscriber(String deviceName) {
    Log.d(TAG, "In Constructor!!!!");
    connectedDeviceName = deviceName;

  }

  @Override
  public void run() {
    try {

      lslStreamInfoOrn =
          new StreamInfo(
              connectedDeviceName + "_ORN",
              "ORN",
              dataCountOrientation,
              nominalSamplingRateOrientation,
              ChannelFormat.float32,
              connectedDeviceName + "_ORN");
      if (lslStreamInfoOrn == null) {
        throw new IOException("Stream Info is Null!!");
      }
      lslStreamOutletOrn = new LslLoader.StreamOutlet(lslStreamInfoOrn);

      lslStreamInfoMarker =
          new StreamInfo(connectedDeviceName + "_Marker", "Markers", 1, 0, ChannelFormat.int32, connectedDeviceName+ "_Markers");

      if (lslStreamInfoMarker == null) {
        throw new IOException("Stream Info is Null!!");
      }
      lslStreamOutletMarker = new LslLoader.StreamOutlet(lslStreamInfoMarker);
      Log.d(TAG, "Subscribing!!");
      PubSubManager.getInstance().subscribe("ExG", this::packetCallbackExG);

      PubSubManager.getInstance().subscribe("Orn", this::packetCallbackOrn);

      PubSubManager.getInstance().subscribe("Marker", this::packetCallbackMarker);
    } catch (IOException exception) {
      exception.printStackTrace();
    }
  }

  public void packetCallbackExG(Packet packet) {
    if (lslStreamInfoExg == null) {
      lslStreamInfoExg =
          new StreamInfo(
              connectedDeviceName + "_ExG",
              "ExG",
              packet.getDataCount(),
              250,
              LslLoader.ChannelFormat.float32,
              connectedDeviceName + "_ExG");
      if (lslStreamInfoExg != null) {
        try {
          lslStreamOutletExg = new LslLoader.StreamOutlet(lslStreamInfoExg);
        } catch (IOException exception) {
          exception.printStackTrace();
        }
      }
    }
    Log.d("TAG", "packetCallbackExG");
    lslStreamOutletExg.push_chunk(convertArraylistToFloatArray(packet));
  }

  public void packetCallbackOrn(Packet packet) {
    Log.d("TAG", "packetCallbackOrn");
    lslStreamOutletOrn.push_sample(convertArraylistToFloatArray(packet));
  }

  public void packetCallbackMarker(Packet packet) {
    Log.d("TAG", "packetCallbackMarker");
    lslStreamOutletMarker.push_sample(convertArraylistToFloatArray(packet));
  }

  float[] convertArraylistToFloatArray(Packet packet) {
    ArrayList<Float> packetVoltageValues = packet.getData();
    float[] floatArray = new float[packetVoltageValues.size()];
    Object[] array = packetVoltageValues.toArray();
    for (int index = 0; index < packetVoltageValues.size(); index++) {
      floatArray[index] = ((Float) packetVoltageValues.get(index)).floatValue();
    }
    return floatArray;
  }
}
