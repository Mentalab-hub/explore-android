package com.mentalab.io;

// Fantastic solution to hierarchical enums:
// https://stackoverflow.com/questions/19680418/how-to-use-enum-with-grouping-and-subgrouping-hierarchy-nesting
public enum Switch {
    ENVIRONMENT(Group.Module, true, 0),
    ORN(Group.Module, true, 1),
    EXG(Group.Module, true, 2),
    CHANNEL_0(Group.Channel, true, 0),
    CHANNEL_1(Group.Channel, true, 1),
    CHANNEL_2(Group.Channel, true, 2),
    CHANNEL_3(Group.Channel, true, 3),
    CHANNEL_4(Group.Channel, true, 4),
    CHANNEL_5(Group.Channel, true, 5),
    CHANNEL_6(Group.Channel, true, 6),
    CHANNEL_7(Group.Channel, true, 7);


    private final Group group;
    private final boolean on;
    private final int id;


    Switch(Group group, boolean on, int id) {
        this.group = group;
        this.on = on;
        this.id = id;
    }


    public enum Group {
        Module,
        Channel;
    }


    public boolean isInGroup(Group group) {
        return this.group == group;
    }

    
    public boolean isOn() {
        return this.on;
    }


    public int getID() {
        return this.id;
    }
}
