package com.mentalab.packets.info;

import android.util.Log;

import static com.mentalab.packets.PacketDataType.OFFSET;
import static com.mentalab.packets.PacketDataType.SLOPE;

import com.mentalab.exception.InvalidDataException;
import com.mentalab.packets.Packet;
import com.mentalab.packets.PacketUtils;
import com.mentalab.utils.constants.Topic;
import java.util.EnumSet;

public class ImpedanceInfoPacketV1 extends ImpedanceInfoPacket {

  public ImpedanceInfoPacketV1(double timeStamp) {
    super(timeStamp);
  }

  public void populate(byte[] byteBuffer) throws InvalidDataException {
    super.populate(byteBuffer);
    this.offset = PacketUtils.bytesToInt(byteBuffer[2], byteBuffer[3]) * 0.001d;
    Log.i("IIPV1", "Offset is set to 0.001");
  }
}
