package com.mentalab.utils;

import android.util.Log;

import com.mentalab.exception.NoConnectionException;

public class Utils {

  public static final String TAG = "Explore";

  public static String checkName(String deviceName) throws NoConnectionException {
    if (deviceName.length() == 4) {
      Log.i(TAG, "Appending device name with 'Explore_'.");
      deviceName = "Explore_" + deviceName;
    }

    if (!deviceName.startsWith("Explore_")) {
      throw new NoConnectionException(
          "Device names must begin with 'Explore_'. Provided device name: '"
              + deviceName
              + "'. Exiting.");
    }
    return deviceName;
  }
}
