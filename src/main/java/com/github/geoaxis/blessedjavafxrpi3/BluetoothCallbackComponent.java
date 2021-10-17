package com.github.geoaxis.blessedjavafxrpi3;

import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.ScanResult;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BluetoothCallbackComponent extends BluetoothCentralManagerCallback {

  private final ObservableList<String> discoveredDevices = FXCollections.observableArrayList();

  public ObservableList<String> getDiscoveredDevices() {
    return this.discoveredDevices;
  }

  @Override
  public void onDiscoveredPeripheral(final BluetoothPeripheral peripheral,
      final ScanResult scanResult) {
    log.info("Discovered device" + peripheral.getAddress());
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        discoveredDevices.add(peripheral.getAddress());
      }
    });
  }
}
