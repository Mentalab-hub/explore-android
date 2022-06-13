package com.mentalab.service;

import android.content.Context;
import android.net.Uri;
import com.mentalab.ExploreDevice;
import com.mentalab.io.ContentServer;
import com.mentalab.io.RecordSubscriber;
import com.mentalab.utils.constants.SamplingRate;
import com.mentalab.utils.constants.Topic;

import java.io.*;
import java.util.Map;
import java.util.concurrent.Callable;

public class RecordTask implements Callable<Boolean> {

  private final Map<Topic, Uri> generatedFiles;
  private final Context c;
  private final SamplingRate sr;

  public RecordTask(Map<Topic, Uri> generatedFiles, Context ctx, SamplingRate s) {
    this.generatedFiles = generatedFiles;
    this.c = ctx;
    this.sr = s;
  }

  @Override
  public Boolean call() throws IOException {
    record(Topic.EXG);
    record(Topic.ORN);
    record(Topic.MARKER);
    return true;
  }

  private void record(Topic t) throws IOException {
    try (final BufferedWriter writer = getWriter(t)) {
      ContentServer.getInstance().registerSubscriber(new RecordSubscriber(t, writer, sr));
    }
  }

  private BufferedWriter getWriter(Topic t) throws FileNotFoundException {
    final OutputStream out = c.getContentResolver().openOutputStream(generatedFiles.get(t), "wa");
    return new BufferedWriter(new OutputStreamWriter(out));
  }
}
