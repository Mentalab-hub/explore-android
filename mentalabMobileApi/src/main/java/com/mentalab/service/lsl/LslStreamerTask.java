package com.mentalab.service.lsl;

import android.util.Log;
import com.mentalab.ExploreDevice;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.service.io.ContentServer;
import com.mentalab.service.io.Subscriber;
import com.mentalab.packets.Packet;
import com.mentalab.utils.constants.Topic;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

public class LslStreamerTask implements Callable<Boolean> {

  private static final int nominalSamplingRateOrientation = 20;
  private static final int dataCountOrientation = 9;
  private static int samplingRate;

  static StreamOutlet lslStreamOutletExg;
  static StreamOutlet lslStreamOutletOrn;
  static StreamOutlet lslStreamOutletMarker;

  private final ExploreDevice connectedDevice;

  private StreamInfo lslStreamInfoExg;
  private StreamInfo lslStreamInfoOrn;
  private StreamInfo lslStreamInfoMarker;

  // Pushes ExG, Orientation and Marker packets to LSL(Lab Streaming Layer)
  public LslStreamerTask(ExploreDevice device) {
    this.connectedDevice = device;
    samplingRate = device.getSamplingRate().getAsInt();
  }

  @Override
  public Boolean call() throws InvalidDataException {
    try {

      lslStreamInfoExg =
          new StreamInfo(
              connectedDevice + "_ExG",
              "ExG",
              connectedDevice.getChannelCount(),
              samplingRate,
              ChannelFormat.FLOAT_32,
              connectedDevice + "_ExG");

      if (lslStreamInfoExg == null) {
        throw new IOException("Stream Info is Null!!");
      }

      lslStreamOutletExg = new StreamOutlet(lslStreamInfoExg);

      lslStreamInfoOrn =
          new StreamInfo(
              connectedDevice.getDeviceName() + "_ORN",
              "ORN",
              dataCountOrientation,
              nominalSamplingRateOrientation,
              ChannelFormat.FLOAT_32,
              connectedDevice.getDeviceName() + "_ORN");
      if (lslStreamInfoOrn == null) {
        throw new IOException("Stream Info is Null!!");
      }
      lslStreamOutletOrn = new StreamOutlet(lslStreamInfoOrn);

      lslStreamInfoMarker =
          new StreamInfo(
              connectedDevice.getDeviceName() + "_Marker",
              "Markers",
              1,
              0,
              ChannelFormat.INT_32,
              connectedDevice.getDeviceName() + "_Markers");

      if (lslStreamInfoMarker == null) {
        throw new IOException("Stream Info is Null!!");
      }
      lslStreamOutletMarker = new StreamOutlet(lslStreamInfoMarker);

      ContentServer.getInstance()
          .registerSubscriber(
              new Subscriber(Topic.EXG) {

                @Override
                public void accept(Packet packet) {
                  lslStreamOutletExg.push_chunk(convertArraylistToFloatArray(packet));
                }
              });

      ContentServer.getInstance()
          .registerSubscriber(
              new Subscriber(Topic.ORN) {
                @Override
                public void accept(Packet packet) {
                  lslStreamOutletOrn.push_sample(convertArraylistToFloatArray(packet));
                }
              });

      ContentServer.getInstance()
          .registerSubscriber(
              new Subscriber(Topic.MARKER) {
                @Override
                public void accept(Packet packet) {
                  lslStreamOutletMarker.push_sample(convertArraylistToFloatArray(packet));
                }
              });
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
              ChannelFormat.FLOAT_32,
              connectedDevice + "_ExG");
      if (lslStreamInfoExg != null) {
        try {
          lslStreamOutletExg = new StreamOutlet(lslStreamInfoExg);
        } catch (IOException exception) {
          exception.printStackTrace();
        }
      }
    }
    Log.d("TAG", "packetCallbackExG");
    lslStreamOutletExg.push_chunk(convertArraylistToFloatArray(packet));
  }

  float[] convertArraylistToFloatArray(Packet packet) {
    List<Float> packetVoltageValues = packet.getData();
    float[] floatArray = new float[packetVoltageValues.size()];
    Object[] array = packetVoltageValues.toArray();
    for (int index = 0; index < packetVoltageValues.size(); index++) {
      floatArray[index] = ((Float) packetVoltageValues.get(index)).floatValue();
    }
    return floatArray;
  }
}
