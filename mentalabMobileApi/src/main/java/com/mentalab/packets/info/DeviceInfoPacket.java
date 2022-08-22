package com.mentalab.packets.info;

import androidx.annotation.NonNull;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.packets.Packet;
import com.mentalab.packets.PacketUtils;
import com.mentalab.packets.Publishable;
import com.mentalab.utils.constants.SamplingRate;
import com.mentalab.utils.constants.Topic;

import java.util.EnumSet;

import static com.mentalab.packets.PacketDataType.ADS_MASK;
import static com.mentalab.packets.PacketDataType.SR;

/** Device related information packet to transmit firmware version, ADC mask and sampling rate */
public class DeviceInfoPacket extends Packet implements Publishable {

  private SamplingRate samplingRate;
  private int adsMask;

  public DeviceInfoPacket(double timeStamp) {
    super(timeStamp);
    super.type = EnumSet.of(ADS_MASK, SR);
  }

  @Override
  public void convertData(byte[] byteBuffer) throws InvalidDataException {
    final int adsSamplingRateCode = PacketUtils.bytesToInt(byteBuffer[2]); // 4, 5, or 6
    this.samplingRate = PacketUtils.adsCodeToSamplingRate(adsSamplingRateCode);
    this.adsMask = byteBuffer[3] & 0xFF;
  }

  @NonNull
  @Override
  public String toString() {
    return "PACKET: DeviceInfo";
  }

  @Override
  public int getDataCount() {
    return 2;
  }

  public int getChannelMask() {
    return this.adsMask;
  }

  public SamplingRate getSamplingRate() {
    return samplingRate;
  }

  @Override
  public Topic getTopic() {
    return Topic.DEVICE_INFO;
  }
}
