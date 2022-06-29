package com.mentalab.service.record;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.RequiresApi;
import com.mentalab.ExploreDevice;
import com.mentalab.io.ContentServer;
import com.mentalab.io.RecordSubscriber;
import com.mentalab.io.SampledRecordSubscriber;
import com.mentalab.utils.FileGenerator;
import com.mentalab.utils.constants.SamplingRate;
import com.mentalab.utils.constants.Topic;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.Callable;

public class RecordTask implements Callable<Boolean>, AutoCloseable {

  private static final int ORN_SR = 20;

  private final Context cxt;
  private final String filename;
  private final SamplingRate sr;
  private final int count;

  private BufferedWriter eegWr;
  private BufferedWriter ornWr;
  private BufferedWriter markerWr;

  public RecordTask(Context c, String filename, ExploreDevice e) {
    this.cxt = c;
    this.filename = filename;
    this.sr = e.getSamplingRate();
    this.count = e.getChannelCount();
  }

  @RequiresApi(api = Build.VERSION_CODES.Q)
  @Override
  public Boolean call() throws IOException {
    final FileGenerator androidFileGenerator = new FileGenerator(cxt);

    final Uri exgFile = androidFileGenerator.generateFile(filename + "_Exg");
    final Uri ornFile = androidFileGenerator.generateFile(filename + "_Orn");
    final Uri markerFile = androidFileGenerator.generateFile(filename + "_Marker");

    recordEeg(exgFile);
    recordOrn(ornFile);
    recordMarker(markerFile);
    return true;
  }

  @RequiresApi(api = Build.VERSION_CODES.Q)
  private void recordEeg(Uri exgFile) throws IOException {
    this.eegWr =
        new BufferedWriter(
            new OutputStreamWriter(
                cxt.getContentResolver().openOutputStream(exgFile, "wa")));
    writeHeader(eegWr, buildEEGHeader(count));
    ContentServer.getInstance()
        .registerSubscriber(new SampledRecordSubscriber(Topic.EXG, eegWr, sr.getAsInt()));
  }

  @RequiresApi(api = Build.VERSION_CODES.Q)
  private void recordOrn(Uri ornFile) throws IOException {
    this.ornWr =
        new BufferedWriter(
            new OutputStreamWriter(
                cxt.getContentResolver().openOutputStream(ornFile, "wa")));
    writeHeader(ornWr, "TimeStamp,ax,ay,az,gx,gy,gz,mx,my,mz");
    ContentServer.getInstance()
        .registerSubscriber(new SampledRecordSubscriber(Topic.ORN, ornWr, ORN_SR));
  }

  @RequiresApi(api = Build.VERSION_CODES.Q)
  private void recordMarker(Uri markerFile) throws IOException {
    this.markerWr =
        new BufferedWriter(
            new OutputStreamWriter(
                cxt.getContentResolver().openOutputStream(markerFile, "wa")));
    writeHeader(markerWr, "TimeStamp,Code");
    ContentServer.getInstance().registerSubscriber(new RecordSubscriber(Topic.MARKER, markerWr));
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

  @Override
  public void close() throws IOException {
    eegWr.close();
    ornWr.close();
    markerWr.close();
  }
}
