package com.mentalab;

import com.mentalab.commandtranslators.Command;
import com.mentalab.commandtranslators.CommandTranslator;
import com.mentalab.exception.InvalidCommandException;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.packets.Packet;
import com.mentalab.packets.PacketId;
import com.mentalab.service.ExploreExecutor;
import com.mentalab.service.ParseRawDataTask;

import java.io.InputStream;


public class MentalabCodec {


    /**
     * Tells the ExploreExecutor to decode the raw data.
     *
     * @param rawData InputStream of device bytes
     */
    public static void startDecode(InputStream rawData) {
        ParseRawDataTask.setInputStream(rawData);
        ExploreExecutor.submitDecoderTask();
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


    static void stopDecoder() {
        ExploreExecutor.terminateDecoderTask();
    }
}
