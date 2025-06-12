package com.weatherapp.controller;

import com.weatherapp.model.HistoryEntry;
import com.weatherapp.model.WeatherData;
import com.weatherapp.model.WeatherForecastResponse;
import com.weatherapp.repository.WeatherRepository;
import com.weatherapp.service.WeatherService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

public class WeatherController {
    private static final Logger logger = LoggerFactory.getLogger(WeatherController.class);
    private static final String[] COMPASS_DIRECTIONS = {
            "Пн ↑", "Пн-Сх ↗", "Сх →", "Пд-Сх ↘",
            "Пд ↓", "Пд-Зх ↙", "Зх ←", "Пн-Зх ↖"
    };
    private static final Locale LOCALE = Locale.forLanguageTag("uk");

    // UI Компоненти
    @FXML
    private TextField cityInput;
    @FXML
    private Label dateLabel, temperatureLabel, descriptionLabel, humidityLabel,
            pressureLabel, windLabel, cityLabel, tempMinLabel,
            tempMaxLabel, windDirectionLabel, popLabel, labelTemp, labelTempMin,
            labelTempMax, labelDescription, labelHumidity, labelPressure, labelWind,
            labelWindDir, labelPop, labelWriteTheCity, weatherFactText;
    @FXML
    private HBox forecastContainer;
    @FXML
    private ImageView weatherFactImage;

    @FXML
    private VBox factSectionContainer;
    @FXML
    private VBox weatherSectionContainer;

    // Сервіси
    private final WeatherRepository weatherRepository = new WeatherRepository();
    private final WeatherService weatherService = new WeatherService();

    private final List<String> facts = List.of(
            "Урагани можуть мати швидкість вітру понад 250 км/год!",
            "Блискавка нагріває повітря до 30 000°C.",
            "Найнижча температура на Землі — -89.2°C (Антарктида).",
            "Найспекотніше місто на Землі – Кувейт-Сіті. Температура тут може підніматися до 54 градусів Цельсія."
    );

    @FXML
    private void initialize() {
        labelWriteTheCity.setVisible(false);
        labelWriteTheCity.setManaged(false);

        factSectionContainer.setVisible(true);
        factSectionContainer.setManaged(true);

        weatherSectionContainer.setVisible(false);
        weatherSectionContainer.setManaged(false);

        resetUIState();

        Platform.runLater(() -> labelWriteTheCity.requestFocus());

        String randomFact = facts.get(new Random().nextInt(facts.size()));
        weatherFactText.getStyleClass().add("weather-fact-text");
        weatherFactText.setText(randomFact);
        weatherFactImage.setImage(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icon.png")))
        );
    }

    private void resetUIState() {
        setWeatherElementsVisible(false);
        labelWriteTheCity.setVisible(true);
    }

    @FXML
    private void onUpdateClicked() {
        String city = cityInput.getText().trim();

        weatherSectionContainer.setVisible(true);
        weatherSectionContainer.setManaged(true);

        factSectionContainer.setVisible(false);
        factSectionContainer.setManaged(false);

        if (city.isEmpty()) {
            displayError("Введіть назву міста.");
            factSectionContainer.setVisible(false);
            factSectionContainer.setManaged(false);

            weatherSectionContainer.setVisible(false);
            weatherSectionContainer.setManaged(false);

            return;
        }

        try {
            fetchAndDisplayWeatherData(city);
        } catch (WeatherService.WeatherApiException e) {
            handleWeatherApiError(city, e);
        } catch (Exception e) {
            handleUnexpectedError(city, e);
        }
    }

    private void fetchAndDisplayWeatherData(String city) throws WeatherService.WeatherApiException {
        WeatherData currentWeather = weatherService.getWeatherByCity(city);
        validateWeatherData(currentWeather);

        WeatherForecastResponse forecast = weatherService.getWeatherForecast(city);
        HistoryEntry entry = createHistoryEntry(currentWeather, forecast);

        weatherRepository.saveHistory(entry);

        updateWeatherUI(currentWeather, forecast);
    }

    private void validateWeatherData(WeatherData data) throws WeatherService.WeatherApiException {
        if (data.getName().isEmpty()) {
            throw new WeatherService.WeatherApiException("Отримано невірні погодні дані");
        }
    }

    private void updateWeatherUI(WeatherData weather, WeatherForecastResponse forecast) {
        updateCurrentWeatherDisplay(weather, forecast);
        updateForecastDisplay(forecast);
        setWeatherElementsVisible(true);
        labelWriteTheCity.setVisible(false);
    }

    private void handleWeatherApiError(String city, WeatherService.WeatherApiException e) {
        logger.error("Помилка API погоди для міста {}: {}", city, e.getMessage());
        clearWeatherDisplay();
        displayError(e instanceof WeatherService.CityNotFoundException ?
                "Місто не знайдено." : "Помилка при отриманні даних.");
        weatherSectionContainer.setVisible(false);
        weatherSectionContainer.setManaged(false);
    }

    private void handleUnexpectedError(String city, Exception e) {
        logger.error("Неочікувана помилка для міста {}: {}", city, e.getMessage(), e);
        clearWeatherDisplay();
        displayError("Сталася неочікувана помилка. Спробуйте ще раз.");
    }

    // Методи оновлення UI
    private void updateCurrentWeatherDisplay(WeatherData data, WeatherForecastResponse forecast) {
        cityLabel.setText(formatLocation(data.getName(), data.getSys().getCountry()));
        dateLabel.setText(formatDate(System.currentTimeMillis() / 1000));

        temperatureLabel.setText(formatTemperature(data.getMain().getTemp()));
        tempMinLabel.setText(formatTemperature(data.getMain().getTempMin()));
        tempMaxLabel.setText(formatTemperature(data.getMain().getTempMax()));

        descriptionLabel.setText(data.getWeather().getFirst().getDescription());
        humidityLabel.setText(formatPercentage(data.getMain().getHumidity()));
        pressureLabel.setText(formatPressure(data.getMain().getPressure()));
        windLabel.setText(formatWindSpeed(data.getWind().getSpeed()));
        windDirectionLabel.setText(getCompassDirection(data.getWind().getDeg()));
        popLabel.setText(formatPopProbability(getPopProbability(forecast)));
    }

    private double getPopProbability(WeatherForecastResponse forecast) {
        return (forecast != null && !forecast.getList().isEmpty()) ?
                forecast.getList().getFirst().getProbabilityOfPrecipitation() * 100 : 0;
    }

    private void updateForecastDisplay(WeatherForecastResponse forecast) {
        forecastContainer.getChildren().clear();

        if (forecast == null || forecast.getList() == null) return;

        forecast.getList().stream()
                .filter(data -> data.getWeather() != null && !data.getWeather().isEmpty())
                .limit(8)
                .map(this::createForecastBlock)
                .forEach(forecastContainer.getChildren()::add);
    }

    private VBox createForecastBlock(WeatherData data) {
        String time = formatTime(data.getDt());
        String temp = formatTemperature(data.getMain().getTemp());
        String iconUrl = formatIconUrl(data.getWeather().getFirst().getIcon());

        return createForecastVBox(time, iconUrl, temp);
    }

    // Допоміжні методи
    private HistoryEntry createHistoryEntry(WeatherData data, WeatherForecastResponse forecast) {
        double probability = 0.0;
        if (forecast != null && !forecast.getList().isEmpty()) {
            probability = Math.max(0.0, Math.min(1.0, forecast.getList().getFirst().getProbabilityOfPrecipitation()));
        }

        return HistoryEntry.builder()
                .city(data.getName())
                .temperature(data.getMain().getTemp())
                .description(data.getWeather().getFirst().getDescription())
                .humidity(data.getMain().getHumidity())
                .pressure(data.getMain().getPressure())
                .windSpeed(data.getWind().getSpeed())
                .windDirection(getCompassDirection(data.getWind().getDeg()))
                .probabilityOfPrecipitation(probability)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private VBox createForecastVBox(String time, String iconUrl, String temp) {
        VBox block = new VBox(7);
        block.getStyleClass().add("forecast-block");

        ImageView iconImage = new ImageView(iconUrl);
        iconImage.setFitWidth(60);
        iconImage.setFitHeight(60);

        block.getChildren().addAll(
                new Label(time) {{
                    getStyleClass().add("forecast-time");
                }},
                iconImage,
                new Label(temp) {{
                    getStyleClass().add("forecast-temp");
                }}
        );
        return block;
    }

    // Методи форматування
    private String formatLocation(String city, String country) {
        return String.format("%s, %s", city, country);
    }

    private String formatTemperature(double temp) {
        return String.format("%d°C", Math.round(temp));
    }

    private String formatPercentage(int value) {
        return String.format("%d %%", value);
    }

    private String formatPressure(int pressure) {
        return String.format("%d гПа", pressure);
    }

    private String formatWindSpeed(double speed) {
        return String.format("%.1f м/с", speed);
    }

    private String formatPopProbability(double pop) {
        return String.format("%d%%", Math.round(pop));
    }

    private String formatTime(long unixTime) {
        return new SimpleDateFormat("HH:mm").format(new Date(unixTime * 1000));
    }

    private String formatIconUrl(String iconCode) {
        return String.format("https://openweathermap.org/img/wn/%s@2x.png", iconCode);
    }

    private String getCompassDirection(int degrees) {
        return COMPASS_DIRECTIONS[(int) Math.round(((double) degrees % 360) / 45) % 8];
    }

    private String formatDate(long unixTime) {
        return new SimpleDateFormat("EEEE, dd MMMM", LOCALE).format(new Date(unixTime * 1000));
    }

    // Методи керування UI
    private void setWeatherElementsVisible(boolean visible) {
        forecastContainer.setVisible(visible);
        forecastContainer.setManaged(visible);

        Arrays.asList(
                temperatureLabel, tempMinLabel, tempMaxLabel,
                descriptionLabel, humidityLabel, pressureLabel,
                windLabel, windDirectionLabel, labelTemp, labelTempMin,
                labelTempMax, labelDescription, labelHumidity,
                labelPressure, labelWind, labelWindDir, labelPop
        ).forEach(label -> {
            label.setVisible(visible);
            label.setManaged(visible);
        });
    }

    private void clearWeatherDisplay() {
        Arrays.asList(
                cityLabel, dateLabel, temperatureLabel, tempMinLabel,
                tempMaxLabel, descriptionLabel, humidityLabel, pressureLabel,
                windLabel, windDirectionLabel, popLabel
        ).forEach(label -> label.setText(""));

        forecastContainer.getChildren().clear();
    }

    private void displayError(String message) {
        clearWeatherDisplay();
        labelWriteTheCity.setText(message);
        labelWriteTheCity.setVisible(true);
        labelWriteTheCity.setManaged(true);
        setWeatherElementsVisible(false);
    }

    @FXML
    private void onHistoryClicked() {
        Stage historyStage = createHistoryStage();
        historyStage.show();
    }

    private Stage createHistoryStage() {
        Stage stage = new Stage();
        stage.setTitle("Історія запитів");

        TableView<HistoryEntry> table = createHistoryTableView();
        VBox vbox = new VBox(table);
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);

        stage.setScene(new Scene(vbox, 1000, 450));
        return stage;
    }

    private TableView<HistoryEntry> createHistoryTableView() {
        TableView<HistoryEntry> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().addAll(createTableColumns());
        table.getItems().addAll(weatherRepository.getAllHistory());

        TableColumn<HistoryEntry, Void> deleteColumn = new TableColumn<>("Дія");
        deleteColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deleteButton = new Button("Видалити");

            {
                deleteButton.setOnAction(e -> {
                    HistoryEntry entry = getTableView().getItems().get(getIndex());
                    if (entry != null) {
                        weatherRepository.deleteHistoryEntry(entry.getId());
                        getTableView().getItems().remove(entry);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });
        table.getColumns().add(deleteColumn);

        return table;
    }

    private List<TableColumn<HistoryEntry, String>> createTableColumns() {
        return Arrays.asList(
                createColumn("Місто", HistoryEntry::getCity),
                createColumn("Опис", HistoryEntry::getDescription),
                createColumn("Температура", e -> formatTemperature(e.getTemperature())),
                createColumn("Вологість", e -> formatPercentage(e.getHumidity())),
                createColumn("Тиск", e -> formatPressure(e.getPressure())),
                createColumn("Вітер", e -> formatWindSpeed(e.getWindSpeed())),
                createColumn("Напрямок вітру", HistoryEntry::getWindDirection),
                createColumn("Ймовірність опадів", e -> formatPopProbability(e.getProbabilityOfPrecipitation() * 100)),
                createColumn("Час", e -> e.getTimestamp().toString().replace("T", " "))
        );
    }

    private TableColumn<HistoryEntry, String> createColumn(String title, Function<HistoryEntry, String> mapper) {
        TableColumn<HistoryEntry, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new SimpleStringProperty(mapper.apply(data.getValue())));
        return column;
    }
}