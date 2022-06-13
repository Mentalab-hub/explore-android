package com.mentalab.service;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.RequiresApi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static android.provider.MediaStore.MediaColumns.DISPLAY_NAME;
import static android.provider.MediaStore.MediaColumns.MIME_TYPE;

public class RecordFile {

  ContentValues metaData;
  String header;

  public RecordFile(String filename, String header) {
    metaData.put(MIME_TYPE, "text/csv");
    metaData.put(DISPLAY_NAME, filename);
    this.header = header;
  }

  @RequiresApi(api = Build.VERSION_CODES.Q)
  public Uri initFile(File directory, Context ctx) throws IOException {
    final Uri exgFile = createFile(directory, ctx);
    writeHeader(ctx, exgFile);
    return exgFile;
  }

  private Uri createFile(File d, Context ctx) throws IOException {
    final Uri file = ctx.getContentResolver().insert(Uri.fromFile(d), metaData);
    if (file == null) {
      throw new IOException("File already exists, please choose another filename. File path: " + d);
    }
    return file;
  }

  @RequiresApi(api = Build.VERSION_CODES.Q)
  private void writeHeader(Context cxt, Uri to) throws IOException {
    try (final BufferedWriter writer =
        new BufferedWriter(
            new OutputStreamWriter(cxt.getContentResolver().openOutputStream(to, "wa")))) {
      writer.write(this.header);
      writer.newLine();
    }
  }
}
