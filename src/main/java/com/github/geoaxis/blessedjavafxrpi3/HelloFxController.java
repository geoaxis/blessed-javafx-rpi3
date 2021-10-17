package com.github.geoaxis.blessedjavafxrpi3;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.springframework.stereotype.Component;

@Component
public class HelloFxController implements Initializable {

  private final SensorTagService sensorTagService;

  @FXML
  private Label welcomeText;

  @FXML
  private Button scan;

  @FXML
  private ListView<String> devices = new ListView<>();


  public HelloFxController(SensorTagService sensorTagService) {
    this.sensorTagService = sensorTagService;
  }

  @FXML
  protected void onScanButton() {
    welcomeText.setText("Scanning");
    sensorTagService.scan();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    devices.setItems(sensorTagService.getDevices());
  }
}
