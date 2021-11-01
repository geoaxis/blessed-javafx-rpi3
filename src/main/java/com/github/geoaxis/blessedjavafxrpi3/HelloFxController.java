package com.github.geoaxis.blessedjavafxrpi3;

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
import java.util.HashSet;
import java.util.List;
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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HelloFxController implements Initializable {

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

  private final ObjectProperty<States> state = new SimpleObjectProperty<>(States.READY);
  private final StringProperty irTemperatureString = new SimpleStringProperty("00.00");
  private final StringProperty ambientTemperatureString = new SimpleStringProperty("00.00");
  private BluetoothPeripheral connectedPeripheral;

  public HelloFxController() {
    log.info("initializing BluetoothCentral");
    discoveredDevices = FXCollections.observableArrayList();

    central = new BluetoothCentralManager(managerCallback,
        Set.of(SCANOPTION_NO_NULL_NAMES));
  }

  @FXML
  protected void onScanButton() {
    scan();

    Task<Void> sleeper = new Task<>() {
      @Override
      protected Void call() throws Exception {
        try {
          Thread.sleep(8000);
        } catch (InterruptedException e) {
        }
        return null;
      }
    };
    sleeper.setOnSucceeded(event -> {
      state.setValue(States.READY);
      log.info("Stopping Scan");
    });
    new Thread(sleeper).start();
  }


  @FXML
  protected void onDisconnect() {
    connectableAddress = "";
    state.setValue(States.DISCONNECTING);
    central.cancelConnection(connectedPeripheral);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Bindings.bindContent(devices.getItems(), discoveredDevices);
    scanProgress.visibleProperty().bind(state.isEqualTo(States.SCANNING));
    temperaturePane.visibleProperty().bind(state.isEqualTo(States.CONNECTED));
    scanButton.disableProperty().bind(state.isNotEqualTo(States.READY));
    disconnectButton.disableProperty().bind(state.isNotEqualTo(States.CONNECTED));
    status.textProperty().bind(
        Bindings.createStringBinding(
            () -> state.getValue().text,
            state)
    );
    irTemperatureLabel.textProperty().bind(irTemperatureString);
    ambientTemperatureLabel.textProperty().bind(ambientTemperatureString);
    devices.disableProperty().bind(state.isNotEqualTo(States.READY));

    devices.setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent touchEvent) {
        connect(devices.getSelectionModel().getSelectedItem());
      }
    });
  }


  private static final float SCALE_LSB = 0.03125f;

  public static final UUID UUID_TEMPERATURE_SERVICE = UUID.fromString(
      "f000aa00-0451-4000-b000-000000000000");
  public static final UUID UUID_TEMPERATURE_DATA = UUID.fromString(
      "f000aa01-0451-4000-b000-000000000000");
  public static final UUID UUID_TEMPERATURE_CONFIG = UUID.fromString(
      "f000aa02-0451-4000-b000-000000000000");
  public static final UUID UUID_TEMPERATURE_PERIOD = UUID.fromString(
      "f000aa03-0451-4000-b000-000000000000");

  private static final byte[] ENABLE = {0x01};

  private BluetoothCentralManager central;

  private final ObservableList<String> discoveredDevices;

  public Set<String> localSet = new HashSet<>();
  private String connectableAddress = "";

  private final BluetoothCentralManagerCallback managerCallback = new BluetoothCentralManagerCallback() {

    @Override
    public void onConnectedPeripheral(BluetoothPeripheral peripheral) {
      log.info("Connected peripheral {}" + peripheral.getAddress());
      connectedPeripheral = peripheral;

      Platform.runLater(() -> state.setValue(States.CONNECTED));
    }

    @Override
    public void onConnectionFailed(BluetoothPeripheral peripheral, BluetoothCommandStatus status) {
      log.error("Failed to connect peripheral {}" + peripheral.getAddress());
      Platform.runLater(() -> state.setValue(States.READY));
    }

    @Override
    public void onDisconnectedPeripheral(BluetoothPeripheral peripheral,
        BluetoothCommandStatus status) {
      log.info("Disconnected peripheral {}" + peripheral.getAddress());
      Platform.runLater(() -> state.setValue(States.READY));
    }

    @Override
    public void onDiscoveredPeripheral(final BluetoothPeripheral peripheral,
        final ScanResult scanResult) {
      log.info("Discovered device" + peripheral.getAddress());
      if (connectableAddress.contains(peripheral.getAddress())) {
        central.stopScan();
        BluetoothPeripheralCallback cb = new BluetoothPeripheralCallback() {
          @Override
          public void onServicesDiscovered(BluetoothPeripheral peripheral,
              List<BluetoothGattService> services) {
            if (peripheral.getService(UUID_TEMPERATURE_SERVICE) != null) {
              peripheral.writeCharacteristic(UUID_TEMPERATURE_SERVICE,
                  UUID_TEMPERATURE_CONFIG,
                  ENABLE,
                  WriteType.WITH_RESPONSE);
              BluetoothGattCharacteristic movementChar = peripheral.getCharacteristic(
                  UUID_TEMPERATURE_SERVICE,
                  UUID_TEMPERATURE_DATA);
              peripheral.setNotify(movementChar, true);

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
              log.info("period done");

              BluetoothGattCharacteristic movementChar = peripheral.getCharacteristic(
                  UUID_TEMPERATURE_SERVICE,
                  UUID_TEMPERATURE_DATA);
              peripheral.setNotify(movementChar, true);

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

            log.info("receiving an update");
            if (characteristicUUID.equals((UUID_TEMPERATURE_DATA))) {
              var result = calculateTemperature(value);

              if (result.length >= 2) {

                Platform.runLater(() -> {
                  irTemperatureString.setValue(String.format("%.2f", result[0]));
                  ambientTemperatureString.setValue(String.format("%.2f", result[1]));

                });
                //}
              }
            }
          }
        };
        central.connectPeripheral(peripheral, cb);
      } else if (!localSet.contains(peripheral.getAddress()) && state.getValue()
          .equals(States.SCANNING)) {
        localSet.add(peripheral.getAddress());
        discoveredDevices.add(peripheral.getAddress());
      }
    }

    public float[] calculateTemperature(byte[] data) {
      float[] temperatures = new float[2];

      int rawObjTemp = unsigned16Bits(data, 0);
      int rawAmbTemp = unsigned16Bits(data, 2);

      temperatures[0] = (rawObjTemp >> 2) * SCALE_LSB;
      temperatures[1] = (rawAmbTemp >> 2) * SCALE_LSB;

      return temperatures;
    }

    private int unsigned16Bits(byte[] data, int offset) {
      int byte0 = data[offset] & 0xff;
      int byte1 = data[offset + 1] & 0xff;

      return (byte1 << 8) + byte0;
    }


    @Override
    public void onScanFailed(int errorCode) {
      Platform.runLater(new Runnable() {
        @Override
        public void run() {
          state.setValue(States.READY);
        }
      });
      log.error("Scan failed");
    }

  };

  public void scan() {
    state.setValue(States.SCANNING);
    clearDevices();
    central.scanForPeripheralsWithNames(new String[]{"CC2650 SensorTag"});
  }

  public void stopScan() {
    state.setValue(States.READY);
    central.stopScan();
  }

  public void clearDevices() {
    Platform.runLater(() -> {
      log.error("Clearing previously found");
      discoveredDevices.clear();
      localSet.clear();
    });
  }

  public void connect(String address) {
    central.stopScan();
    state.setValue(States.CONNECTING);
    connectableAddress = address;
    central.scanForPeripheralsWithAddresses(new String[]{address});
  }
}
