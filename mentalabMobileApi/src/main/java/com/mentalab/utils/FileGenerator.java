package com.mentalab.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import androidx.annotation.RequiresApi;
import com.mentalab.service.RecordFile;
import com.mentalab.utils.constants.Topic;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FileGenerator {

  private static final String RESERVED_CHARS = "|\\?*<\":>+[]/'";

  private final Map<Topic, Uri> createdUris = new HashMap<>();
  private final File directory;
  private final Context context;

  public FileGenerator(Context context) {
    this.context = context;
    this.directory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
  }

  @RequiresApi(api = Build.VERSION_CODES.Q)
  public Map<Topic, Uri> generateFiles(String filename, int channelCount) throws IOException {
    validateFilename(filename);

    final RecordFile exg = new RecordFile(filename + "_Exg", getEEGHeader(channelCount));
    final Uri exgFile = exg.initFile(directory, context);
    createdUris.put(Topic.EXG, exgFile);

    final RecordFile orn = new RecordFile(filename + "_Orn", "TimeStamp,ax,ay,az,gx,gy,gz,mx,my,mz");
    final Uri ornFile = orn.initFile(directory, context);
    createdUris.put(Topic.ORN, ornFile);

    final RecordFile marker = new RecordFile(filename + "_Marker", "TimeStamp,Code");
    final Uri markerFile = marker.initFile(directory, context);
    createdUris.put(Topic.MARKER, markerFile);

    return createdUris;
  }

  private static void validateFilename(String filename) throws IOException {
    if (filename.length() < 1) {
      throw new IOException("Filename is empty.");
    }
    checkValidChars(filename);
  }

  private static void checkValidChars(String filename) throws IOException {
    for (int i = 0; i < filename.length(); i++) {
      char c = filename.charAt(i);
      checkChar(c);
    }
  }

  private static void checkChar(char c) throws IOException {
    if (RESERVED_CHARS.indexOf(c) > -1) {
      throw new IOException("Invalid filename, contains character: " + c);
    }
  }

  private static String getEEGHeader(int channelCount) {
    return buildEEGHeader(channelCount).toString();
  }

  private static StringBuilder buildEEGHeader(int channelCount) {
    final StringBuilder headerBuilder = new StringBuilder("TimeStamp,ch1,ch2,ch3,ch4");
    for (int i = 5; i < channelCount; i++) {
      headerBuilder.append(",").append("ch").append(i);
    }
    return headerBuilder;
  }
}
