package com.weatherapp.model;

import java.util.List;

// Клас для обробки прогнозу на кілька днів
public class WeatherForecastResponse {
    private List<WeatherData> list; // Список прогнозів

    public List<WeatherData> getList() {
        return list;
    }

    public void setList(List<WeatherData> list) {
        this.list = list;
    }
}
