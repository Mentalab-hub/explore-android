package com.mentalab.utils;

import com.mentalab.utils.constants.InputProtocol;

public class InputSwitch {

  private final InputProtocol inputProtocol;
  private final boolean on;

  public InputSwitch(InputProtocol p, boolean turnOn) {
    this.inputProtocol = p;
    this.on = turnOn;
  }

  public boolean isOn() {
    return on;
  }

  public InputProtocol getProtocol() {
    return inputProtocol;
  }
}
