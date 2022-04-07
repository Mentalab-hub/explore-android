package com.mentalab;

import android.util.Log;
import com.mentalab.commandtranslators.Command;
import com.mentalab.commandtranslators.CommandTranslator;
import com.mentalab.exception.InvalidCommandException;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.io.ContentServer;
import com.mentalab.packets.Packet;
import com.mentalab.packets.PacketId;
import com.mentalab.packets.Publishable;
import com.mentalab.packets.info.DeviceInfoPacket;
import com.mentalab.packets.sensors.exg.EEGPacket;
import com.mentalab.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MentalabCodec {

  private static final ExecutorService DECODE_EXECUTOR = Executors.newSingleThreadExecutor();
  private static final ParseRawDataTask DECODER_TASK = new ParseRawDataTask();
  private static ExploreDevice exploreDevice;

  private MentalabCodec() { // Static class
  }

  /**
   * Tells the ExploreExecutor to decode the raw data.
   *
   * @param rawData InputStream of device bytes
   */
  public static void startDecode(InputStream rawData, ExploreDevice device) {
    DECODER_TASK.setInputStream(rawData);
    exploreDevice = device;
    DECODE_EXECUTOR.submit(DECODER_TASK);
  }

  /**
   * Encodes a command
   *
   * @return byte[] encoded commands that can be sent to the device
   * @throws InvalidCommandException when the command is not recognized
   */
  static byte[] encodeCommand(Command command) {
    final CommandTranslator translator = command.createCommandTranslator();
    return translator.translateCommand();
  }

  private static Packet parsePayloadData(int pId, double timeStamp, byte[] byteBuffer)
      throws InvalidDataException {
    for (PacketId packetId : PacketId.values()) {
      if (packetId.getNumVal() != pId) {
        continue;
      }

      final Packet packet = packetId.createInstance(timeStamp);
      if (packet != null) {
        packet.convertData(byteBuffer);
        return packet;
      }
    }
    return null;
  }

  public static void shutdown() {
    DECODE_EXECUTOR.shutdownNow();
  }

  private static class ParseRawDataTask implements Callable<Void> {

    private InputStream btInputStream;
    private byte[] buffer = new byte[1024];

    public void setInputStream(InputStream inputStream) {
      btInputStream = inputStream; // todo: do we need to stop call while this happens?
    }

    public Void call() throws IOException, InvalidDataException {
      while (!Thread.currentThread().isInterrupted()) { // otherwise end of stream
        buffer = new byte[1024];
        int pID = readStreamToInt(1);
        int count = readStreamToInt(1);
        int payload = readStreamToInt(2);
        double timeStamp = readStreamToInt(4);
        timeStamp = timeStamp / 10_000; // convert to seconds

        // read payload data
        buffer = new byte[payload - 4];
        btInputStream.read(buffer, 0, buffer.length); // read into buffer

        // parsing payload data
        final Packet packet =
            parsePayloadData(pID, timeStamp, Arrays.copyOfRange(buffer, 0, buffer.length - 4));
        Log.d(Utils.TAG, "Packet received: " + packet.toString());
        if (packet instanceof DeviceInfoPacket) {
          exploreDevice.setActiveSamplingRate(((DeviceInfoPacket) packet).getSamplingRate());
          exploreDevice.setCurrentChannelMask(((DeviceInfoPacket) packet).getChannelMask());
        } else if (packet instanceof Publishable) {
          ContentServer.getInstance().publish(((Publishable) packet).getTopic(), packet);
        }

        // sets max channel numbers, will be changed when channel number attribute is added in
        // device info from FW
        if (packet instanceof EEGPacket && exploreDevice.getNoChannels() == 0) {
          exploreDevice.setNumberOfChannels(packet.getDataCount());
        }
      }
      return null;
    }

    private int readStreamToInt(int length) throws IOException {
      btInputStream.read(buffer, 0, length); // read into buffer
      int value = ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
      buffer = new byte[1024]; // reset buffer for next read
      return value;
    }
  }
}
