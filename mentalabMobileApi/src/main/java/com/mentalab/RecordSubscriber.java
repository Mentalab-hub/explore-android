package com.mentalab;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.RequiresApi;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Set;

public class RecordSubscriber extends Thread {

    private final Uri directory;
    private final String filename;
    private final Context context;
    private boolean overwrite;
    private boolean blocking;
    private MentalabEnums.FileType fileType;
    private Double duration;
    private Set<UriTopicBean> generatedFies;


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
        PubSubManager.getInstance().subscribe(MentalabEnums.Topics.ExG.name(), this::writeExg);
        PubSubManager.getInstance().subscribe(MentalabEnums.Topics.Orn.name(), this::writeOrn);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void writeExg(Packet packet) {
        //todo: validate what's being written
        final int noChannels = packet.getDataCount();
        double timestamp = packet.getTimeStamp();

        UriTopicBean exgUriTopic = generatedFies.stream()
                .filter(b -> b.getTopic() == MentalabEnums.Topics.ExG)
                .findFirst()
                .orElse(null);
        if (exgUriTopic == null) {
            return;
        }
        Uri location = exgUriTopic.getUri();
        try (final BufferedWriter writer =
                     new BufferedWriter(
                             new OutputStreamWriter(context.getContentResolver()
                                     .openOutputStream(location, "wa")))) {
            writer.newLine();
            writePacketToCSV(writer, packet, timestamp, noChannels);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void writeOrn(Packet packet) {
        //todo: validate what's being written
        final int noChannels = 9;
        double timestamp = packet.getTimeStamp();

        UriTopicBean ornUriTopic = generatedFies.stream()
                .filter(b -> b.getTopic() == MentalabEnums.Topics.Orn)
                .findFirst()
                .orElse(null);
        if (ornUriTopic == null) {
            return;
        }
        Uri location = ornUriTopic.getUri();
        try (final BufferedWriter writer =
                     new BufferedWriter(
                             new OutputStreamWriter(context.getContentResolver().openOutputStream(location, "wa")))) {
            writer.newLine();
            writePacketToCSV(writer, packet, timestamp, noChannels);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void writeMarker(Packet packet) {
        //todo: validate what's being written
        final int noChannels = 9;
        double timestamp = packet.getTimeStamp();

        UriTopicBean markerUriTopic = generatedFies.stream()
                .filter(b -> b.getTopic() == MentalabEnums.Topics.Marker)
                .findFirst()
                .orElse(null);
        if (markerUriTopic == null) {
            return;
        }
        Uri location = markerUriTopic.getUri();
        try (final BufferedWriter writer =
                     new BufferedWriter(
                             new OutputStreamWriter(context.getContentResolver().openOutputStream(location, "wa")))) {
            writer.newLine();
            writePacketToCSV(writer, packet, timestamp, noChannels);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void writePacketToCSV(BufferedWriter writer, Packet packet, double timestamp, int noChannels) throws IOException {
        writer.write(String.valueOf(timestamp));
        writer.write(",");
        writer.write(packet.getData().get(0).toString());
        for (int i = 1; i < packet.getData().size(); i++) {
            writer.write(",");
            writer.write(packet.getData().get(i).toString());

            final int channelNo = i % noChannels + 1; // 1, 2, 3, 4,...
            if (channelNo == noChannels) { // break line after 4 or 8 entries
                writer.newLine();
                timestamp += 1 / getSamplingRate(packet);
                if ((packet.getData().size() - i) > 2) {
                    writer.write(String.valueOf(timestamp));
                }
            }
        }
    }


    private int getSamplingRate(Packet packet) {
        return Integer.MAX_VALUE;
    }

    public void setGeneratedFiles(Set<UriTopicBean> generatedFies) {
        this.generatedFies = generatedFies;
    }


    public static class Builder {
        private final Uri directory;
        private final String filename;
        private final Context context;

        private boolean overwrite = false;
        private boolean blocking = false;
        private MentalabEnums.FileType fileType = MentalabEnums.FileType.CSV;
        private Double duration = null;

        public Builder(Uri destination, String filename, Context context) {
            this.directory = destination;
            this.filename = filename;
            this.context = context;
        }


        public Builder setOverwrite(boolean overwrite) {
            this.overwrite = overwrite;
            return this;
        }

        public Builder setBlocking(boolean blocking) {
            this.blocking = blocking;
            return this;
        }

        public Builder setFileType(MentalabEnums.FileType fileType) {
            this.fileType = fileType;
            return this;
        }

        public Builder setDuration(double duration) {
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
