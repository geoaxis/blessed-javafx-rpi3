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
  private  HelloFxController helloFxController;

  public static void main(String[] args) {
    Application.launch();
  }

  @Override
  public void init() {
    SpringApplication.run(getClass()).getAutowireCapableBeanFactory().autowireBean(this);
  }

  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(BlessedJavafxRpi3Application.class.getResource("hello-view.fxml"));
    fxmlLoader.setControllerFactory(param -> helloFxController);

    Scene scene = new Scene(fxmlLoader.load(), 800, 480);
    stage.setTitle("Hello!");
    stage.setScene(scene);
    stage.show();
  }

}
