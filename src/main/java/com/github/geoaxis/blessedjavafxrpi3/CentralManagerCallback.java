package com.github.geoaxis.blessedjavafxrpi3;

import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothCommandStatus;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.ScanResult;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.NotNull;

public class CentralManagerCallback extends BluetoothCentralManagerCallback {

  private final ObjectProperty<BLEState> bleStateProperty;
  private final ObjectProperty<BluetoothPeripheral> connectedPeripheral;
  private final ObservableList<String> discoveredDevices;
  private final Map<String, BluetoothPeripheral> peripheralMap;

  public CentralManagerCallback(ObjectProperty<BLEState> bleStateProperty,
      ObjectProperty<BluetoothPeripheral> connectedPeripheral,
      ObservableList<String> discoveredDevices,
      Map<String, BluetoothPeripheral> peripheralMap) {
    this.bleStateProperty = bleStateProperty;
    this.connectedPeripheral = connectedPeripheral;
    this.discoveredDevices = discoveredDevices;
    this.peripheralMap = peripheralMap;
  }

  @Override
  public void onConnectedPeripheral(@NotNull BluetoothPeripheral peripheral) {
    System.out.println("Connected peripheral {}" + peripheral.getAddress());
    connectedPeripheral.set(peripheral);

    Platform.runLater(() -> bleStateProperty.setValue(BLEState.CONNECTED));
  }

  @Override
  public void onConnectionFailed(@NotNull BluetoothPeripheral peripheral, @NotNull BluetoothCommandStatus status) {
    System.err.println("Failed to connect peripheral {}" + peripheral.getAddress());
    Platform.runLater(() -> bleStateProperty.setValue(BLEState.READY));
  }

  @Override
  public void onDisconnectedPeripheral(@NotNull BluetoothPeripheral peripheral,
      @NotNull  BluetoothCommandStatus status) {
    System.out.println("Disconnected peripheral {}" + peripheral.getAddress());
    Platform.runLater(() -> {
      bleStateProperty.setValue(BLEState.READY);
      connectedPeripheral.set(null);
    });
  }

  @Override
  public void onDiscoveredPeripheral(@NotNull BluetoothPeripheral peripheral,
      @NotNull ScanResult scanResult) {
    System.out.println("Discovered device" + peripheral.getAddress());
    if (!discoveredDevices.contains(peripheral.getAddress()) && bleStateProperty.getValue()
        .equals(BLEState.SCANNING)) {
      discoveredDevices.add(peripheral.getAddress());
      Platform.runLater(() -> {
        peripheralMap.put(peripheral.getAddress(), peripheral);

      });
    }
  }

  @Override
  public void onScanFailed(int errorCode) {
    Platform.runLater(() -> bleStateProperty.setValue(BLEState.READY));
    System.err.println("Scan failed");
  }


}
