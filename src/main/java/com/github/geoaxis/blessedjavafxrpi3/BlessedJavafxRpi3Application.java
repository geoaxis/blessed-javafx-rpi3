package com.github.geoaxis.blessedjavafxrpi3;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BlessedJavafxRpi3Application extends Application {


  public static void main(String[] args) {
    launch(args);
  }


  @Override
  public void start(Stage stage) throws IOException {

    Parent root = FXMLLoader.load(getClass().getResource("sensor-tag-scanner-view.fxml"));
    stage.setTitle("Registration Form FXML Application");
    stage.setScene(new Scene(root, 800, 480));
    stage.show();
  }

}
