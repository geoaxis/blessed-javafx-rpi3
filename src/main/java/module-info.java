module com.github.geoaxis.blessedjavafxrpi3 {
  requires javafx.controls;
  requires javafx.fxml;

  requires spring.boot.autoconfigure;
  requires spring.boot;
  requires spring.context;
  requires spring.beans;
  requires spring.core;
  requires org.slf4j;

  requires java.annotation;

  opens com.github.geoaxis.blessedjavafxrpi3 to javafx.fxml,spring.core;

  exports com.github.geoaxis.blessedjavafxrpi3;
}