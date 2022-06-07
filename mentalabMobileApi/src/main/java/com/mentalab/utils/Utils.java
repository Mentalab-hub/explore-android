package com.mentalab.utils;

import android.util.Log;
import com.mentalab.exception.InvalidCommandException;
import com.mentalab.exception.NoConnectionException;
import com.mentalab.utils.constants.InputProtocol;

import java.util.Set;

public class Utils {

  public static final String TAG = "Explore";

  public static String checkName(String deviceName) throws NoConnectionException {
    deviceName = tryAppendWithExplore(deviceName);
    checkNameStartsWithExplore(deviceName);
    return deviceName;
  }

  private static void checkNameStartsWithExplore(String deviceName) throws NoConnectionException {
    if (!deviceName.startsWith("Explore_")) {
      throw new NoConnectionException(
          "Device names must begin with 'Explore_'. Provided device name: '"
              + deviceName
              + "'. Exiting.");
    }
  }

  private static String tryAppendWithExplore(String deviceName) {
    if (deviceName.length() == 4) {
      Log.i(TAG, "Appending device name with 'Explore_'.");
      deviceName = "Explore_" + deviceName;
    }
    return deviceName;
  }

  public static void checkSwitchTypes(Set<InputSwitch> switches, InputProtocol.Type type)
      throws InvalidCommandException {
    if (!switches.stream().allMatch(s -> s.getProtocol().isOfType(type))) {
      throw new InvalidCommandException(invalidSwitchString(type));
    }
  }

  public static void checkSwitchType(InputSwitch switchI, InputProtocol.Type type)
      throws InvalidCommandException {
    if (!switchI.getProtocol().isOfType(type)) {
      throw new InvalidCommandException(invalidSwitchString(type));
    }
  }

  private static String invalidSwitchString(InputProtocol.Type falseType) {
    return "Attempting to send a command using an invalid switch of type: "
        + falseType
        + ". Exiting.";
  }
}
