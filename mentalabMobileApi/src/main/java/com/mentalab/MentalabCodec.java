package com.mentalab;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import com.mentalab.commandtranslators.Command;
import com.mentalab.commandtranslators.CommandTranslator;
import com.mentalab.exception.InvalidCommandException;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.packets.Packet;
import com.mentalab.packets.PacketId;
import com.mentalab.packets.QueueablePacket;
import com.mentalab.service.ParseRawDataTask;
import com.mentalab.service.ExecutorServiceManager;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mentalab.utils.Utils.TAG;


public class MentalabCodec {

    public static Map<String, Queue<Float>> decodedDataMap = null;
    private static Future<?> decoderTask = null;


    /**
     * Decodes a device raw data stream
     *
     * <p>Incoming bytes from Bluetooth are converted to an immutable Map of Double Ended Queue of
     * Float numbers. ExG channels are saved as single precision floating point numbers (Float) in the
     * unit of mVolt. Launches one worker thread on first invocation. Currently the it provides the
     * following data queues from Explore device: Channel_1, Channel_2...Channel_N where N is the maximum
     * available numbers of channel of the device. Acc_X, Acc_Y, Acc_Z in the units of mg/LSB. Gyro_X,
     * Gyro_Y and Gyro_Z in mdps/LSB. MAG_X, Mag_Y, Mag_Z in mgauss/LSB. To get a specific instance of
     * the queue:
     * <pre>{@code
     * Map<String, Queue<Float>> map = MentalabCodec.decode(stream);
     * Queue<Float> accXMap = map.get("Acc_X").poll();
     * Queue<Float> channel2 = map.get("Channel2").poll();
     * }<pre>
     *
     * @throws InvalidDataException throws when invalid data is received
     * @stream InputStream of device bytes
     * @return Immutable Map of Queues of Numbers
     */
    public static Future<Void> decode(InputStream rawData) throws InvalidDataException {
        ParseRawDataTask.setInputStream(rawData);
        return ExecutorServiceManager.submitDecoderTask(ParseRawDataTask.getInstance());
    }


    /**
     * Encodes a command
     *
     * @return byte[] encoded commands that can be sent to the device
     * @throws InvalidCommandException when the command is not recognized
     */
    static byte[] encodeCommand(Command command) {
        CommandTranslator translator = command.createCommandTranslator();
        return translator.translateCommand();
    }


    static byte[] encodeCommand(Command command, int arg) {
        return encodeCommand(command.setArg(arg));
    }


    public static Packet parsePayloadData(int pId, double timeStamp, byte[] byteBuffer) throws InvalidDataException {
        for (PacketId packetId : PacketId.values()) {
            if (packetId.getNumVal() != pId) {
                continue;
            }

            Packet packet = packetId.createInstance(timeStamp);
            if (packet != null) {
                packet.convertData(byteBuffer);
                return packet;
            }
        }
        return null;
    }


    public static void pushDataInQueue(Packet packet) {
        if (packet instanceof QueueablePacket) {
            int channelCount = packet.getDataCount();
            ArrayList<Float> convertedSamples = packet.getData();
            List<String> attributes = packet.attributes;
            for (int index = 0; index < channelCount; index++) {
                synchronized (decodedDataMap) {
                    String channelKey = attributes.get(index);
                    if (decodedDataMap.get(channelKey) == null) {
                        decodedDataMap.put(channelKey, new ConcurrentLinkedDeque<>());
                    }

                    ConcurrentLinkedDeque<Float> floats =
                            (ConcurrentLinkedDeque) decodedDataMap.get(channelKey);
                    floats.offerFirst(convertedSamples.get(index));
                }
            }
        }
    }


    public static int getAdsMask() {
        return Objects.requireNonNull(decodedDataMap.get("Ads_Mask").poll()).intValue();
    }


    public static float getSamplingRate() {
        return Objects.requireNonNull(decodedDataMap.get("Sampling_Rate").poll());
    }


    public static void pushToLsl(BluetoothDevice device) {
        ExecutorServiceManager.submitTask(new LslPacketSubscriber(device));
    }


    static void stopDecoder() {
        decoderTask.cancel(true);
    }


}
