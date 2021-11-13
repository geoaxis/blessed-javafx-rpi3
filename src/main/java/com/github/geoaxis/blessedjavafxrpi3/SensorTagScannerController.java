package com.github.geoaxis.blessedjavafxrpi3;

import static com.github.geoaxis.blessedjavafxrpi3.SensorTagConstants.CC_2650_SENSOR_TAG_SCAN_NAME;
import static com.github.geoaxis.blessedjavafxrpi3.SensorTagConstants.ENABLE_COMMAND;
import static com.github.geoaxis.blessedjavafxrpi3.SensorTagConstants.UUID_TEMPERATURE_CONFIG;
import static com.github.geoaxis.blessedjavafxrpi3.SensorTagConstants.UUID_TEMPERATURE_DATA;
import static com.github.geoaxis.blessedjavafxrpi3.SensorTagConstants.UUID_TEMPERATURE_SERVICE;
import static com.github.geoaxis.blessedjavafxrpi3.CC2650DataProcessor.calculateTemperature;
import static com.welie.blessed.BluetoothCentralManager.SCANOPTION_NO_NULL_NAMES;
import static com.welie.blessed.BluetoothCommandStatus.COMMAND_SUCCESS;

import com.welie.blessed.BluetoothBytesParser;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothCommandStatus;
import com.welie.blessed.BluetoothGattCharacteristic;
import com.welie.blessed.BluetoothGattCharacteristic.WriteType;
import com.welie.blessed.BluetoothGattService;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;
import com.welie.blessed.ScanResult;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.FlowPane;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SensorTagScannerController implements Initializable {

  @FXML
  public Label status;

  @FXML
  public Button scanButton;

  @FXML
  public Button disconnectButton;

  @FXML
  public ProgressIndicator scanProgress;

  @FXML
  public Label irTemperatureLabel;

  @FXML
  public Label ambientTemperatureLabel;

  @FXML
  public FlowPane temperaturePane;

  @FXML
  private ListView<String> devices = new ListView<>();

  private final ObjectProperty<BLEState> state = new SimpleObjectProperty<>(BLEState.READY);

  private final StringProperty irTemperatureString = new SimpleStringProperty("00.00");
  private final StringProperty ambientTemperatureString = new SimpleStringProperty("00.00");

  private final ObservableList<String> discoveredDevices = FXCollections.observableArrayList();
  private final Map<String, BluetoothPeripheral> peripheralMap = new HashMap<>();

  private BluetoothPeripheral connectedPeripheral;

  private final BluetoothCentralManager centralManager;

  public SensorTagScannerController() {
    log.info("initializing BluetoothCentral");
    centralManager = new BluetoothCentralManager(managerCallback,
        Set.of(SCANOPTION_NO_NULL_NAMES));
  }

  @FXML
  protected void onScanButton() {
    scan();

    //special case to handle bluez limitation
    // see https://github.com/weliem/blessed-bluez/wiki/Bluez-Bugs
    Task<Void> sleeper = new Task<>() {
      @Override
      protected Void call() throws InterruptedException {
        Thread.sleep(8000);
        return null;
      }
    };
    sleeper.setOnSucceeded(event -> {
      state.setValue(BLEState.READY);
      log.info("Stopping Scan");
    });
    new Thread(sleeper).start();
  }


  @FXML
  protected void onDisconnect() {
    state.setValue(BLEState.DISCONNECTING);
    centralManager.cancelConnection(connectedPeripheral);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Bindings.bindContent(devices.getItems(), discoveredDevices);
    scanProgress.visibleProperty().bind(state.isEqualTo(BLEState.SCANNING));
    temperaturePane.visibleProperty().bind(state.isEqualTo(BLEState.CONNECTED));
    scanButton.disableProperty().bind(state.isNotEqualTo(BLEState.READY));
    disconnectButton.disableProperty().bind(state.isNotEqualTo(BLEState.CONNECTED));
    status.textProperty().bind(
        Bindings.createStringBinding(
            () -> state.getValue().text,
            state)
    );
    irTemperatureLabel.textProperty().bind(irTemperatureString);
    ambientTemperatureLabel.textProperty().bind(ambientTemperatureString);
    devices.disableProperty().bind(state.isNotEqualTo(BLEState.READY));

    devices.setOnMouseClicked(touchEvent -> connect(devices.getSelectionModel().getSelectedItem()));
  }

  private final BluetoothCentralManagerCallback managerCallback = new BluetoothCentralManagerCallback() {

    @Override
    public void onConnectedPeripheral(BluetoothPeripheral peripheral) {
      log.info("Connected peripheral {}" + peripheral.getAddress());
      connectedPeripheral = peripheral;

      Platform.runLater(() -> state.setValue(BLEState.CONNECTED));
    }

    @Override
    public void onConnectionFailed(BluetoothPeripheral peripheral, BluetoothCommandStatus status) {
      log.error("Failed to connect peripheral {}" + peripheral.getAddress());
      Platform.runLater(() -> state.setValue(BLEState.READY));
    }

    @Override
    public void onDisconnectedPeripheral(BluetoothPeripheral peripheral,
        BluetoothCommandStatus status) {
      log.info("Disconnected peripheral {}" + peripheral.getAddress());
      Platform.runLater(() -> state.setValue(BLEState.READY));
    }

    @Override
    public void onDiscoveredPeripheral(final BluetoothPeripheral peripheral,
        final ScanResult scanResult) {
      log.info("Discovered device" + peripheral.getAddress());
      if (!discoveredDevices.contains(peripheral.getAddress()) && state.getValue()
          .equals(BLEState.SCANNING)) {
        discoveredDevices.add(peripheral.getAddress());
        peripheralMap.put(peripheral.getAddress(), peripheral);
      }
    }

    @Override
    public void onScanFailed(int errorCode) {
      Platform.runLater(() -> state.setValue(BLEState.READY));
      log.error("Scan failed");
    }

  };

  public void scan() {
    state.setValue(BLEState.SCANNING);
    clearDevices();
    centralManager.scanForPeripheralsWithNames(new String[]{CC_2650_SENSOR_TAG_SCAN_NAME});
  }

  public void stopScan() {
    state.setValue(BLEState.READY);
    centralManager.stopScan();
  }

  public void clearDevices() {
    Platform.runLater(() -> {
      log.error("Clearing previously found devices");
      discoveredDevices.clear();
      peripheralMap.clear();
    });
  }

  public void connect(String address) {
    centralManager.stopScan();
    state.setValue(BLEState.CONNECTING);

    BluetoothPeripheralCallback cb = new BluetoothPeripheralCallback() {
      @Override
      public void onServicesDiscovered(BluetoothPeripheral peripheral,
        List<BluetoothGattService> services) {
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
      public void onCharacteristicWrite(BluetoothPeripheral peripheral,
      byte[] value,
      BluetoothGattCharacteristic characteristicUUID,
      BluetoothCommandStatus status) {

      // Deal with errors
      if (status != COMMAND_SUCCESS) {
        log.error("command failed with status {}", status);
        return;
      }

      if (characteristicUUID.equals(UUID_TEMPERATURE_CONFIG)) {
        log.info("temperature notifications configured");

        BluetoothGattCharacteristic temperatureDataChar = peripheral.getCharacteristic(
            UUID_TEMPERATURE_SERVICE,
            UUID_TEMPERATURE_DATA);
        peripheral.setNotify(temperatureDataChar, true);

      }

    }

      @Override
      public void onCharacteristicUpdate(BluetoothPeripheral peripheral,
      byte[] value,
      BluetoothGattCharacteristic characteristic,
      BluetoothCommandStatus status) {
      final UUID characteristicUUID = characteristic.getUuid();
      final BluetoothBytesParser parser = new BluetoothBytesParser(value);

      // Deal with errors
      if (status != COMMAND_SUCCESS) {
        log.error("command failed with status {}", status);
        return;
      }

      if (characteristicUUID.equals((UUID_TEMPERATURE_DATA))) {
        log.info("receiving temperature data update");

        var result = calculateTemperature(value);

        if (result.length >= 2) {

          Platform.runLater(() -> {
            irTemperatureString.setValue(String.format("%.2f", result[0]));
            ambientTemperatureString.setValue(String.format("%.2f", result[1]));

          });
        }
      }
    }
    };

    centralManager.connectPeripheral(peripheralMap.get(address),cb);}
}
