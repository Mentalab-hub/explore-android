package com.mentalab.io.constants;

// Fantastic solution to hierarchical enums:
// https://stackoverflow.com/questions/19680418/how-to-use-enum-with-grouping-and-subgrouping-hierarchy-nesting
public enum Switch {
    ENVIRONMENT(Group.Module),
    ORN(Group.Module),
    EXG(Group.Module),
    CHANNEL_0(Group.Channel),
    CHANNEL_1(Group.Channel),
    CHANNEL_2(Group.Channel),
    CHANNEL_3(Group.Channel),
    CHANNEL_4(Group.Channel),
    CHANNEL_5(Group.Channel),
    CHANNEL_6(Group.Channel),
    CHANNEL_(Group.Channel);


    private final Group group;


    Switch(Group group) {
        this.group = group;
    }


    public enum Group {
        Module,
        Channel;
    }


    public boolean isInGroup(Group group) {
        return this.group == group;
    }
}
