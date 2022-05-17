package com.mentalab.packets.info;

import androidx.annotation.NonNull;
import com.mentalab.MentalabCommands;
import com.mentalab.utils.constants.SamplingRate;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static com.mentalab.packets.Attributes.ADS_MASK;
import static com.mentalab.packets.Attributes.SR;

/** Device related information packet to transmit firmware version, ADC mask and sampling rate */
public class DeviceInfoPacket extends InfoPacket {

  private SamplingRate samplingRate;
  private int adsMask;

  public DeviceInfoPacket(double timeStamp) {
    super(timeStamp);
    super.attributes = EnumSet.of(ADS_MASK, SR);
  }

  @Override
  public void convertData(byte[] byteBuffer) {
    final int samplingRateMultiplier =
        ByteBuffer.wrap(new byte[] {byteBuffer[2], 0, 0, 0})
            .order(ByteOrder.LITTLE_ENDIAN)
            .getInt();

    final double sr = 16000 / (Math.pow(2, samplingRateMultiplier));
    if (sr < 300) {
      this.samplingRate = SamplingRate.SR_250;
    } else if (sr < 600) {
      this.samplingRate = SamplingRate.SR_500;
    } else {
      this.samplingRate = SamplingRate.SR_1000;
    }

    this.adsMask = byteBuffer[3] & 0xFF;

    super.data = new ArrayList<>(Arrays.asList((float) adsMask, (float) sr));
  }

  @Override
  public List<Float> getData() {
    return super.data;
  }

  @NonNull
  @Override
  public String toString() {
    return "DeviceInfoPacket";
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
}
