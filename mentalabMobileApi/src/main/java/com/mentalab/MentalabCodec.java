package com.mentalab;

import android.util.Log;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.packets.Packet;
import com.mentalab.packets.PacketId;
import com.mentalab.packets.Publishable;
import com.mentalab.service.io.ContentServer;
import com.mentalab.utils.Utils;
import com.mentalab.utils.commandtranslators.Command;
import com.mentalab.utils.commandtranslators.CommandTranslator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MentalabCodec {

  private static final ExecutorService DECODE_EXECUTOR = Executors.newSingleThreadExecutor();
  private static final ParseRawDataTask DECODER_TASK = new ParseRawDataTask();

  private MentalabCodec() { // Static class
  }

  /**
   * Tells the ExploreExecutor to decode the raw data.
   *
   * @param rawData InputStream of device bytes
   */
  public static void decodeInputStream(InputStream rawData) {
    DECODER_TASK.setInputStream(rawData);
    DECODE_EXECUTOR.submit(DECODER_TASK);
  }

  /**
   * Encodes a command
   *
   * @return byte[] encoded commands that can be sent to the device
   */
  static byte[] encodeCommand(Command command) {
    final CommandTranslator translator = command.createCommandTranslator();
    return translator.translateCommand();
  }

  public static void shutdown() {
    Thread.currentThread().interrupt();
    DECODE_EXECUTOR.shutdownNow();
  }

  private static class ParseRawDataTask implements Callable<Void> {

    private InputStream btInputStream;

    public void setInputStream(InputStream inputStream) {
      this.btInputStream = inputStream;
    }

    public Void call() throws IOException, InvalidDataException {
      while (!Thread.currentThread().isInterrupted()) {
        final int pID = readToInt(btInputStream, 1);
        final int count = readToInt(btInputStream, 1);
        final int length = readToInt(btInputStream, 2);
        final double timeStamp = readToDouble(btInputStream, 4);

        byte[] buffer = readPayload(btInputStream, length - 4); // ignore 4-byte unused Fletcher
        final Packet packet = parsePayload(buffer, pID, timeStamp);

        if (packet instanceof Publishable) {
          ContentServer.getInstance().publish(((Publishable) packet).getTopic(), packet);
        }
      }
      return null;
    }

    private static int readToInt(InputStream i, int l) throws IOException {
      final byte[] buffer = readPayload(i, l);
      return ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private static double readToDouble(InputStream i, int l) throws IOException {
      final byte[] buffer = readPayload(i, l);
      return ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getDouble();
    }

    private static byte[] readPayload(InputStream i, int length) throws IOException {
      final byte[] buffer = new byte[length];
      int read = i.read(buffer, 0, buffer.length); // read into buffer
      if (read < length) {
        Log.e(Utils.TAG, "Not all payload data read into buffer");
      }
      return buffer;
    }

    private static Packet parsePayload(byte[] bufferedData, int pId, double timeStamp)
        throws InvalidDataException {
      final PacketId id = getPacketId(pId);
      final Packet packet = id.createInstance(timeStamp / 10_000); // to seconds
      packet.populate(bufferedData);
      return packet;
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
}
