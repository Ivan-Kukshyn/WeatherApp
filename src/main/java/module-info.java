module com.weatherapp.weatherapp {
    requires java.desktop;

    requires com.google.gson;
    requires okhttp3;
    requires java.sql;
    requires org.slf4j;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;

    opens com.weatherapp to javafx.fxml;
    opens com.weatherapp.controller to javafx.fxml;
    opens com.weatherapp.model to com.google.gson;

    exports com.weatherapp;
    exports com.weatherapp.controller;
    exports com.weatherapp.model;
}