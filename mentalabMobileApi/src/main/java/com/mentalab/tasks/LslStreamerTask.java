package com.mentalab.tasks;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import com.mentalab.PubSubManager;
import com.mentalab.packets.Packet;
import com.mentalab.tasks.LslLoader.ChannelFormat;
import com.mentalab.tasks.LslLoader.StreamInfo;
import com.mentalab.tasks.LslLoader.StreamOutlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class LslStreamerTask implements Callable<Void> {

  private static final String TAG = "EXPLORE_LSL_DEV";
  private static final int nominalSamplingRateOrientation = 20;
  private static final int dataCountOrientation = 9;
  static StreamOutlet lslStreamOutletExg;
  static StreamOutlet lslStreamOutletOrn;
  static StreamOutlet lslStreamOutletMarker;
  static LslLoader lslLoader = new LslLoader();
  private static BluetoothDevice connectedDevice;
  private LslLoader.StreamInfo lslStreamInfoExg;
  private LslLoader.StreamInfo lslStreamInfoOrn;
  private LslLoader.StreamInfo lslStreamInfoMarker;

  public LslStreamerTask(BluetoothDevice device) {
    connectedDevice = device;
  }

  @Override
  public Void call() {
    try {

      lslStreamInfoOrn =
          new StreamInfo(
              connectedDevice.getName() + "_ORN",
              "ORN",
              dataCountOrientation,
              nominalSamplingRateOrientation,
              ChannelFormat.float32,
              connectedDevice.getName() + "_ORN");
      if (lslStreamInfoOrn == null) {
        throw new IOException("Stream Info is Null!!");
      }
      lslStreamOutletOrn = new LslLoader.StreamOutlet(lslStreamInfoOrn);

      lslStreamInfoMarker =
          new StreamInfo(
              connectedDevice.getName() + "_Marker",
              "Markers",
              1,
              0,
              ChannelFormat.int32,
              connectedDevice.getName() + "_Markers");

      if (lslStreamInfoMarker == null) {
        throw new IOException("Stream Info is Null");
      }
      lslStreamOutletMarker = new LslLoader.StreamOutlet(lslStreamInfoMarker);
      Log.d(TAG, "Subscribing!!");
      PubSubManager.getInstance().subscribe("ExG", this::packetCallbackExG);

      PubSubManager.getInstance().subscribe("Orn", this::packetCallbackOrn);

      PubSubManager.getInstance().subscribe("Marker", this::packetCallbackMarker);
    } catch (IOException exception) {
      lslStreamOutletExg.close();
      lslStreamOutletOrn.close();
      lslStreamOutletMarker.close();
    }
    return null;
  }

  public void packetCallbackExG(Packet packet) {
    if (lslStreamInfoExg == null) {
      lslStreamInfoExg =
          new StreamInfo(
              connectedDevice + "_ExG",
              "ExG",
              packet.getDataCount(),
              250,
              LslLoader.ChannelFormat.float32,
              connectedDevice + "_ExG");
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
