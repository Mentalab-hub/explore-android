package com.mentalab.service;

import com.mentalab.ExploreDevice;

import java.util.concurrent.Callable;

public class DeviceInfoUpdaterTask implements Callable<Boolean> {
    ExploreDevice device;
    public DeviceInfoUpdaterTask(ExploreDevice device) {
        this.device = device;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Boolean call() throws Exception {
        return null;
    }
}
