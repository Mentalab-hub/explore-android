package com.mentalab.tasks;

import android.util.Log;
import com.mentalab.PubSubManager;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.packets.Packet;
import com.mentalab.packets.PacketId;
import com.mentalab.packets.PublishablePacket;
import com.mentalab.packets.QueueablePacket;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;

public class DecoderTask implements Callable<Void> {
  public static Map<String, Queue<Float>> decodedDataMap;
  private final InputStream inputStream;
  private static final String TAG = "Explore";

  public DecoderTask(InputStream inputStream) {
    this.inputStream = inputStream;
    initializeMapInstance();
  }

  public Void call() {

    int pId = 0;
    while (!Thread.currentThread().isInterrupted()) {
      try {
        byte[] buffer = new byte[1024];
        // reading PID
        inputStream.read(buffer, 0, 1);
        pId = ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        Log.d(TAG, "pid .." + pId);
        buffer = new byte[1024];

        // reading count
        inputStream.read(buffer, 0, 1);
        int count = ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        buffer = new byte[1024];

        // reading payload
        inputStream.read(buffer, 0, 2);
        int payload = ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        buffer = new byte[1024];

        // reading timestamp
        inputStream.read(buffer, 0, 4);
        double timeStamp = ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        timeStamp = timeStamp / 10_000; // convert to seconds

        Log.d(TAG, "pid .." + pId + " payload is : " + payload);

        // reading payload data
        buffer = new byte[payload - 4];
        int read = inputStream.read(buffer, 0, buffer.length);
        Log.d(TAG, "reading count is ...." + read);
        // parsing payload data

        Packet packet =
            parsePayloadData(pId, timeStamp, Arrays.copyOfRange(buffer, 0, buffer.length - 4));
        if (packet instanceof QueueablePacket) {
          pushDataInQueue(packet);
        }
        if (packet instanceof PublishablePacket) {
          PubSubManager.getInstance()
              .publish(((PublishablePacket) packet).getTopic().toString(), packet);
        }

      } catch (IOException | InvalidDataException exception) {
        Thread.currentThread().interrupt();
      }
    }
    return null;
  }

  void initializeMapInstance() {

    if (decodedDataMap == null) {
      decodedDataMap = new HashMap<>();
    }
  }

  private static Packet parsePayloadData(int pId, double timeStamp, byte[] byteBuffer)
      throws InvalidDataException {

    for (PacketId packetId : PacketId.values()) {
      if (packetId.getNumVal() == pId) {
        Log.d(TAG, "Converting data for Explore");
        Packet packet = packetId.createInstance(timeStamp);
        if (packet != null) {
          packet.convertData(byteBuffer);
          Log.d(TAG, "Data decoded is " + packet.toString());
          return packet;
        }
      }
    }
    return null;
  }

  private static void pushDataInQueue(Packet packet) {

    if (packet instanceof QueueablePacket) {
      int channelCount = packet.getDataCount();
      ArrayList<Float> convertedSamples = packet.getData();
      List<String> attributes = packet.attributes;
      for (int index = 0; index < channelCount; index++) {
        synchronized (decodedDataMap) {
          String channelKey = attributes.get(index);
          if (decodedDataMap.get(channelKey) == null) {
            decodedDataMap.put(channelKey, new ConcurrentLinkedDeque<>());
          }

          ConcurrentLinkedDeque<Float> floats =
              (ConcurrentLinkedDeque) decodedDataMap.get(channelKey);
          floats.offerFirst(convertedSamples.get(index));
        }
      }
    }
  }

  public static Map<String, Queue<Float>> getDataMap() {
    return decodedDataMap;
  }
}
