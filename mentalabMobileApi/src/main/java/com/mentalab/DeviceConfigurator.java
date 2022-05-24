package com.mentalab;

import com.mentalab.utils.constants.SamplingRate;

public class DeviceConfigurator {
    private ExploreDevice device;
    public DeviceConfigurator(ExploreDevice exploreDevice){
        device = exploreDevice;
    }

    void configureSamplingRateInfo(SamplingRate samplingRate){
        device.samplingRate = samplingRate;
    }
}
