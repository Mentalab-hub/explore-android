package com.mentalab.packets;

import com.mentalab.packets.command.CommandReceivedPacket;
import com.mentalab.packets.command.CommandStatusPacket;
import com.mentalab.packets.sensors.exg.Eeg94;
import com.mentalab.packets.sensors.exg.Eeg98;
import com.mentalab.packets.info.DeviceInfoPacket;
import com.mentalab.packets.info.Environment;
import com.mentalab.packets.info.Orientation;
import com.mentalab.packets.sensors.MarkerPacket;

public enum PacketId {
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
            return new Eeg98(timeStamp);
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
