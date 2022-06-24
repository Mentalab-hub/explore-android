package com.mentalab.io;

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
  protected void writeNewLine(int channelCount, int i) throws IOException {
    if (requireNewLine(channelCount, i)) {
      currentTimestamp += 1d / sr;
      initNewLine(currentTimestamp);
    }
  }
}
