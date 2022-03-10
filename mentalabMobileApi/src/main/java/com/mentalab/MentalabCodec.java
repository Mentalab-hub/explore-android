package com.mentalab;

import android.util.Log;

import com.mentalab.commandtranslators.Command;
import com.mentalab.commandtranslators.CommandTranslator;
import com.mentalab.exception.InvalidCommandException;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.exception.NoConnectionException;
import com.mentalab.io.ContentServer;
import com.mentalab.packets.Packet;
import com.mentalab.packets.PacketId;
import com.mentalab.packets.Publishable;
import com.mentalab.packets.info.Device;
import com.mentalab.packets.sensors.exg.EEGPacket;
import com.mentalab.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MentalabCodec {

    private static final ExecutorService DECODE_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final ParseRawDataTask DECODER_TASK = new ParseRawDataTask();
    private static ExploreDevice exploreDevice;

    private MentalabCodec() { // Static class
    }

    /**
     * Tells the ExploreExecutor to decode the raw data.
     *
     * @param rawData InputStream of device bytes
     */
    public static void startDecode(InputStream rawData, ExploreDevice device) {
        DECODER_TASK.setInputStream(rawData);
        exploreDevice = device;
        DECODE_EXECUTOR.submit(DECODER_TASK);
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

    private static Packet parsePayloadData(int pId, double timeStamp, byte[] byteBuffer)
        throws InvalidDataException {
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
    }

    private static class ParseRawDataTask implements Callable<Void> {

        private InputStream btInputStream;

        public void setInputStream(InputStream inputStream) {
            btInputStream = inputStream; // todo: do we need to stop call while this happens?
        }

        public Void call()
            throws IOException, InvalidDataException, NoBluetoothException, NoConnectionException {
            InputStream btInputStream = MentalabCommands.getRawData();
            while (!Thread.currentThread().isInterrupted()) { // otherwise end of stream
                byte[] buffer = new byte[1024];
                // reading PID
                btInputStream.read(buffer, 0, 1);
                int pId = ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
                Log.d(Utils.TAG, "pid .." + pId);
                buffer = new byte[1024];

                // reading count
                btInputStream.read(buffer, 0, 1);
                int count = ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
                buffer = new byte[1024];

                // reading payload
                btInputStream.read(buffer, 0, 2);
                int payload = ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
                buffer = new byte[1024];

                // reading timestamp
                btInputStream.read(buffer, 0, 4);
                double timeStamp = ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();

                // reading payload data
                buffer = new byte[payload - 4];
                int read = btInputStream.read(buffer, 0, buffer.length);

                // parsing payload data
                final Packet packet =
                    parsePayloadData(pId, timeStamp, Arrays.copyOfRange(buffer, 0, buffer.length - 4));
                Log.d(Utils.TAG, "Packet received: " + packet.toString());
                if (packet instanceof Device) {
                    exploreDevice.setActiveSamplingRate(((Device) packet).getSamplingRate());
                    exploreDevice.setCurrentChannelMask(((Device) packet).getChannelMask());
                } else if (packet instanceof Publishable) {

                    ContentServer.getInstance().publish(((Publishable) packet).getTopic(), packet);
                }
                // sets max channel numbers, will be changed when channel number attribute is added in
                // device info from FW
                if (packet instanceof EEGPacket && exploreDevice.getNoChannels() == 0) {
                    exploreDevice.setNumberOfChannels(((EEGPacket) packet).getDataCount());
                }
            }
            return null;
        }
    }
}
