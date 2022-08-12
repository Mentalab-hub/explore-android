package com.mentalab;

import android.util.Log;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.packets.command.CommandStatus;
import com.mentalab.packets.info.CalibrationInfo;
import com.mentalab.service.io.ContentServer;
import com.mentalab.packets.Packet;
import com.mentalab.packets.PacketId;
import com.mentalab.packets.Publishable;
import com.mentalab.utils.Utils;
import com.mentalab.utils.commandtranslators.Command;
import com.mentalab.utils.commandtranslators.CommandTranslator;
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

  private static Packet parsePayloadData(int pId, double timeStamp, byte[] byteBuffer)
      throws InvalidDataException {
    final PacketId p =
        Arrays.stream(PacketId.values())
            .filter(packetId -> packetId.getNumVal() == pId)
            .findFirst()
            .orElse(null);
    if (p == null) {
      return null;
    }
    final Packet packet = p.createInstance(timeStamp);
    if (packet != null) {
      packet.convertData(byteBuffer);
      return packet;
    }
    return null;
  }

  public static void shutdown() {
    DECODE_EXECUTOR.shutdownNow();
  }

  private static class ParseRawDataTask implements Callable<Void> {

    private InputStream btInputStream;
    private byte[] buffer;

    public void setInputStream(InputStream inputStream) {
      btInputStream = inputStream;
    }

    public Void call() throws IOException, InvalidDataException {
      while (!Thread.currentThread().isInterrupted()) {
        buffer = new byte[1024];
        final int pID = readStreamToInt(1);
        Log.d("IMPEDANCE", "Getting here PID: " + pID);
        if (pID == 195){
          Log.d("", "");
        };
        readStreamToInt(1); // count, ignore
        final int payload = readStreamToInt(2);
        double timeStamp = readStreamToInt(4);
        timeStamp = timeStamp / 10_000; // convert to seconds

        // read payload data
        buffer = new byte[payload - 4];
        btInputStream.read(buffer, 0, buffer.length); // read into buffer

        // parsing payload data
        final Packet packet =
            parsePayloadData(pID, timeStamp, Arrays.copyOfRange(buffer, 0, buffer.length - 4));

        if (packet instanceof Publishable) {
          ContentServer.getInstance().publish(((Publishable) packet).getTopic(), packet);
          Log.d("TEST_", "getting packets!!!");
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
