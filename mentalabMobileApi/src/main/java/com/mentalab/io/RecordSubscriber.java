package com.mentalab.io;

import android.util.Log;
import com.mentalab.packets.Packet;
import com.mentalab.utils.Utils;
import com.mentalab.utils.constants.SamplingRate;
import com.mentalab.utils.constants.Topic;

import java.io.BufferedWriter;
import java.io.IOException;

public class RecordSubscriber extends Subscriber {

  private final BufferedWriter wr;
  private final SamplingRate sr;

  public RecordSubscriber(Topic t, BufferedWriter w, SamplingRate s) {
    super(t);
    this.wr = w;
    this.sr = s;
  }

  @Override
  public void accept(Packet p) {
    try {
      writePacketToCSV(wr, p);
    } catch (IOException e) {
      Log.e(Utils.TAG, "Unable to write lines in CSV");
    }
  }

  private void writePacketToCSV(BufferedWriter writer, Packet packet) throws IOException {
    initialiseFirstLine(writer, packet);
    writePacketContents(writer, packet);
  }

  private static void initialiseFirstLine(BufferedWriter writer, Packet packet) throws IOException {
    writer.write(String.valueOf(packet.getTimeStamp()));
    writer.write(",");
    writer.write(packet.getData().get(0).toString());
  }

  private void writePacketContents(BufferedWriter writer, Packet packet) throws IOException {
    for (int i = 1; i < packet.getData().size(); i++) {
      writer.write(",");
      writer.write(packet.getData().get(i).toString());

      final int channelCount = packet.getDataCount();
      if (newLine(channelCount, i)) {
        int ts =  (int) (packet.getTimeStamp() + 1f / sr.getValue());
        writeNewLine(writer, channelCount, ts, i);
      }
    }
  }

  private static boolean newLine(int channelCount, int i) {
    final int channelNo = i % channelCount + 1; // 1, 2, 3, 4,...
    return channelNo == channelCount; // break line after 2, 4 or 8 entries
  }

  private void writeNewLine(BufferedWriter writer, int channelCount, int i, double ts) throws IOException {
    writer.newLine();
    if ((channelCount - i) > 2) {
      writer.write(String.valueOf(ts));
    }
  }
}
