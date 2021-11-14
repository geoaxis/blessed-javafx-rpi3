package com.github.geoaxis.blessedjavafxrpi3;

public class CC2650DataProcessor {

  private static final float SCALE_LSB = 0.03125f;

  public static float[] calculateTemperature(byte[] data) {
    float[] temperatures = new float[2];

    int rawObjTemp = unsigned16Bits(data, 0);
    int rawAmbTemp = unsigned16Bits(data, 2);

    temperatures[0] = (rawObjTemp >> 2) * SCALE_LSB;
    temperatures[1] = (rawAmbTemp >> 2) * SCALE_LSB;

    return temperatures;
  }

  private static int unsigned16Bits(byte[] data, int offset) {
    int byte0 = data[offset] & 0xff;
    int byte1 = data[offset + 1] & 0xff;

    return (byte1 << 8) + byte0;
  }

}
