package com.github.geoaxis.blessedjavafxrpi3;

import static com.github.geoaxis.blessedjavafxrpi3.SensorTagConstants.CC_2650_SENSOR_TAG_SCAN_NAME;
import static com.github.geoaxis.blessedjavafxrpi3.SensorTagConstants.SENSOR_TAG_SCAN_NAME;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.FlowPane;

public class SensorTagScannerController  {

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

  private final ObjectProperty<BLEState> bleStateProperty = new SimpleObjectProperty<>(BLEState.READY);

  private final StringProperty irTemperatureString = new SimpleStringProperty("00.00");
  private final StringProperty ambientTemperatureString = new SimpleStringProperty("00.00");

  private final ObservableList<String> discoveredDevices = FXCollections.observableArrayList();




  public SensorTagScannerController() {
    System.out.println("initializing BluetoothCentral");
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
      System.out.println("Stopping Scan");
    });
    new Thread(sleeper).start();
  }

  @FXML
  protected void onDisconnect() {
    bleStateProperty.setValue(BLEState.DISCONNECTING);
  }

  public void initialize() {
    Bindings.bindContent(devices.getItems(), discoveredDevices);
    scanProgress.visibleProperty().bind(bleStateProperty.isEqualTo(BLEState.SCANNING));
    temperaturePane.visibleProperty().bind(bleStateProperty.isEqualTo(BLEState.CONNECTED));
    scanButton.disableProperty().bind(bleStateProperty.isNotEqualTo(BLEState.READY));
    disconnectButton.disableProperty().bind(bleStateProperty.isNotEqualTo(BLEState.CONNECTED));
    status.textProperty().bind(
        Bindings.createStringBinding(
            () -> bleStateProperty.getValue().text,
            bleStateProperty)
    );
    irTemperatureLabel.textProperty().bind(irTemperatureString);
    ambientTemperatureLabel.textProperty().bind(ambientTemperatureString);
    devices.disableProperty().bind(bleStateProperty.isNotEqualTo(BLEState.READY));

    devices.setOnMouseClicked(touchEvent -> connect(devices.getSelectionModel().getSelectedItem()));
  }

  public void scan() {
    clearDevices();
    discoveredDevices.add("Device");
  }

  public void clearDevices() {
    Platform.runLater(() -> {
      System.err.println("Clearing previously found devices");
      discoveredDevices.clear();
    });
  }

  public void connect(String address) {
    bleStateProperty.setValue(BLEState.CONNECTING);
  }
}
