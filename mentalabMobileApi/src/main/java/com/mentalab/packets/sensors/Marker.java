package com.mentalab.packets.sensors;

import androidx.annotation.NonNull;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.packets.Attributes;
import com.mentalab.packets.PublishablePacket;
import com.mentalab.utils.Utils;
import com.mentalab.utils.constants.Topic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;

public class Marker extends PublishablePacket {

  public Marker(double timeStamp) {
    super(timeStamp);
    super.attributes = EnumSet.of(Attributes.MARKER);
  }

  @Override
  public void convertData(byte[] byteBuffer) throws InvalidDataException {
    int markerCode = Utils.bitsToShort(byteBuffer[0]);
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
