package com.github.geoaxis.blessedjavafxrpi3;

import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothCommandStatus;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.ScanResult;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
@AllArgsConstructor
public class CentralManagerCallback extends BluetoothCentralManagerCallback {

  private final ObjectProperty<BLEState> bleStateProperty;
  private final ObjectProperty<BluetoothPeripheral> connectedPeripheral;
  private final ObservableList<String> discoveredDevices;
  private final Map<String, BluetoothPeripheral> peripheralMap;

  @Override
  public void onConnectedPeripheral(@NotNull BluetoothPeripheral peripheral) {
    log.info("Connected peripheral {}" + peripheral.getAddress());
    connectedPeripheral.set(peripheral);

    Platform.runLater(() -> bleStateProperty.setValue(BLEState.CONNECTED));
  }

  @Override
  public void onConnectionFailed(@NotNull BluetoothPeripheral peripheral, @NotNull BluetoothCommandStatus status) {
    log.error("Failed to connect peripheral {}" + peripheral.getAddress());
    Platform.runLater(() -> bleStateProperty.setValue(BLEState.READY));
  }

  @Override
  public void onDisconnectedPeripheral(@NotNull BluetoothPeripheral peripheral,
      @NotNull  BluetoothCommandStatus status) {
    log.info("Disconnected peripheral {}" + peripheral.getAddress());
    Platform.runLater(() -> {
      bleStateProperty.setValue(BLEState.READY);
      connectedPeripheral.set(null);
    });
  }

  @Override
  public void onDiscoveredPeripheral(@NotNull BluetoothPeripheral peripheral,
      @NotNull ScanResult scanResult) {
    log.info("Discovered device" + peripheral.getAddress());
    if (!discoveredDevices.contains(peripheral.getAddress()) && bleStateProperty.getValue()
        .equals(BLEState.SCANNING)) {
      Platform.runLater(() -> {
        discoveredDevices.add(peripheral.getAddress());
        peripheralMap.put(peripheral.getAddress(), peripheral);

      });
    }
  }

  @Override
  public void onScanFailed(int errorCode) {
    Platform.runLater(() -> bleStateProperty.setValue(BLEState.READY));
    log.error("Scan failed");
  }


}
