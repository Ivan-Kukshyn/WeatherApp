package com.weatherapp.service;

import com.weatherapp.model.WeatherData;
import com.weatherapp.model.WeatherForecastResponse;
import com.google.gson.Gson;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class WeatherService {
    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);
    private static final String API_KEY = "e3b9680f2a4460ef68704d96f8c4426e";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast";
    private static final String METRIC_UNITS = "metric";
    private static final String LANGUAGE = "uk";
    private static final int FORECAST_ITEMS = 7;

    private final OkHttpClient client;
    private final Gson gson;

    public WeatherService() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    public WeatherData getWeatherByCity(String cityName) throws WeatherApiException {
        Request request = buildWeatherRequest(cityName);

        try (Response response = client.newCall(request).execute()) {
            validateResponse(response);
            return parseWeatherResponse(response);
        } catch (IOException e) {
            logger.error("Не вдалося отримати погоду для міста: {}", cityName, e);
            throw new WeatherApiException("Не вдалося отримати дані про погоду", e);
        }
    }

    public WeatherForecastResponse getWeatherForecast(String cityName) throws WeatherApiException {
        Request request = buildForecastRequest(cityName);

        try (Response response = client.newCall(request).execute()) {
            validateResponse(response);
            return parseForecastResponse(response);
        } catch (IOException e) {
            logger.error("Не вдалося отримати прогноз для міста: {}", cityName, e);
            throw new WeatherApiException("Не вдалося отримати дані прогнозу", e);
        }
    }

    private Request buildWeatherRequest(String cityName) {
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(BASE_URL)).newBuilder()
                .addQueryParameter("q", cityName)
                .addQueryParameter("appid", API_KEY)
                .addQueryParameter("units", METRIC_UNITS)
                .addQueryParameter("lang", LANGUAGE)
                .build();

        return new Request.Builder()
                .url(url)
                .get()
                .build();
    }

    private Request buildForecastRequest(String cityName) {
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(FORECAST_URL)).newBuilder()
                .addQueryParameter("q", cityName)
                .addQueryParameter("appid", API_KEY)
                .addQueryParameter("units", METRIC_UNITS)
                .addQueryParameter("cnt", String.valueOf(FORECAST_ITEMS))
                .build();

        return new Request.Builder()
                .url(url)
                .get()
                .build();
    }

    private void validateResponse(Response response) throws WeatherApiException {
        if (!response.isSuccessful()) {
            if (response.code() == 404) {
                throw new CityNotFoundException("Місто не знайдено");
            }
            throw new WeatherApiException("Запит до API завершився невдачею з кодом: " + response.code());
        }

        if (response.body() == null) {
            throw new WeatherApiException("Порожня відповідь від сервера");
        }
    }

    private WeatherData parseWeatherResponse(Response response) throws IOException {
        String responseBody = response.body().string();
        return gson.fromJson(responseBody, WeatherData.class);
    }

    private WeatherForecastResponse parseForecastResponse(Response response) throws IOException {
        String responseBody = response.body().string();
        return gson.fromJson(responseBody, WeatherForecastResponse.class);
    }

    public static class WeatherApiException extends Exception {
        public WeatherApiException(String message) {
            super(message);
        }

        public WeatherApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class CityNotFoundException extends WeatherApiException {
        public CityNotFoundException(String message) {
            super(message);
        }
    }
}