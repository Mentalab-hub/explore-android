package com.mentalab.utils;

import com.mentalab.utils.constants.Protocol;

public class InputSwitch {

  private final Protocol protocol;
  private boolean on;

  public InputSwitch(Protocol p, boolean turnOn) {
    this.protocol = p;
    this.on = turnOn;
  }

  public void turnOn() {
    this.on = true;
  }

  public void turnOff() {
    this.on = false;
  }

  public boolean isOn() {
    return on;
  }

  public Protocol getProtocol() {
    return protocol;
  }
}
