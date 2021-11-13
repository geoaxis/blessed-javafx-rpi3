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

@Slf4j
@AllArgsConstructor
public class CentralManagerCallback extends BluetoothCentralManagerCallback {

  private final ObjectProperty<BLEState> bleStateProperty;
  private final ObjectProperty<BluetoothPeripheral> connectedPeripheral;
  private final ObservableList<String> discoveredDevices;
  private final Map<String, BluetoothPeripheral> peripheralMap;

  @Override
  public void onConnectedPeripheral(BluetoothPeripheral peripheral) {
    log.info("Conndected peripheral {}" + peripheral.getAddress());
    connectedPeripheral.set(peripheral);

    Platform.runLater(() -> bleStateProperty.setValue(BLEState.CONNECTED));
  }

  @Override
  public void onConnectionFailed(BluetoothPeripheral peripheral, BluetoothCommandStatus status) {
    log.error("Failed to connect peripheral {}" + peripheral.getAddress());
    Platform.runLater(() -> bleStateProperty.setValue(BLEState.READY));
  }

  @Override
  public void onDisconnectedPeripheral(BluetoothPeripheral peripheral,
      BluetoothCommandStatus status) {
    log.info("Disconnected peripheral {}" + peripheral.getAddress());
    Platform.runLater(() -> {
      bleStateProperty.setValue(BLEState.READY);
      connectedPeripheral.set(null);
    });
  }

  @Override
  public void onDiscoveredPeripheral(final BluetoothPeripheral peripheral,
      final ScanResult scanResult) {
    log.info("Discovered device" + peripheral.getAddress());
    if (!discoveredDevices.contains(peripheral.getAddress()) && bleStateProperty.getValue()
        .equals(BLEState.SCANNING)) {
      discoveredDevices.add(peripheral.getAddress());
      peripheralMap.put(peripheral.getAddress(), peripheral);
    }
  }

  @Override
  public void onScanFailed(int errorCode) {
    Platform.runLater(() -> bleStateProperty.setValue(BLEState.READY));
    log.error("Scan failed");
  }


}
