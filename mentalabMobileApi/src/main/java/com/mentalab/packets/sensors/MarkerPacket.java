package com.mentalab.packets.sensors;

import androidx.annotation.NonNull;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.packets.Packet;
import com.mentalab.packets.PacketDataType;
import com.mentalab.packets.PacketUtils;
import com.mentalab.packets.Publishable;
import com.mentalab.utils.constants.Topic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;

public class MarkerPacket extends Packet implements Publishable {

  public MarkerPacket(double timeStamp) {
    super(timeStamp);
    super.type = EnumSet.of(PacketDataType.MARKER);
  }

  @Override
  public void convertData(byte[] byteBuffer) throws InvalidDataException {
    int markerCode = PacketUtils.bytesToShort(byteBuffer[0]);
    super.data = new ArrayList<>(Collections.singletonList((float) markerCode));
  }

  @NonNull
  @Override
  public String toString() {
    return "PACKET: Marker";
  }

  @Override
  public int getDataCount() {
    return 1;
  }

  public Topic getTopic() {
    return Topic.MARKER;
  }
}
