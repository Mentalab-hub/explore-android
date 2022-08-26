package com.mentalab;

import android.util.Log;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.packets.Packet;
import com.mentalab.packets.PacketId;
import com.mentalab.packets.Publishable;
import com.mentalab.service.io.ContentServer;
import com.mentalab.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Callable;

class ParseRawDataTask implements Callable<Void> {

  private InputStream btInputStream;

  public void setInputStream(InputStream inputStream) {
    this.btInputStream = inputStream;
  }

  public Void call() {
    while (!Thread.currentThread().isInterrupted()) {
      try {
        final int pID = readToInt(btInputStream, 1); // package identification
        final int count = readToInt(btInputStream, 1); // package count
        final int length = readToInt(btInputStream, 2); // bytes = timestamp + payload + fletcher
        final double timeStamp = readToDouble(btInputStream, 4); // in ms * 10

        final Packet packet = createPacket(pID, length, timeStamp);

        if (packet instanceof Publishable) {
          ContentServer.getInstance().publish(((Publishable) packet).getTopic(), packet);
        }
      } catch (IOException e) {
        Log.e(Utils.TAG, "Error reading input stream. Exiting.", e);
        break;
      }
    }
    return null;
  }

  private Packet createPacket(int pID, int length, double timeStamp) throws IOException {
    byte[] buffer = readPayload(btInputStream, length - 4, length - 4); // already read timestamp
    byte[] noFletcherBuffer =
        Arrays.copyOfRange(buffer, 0, buffer.length - 4); // ignore 4 byte Fletcher
    return parsePayload(noFletcherBuffer, pID, timeStamp);
  }

  private static int readToInt(InputStream i, int noBytesToRead) throws IOException {
    final byte[] buffer = readPayload(i, noBytesToRead, 1024);
    return ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
  }

  private static double readToDouble(InputStream i, int noBytesToRead) throws IOException {
    final byte[] buffer = readPayload(i, noBytesToRead, 1024);
    return ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getDouble();
  }

  private static byte[] readPayload(InputStream i, int noBytesToRead, int initialBufferLength)
      throws IOException {
    final byte[] buffer = new byte[initialBufferLength];
    int read = i.read(buffer, 0, noBytesToRead); // read into buffer
    if (read < noBytesToRead) {
      Log.e(Utils.TAG, "Not all payload data read into buffer");
    }
    return buffer;
  }

  private static Packet parsePayload(byte[] bufferedData, int pId, double timeStamp)
      throws IOException {
    try {
      final PacketId id = getPacketId(pId);
      final Packet packet = id.createInstance(timeStamp / 10_000); // to seconds
      packet.populate(bufferedData);
      return packet;
    } catch (InvalidDataException e) {
      Log.e(Utils.TAG, "Error parsing payload: ", e);
      return null;
    }
  }

  private static PacketId getPacketId(int pId) throws InvalidDataException {
    for (PacketId p : PacketId.values()) {
      if (pId == p.getNumVal()) {
        return p;
      }
    }
    throw new InvalidDataException("Cannot identify packet type.");
  }
}
