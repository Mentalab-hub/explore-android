package com.mentalab.service;

import android.content.Context;
import android.net.Uri;

import com.mentalab.utils.constants.FileType;
import com.mentalab.utils.constants.Topic;

import java.util.Map;
import java.util.concurrent.Callable;

public class RecordTask implements Callable<Boolean> {

  private final Uri directory;
  private final String filename;
  private final Context context;

  private boolean overwrite;
  private boolean blocking;
  private FileType fileType;
  private int adcMask = 0;
  private float samplingRate = Integer.MAX_VALUE;
  private Double duration;
  private Map<Topic, Uri> generatedFiles;

  private RecordTask(Uri directory, String filename, Context context) {
    this.directory = directory; // todo: validate
    if (filename.contains(".")) {
      this.filename = filename.split("\\.")[0]; // todo: validate
    } else {
      this.filename = filename;
    }

    this.context = context;
  }

  public Context getContext() {
    return context;
  }

  public Uri getDirectory() {
    return directory;
  }

  public boolean getOverwrite() {
    return overwrite;
  }

  public String getFilename() {
    return filename;
  }

  @Override
  public Boolean call() {
    // ContentServer.getInstance().registerSubscriber(new RecordSubscriber(Topic.EXG, context, ));
    // ContentServer.getInstance().registerSubscriber(new RecordSubscriber(Topic.ORN, context, ));
    // ContentServer.getInstance().registerSubscriber(new RecordSubscriber(Topic.MARKER, context,
    // ));
    return true; // todo: check success
  }

  public void setGeneratedFiles(Map<Topic, Uri> generatedFies) {
    // this.generatedFies = generatedFies;
  }

  private void setAdcMask(int adcMask) { // Todo: Private until we have Adc mask functionality
    this.adcMask = adcMask;
  }

  public void setSamplingRate(float samplingRate) {
    this.samplingRate = samplingRate;
  }

  public static class Builder {

    private final Uri directory;
    private final String filename;
    private final Context context;

    private boolean overwrite = false;
    private boolean blocking = false;
    private FileType fileType = FileType.CSV;
    private Double duration = null;

    public Builder(Uri destination, String filename, Context context) {
      this.directory = destination;
      this.filename = filename;
      this.context = context;
    }

    private Builder setOverwrite(
        boolean overwrite) { // Todo: Private until we have delete functionality
      this.overwrite = overwrite;
      return this;
    }

    private Builder setBlocking(
        boolean blocking) { // Todo: Private until we have blocking functionality
      this.blocking = blocking;
      return this;
    }

    private Builder setFileType(
        FileType fileType) { // Todo: Private until we support other file types
      this.fileType = fileType;
      return this;
    }

    private Builder setDuration(
        double duration) { // Todo: Private until we have duration functionality
      this.duration = duration;
      return this;
    }

    public RecordTask build() {
      RecordTask subscriber = new RecordTask(directory, filename, context);
      subscriber.overwrite = this.overwrite;
      subscriber.blocking = this.blocking;
      subscriber.fileType = this.fileType;
      subscriber.duration = this.duration;

      return subscriber;
    }
  }
}
