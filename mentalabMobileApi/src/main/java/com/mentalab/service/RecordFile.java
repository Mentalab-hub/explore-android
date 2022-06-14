package com.mentalab.service;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.IOException;

import static android.provider.MediaStore.MediaColumns.DISPLAY_NAME;
import static android.provider.MediaStore.MediaColumns.MIME_TYPE;

public class RecordFile {

  private final ContentValues metaData = new ContentValues();

  public RecordFile(String filename) {
    metaData.put(MIME_TYPE, "text/csv");
    metaData.put(DISPLAY_NAME, filename);
  }

  public Uri createFile(Uri d, Context ctx) throws IOException {
    final Uri file = ctx.getContentResolver().insert(d, metaData);
    if (file == null) {
      throw new IOException("File already exists, please choose another filename. File path: " + d);
    }
    return file;
  }
}
