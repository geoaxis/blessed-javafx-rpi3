package com.github.geoaxis.blessedjavafxrpi3;

import java.util.UUID;

public class SensorTagConstants {
  public static final String CC_2650_SENSOR_TAG_SCAN_NAME = "CC2650 SensorTag";

  public static final UUID UUID_TEMPERATURE_SERVICE = UUID.fromString(
      "f000aa00-0451-4000-b000-000000000000");
  public static final UUID UUID_TEMPERATURE_DATA = UUID.fromString(
      "f000aa01-0451-4000-b000-000000000000");
  public static final UUID UUID_TEMPERATURE_CONFIG = UUID.fromString(
      "f000aa02-0451-4000-b000-000000000000");
  public static final byte[] ENABLE_COMMAND = {0x01};

}
