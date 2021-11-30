package com.mentalab;

import com.mentalab.CommandTranslators.ChannelMaskTranslator;
import com.mentalab.CommandTranslators.CommandTranslator;
import com.mentalab.CommandTranslators.FormatMemoryCommandTranslator;
import com.mentalab.CommandTranslators.ModuleDisableTranslator;
import com.mentalab.CommandTranslators.ModuleEnableTranslator;
import com.mentalab.CommandTranslators.SamplingRateCommandTranslator;
import com.mentalab.CommandTranslators.SoftResetCommandTranslator;

public class MentalabConstants {

  public enum SamplingRate {
    // enum fields
    SR_250(0x01),
    SR_500(0x02),
    SR_1000(0x03);

    // internal state
    private int samplingRate;

    // constructor
    private SamplingRate(final int samplingRate) {
      this.samplingRate = samplingRate;
    }

    public int getValue() {
      return samplingRate;
    }
  }

  public enum Command {
    // enum fields
    CMD_SAMPLING_RATE_SET(0xA1) {
      @Override
      public CommandTranslator createInstance(Command command, int extraArguments) {
        return new SamplingRateCommandTranslator(command.getValue(), extraArguments);
      }
    },
    CMD_CHANNEL_SET(0xA2) {
      @Override
      public CommandTranslator createInstance(Command command, int extraArguments) {
        return new ChannelMaskTranslator(command.getValue(), extraArguments);
      }
    },
    CMD_MEMORY_FORMAT(0xA3) {
      @Override
      public CommandTranslator createInstance(Command command, int extraArguments) {
        return new FormatMemoryCommandTranslator(command.getValue(), extraArguments);
      }
    },
    CMD_REC_TIME_SET(0xB1) {
      @Override
      public CommandTranslator createInstance(Command command, int extraArguments) {
        return null;
      }
    },
    CMD_MODULE_DISABLE(0xA4) {
      @Override
      public CommandTranslator createInstance(Command command, int extraArguments) {
        return new ModuleDisableTranslator(command.getValue(), extraArguments);
      }
    },
    CMD_MODULE_ENABLE(0xA5) {
      @Override
      public CommandTranslator createInstance(Command command, int extraArguments) {
        return new ModuleEnableTranslator(command.getValue(), extraArguments);
      }
    },
    CMD_ZM_DISABLE(0xA6) {
      @Override
      public CommandTranslator createInstance(Command command, int extraArguments) {
        return null;
      }
    },
    CMD_ZM_ENABLE(0xA7) {
      @Override
      public CommandTranslator createInstance(Command command, int extraArguments) {
        return null;
      }
    },
    CMD_SOFT_RESET(0xA8) {
      @Override
      public CommandTranslator createInstance(Command command, int extraArguments) {
        return new SoftResetCommandTranslator(command.getValue(), extraArguments);
      }
    };

    // internal state
    private int opCode;

    // constructor
    private Command(final int opCode) {
      this.opCode = opCode;
    }

    public int getValue() {
      return opCode;
    }

    public abstract CommandTranslator createInstance(Command command, int extraArguments);
  }

  /** Topics available for Publisher Subscriber manager */
  enum Topic {
    ExG,
    Orn,
    Marker,
    Command;
  }


  enum FileType {
    CSV
  }


  interface QueueAttribute {
    enum OrientationAttribute {
      ACC_X,
      ACC_Y,
      Acc_Z,
      Mag_X,
      Mag_Y,
      Mag_Z,
      Gyro_X,
      Gyro_Y,
      Gyro_Z;
    }

    enum ExgChannel {
      CHANNEL_0,
      CHANNEL_1,
      CHANNEL_2,
      CHANNEL_3,
      CHANNEL_4,
      CHANNEL_5,
      CHANNEL_6,
      CHANNEL_7;
    }

    enum DeviceInfoAttribute {
      FIRMWARE_VERSION,
      SAMPLING_RATE,
      ADS_MASK;
    }
  }

  public interface DeviceConfigSwitches {
    String[] Modules = {"ModuleEnv", "ModuleOrn", "ModuleExg"};
    String[] Channels = {
      "CHANNEL_0",
      "CHANNEL_1",
      "CHANNEL_2",
      "CHANNEL_3",
      "CHANNEL_4",
      "CHANNEL_5",
      "CHANNEL_6",
      "CHANNEL_7"
    };
  }
}
