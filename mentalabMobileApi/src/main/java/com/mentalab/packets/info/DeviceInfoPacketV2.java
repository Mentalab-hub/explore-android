package com.mentalab.packets.info;

import android.util.Log;

import com.mentalab.exception.InvalidDataException;
import com.mentalab.packets.Packet;
import com.mentalab.packets.PacketDataType;
import com.mentalab.packets.PacketUtils;
import com.mentalab.utils.constants.SamplingRate;
import com.mentalab.utils.constants.Topic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;

import androidx.annotation.NonNull;

public class DeviceInfoPacketV2 extends Packet {

    private SamplingRate samplingRate;
    private int adsMask;
    private String boardId;
    private int memoryInfo;

    public DeviceInfoPacketV2(double timeStamp) {
        super(timeStamp);
        // TODO add other fields of packet
        super.type = EnumSet.of(PacketDataType.ADS_MASK, PacketDataType.SR);
    }

    @Override
    public void populate(byte[] data) throws InvalidDataException, IOException {
    // TODO add other fields (check explorepy for fields)
    // 2022-12-07 11:50:44,017 - [INFO] - Device info: {'device_name': 'Explore_8526',
    // 'firmware_version': '3.0.0', 'adc_mask': [1, 1, 1, 1, 1, 1, 1, 1], 'sampling_rate': 250.0,
    // 'board_id': 'PCB_304_801_XXX', 'memory_info': 0}
        this.boardId = new String(Arrays.copyOfRange(data, 0, 15), StandardCharsets.UTF_8);
        Log.i("DIPV2", "Board ID: " + this.boardId);
        final int adsSamplingRateCode = PacketUtils.bytesToInt(data[18]);
        this.samplingRate = PacketUtils.adsCodeToSamplingRate(adsSamplingRateCode);
        Log.i("DIPV2", "Calculated sampling rate: " + this.samplingRate.toString());
        this.adsMask = data[19] & 0xFF;
        Log.i("DIPV2", "Calculated adsMask: " + Integer.toBinaryString(this.adsMask));
        // self.is_memory_available = bin_data[20]
        this.memoryInfo = data[20];
        Log.i("DIPV2", "Memory Info: " + this.memoryInfo);
    }

    @NonNull
    @Override
    public String toString() {
        return "PACKET: DeviceInfoV2";
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
