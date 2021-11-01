package com.github.geoaxis.blessedjavafxrpi3;

public enum States {
  READY("Ready"),
  SCANNING("Scanning"),
  CONNECTING("Connecting"),
  CONNECTED("Connected"),
  DISCONNECTING("Disconnecting");

  String text;

  States(String text) {
    this.text = text;
  }

}
