package com.mentalab.packets.info;

import androidx.annotation.NonNull;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Device related information packet to transmit firmware version, ADC mask and sampling rate
 */
public class Device extends InfoPacket {

  int adsMask;
  int samplingRate;

  public Device(double timeStamp) {
    super(timeStamp);
    super.attributes = Arrays.asList("Ads_Mask", "Sampling_Rate");
  }


  @Override
  public void convertData(byte[] byteBuffer) {
    int samplingRateMultiplier =
        ByteBuffer.wrap(new byte[]{byteBuffer[2], 0, 0, 0})
            .order(ByteOrder.LITTLE_ENDIAN)
            .getInt();
    this.samplingRate = (int) (16000 / (Math.pow(2, samplingRateMultiplier)));
    this.adsMask = byteBuffer[3] & 0xFF;

    super.convertedSamples =
        new ArrayList<>(
            Arrays.asList((float) adsMask, (float) samplingRate));
  }


  @Override
  public ArrayList<Float> getData() {
    return super.convertedSamples;
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
    return adsMask;
  }

  public int getSamplingRate() {
    return samplingRate;
  }
}
