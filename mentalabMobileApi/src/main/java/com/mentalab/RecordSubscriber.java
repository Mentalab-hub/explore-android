package com.mentalab;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.RequiresApi;
import com.mentalab.packets.Packet;
import com.mentalab.utils.MentalabConstants;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

public class RecordSubscriber extends Thread {

    private final static int NO_MARKER_COLS = 2;
    private final static int NO_ORN_COLS = 9;

    private final Uri directory;
    private final String filename;
    private final Context context;

    private boolean overwrite;
    private boolean blocking;
    private MentalabConstants.FileType fileType;
    private int adcMask = 0;
    private float samplingRate = Integer.MAX_VALUE;
    private Double duration;
    private Map<MentalabConstants.Topic, Uri> generatedFies;


    private RecordSubscriber(Uri directory, String filename, Context context) {
        this.directory = directory; //todo: validate
        if (filename.contains(".")) {
            this.filename = filename.split("\\.")[0]; //todo: validate
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
    public void run() {
        PubSubManager.getInstance().subscribe(MentalabConstants.Topic.ExG.name(), this::writeExg);
        PubSubManager.getInstance().subscribe(MentalabConstants.Topic.Orn.name(), this::writeOrn);
        PubSubManager.getInstance().subscribe(MentalabConstants.Topic.Marker.name(), this::writeMarker);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void writeExg(Packet packet) {
        //todo: validate what's being written
        final int noChannels = packet.getDataCount();
        double timestamp = packet.getTimeStamp();

        final Uri location = generatedFies.get(MentalabConstants.Topic.ExG);
        try (final BufferedWriter writer =
                     new BufferedWriter(
                             new OutputStreamWriter(context.getContentResolver()
                                     .openOutputStream(location, "wa")))) {
            writePacketToCSV(writer, packet, timestamp, noChannels);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void writeOrn(Packet packet) {
        //todo: validate what's being written
        double timestamp = packet.getTimeStamp();

        final Uri location = generatedFies.get(MentalabConstants.Topic.Orn);
        try (final BufferedWriter writer =
                     new BufferedWriter(
                             new OutputStreamWriter(context.getContentResolver().openOutputStream(location, "wa")))) {
            writePacketToCSV(writer, packet, timestamp, NO_ORN_COLS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void writeMarker(Packet packet) {
        //todo: validate what's being written
        double timestamp = packet.getTimeStamp();

        final Uri location = generatedFies.get(MentalabConstants.Topic.Marker);
        try (final BufferedWriter writer =
                     new BufferedWriter(
                             new OutputStreamWriter(context.getContentResolver().openOutputStream(location, "wa")))) {
            writePacketToCSV(writer, packet, timestamp, NO_MARKER_COLS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void writePacketToCSV(BufferedWriter writer, Packet packet, double timestamp, int lineBreak) throws IOException {
        writer.write(String.valueOf(timestamp));
        writer.write(",");
        writer.write(packet.getData().get(0).toString());
        for (int i = 1; i < packet.getData().size(); i++) {
            writer.write(",");
            writer.write(packet.getData().get(i).toString());

            final int channelNo = i % lineBreak + 1; // 1, 2, 3, 4,...
            if (channelNo == lineBreak) { // break line after 2, 4 or 8 entries
                writer.newLine();
                timestamp += 1 / samplingRate;
                if ((packet.getData().size() - i) > 2) {
                    writer.write(String.valueOf(timestamp));
                }
            }
        }
    }


    public void setGeneratedFiles(Map<MentalabConstants.Topic, Uri> generatedFies) {
        this.generatedFies = generatedFies;
    }


    private void setAdcMask(int adcMask) {  // Todo: Private until we have Adc mask functionality
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
        private MentalabConstants.FileType fileType = MentalabConstants.FileType.CSV;
        private Double duration = null;

        public Builder(Uri destination, String filename, Context context) {
            this.directory = destination;
            this.filename = filename;
            this.context = context;
        }


        private Builder setOverwrite(boolean overwrite) { // Todo: Private until we have delete functionality
            this.overwrite = overwrite;
            return this;
        }

        private Builder setBlocking(boolean blocking) { // Todo: Private until we have blocking functionality
            this.blocking = blocking;
            return this;
        }

        private Builder setFileType(MentalabConstants.FileType fileType) { // Todo: Private until we support other file types
            this.fileType = fileType;
            return this;
        }

        private Builder setDuration(double duration) { // Todo: Private until we have duration functionality
            this.duration = duration;
            return this;
        }


        public RecordSubscriber build() {
            RecordSubscriber subscriber = new RecordSubscriber(directory, filename, context);
            subscriber.overwrite = this.overwrite;
            subscriber.blocking = this.blocking;
            subscriber.fileType = this.fileType;
            subscriber.duration = this.duration;

            return subscriber;
        }
    }
}
