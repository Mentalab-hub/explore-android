package com.mentalab;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.RequiresApi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;

import static android.provider.MediaStore.MediaColumns.DISPLAY_NAME;
import static android.provider.MediaStore.MediaColumns.MIME_TYPE;

public class FileGenerator {

    private final Context context;
    private final boolean overwrite;


    public FileGenerator(RecordSubscriber recordSubscriber) {
        this.context = recordSubscriber.getContext();
        this.overwrite = recordSubscriber.getOverwrite();
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public Set<UriTopicBean> generateFiles(Uri directory, String filename) throws IOException {
        //todo: handle filename, what if overwrite is true etc
        ContentValues metaDataExg = new ContentValues();
        metaDataExg.put(DISPLAY_NAME, filename + "_Exg");
        metaDataExg.put(MIME_TYPE, "text/csv");

        ContentValues metaDataOrn = new ContentValues();
        metaDataOrn.put(DISPLAY_NAME, filename + "_Orn");
        metaDataOrn.put(MIME_TYPE, "text/csv");

        ContentValues metaDataMarkers = new ContentValues();
        metaDataMarkers.put(DISPLAY_NAME, filename + "_Markers");
        metaDataMarkers.put(MIME_TYPE, "text/csv");

        ContentResolver resolver = context.getContentResolver();

        Set<UriTopicBean> createdUris = new HashSet<>();
        Uri exgFile;
        Uri ornFile;
        Uri markerFile;
        if (overwrite) { //todo: delete doesn't work
            deleteIfExists(directory, filename + "_Exg.csv");
            exgFile = resolver.insert(directory, metaDataExg);
            createdUris.add(new UriTopicBean(exgFile, MentalabEnums.Topics.ExG));

            deleteIfExists(directory, filename + "_Orn.csv");
            ornFile = resolver.insert(directory, metaDataOrn);
            createdUris.add(new UriTopicBean(ornFile, MentalabEnums.Topics.Orn));

            deleteIfExists(directory, filename + "_Markers.csv");
            markerFile = resolver.insert(directory, metaDataMarkers);
            createdUris.add(new UriTopicBean(markerFile, MentalabEnums.Topics.Marker));
        } else {
            exgFile = createNewFile(directory, filename, metaDataExg, MentalabEnums.Topics.ExG);
            createdUris.add(new UriTopicBean(exgFile, MentalabEnums.Topics.ExG));
            ornFile = createNewFile(directory, filename, metaDataOrn, MentalabEnums.Topics.Orn);
            createdUris.add(new UriTopicBean(ornFile, MentalabEnums.Topics.Orn));
            markerFile = createNewFile(directory, filename, metaDataMarkers, MentalabEnums.Topics.Marker);
            createdUris.add(new UriTopicBean(markerFile, MentalabEnums.Topics.Marker));
        }

        addExgHeader(exgFile);
        addOrnHeader(ornFile);
        addMarkerHeader(markerFile);
        return createdUris;
    }


    private void addMarkerHeader(Uri location) throws IOException {
        try (final BufferedWriter writer =
                     new BufferedWriter(
                             new OutputStreamWriter(context.getContentResolver()
                                     .openOutputStream(location, "wa")))) {
            writer.write("TimeStamp,Code");
        }
    }


    private void addOrnHeader(Uri location) throws IOException {
        try (final BufferedWriter writer =
                     new BufferedWriter(
                             new OutputStreamWriter(context.getContentResolver()
                                     .openOutputStream(location, "wa")))) {
            writer.write("TimeStamp,ax,ay,az,gx,gy,gz,mx,my,mz");
        }
    }


    private void addExgHeader(Uri location) throws IOException {
        try (final BufferedWriter writer =
                     new BufferedWriter(
                             new OutputStreamWriter(context.getContentResolver()
                                     .openOutputStream(location, "wa")))) {
            writer.write("TimeStamp,ch1,ch2,ch3,ch4,ch5,ch6,ch7,ch8");
        }
    }

    private Uri createNewFile(Uri directory, String filename, ContentValues metaData, MentalabEnums.Topics topic) {
        Uri location;
        int i = 1;
        while ((location = context.getContentResolver().insert(directory, metaData)) == null) {
            metaData.put(DISPLAY_NAME, filename + "(" + i + ")_" + topic.name());
            i++;
        }
        return location;
    }


    private void deleteIfExists(Uri directory, String filename) {
        final Uri fullPath = Uri.parse(directory.toString() + File.separator + filename);
        final boolean fileExists = checkIfUriExists(fullPath);
        if (fileExists) {
            context.getContentResolver().delete(fullPath, null, null);
        }
    }

    private boolean checkIfUriExists(Uri contentUri) {
        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(contentUri, null, null, null, null);
        if (cur != null) {
            if (cur.moveToFirst()) {
                String filePath = cur.getString(0);

                if (new File(filePath).exists()) {
                    cur.close();
                    return true;// do something if it exists
                } else {
                    cur.close();
                    return false;// File was not found
                }
            } else {
                cur.close();
                return false;// Uri was ok but no entry found.
            }
        } else {
            return false;// content Uri was invalid or some other error occurred
        }
    }
}
