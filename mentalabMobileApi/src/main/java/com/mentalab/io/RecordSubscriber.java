package com.mentalab.io;

import android.util.Log;
import com.mentalab.packets.Packet;
import com.mentalab.utils.Utils;
import com.mentalab.utils.constants.Topic;

import java.io.BufferedWriter;
import java.io.IOException;

public class RecordSubscriber extends Subscriber {

  protected final BufferedWriter wr;

  public RecordSubscriber(Topic t, BufferedWriter w) {
    super(t);
    this.wr = w;
  }

  @Override
  public void accept(Packet packet) {
    try {
      writePacketToCSV(wr, packet);
    } catch (IOException e) {
      Log.e(Utils.TAG, e.getMessage());
    }
  }

  protected void writePacketToCSV(BufferedWriter writer, Packet packet) throws IOException {
    double currentTimestamp = packet.getTimeStamp();
    initNewLine(writer, currentTimestamp);
    for (int i = 0; i < packet.getData().size(); i++) {
      writeToLine(writer, packet, i);
      if (requireNewLine(packet, i)) {
        initNewLine(writer, currentTimestamp);
      }
    }
  }

  protected void writeToLine(BufferedWriter writer, Packet packet, int i) throws IOException {
    writer.write(",");
    writer.write(packet.getData().get(i).toString());
  }

  protected static boolean requireNewLine(Packet p, int i) {
    if (i == p.getData().size() - 1) {
      return false;
    }
    final int channelCount = p.getDataCount();
    final int channelNo = i % channelCount + 1; // 1, 2, 3, 4,...
    return channelNo == channelCount; // break line after 2, 4 or 8 entries
  }

  protected static void initNewLine(BufferedWriter writer, double ts) throws IOException {
    writer.newLine();
    writer.write(Utils.round(ts));
    writer.flush();
  }
}
