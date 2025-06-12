package com.weatherapp.model;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class WeatherData {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM", Locale.getDefault());

    private Main main;
    private List<Weather> weather;
    private Wind wind;
    private long dt;
    private String name;
    private Sys sys;

    @SerializedName("pop")
    private double probabilityOfPrecipitation;

    // Getters and setters
    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public void setWeather(List<Weather> weather) {
        this.weather = weather;
    }

    public Wind getWind() {
        return wind;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

    public long getDt() {
        return dt;
    }

    public void setDt(long dt) {
        this.dt = dt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Sys getSys() {
        return sys;
    }

    public void setSys(Sys sys) {
        this.sys = sys;
    }

    public double getProbabilityOfPrecipitation() {
        return probabilityOfPrecipitation;
    }

    public void setProbabilityOfPrecipitation(double probabilityOfPrecipitation) {
        this.probabilityOfPrecipitation = probabilityOfPrecipitation;
    }

    public String getFormattedDate() {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(dt), ZoneId.systemDefault())
                .format(DATE_FORMATTER);
    }

    public static class Main {
        private float temp;
        @SerializedName("temp_min")
        private float tempMin;
        @SerializedName("temp_max")
        private float tempMax;
        private int pressure;
        private int humidity;

        // Getters and setters
        public float getTemp() {
            return temp;
        }

        public void setTemp(float temp) {
            this.temp = temp;
        }

        public float getTempMin() {
            return tempMin;
        }

        public void setTempMin(float tempMin) {
            this.tempMin = tempMin;
        }

        public float getTempMax() {
            return tempMax;
        }

        public void setTempMax(float tempMax) {
            this.tempMax = tempMax;
        }

        public int getPressure() {
            return pressure;
        }

        public void setPressure(int pressure) {
            this.pressure = pressure;
        }

        public int getHumidity() {
            return humidity;
        }

        public void setHumidity(int humidity) {
            this.humidity = humidity;
        }
    }

    public static class Weather {
        private String description;
        private String icon;

        // Getters and setters
        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }
    }

    public static class Wind {
        private float speed;
        private int deg;

        // Getters and setters
        public float getSpeed() {
            return speed;
        }

        public void setSpeed(float speed) {
            this.speed = speed;
        }

        public int getDeg() {
            return deg;
        }

        public void setDeg(int deg) {
            this.deg = deg;
        }
    }

    public static class Sys {
        private String country;

        // Getters and setters
        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }
    }
}