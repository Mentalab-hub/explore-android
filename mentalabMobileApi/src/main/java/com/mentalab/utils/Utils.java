package com.mentalab.utils;

import android.util.Log;
import com.mentalab.exception.NoConnectionException;

import java.util.Set;

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

  // todo: consider current state
  public static int generateChannelsArg(Set<InputSwitch> switches, int channelCount) {
    int binaryArg;
    if (channelCount < 8) {
      binaryArg = 0b1111;
    } else {
      binaryArg = 0b11111111;
    }

    for (InputSwitch aSwitch : switches) {
      if (!aSwitch.isOn()) {
        final int channelID = aSwitch.getProtocol().getID();
        binaryArg &= ~(1 << channelID); // reverse the bit at the channel id
      }
    }
    return binaryArg;
  }
}
