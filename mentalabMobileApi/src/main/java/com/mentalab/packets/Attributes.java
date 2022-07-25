package com.mentalab.packets;

public enum Attributes {
  ADS_MASK("Ads Mask"),
  SR("Sampling Rate"),

  TEMP("Temperature"),
  LIGHT("Light"),
  BATTERY("Battery"),

  SLOPE("slope"),
  OFFSET("offset"),

  MARKER("Marker"),

  ACCX("Accelerometer X"),
  ACCY("Accelerometer Y"),
  ACCZ("Accelerometer Z"),
  MAGX("Magnetometer X"),
  MAGY("Magnetometer Y"),
  MAGZ("Magnetometer Z"),
  GYROX("Gyroscope X"),
  GYROY("Gyroscope Y"),
  GYROZ("Gyroscope Z"),

  CH1("Channel 1"),
  CH2("Channel 2"),
  CH3("Channel 3"),
  CH4("Channel 4"),
  CH5("Channel 5"),
  CH6("Channel 6"),
  CH7("Channel 7"),
  CH8("Channel 8");

  private final String name;

  Attributes(String s) {
    name = s;
  }

  public String toString() {
    return this.name;
  }
}
