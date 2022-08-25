package com.mentalab.utils;

import android.util.Log;
import com.mentalab.exception.InvalidCommandException;
import com.mentalab.exception.NoConnectionException;
import com.mentalab.packets.Packet;
import com.mentalab.utils.constants.ChannelCount;
import com.mentalab.utils.constants.ConfigProtocol;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;

public class Utils {

  public static final String TAG = "Explore";
  public static final DecimalFormat DF = new DecimalFormat("#.####");

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

  public static void checkSwitchTypes(Set<ConfigSwitch> switches, ConfigProtocol.Type type)
      throws InvalidCommandException {
    if (!switches.stream().allMatch(s -> s.getProtocol().isOfType(type))) {
      throw new InvalidCommandException(invalidSwitchString(type));
    }
  }

  public static void checkSwitchType(ConfigSwitch switchI, ConfigProtocol.Type type)
      throws InvalidCommandException {
    if (!switchI.getProtocol().isOfType(type)) {
      throw new InvalidCommandException(invalidSwitchString(type));
    }
  }

  private static String invalidSwitchString(ConfigProtocol.Type falseType) {
    return "Attempting to send a command using an invalid switch of type: "
        + falseType
        + ". Exiting.";
  }

  public static String round(double d) {
    DF.setRoundingMode(RoundingMode.FLOOR);
    return DF.format(d);
  }

  public static double[] convertArraylistToDoubleArray(Packet packet) {
    List<Float> packetVoltageValues = packet.getData();
    double[] floatArray = new double[packetVoltageValues.size()];

    for (int index = 0; index < packetVoltageValues.size(); index++) {
      floatArray[index] = packetVoltageValues.get(index).doubleValue();
    }
    return floatArray;
  }

  public static ChannelCount getChannelCountFromInt(int i) {
    if (i < 5) {
      return ChannelCount.CC_4;
    }
    return ChannelCount.CC_8;
  }

}
