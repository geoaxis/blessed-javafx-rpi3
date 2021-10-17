package com.github.geoaxis.blessedjavafxrpi3;

import static com.welie.blessed.BluetoothCentralManager.SCANOPTION_NO_NULL_NAMES;

import com.welie.blessed.BluetoothCentralManager;
import java.util.Set;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SensorTagService {

  private final BluetoothCentralManager central;

  private final BluetoothCallbackComponent bluetoothCallbackComponent;

  SensorTagService(BluetoothCallbackComponent bluetoothCallbackComponent) {

    log.info("initializing BluetoothCentral");
    this.bluetoothCallbackComponent = bluetoothCallbackComponent;
    central = new BluetoothCentralManager(bluetoothCallbackComponent,
        Set.of(SCANOPTION_NO_NULL_NAMES));
  }

  public void scan() {
    central.scanForPeripherals();
  }

  public ObservableList<String> getDevices() {
    return bluetoothCallbackComponent.getDiscoveredDevices();
  }
}
