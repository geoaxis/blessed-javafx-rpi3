package com.github.geoaxis.blessedjavafxrpi3;

public enum BLEState {
  READY("Ready"),
  SCANNING("Scanning"),
  CONNECTING("Connecting"),
  CONNECTED("Connected"),
  DISCONNECTING("Disconnecting");

  final String text;

  BLEState(String text) {
    this.text = text;
  }

}
