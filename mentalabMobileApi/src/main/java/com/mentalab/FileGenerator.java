package com.mentalab;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.RequiresApi;
import com.mentalab.utils.MentalabConstants;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import static android.provider.MediaStore.MediaColumns.DISPLAY_NAME;
import static android.provider.MediaStore.MediaColumns.MIME_TYPE;

public class FileGenerator {

    private final Context context;
    private final boolean overwrite;


    public FileGenerator(Context context, boolean overwrite) {
        this.context = context;
        this.overwrite = overwrite;
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public Map<MentalabConstants.Topic, Uri> generateFiles(Uri directory, String filename) throws IOException {
        //todo: handle & validate filename
        final ContentValues metaDataExg = createMetaDataForFile(filename + "_Exg");
        final ContentValues metaDataOrn = createMetaDataForFile(filename + "_Orn");
        final ContentValues metaDataMarkers = createMetaDataForFile(filename + "_Markers");

        final Map<MentalabConstants.Topic, Uri> createdUris = new HashMap<>();
        if (!overwrite) {
            final Uri exgFile = createFile(directory, metaDataExg, MentalabConstants.Topic.ExG);
            addExgHeader(exgFile);
            createdUris.put(MentalabConstants.Topic.ExG, exgFile);

            final Uri ornFile = createFile(directory, metaDataOrn, MentalabConstants.Topic.Orn);
            addOrnHeader(ornFile);
            createdUris.put(MentalabConstants.Topic.Orn, ornFile);

            final Uri markerFile = createFile(directory, metaDataMarkers, MentalabConstants.Topic.Marker);
            addMarkerHeader(markerFile);
            createdUris.put(MentalabConstants.Topic.Marker, markerFile);
        } else {
            //Todo: include a delete function
        }

        return createdUris;
    }


    private ContentValues createMetaDataForFile(String filename) {
        final ContentValues metaData = new ContentValues();
        metaData.put(DISPLAY_NAME, filename);
        metaData.put(MIME_TYPE, "text/csv");
        return metaData;
    }


    private void addMarkerHeader(Uri location) throws IOException {
        try (final BufferedWriter writer =
                     new BufferedWriter(
                             new OutputStreamWriter(context.getContentResolver()
                                     .openOutputStream(location, "wa")))) {
            writer.write("TimeStamp,Code");
            writer.newLine();
        }
    }


    private void addOrnHeader(Uri location) throws IOException {
        try (final BufferedWriter writer =
                     new BufferedWriter(
                             new OutputStreamWriter(context.getContentResolver()
                                     .openOutputStream(location, "wa")))) {
            writer.write("TimeStamp,ax,ay,az,gx,gy,gz,mx,my,mz");
            writer.newLine();
        }
    }


    private void addExgHeader(Uri location) throws IOException {
        try (final BufferedWriter writer =
                     new BufferedWriter(
                             new OutputStreamWriter(context.getContentResolver()
                                     .openOutputStream(location, "wa")))) {
            writer.write("TimeStamp,ch1,ch2,ch3,ch4,ch5,ch6,ch7,ch8");
            writer.newLine();
        }
    }


    private Uri createFile(Uri directory, ContentValues metaData, MentalabConstants.Topic topic) {
        Uri location;
        int i = 1;
        while ((location = context.getContentResolver().insert(directory, metaData)) == null) {
            metaData.put(DISPLAY_NAME, metaData.get(DISPLAY_NAME) + "(" + i + ")_" + topic.name());
            i++;
        }
        return location;
    }
}
