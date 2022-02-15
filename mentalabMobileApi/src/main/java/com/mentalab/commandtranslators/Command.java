package com.mentalab.commandtranslators;

public enum Command {
    // enum fields
    CMD_SAMPLING_RATE_SET(0xA1) {
        @Override
        public CommandTranslator createInstance(Command command) {
            return new SamplingRateCommandTranslator(command.getOperationCode(), command.getValue());
        }
    },
    CMD_CHANNEL_SET(0xA2) {
        @Override
        public CommandTranslator createInstance(Command command) {
            return new ChannelMaskTranslator(command.getOperationCode(), command.getValue());
        }
    },
    CMD_MEMORY_FORMAT(0xA3) {
        @Override
        public CommandTranslator createInstance(Command command) {
            return new FormatMemoryCommandTranslator(command.getOperationCode(), command.getValue());
        }
    },
    CMD_REC_TIME_SET(0xB1) {
        @Override
        public CommandTranslator createInstance(Command command) {
            return null;
        }
    },
    CMD_MODULE_DISABLE(0xA4) {
        @Override
        public CommandTranslator createInstance(Command command) {
            return new ModuleDisableTranslator(command.getOperationCode(), command.getValue());
        }
    },
    CMD_MODULE_ENABLE(0xA5) {
        @Override
        public CommandTranslator createInstance(Command command) {
            return new ModuleEnableTranslator(command.getOperationCode(), command.getValue());
        }
    },
    CMD_ZM_DISABLE(0xA6) {
        @Override
        public CommandTranslator createInstance(Command command) {
            return null;
        }
    },
    CMD_ZM_ENABLE(0xA7) {
        @Override
        public CommandTranslator createInstance(Command command) {
            return null;
        }
    },
    CMD_SOFT_RESET(0xA8) {
        @Override
        public CommandTranslator createInstance(Command command) {
            return new SoftResetCommandTranslator(command.getOperationCode(), command.getValue());
        }
    };

    // internal state
    private final int operation;
    private int value = 0; // default no argument

    // constructor
    Command(final int opCode) {
        this.operation = opCode;
    }

    public int getOperationCode() {
        return operation;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public abstract CommandTranslator createInstance(Command command);
}
