package com.github.geoaxis.blessedjavafxrpi3;

import static com.github.geoaxis.blessedjavafxrpi3.SensorTagConstants.CC_2650_SENSOR_TAG_SCAN_NAME;
import static com.github.geoaxis.blessedjavafxrpi3.SensorTagConstants.SENSOR_TAG_SCAN_NAME;
import static com.welie.blessed.BluetoothCentralManager.SCANOPTION_NO_NULL_NAMES;

import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SensorTagScannerController implements Initializable {

  private static final String ZERO_CELSIUS_STRING = "00.00";
  @FXML
  public Label status;

  @FXML
  public Button scanButton;

  @FXML
  public Button disconnectButton;

  @FXML
  public ProgressIndicator progressIndicator;

  @FXML
  public Label irTemperatureLabel;

  @FXML
  public Label ambientTemperatureLabel;

  @FXML
  public VBox datePane;

  @FXML
  private ListView<String> devices = new ListView<>();

  private final ObjectProperty<BLEState> bleStateProperty = new SimpleObjectProperty<>(BLEState.READY);
  private final ObjectProperty<BluetoothPeripheral> connectedPeripheral = new SimpleObjectProperty<>();

  private final StringProperty irTemperatureProperty = new SimpleStringProperty(ZERO_CELSIUS_STRING);
  private final StringProperty ambientTemperatureProperty = new SimpleStringProperty(
      ZERO_CELSIUS_STRING);

  private final ObservableList<String> discoveredDevices = FXCollections.observableArrayList();
  private final Map<String, BluetoothPeripheral> peripheralMap = new ConcurrentHashMap<>();

  private final PeripheralCallback peripheralCallback = new PeripheralCallback(irTemperatureProperty,
      ambientTemperatureProperty);


  private final BluetoothCentralManager centralManager;

  public SensorTagScannerController() {
    log.info("initializing BluetoothCentral");
    BluetoothCentralManagerCallback managerCallback = new CentralManagerCallback(
        bleStateProperty,
        connectedPeripheral,
        discoveredDevices,
        peripheralMap);
    centralManager = new BluetoothCentralManager(managerCallback, Set.of(SCANOPTION_NO_NULL_NAMES));
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
      bleStateProperty.setValue(BLEState.READY);
      log.info("Stopping Scan");
    });
    new Thread(sleeper).start();
  }

  @FXML
  protected void onDisconnect() {
    clearData();
    bleStateProperty.setValue(BLEState.DISCONNECTING);
    centralManager.cancelConnection(connectedPeripheral.getValue());
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Bindings.bindContent(devices.getItems(), discoveredDevices);
    datePane.visibleProperty().bind(bleStateProperty.isEqualTo(BLEState.CONNECTED));
    scanButton.disableProperty().bind(bleStateProperty.isNotEqualTo(BLEState.READY));
    disconnectButton.disableProperty().bind(bleStateProperty.isNotEqualTo(BLEState.CONNECTED));
    status.textProperty().bind(
        Bindings.createStringBinding(
            () -> bleStateProperty.getValue().text,
            bleStateProperty)
    );
    irTemperatureLabel.textProperty().bind(irTemperatureProperty);
    ambientTemperatureLabel.textProperty().bind(ambientTemperatureProperty);

    devices.disableProperty().bind(bleStateProperty.isNotEqualTo(BLEState.READY));

    BooleanBinding progressBinding =
        bleStateProperty.isEqualTo(BLEState.SCANNING)
            .or(bleStateProperty.isEqualTo(BLEState.CONNECTING))
            .or(bleStateProperty.isEqualTo(BLEState.DISCONNECTING));

    progressIndicator.visibleProperty().bind(progressBinding);

    ChangeListener<? super String> l = (observable, oldValue, newValue) -> {
      if (newValue != null) {
        log.info("Ad address was selected selected , old{} new{}", oldValue, newValue);
        connect(newValue);
      }
    };
    devices.getSelectionModel().selectedItemProperty().addListener(l);

    devices.setOnMouseClicked(touchEvent -> connect(devices.getSelectionModel().getSelectedItem()));
  }

  public void scan() {
    bleStateProperty.setValue(BLEState.SCANNING);
    clearData();
    centralManager.scanForPeripheralsWithNames(new String[]{CC_2650_SENSOR_TAG_SCAN_NAME,
        SENSOR_TAG_SCAN_NAME});
  }

  public void clearData() {
    log.error("Clearing previously found devices");
    discoveredDevices.clear();
    peripheralMap.clear();
    irTemperatureProperty.setValue(ZERO_CELSIUS_STRING);
    ambientTemperatureProperty.setValue(ZERO_CELSIUS_STRING);
  }

  public void connect(String address) {
    if (address != null) {
      centralManager.stopScan();
      bleStateProperty.setValue(BLEState.CONNECTING);

      centralManager.connectPeripheral(peripheralMap.get(address), peripheralCallback);
    } else {
      log.error("Did not really select any thing to connect to");
    }
  }
}
