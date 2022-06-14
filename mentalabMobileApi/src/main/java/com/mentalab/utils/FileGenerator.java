package com.mentalab.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.RequiresApi;
import com.mentalab.service.RecordFile;
import com.mentalab.utils.constants.Topic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FileGenerator {

  private static final String RESERVED_CHARS = "|\\?*<\":>+[]/'";

  private final Map<Topic, Uri> files = new HashMap<>();
  private final Uri directory;
  private final Context context;

  public FileGenerator(Context context) {
    this.context = context;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      this.directory = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
    } else {
      this.directory =
          Uri.fromFile(
              Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.Q)
  public Map<Topic, Uri> generateFiles(String filename) throws IOException {
    validateFilename(filename);

    final RecordFile exg = new RecordFile(filename + "_Exg");
    final Uri exgDir = exg.createFile(directory, context);
    files.put(Topic.EXG, exgDir);

    final RecordFile orn = new RecordFile(filename + "_Orn");
    final Uri ornDir = orn.createFile(directory, context);
    files.put(Topic.ORN, ornDir);

    final RecordFile marker = new RecordFile(filename + "_Marker");
    final Uri markerDir = marker.createFile(directory, context);
    files.put(Topic.MARKER, markerDir);

    return files;
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
}
