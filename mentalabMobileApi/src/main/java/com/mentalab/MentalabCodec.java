package com.mentalab;

import com.mentalab.commandtranslators.Command;
import com.mentalab.commandtranslators.CommandTranslator;
import com.mentalab.exception.InvalidCommandException;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.io.ContentServer;
import com.mentalab.packets.Packet;
import com.mentalab.packets.PacketId;
import com.mentalab.packets.PublishablePacket;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;


public final class MentalabCodec {

    private final static ExecutorService DECODE_EXECUTOR = Executors.newSingleThreadExecutor();
    private final static Semaphore DECODE_SEMAPHORE = new Semaphore(1);
    private final static ParseRawDataTask DECODER_TASK = new ParseRawDataTask();


    private MentalabCodec() { // Static class
    }


    /**
     * Tells the ExploreExecutor to decode the raw data.
     *
     * @param rawData InputStream of device bytes
     */
    public static void startDecode(InputStream rawData) {
        DECODER_TASK.setInputStream(rawData);
        if (DECODE_SEMAPHORE.tryAcquire()) {
            DECODE_EXECUTOR.submit(DECODER_TASK);
        }
    }


    /**
     * Encodes a command
     *
     * @return byte[] encoded commands that can be sent to the device
     * @throws InvalidCommandException when the command is not recognized
     */
    static byte[] encodeCommand(Command command) {
        final CommandTranslator translator = command.createCommandTranslator();
        return translator.translateCommand();
    }


    public static Packet parsePayloadData(int pId, double timeStamp, byte[] byteBuffer) throws InvalidDataException {
        for (PacketId packetId : PacketId.values()) {
            if (packetId.getNumVal() != pId) {
                continue;
            }

            final Packet packet = packetId.createInstance(timeStamp);
            if (packet != null) {
                packet.convertData(byteBuffer);
                return packet;
            }
        }
        return null;
    }


    public static void shutdown() {
        DECODE_EXECUTOR.shutdownNow();
        DECODE_SEMAPHORE.release();
    }


    private static class ParseRawDataTask implements Callable<Void> {

        private InputStream mmInStream;
        private byte[] buffer = new byte[1024];


        public void setInputStream(InputStream inputStream) {
            mmInStream = inputStream; // todo: do we need to stop call while this happens?
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
                final Packet packet = MentalabCodec.parsePayloadData(pId, timeStamp, Arrays.copyOfRange(buffer, 0, buffer.length - 4));
                if (packet != null) {
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
}
