package com.weatherapp;

import com.weatherapp.repository.WeatherRepository;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class WeatherApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(WeatherApp.class);
    private static final String APP_TITLE = "Моніторинг погоди";
    private static final String FXML_PATH = "/fxml/main.fxml";
    private static final String ICON_PATH = "/images/icon.png";
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 700;

    @Override
    public void start(Stage primaryStage) {
        try {
            if (!initializeDatabaseConnection()) {
                showErrorAlert();
                return;
            }

            setupPrimaryStage(primaryStage);
        } catch (IOException e) {
            logger.error("Не вдалося ініціалізувати додаток: {}", e.getMessage(), e);
        }
    }

    private boolean initializeDatabaseConnection() {
        try {
            WeatherRepository repository = new WeatherRepository();
            return repository.testConnection();
        } catch (Exception e) {
            logger.error("Не вдалося з'єднатися з базою даних: {}", e.getMessage(), e);
            return false;
        }
    }

    private void setupPrimaryStage(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
        Scene scene = new Scene(loader.load());

        primaryStage.setTitle(APP_TITLE);
        primaryStage.getIcons().add(loadApplicationIcon());
        primaryStage.setScene(scene);
        primaryStage.setWidth(WINDOW_WIDTH);
        primaryStage.setHeight(WINDOW_HEIGHT);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    private Image loadApplicationIcon() {
        return new Image(Objects.requireNonNull(getClass().getResourceAsStream(ICON_PATH)));
    }

    private void showErrorAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Помилка");
        alert.setHeaderText(null);
        alert.setContentText("Помилка підключення до бази даних");
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}