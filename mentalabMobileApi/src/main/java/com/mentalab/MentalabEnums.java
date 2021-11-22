package com.mentalab;

class MentalabEnums {
  /*enum OrientationAttributes {
    ACC_X("Acc_X"), ACC_Y("Acc_Y"), Acc_Z("Acc_Z"), Mag_X("Mag_X"), Mag_Y("Mag_Y"), Mag_Z("Mag_Z"), Gyro_X("Gyro_X"), Gyro_Y("Gyro_Y"), Gyro_Z("Gyro_Z");

    private final String val;

    private OrientationAttributes(String val) {
      this.val = val;
    }

    @Override
    public String toString() {
      return val;
    }
  }*/

  enum OrientationAttributes {
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

  enum Topics {
    ExG,
    Orn,
    Marker;
  }

  enum DeviceInfoAttributes {
    FW_VERSION,
    SAMPLING_RATE,
    ADS_MASK;
  }

  enum FileType {
    CSV
  }
}
