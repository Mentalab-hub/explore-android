package com.mentalab.io;

import android.content.Context;
import com.mentalab.packets.Packet;
import com.mentalab.utils.constants.Topic;
import java.io.BufferedWriter;
import java.io.IOException;

public class RecordSubscriber extends Subscriber {

  private final Context context;
  private final int noCols;


  public RecordSubscriber(Topic t, Context context, int noCols) {
    this.t = t;
    this.context = context;
    this.noCols = noCols;
  }

  public static void writePacketToCSV(BufferedWriter writer, Packet packet, double timestamp,
      int lineBreak) throws IOException {
    writer.write(String.valueOf(timestamp));
    writer.write(",");
    writer.write(packet.getData().get(0).toString());
    for (int i = 1; i < packet.getData().size(); i++) {
      writer.write(",");
      writer.write(packet.getData().get(i).toString());

      final int channelNo = i % lineBreak + 1; // 1, 2, 3, 4,...
      if (channelNo == lineBreak) { // break line after 2, 4 or 8 entries
        writer.newLine();
        //timestamp += 1 / samplingRate; commented to fix build error
        if ((packet.getData().size() - i) > 2) {
          writer.write(String.valueOf(timestamp));
        }
      }
    }
  }

  @Override
  public void accept(Packet packet) {
    //todo: validate what's being written
    final int noChannels = packet.getDataCount();
    double timestamp = packet.getTimeStamp();

    //final Uri location = generatedFiles.get(t);
//        try (final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(context.getContentResolver().openOutputStream(location, "wa")))) {
//            writePacketToCSV(writer, packet, timestamp, noChannels);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
  }
}
