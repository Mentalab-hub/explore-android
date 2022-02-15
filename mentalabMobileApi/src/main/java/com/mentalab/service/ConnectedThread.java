package com.mentalab.service;

import android.util.Log;
import com.mentalab.MentalabCodec;
import com.mentalab.PubSubManager;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.packets.Packet;
import com.mentalab.packets.PublishablePacket;
import com.mentalab.packets.QueueablePacket;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Callable;

import static com.mentalab.utils.Utils.TAG;

public class ConnectedThread implements Callable<Void> {
    private final InputStream mmInStream;

    public ConnectedThread(InputStream inputStream) {
        mmInStream = inputStream;
        initializeMapInstance();
    }


    public Void call() throws InterruptedException {

        int pId = 0;
        while (true) {
            try {
                byte[] buffer = new byte[1024];
                // reading PID
                mmInStream.read(buffer, 0, 1);
                pId = ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
                Log.d(TAG, "pid .." + pId);
                buffer = new byte[1024];

                // reading count
                mmInStream.read(buffer, 0, 1);
                int count = ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
                buffer = new byte[1024];

                // reading payload
                mmInStream.read(buffer, 0, 2);
                int payload = ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
                buffer = new byte[1024];

                // reading timestamp
                mmInStream.read(buffer, 0, 4);
                double timeStamp =
                        ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
                timeStamp = timeStamp / 10_000; // convert to seconds

                Log.d(TAG, "pid .." + pId + " payload is : " + payload);

                // reading payload data
                buffer = new byte[payload - 4];
                int read = mmInStream.read(buffer, 0, buffer.length);
                Log.d(TAG, "reading count is ...." + read);
                // parsing payload data

                Packet packet = MentalabCodec.parsePayloadData(pId, timeStamp, Arrays.copyOfRange(buffer, 0, buffer.length - 4));
                if (packet instanceof QueueablePacket) {
                    MentalabCodec.pushDataInQueue(packet);
                }
                if (packet instanceof PublishablePacket) {
                    PubSubManager.getInstance().publish(((PublishablePacket) packet).getTopic().toString(), packet);
                }

            } catch (IOException | InvalidDataException exception) {
                exception.printStackTrace();
            }
        }
    }

    void initializeMapInstance() {

        if (MentalabCodec.decodedDataMap == null) {
            MentalabCodec.decodedDataMap = new HashMap<>();
        }
    }
}
