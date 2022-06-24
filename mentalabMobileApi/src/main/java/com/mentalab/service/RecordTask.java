package com.mentalab.service;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.RequiresApi;
import com.mentalab.ExploreDevice;
import com.mentalab.io.ContentServer;
import com.mentalab.io.RecordSubscriber;
import com.mentalab.io.SampledRecordSubscriber;
import com.mentalab.utils.constants.SamplingRate;
import com.mentalab.utils.constants.Topic;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.concurrent.Callable;

public class RecordTask implements Callable<Boolean> {

  private static final int ORN_SR = 20;

  private final Context cxt;
  private final Map<Topic, Uri> files;
  private final SamplingRate sr;
  private final int count;

  public RecordTask(Context c, Map<Topic, Uri> f, ExploreDevice e) {
    this.cxt = c;
    this.files = f;
    this.sr = e.getSamplingRate();
    this.count = e.getChannelCount();
  }

  @RequiresApi(api = Build.VERSION_CODES.Q)
  @Override
  public Boolean call() throws IOException {
//    final Uri exgUri = files.get(Topic.EXG);
//    recordEeg(exgUri);
//
//    final Uri ornUri = files.get(Topic.ORN);
//    recordOrn(ornUri);

    final Uri markerUri = files.get(Topic.MARKER);
    recordMarker(markerUri);
    return true;
  }

  @RequiresApi(api = Build.VERSION_CODES.Q)
  private void recordEeg(Uri exgUri) throws IOException {
    final BufferedWriter writer = // todo: close gracefully
        new BufferedWriter(
            new OutputStreamWriter(cxt.getContentResolver().openOutputStream(exgUri, "wa")));
    writeHeader(writer, buildEEGHeader(count));
    ContentServer.getInstance()
        .registerSubscriber(new SampledRecordSubscriber(Topic.EXG, writer, sr.getAsInt()));
  }

  @RequiresApi(api = Build.VERSION_CODES.Q)
  private void recordOrn(Uri ornUri) throws IOException {
    final BufferedWriter writer = // todo: close gracefully
        new BufferedWriter(
            new OutputStreamWriter(cxt.getContentResolver().openOutputStream(ornUri, "wa")));
    writeHeader(writer, "TimeStamp,ax,ay,az,gx,gy,gz,mx,my,mz");
    ContentServer.getInstance().registerSubscriber(new SampledRecordSubscriber(Topic.ORN, writer, ORN_SR));
  }

  @RequiresApi(api = Build.VERSION_CODES.Q)
  private void recordMarker(Uri markerUri) throws IOException {
    final BufferedWriter writer = // todo: close gracefully
            new BufferedWriter(
                    new OutputStreamWriter(cxt.getContentResolver().openOutputStream(markerUri, "wa")));
    writeHeader(writer, "TimeStamp,Code");
    ContentServer.getInstance().registerSubscriber(new RecordSubscriber(Topic.MARKER, writer));
  }

  @RequiresApi(api = Build.VERSION_CODES.Q)
  private static void writeHeader(BufferedWriter wr, String header) throws IOException {
    wr.write(header);
    wr.flush();
  }

  private static String buildEEGHeader(int channelCount) {
    final StringBuilder headerBuilder = new StringBuilder("TimeStamp,ch1,ch2,ch3,ch4");
    for (int i = 5; i <= channelCount; i++) {
      headerBuilder.append(",").append("ch").append(i);
    }
    return headerBuilder.toString();
  }
}
