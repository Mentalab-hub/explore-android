package com.mentalab.service;

import android.util.Log;

import com.mentalab.ExploreDevice;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.io.ContentServer;
import com.mentalab.io.Subscriber;
import com.mentalab.packets.Packet;
import com.mentalab.service.LslLoader.ChannelFormat;
import com.mentalab.service.LslLoader.StreamInfo;
import com.mentalab.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class LslStreamerTask implements Callable<Boolean> {

  private static final int nominalSamplingRateOrientation = 20;
  private static final int dataCountOrientation = 9;

  static LslLoader.StreamOutlet lslStreamOutletExg;
  static LslLoader.StreamOutlet lslStreamOutletOrn;
  static LslLoader.StreamOutlet lslStreamOutletMarker;

  private final ExploreDevice connectedDevice;

  private LslLoader.StreamInfo lslStreamInfoExg;
  private LslLoader.StreamInfo lslStreamInfoOrn;
  private LslLoader.StreamInfo lslStreamInfoMarker;

  public LslStreamerTask(ExploreDevice device) {
    this.connectedDevice = device;
  }

  @Override
  public Boolean call() throws InvalidDataException {
    try {
      lslStreamInfoOrn =
          new StreamInfo(
              connectedDevice.getDeviceName() + "_ORN",
              "ORN",
              dataCountOrientation,
              nominalSamplingRateOrientation,
              ChannelFormat.float32,
              connectedDevice.getDeviceName() + "_ORN");
      if (lslStreamInfoOrn == null) {
        throw new IOException("Stream Info is Null!!");
      }
      lslStreamOutletOrn = new LslLoader.StreamOutlet(lslStreamInfoOrn);

      lslStreamInfoMarker =
          new StreamInfo(
              connectedDevice.getDeviceName() + "_Marker",
              "Markers",
              1,
              0,
              ChannelFormat.int32,
              connectedDevice.getDeviceName() + "_Markers");

      if (lslStreamInfoMarker == null) {
        throw new IOException("Stream Info is Null!!");
      }
      lslStreamOutletMarker = new LslLoader.StreamOutlet(lslStreamInfoMarker);
      Log.d(Utils.TAG, "Subscribing!!");
      ContentServer.getInstance()
          .registerSubscriber(
              new Subscriber() {
                /**
                 * Performs this operation on the given argument.
                 *
                 * @param packet the input argument
                 */
                @Override
                public void accept(Packet packet) {
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
              });

      // ContentServer.getInstance().subscribe("Orn", this::packetCallbackOrn);

      // ContentServer.getInstance().subscribe("Marker", this::packetCallbackMarker);
    } catch (IOException exception) {
      throw new InvalidDataException("Error while stream LSL stream", null);
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
