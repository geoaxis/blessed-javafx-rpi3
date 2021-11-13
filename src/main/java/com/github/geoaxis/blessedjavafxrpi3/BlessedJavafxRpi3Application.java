package com.github.geoaxis.blessedjavafxrpi3;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BlessedJavafxRpi3Application extends Application {

  @Autowired
  private SensorTagScannerController sensorTagScannerController;

  public static void main(String[] args) {
    Application.launch();
  }

  @Override
  public void init() {
    SpringApplication.run(getClass()).getAutowireCapableBeanFactory().autowireBean(this);
  }

  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(BlessedJavafxRpi3Application.class.getResource(
        "sensor-tag-scanner-view.fxml"));
    fxmlLoader.setControllerFactory(param -> sensorTagScannerController);

    Scene scene = new Scene(fxmlLoader.load(), 800, 480);
    stage.setTitle("Sensor Tag Viewer!");
    stage.setScene(scene);
    stage.show();
  }

}
