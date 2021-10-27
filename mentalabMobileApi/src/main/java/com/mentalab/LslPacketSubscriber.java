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

  @Override
  public void run() {
    try {

      lslStreamInfoOrn =
          new StreamInfo(
              "Explore_Orn",
              "Orn",
              dataCountOrientation,
              nominalSamplingRateOrientation,
              ChannelFormat.float32,
              "Orn");
      if (lslStreamInfoOrn == null) {
        throw new IOException("Stream Info is Null!!");
      }
      lslStreamOutletOrn = new LslLoader.StreamOutlet(lslStreamInfoOrn);

      lslStreamInfoMarker =
          new StreamInfo("Explore_Marker", "Marker", 1, 0, ChannelFormat.float32, "Marker");

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
              "Explore_ExG",
              "ExG",
              packet.getDataCount(),
              250,
              LslLoader.ChannelFormat.float32,
              "ExG");
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
