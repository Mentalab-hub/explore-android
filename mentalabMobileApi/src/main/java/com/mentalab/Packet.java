package com.mentalab;

import android.util.Log;
import com.mentalab.exception.InvalidDataException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

/** Placeholder class for publishable packets */
interface PublishablePacket {
  String getPacketTopic();
}

/** Root packet interface */
abstract class Packet {

  // TO DO add constant field
  // TO DO Better Logging method
  protected static final String TAG = "Explore";
  private byte[] byteBuffer = null;
  private int dataCount;
  private double timeStamp;

  public Packet(double timeStamp) {
    this.timeStamp = timeStamp;
  }

  /** String representation of attributes */
  static double[] bytesToDouble(byte[] bytes, int numOfbytesPerNumber) throws InvalidDataException {
    if (bytes.length % numOfbytesPerNumber != 0) {
      throw new InvalidDataException("Illegal length", null);
    }
    int arraySize = bytes.length / numOfbytesPerNumber;
    double[] values = new double[arraySize];
    for (int index = 0; index < bytes.length; index += numOfbytesPerNumber) {
      int signBit = bytes[index + numOfbytesPerNumber - 1] >> 7;
      double value;

      value =
          ByteBuffer.wrap(new byte[] {bytes[index], bytes[index + 1]})
              .order(ByteOrder.LITTLE_ENDIAN)
              .getShort();
      if (signBit == 1) {
        value = -1 * (Math.pow(2, 8 * numOfbytesPerNumber) - value);
      }

      values[index / numOfbytesPerNumber] = value;
    }
    return values;
  }

  /**
   * Converts binary data stream to human readable voltage values
   *
   * @param byteBuffer
   */
  public abstract void convertData(byte[] byteBuffer) throws InvalidDataException;

  /** String representation of attributes */
  public abstract String toString();

  /** Number of element in each packet */
  public abstract int getDataCount();

  /** Get data values from packet structure */
  public ArrayList<Float> getData() {
    return null;
  }
  ;

  enum PacketId {
    ORIENTATION(13) {
      @Override
      public Packet createInstance(double timeStamp) {

        return new Orientation(timeStamp);
      }
    },
    ENVIRONMENT(19) {
      @Override
      public Packet createInstance(double timeStamp) {
        return new Environment(timeStamp);
      }
    },
    TIMESTAMP(27) {
      @Override
      public Packet createInstance(double timeStamp) {
        return null;
      }
    },
    DISCONNECT(25) {
      @Override
      public Packet createInstance(double timeStamp) {
        return null;
      }
    },
    INFO(99) {
      @Override
      public Packet createInstance(double timeStamp) {
        return new DeviceInfoPacket(timeStamp);
      }
    },
    EEG94(144) {
      @Override
      public Packet createInstance(double timeStamp) {
        return new Eeg94(timeStamp);
      }
    },
    EEG98(146) {
      @Override
      public Packet createInstance(double timeStamp) {
        return new Eeg98(timeStamp);
      }
    },
    EEG99S(30) {
      @Override
      public Packet createInstance(double timeStamp) {
        return null;
      }
    },
    EEG99(62) {
      @Override
      public Packet createInstance(double timeStamp) {
        return null;
      }
    },
    EEG94R(208) {
      @Override
      public Packet createInstance(double timeStamp) {
        return null;
      }
    },
    EEG98R(210) {
      @Override
      public Packet createInstance(double timeStamp) {
        return null;
      }
    },
    CMDRCV(192) {
      @Override
      public Packet createInstance(double timeStamp) {
        return new CommandReceivedPacket(timeStamp);
      }
    },
    CMDSTAT(193) {
      @Override
      public Packet createInstance(double timeStamp) {
        return new CommandStatusPacket(timeStamp);
      }
    },
    MARKER(194) {
      @Override
      public Packet createInstance(double timeStamp) {
        return new MarkerPacket(timeStamp);
      }
    },
    CALIBINFO(195) {
      @Override
      public Packet createInstance(double timeStamp) {
        return null;
      }
    };

    private int value;

    PacketId(int value) {
      this.value = value;
    }

    public int getNumVal() {
      return value;
    }

    public abstract Packet createInstance(double timeStamp);
  }
}

/** Interface for different EEG packets */
abstract class DataPacket extends Packet implements PublishablePacket {
  private static final String TAG = "Explore";
  private static byte channelMask;
  public ArrayList<Float> convertedSamples;

  public DataPacket(double timeStamp) {
    super(timeStamp);
  }

  static double[] toInt32(byte[] byteArray) throws InvalidDataException, IOException {
    if (byteArray.length % 3 != 0)
      throw new InvalidDataException("Byte buffer is not read properly", null);
    int arraySize = byteArray.length / 3;
    double[] values = new double[arraySize];

    for (int index = 0; index < byteArray.length; index += 3) {
      if (index == 0) {
        channelMask = byteArray[index];
      }
      int signBit = byteArray[index + 2] >> 7;
      double value;
      if (signBit == 0)
        value =
            ByteBuffer.wrap(
                    new byte[] {byteArray[index], byteArray[index + 1], byteArray[index + 2], 0})
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();
      else {
        int twosComplimentValue =
            ByteBuffer.wrap(
                    new byte[] {byteArray[index], byteArray[index + 1], byteArray[index + 2], 0})
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();
        value = -1 * (Math.pow(2, 24) - twosComplimentValue);
      }
      values[index / 3] = value;
    }

    return values;
  }

  public static byte getChannelMask() {
    return channelMask;
  }

  public static void setChannelMask(byte channelMask) {
    DataPacket.channelMask = channelMask;
  }

  public ArrayList<Float> getData() {
    return convertedSamples;
  }

  @Override
  public String getPacketTopic() {
    return "ExG";
  }
}

/** Interface for packets related to device information */
abstract class InfoPacket extends Packet {
  ArrayList<Float> convertedSamples = null;
  ArrayList<String> attributes;

  public InfoPacket(double timeStamp) {
    super(timeStamp);
  }
}

/** Interface for packets related to device synchronization */
abstract class UtilPacket extends Packet {

  protected ArrayList<Float> convertedSamples;

  public UtilPacket(double timeStamp) {
    super(timeStamp);
  }
}

// class Eeg implements DataPacket {}
class Eeg98 extends DataPacket {
  private static int channelNumber = 8;

  public Eeg98(double timeStamp) {
    super(timeStamp);
  }

  @Override
  public void convertData(byte[] byteBuffer) {
    List<Float> values = new ArrayList<Float>();
    try {
      double[] data = DataPacket.toInt32(byteBuffer);

      for (int index = 0; index < data.length; index++) {
        // skip int representation for status bit
        if (index % 9 == 0) continue;
        // calculation for gain adjustment
        double exgUnit = Math.pow(10, -6);
        double vRef = 2.4;
        double gain = (exgUnit * (Math.pow(2, 23) - 1)) * 6;
        values.add((float) (data[index] * (vRef / gain)));
      }
    } catch (InvalidDataException | IOException e) {
      e.printStackTrace();
    }
    this.convertedSamples = new ArrayList<>(values);
  }

  @Override
  public String toString() {

    String data = "ExG 8 channel: [";

    for (Float convertedSample : this.convertedSamples) {
      data += convertedSample + " ,";
    }
    return data + "]";
  }

  /** Number of element in each packet */
  @Override
  public int getDataCount() {
    return 8;
  }
}

class Eeg94 extends DataPacket {

  private final int channelNumber = 4;

  public Eeg94(double timeStamp) {
    super(timeStamp);
  }

  /**
   * Converts binary data stream to human readable voltage values
   *
   * @param byteBuffer
   */
  @Override
  public void convertData(byte[] byteBuffer) {
    List<Float> values = new ArrayList<Float>();
    try {
      double[] data = DataPacket.toInt32(byteBuffer);

      for (int index = 0; index < data.length; index++) {
        // skip int representation for status bit
        if (index % 5 == 0) continue;
        // calculation for gain adjustment
        double exgUnit = Math.pow(10, -6);
        double vRef = 2.4;
        double gain = (exgUnit * (Math.pow(2, 23) - 1)) * 6;
        values.add((float) (data[index] * (vRef / gain)));
      }
    } catch (InvalidDataException | IOException e) {
      e.printStackTrace();
    }
    this.convertedSamples = new ArrayList<>(values);
  }

  @Override
  public String toString() {

    String data = "ExG 4 channel: [";
    ListIterator<Float> it = this.convertedSamples.listIterator();

    while (it.hasNext()) {
      data += it.next() + " ,";
    }
    return data + "]";
  }

  /** Number of element in each packet */
  @Override
  public int getDataCount() {
    return this.channelNumber;
  }
}

class Eeg99 extends DataPacket {

  public Eeg99(double timeStamp) {
    super(timeStamp);
  }

  /**
   * Converts binary data stream to human readable voltage values
   *
   * @param byteBuffer
   */
  @Override
  public void convertData(byte[] byteBuffer) {}

  /** String representation of attributes */
  @Override
  public String toString() {
    return "Eeg99";
  }

  /** Number of element in each packet */
  @Override
  public int getDataCount() {
    return 0;
  }
}

class Eeg99s extends DataPacket {

  public Eeg99s(double timeStamp) {
    super(timeStamp);
  }

  /**
   * Converts binary data stream to human readable voltage values
   *
   * @param byteBuffer byte array with input data
   */
  @Override
  public void convertData(byte[] byteBuffer) {}

  /** String representation of attributes */
  @Override
  public String toString() {
    return "Eeg99s";
  }

  /** Number of element in each packet */
  @Override
  public int getDataCount() {
    return 0;
  }
}

/** Device related information packet to transmit firmware version, ADC mask and sampling rate */
class Orientation extends InfoPacket implements PublishablePacket {
  ArrayList<Float> listValues = new ArrayList<Float>();

  public Orientation(double timeStamp) {
    super(timeStamp);
    attributes =
        new ArrayList<String>(
            Arrays.asList(
                "Acc_X", "Acc_Y", "Acc_Z", "Mag_X", "Mag_Y", "Mag_Z", "Gyro_X", "Gyro_Y",
                "Gyro_Z"));
  }

  @Override
  public void convertData(byte[] byteBuffer) throws InvalidDataException {
    double[] convertedRawValues = super.bytesToDouble(byteBuffer, 2);

    for (int index = 0; index < convertedRawValues.length; index++) {
      if (index < 3) {
        listValues.add((float) (convertedRawValues[index] * 0.061));
      } else if (index < 6) {
        listValues.add((float) (convertedRawValues[index] * 8.750));
      } else {
        if (index == 6) {
          listValues.add((float) (convertedRawValues[index] * 1.52 * -1));
        } else {
          listValues.add((float) (convertedRawValues[index] * 1.52));
        }
      }
    }
    this.convertedSamples = new ArrayList<>(listValues);
    Log.d("Explore", "Converted samples in the packets are: " + this.convertedSamples.toString());
  }

  @Override
  public String toString() {
    String data = "Orientation packets: [";

    for (int index = 0; index < convertedSamples.size(); index += 1) {
      if (index % 9 < 3) {
        data += " accelerometer: " + convertedSamples.get(index);
      } else if (index % 9 < 6) {
        data += " magnetometer: " + convertedSamples.get(index);
      } else {
        data += "gyroscope: " + convertedSamples.get(index);
      }

      data += ",";
    }

    return data + "]";
  }

  /** Number of element in each packet */
  @Override
  public int getDataCount() {
    return 9;
  }

  /** Number of element in each packet */
  @Override
  public ArrayList<Float> getData() {
    return this.convertedSamples;
  }

  @Override
  public String getPacketTopic() {
    return "Orn";
  }
}

/** Device related information packet to transmit firmware version, ADC mask and sampling rate */
class DeviceInfoPacket extends InfoPacket {
  byte adsMask;
  int samplingRate;

  public DeviceInfoPacket(double timeStamp) {
    super(timeStamp);
  }

  @Override
  public void convertData(byte[] byteBuffer) {
    int samplingRateMultiplier =
        ByteBuffer.wrap(new byte[] {byteBuffer[2], 0, 0, 0})
            .order(ByteOrder.LITTLE_ENDIAN)
            .getInt();
    samplingRate = (int) (16000 / (Math.pow(2, samplingRateMultiplier)));
    adsMask = byteBuffer[3];
    Log.d(TAG, "sampling rate: " + samplingRate + "ads is ..." + adsMask);
  }

  @Override
  public String toString() {
    return "DeviceInfoPacket";
  }

  /** Number of element in each packet */
  @Override
  public int getDataCount() {
    return 0;
  }
}

/**
 * Acknowledgement packet is sent when a configuration command is successfully executed on the
 * device
 */
class AckPacket extends InfoPacket {

  public AckPacket(double timeStamp) {
    super(timeStamp);
  }

  @Override
  public void convertData(byte[] byteBuffer) {}

  @Override
  public String toString() {
    return "AckPacket";
  }

  @Override
  public int getDataCount() {
    return 0;
  }
}

/** Packet sent from the device to sync clocks */
class TimeStampPacket extends UtilPacket {

  public TimeStampPacket(double timeStamp) {
    super(timeStamp);
  }

  @Override
  public void convertData(byte[] byteBuffer) {}

  @Override
  public String toString() {
    return "TimeStampPacket";
  }

  /** Number of element in each packet */
  @Override
  public int getDataCount() {
    return 0;
  }
}

/** Disconnection packet is sent when the host machine is disconnected from the device */
class DisconnectionPacket extends UtilPacket {

  public DisconnectionPacket(double timeStamp) {
    super(timeStamp);
  }

  /**
   * Converts binary data stream to human readable voltage values
   *
   * @param byteBuffer
   */
  @Override
  public void convertData(byte[] byteBuffer) {}

  /** String representation of attributes */
  @Override
  public String toString() {
    return "DisconnectionPacket";
  }

  /** Number of element in each packet */
  @Override
  public int getDataCount() {
    return 0;
  }
}

class Environment extends InfoPacket {
  float temperature, light, battery;

  public Environment(double timeStamp) {
    super(timeStamp);
    super.attributes = new ArrayList(Arrays.asList("Temperature ", "Light ", "Battery "));
  }
  /**
   * Converts binary data stream to human readable voltage values
   *
   * @param byteBuffer
   */
  @Override
  public void convertData(byte[] byteBuffer) throws InvalidDataException {
    List<Float> listValues = new ArrayList<Float>();

    listValues.add(
        (float)
            ByteBuffer.wrap(new byte[] {byteBuffer[0], 0, 0, 0})
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt());
    listValues.add(
        (float)
                (ByteBuffer.wrap(new byte[] {byteBuffer[1], byteBuffer[2], 0, 0})
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .getInt())
            * (1000 / 4095));
    float batteryLevelRaw =
        (float)
            ((ByteBuffer.wrap(new byte[] {byteBuffer[3], byteBuffer[4], 0, 0})
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .getInt()
                    * 16.8
                    / 6.8)
                * (1.8 / 2457));

    listValues.add(getBatteryParcentage(batteryLevelRaw));
    this.convertedSamples = new ArrayList<>(listValues);
  }

  /** String representation of attributes */
  @Override
  public String toString() {
    String data = "Environment packets: [";

    for (int index = 0; index < convertedSamples.size(); index += 1) {
      if (index % 9 < 3) {
        data += " Temperature: " + convertedSamples.get(index);
      } else if (index % 9 < 6) {
        data += " Light: " + convertedSamples.get(index);
      } else {
        data += "Battery: " + convertedSamples.get(index);
      }

      data += ",";
    }

    return data + "]";
  }

  /** Number of element in each packet */
  @Override
  public int getDataCount() {
    return 3;
  }

  float getBatteryParcentage(float voltage) {
    double parcentage = 0;
    if (voltage < 3.1) {
      parcentage = 1;
    } else if (voltage < 3.5) {
      parcentage = (1 + (voltage - 3.1) / .4 * 10);
    } else if (voltage < 3.8) {
      parcentage = 10 + (voltage - 3.5) / .3 * 40;
    } else if (voltage < 3.9) {
      parcentage = 40 + (voltage - 3.8) / .1 * 20;
    } else if (voltage < 4) {
      parcentage = 60 + (voltage - 3.9) / .1 * 15;
    } else if (voltage < 4.1) {
      parcentage = 75 + (voltage - 4.) / .1 * 15;
    } else if (voltage < 4.2) {
      parcentage = 90 + (voltage - 4.1) / .1 * 10;
    } else {
      parcentage = 100;
    }

    return (float) parcentage;
  }
}

class MarkerPacket extends InfoPacket implements PublishablePacket {
  int markerCode;

  public MarkerPacket(double timeStamp) {
    super(timeStamp);
  }

  /**
   * Converts binary data stream to human readable voltage values
   *
   * @param byteBuffer
   */
  @Override
  public void convertData(byte[] byteBuffer) throws InvalidDataException {
    markerCode =
        ByteBuffer.wrap(new byte[] {byteBuffer[0], 0}).order(ByteOrder.LITTLE_ENDIAN).getShort();
  }

  /** String representation of attributes */
  @Override
  public String toString() {
    return "Marker: " + markerCode;
  }

  /** Number of element in each packet */
  @Override
  public int getDataCount() {
    return 1;
  }

  /** Get data values from packet structure */
  @Override
  public ArrayList<Float> getData() {
    return new ArrayList<Float>(markerCode);
  }

  @Override
  public String getPacketTopic() {
    return "Marker";
  }
}

class CommandReceivedPacket extends InfoPacket implements PublishablePacket {
  float markerCode;

  public CommandReceivedPacket(double timeStamp) {
    super(timeStamp);
  }

  /**
   * Converts binary data stream to human readable voltage values
   *
   * @param byteBuffer
   */
  @Override
  public void convertData(byte[] byteBuffer) throws InvalidDataException {
    double[] convertedRawValues = super.bytesToDouble(byteBuffer, 2);
    markerCode = (float) convertedRawValues[0];
  }

  /** String representation of attributes */
  @Override
  public String toString() {
    return null;
  }

  /** Number of element in each packet */
  @Override
  public int getDataCount() {
    return 1;
  }

  @Override
  public String getPacketTopic() {
    return "Command";
  }
}

class CommandStatusPacket extends InfoPacket implements PublishablePacket {
  boolean commandStatus;

  public CommandStatusPacket(double timeStamp) {
    super(timeStamp);
  }

  /**
   * Converts binary data stream to human readable voltage values
   *
   * @param byteBuffer
   */
  @Override
  public void convertData(byte[] byteBuffer) throws InvalidDataException {
    double[] convertedRawValues = super.bytesToDouble(byteBuffer, 2);
    short status =
        ByteBuffer.wrap(new byte[] {byteBuffer[5], 0}).order(ByteOrder.LITTLE_ENDIAN).getShort();
    commandStatus = status != 0;
  }

  /** String representation of attributes */
  @Override
  public String toString() {
    return null;
  }

  /** Number of element in each packet */
  @Override
  public int getDataCount() {
    return 1;
  }

  @Override
  public String getPacketTopic() {
    return "Command";
  }
}
