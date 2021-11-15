package com.github.geoaxis.blessedjavafxrpi3;

import static com.github.geoaxis.blessedjavafxrpi3.SensorTagDataProcessor.calculateTemperature;
import static com.github.geoaxis.blessedjavafxrpi3.SensorTagConstants.ENABLE_COMMAND;
import static com.github.geoaxis.blessedjavafxrpi3.SensorTagConstants.UUID_TEMPERATURE_CONFIG;
import static com.github.geoaxis.blessedjavafxrpi3.SensorTagConstants.UUID_TEMPERATURE_DATA;
import static com.github.geoaxis.blessedjavafxrpi3.SensorTagConstants.UUID_TEMPERATURE_SERVICE;
import static com.welie.blessed.BluetoothCommandStatus.COMMAND_SUCCESS;

import com.welie.blessed.BluetoothCommandStatus;
import com.welie.blessed.BluetoothGattCharacteristic;
import com.welie.blessed.BluetoothGattCharacteristic.WriteType;
import com.welie.blessed.BluetoothGattService;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;
import java.util.List;
import java.util.UUID;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class PeripheralCallback extends BluetoothPeripheralCallback {

    private final StringProperty irTemperatureString;
    private final StringProperty ambientTemperatureString;

    @Override
    public void onServicesDiscovered(@NotNull BluetoothPeripheral peripheral,
        @NotNull  List<BluetoothGattService> services) {
      if (peripheral.getService(UUID_TEMPERATURE_SERVICE) != null) {
        peripheral.writeCharacteristic(UUID_TEMPERATURE_SERVICE,
            UUID_TEMPERATURE_CONFIG,
            ENABLE_COMMAND,
            WriteType.WITH_RESPONSE);
        BluetoothGattCharacteristic temperatureDataChar = peripheral.getCharacteristic(
            UUID_TEMPERATURE_SERVICE,
            UUID_TEMPERATURE_DATA);
        peripheral.setNotify(temperatureDataChar, true);

      }

    }

    @Override
    public void onCharacteristicWrite(@NotNull BluetoothPeripheral peripheral,
    byte[] value,
    @NotNull BluetoothGattCharacteristic characteristicUUID,
    @NotNull BluetoothCommandStatus status) {

      // Deal with errors
      if (status != COMMAND_SUCCESS) {
        System.err.println("command failed with status {}", status);
        return;
      }

      if (characteristicUUID.getUuid().equals(UUID_TEMPERATURE_CONFIG)) {
        System.out.println("temperature notifications configured");

        BluetoothGattCharacteristic temperatureDataChar = peripheral.getCharacteristic(
            UUID_TEMPERATURE_SERVICE,
            UUID_TEMPERATURE_DATA);
        peripheral.setNotify(temperatureDataChar, true);

      }

    }

    @Override
    public void onCharacteristicUpdate(@NotNull BluetoothPeripheral peripheral,
    byte[] value,
    @NotNull BluetoothGattCharacteristic characteristic,
    @NotNull BluetoothCommandStatus status) {
      final UUID characteristicUUID = characteristic.getUuid();

      // Deal with errors
      if (status != COMMAND_SUCCESS) {
        System.err.println("command failed with status {}", status);
        return;
      }

      if (characteristicUUID.equals((UUID_TEMPERATURE_DATA))) {
        System.out.println("receiving temperature data update");

        var result = calculateTemperature(value);

        if (result.length >= 2) {

          Platform.runLater(() -> {
            irTemperatureString.setValue(String.format("%.2f", result[0]));
            ambientTemperatureString.setValue(String.format("%.2f", result[1]));

          });
        }
      }
    }
}
