package com.mentalab.service;

import com.mentalab.MentalabCodec;
import com.mentalab.io.ContentServer;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.packets.Packet;
import com.mentalab.packets.PublishablePacket;
import com.mentalab.packets.QueueablePacket;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class ParseRawDataTask implements Callable<Void> {

    private static ParseRawDataTask INSTANCE;

    private ParseRawDataTask() {}

    public static ParseRawDataTask getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ParseRawDataTask();
        }
        return INSTANCE;
    }

    private static InputStream mmInStream;
    private byte[] buffer = new byte[1024];


    public static void setInputStream(InputStream inputStream) {
        mmInStream = inputStream;
    }


    public Void call() throws IOException, InvalidDataException {
        int pId;
        while ((pId = readStreamToInt(1)) != -1) { // otherwise end of stream
            int count = readStreamToInt(1);
            int payload = readStreamToInt(2);
            double timeStamp = readStreamToInt(4);
            timeStamp = timeStamp / 10_000; // convert to seconds

            // read payload data
            buffer = new byte[payload - 4];
            mmInStream.read(buffer, 0, buffer.length); // read into buffer

            // parsing payload data
            Packet packet = MentalabCodec.parsePayloadData(pId, timeStamp, Arrays.copyOfRange(buffer, 0, buffer.length - 4));
            if (packet instanceof QueueablePacket) {
                MentalabCodec.pushDataInQueue(packet);
            }
            if (packet instanceof PublishablePacket) {
                ContentServer.getInstance().publish(((PublishablePacket) packet).getTopic(), packet);
            }
        }
        return null;
    }


    private int readStreamToInt(int length) throws IOException {
        mmInStream.read(buffer, 0, length); // read into buffer
        int value = ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        buffer = new byte[1024]; // reset buffer for next read
        return value;
    }
}
