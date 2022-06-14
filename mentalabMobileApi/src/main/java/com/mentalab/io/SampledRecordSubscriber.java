package com.mentalab.io;

import com.mentalab.packets.Packet;
import com.mentalab.utils.constants.Topic;

import java.io.BufferedWriter;
import java.io.IOException;

public class SampledRecordSubscriber extends RecordSubscriber {

  private final int sr;

  public SampledRecordSubscriber(Topic t, BufferedWriter w, int s) {
    super(t, w);
    this.sr = s;
  }

  @Override
  protected void writePacketToCSV(BufferedWriter writer, Packet packet) throws IOException {
    double currentTimestamp = packet.getTimeStamp();
    initNewLine(writer, currentTimestamp);
    for (int i = 0; i < packet.getData().size(); i++) {
      writeToLine(writer, packet, i);
      if (requireNewLine(packet, i)) {
        currentTimestamp += 1d / sr;
        initNewLine(writer, currentTimestamp);
      }
    }
  }
}
