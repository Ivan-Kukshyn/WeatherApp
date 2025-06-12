package com.weatherapp.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class HistoryEntry {
    private int id;
    private String city;
    private float temperature;
    private String description;
    private int humidity;
    private int pressure;
    private float windSpeed;
    private String windDirection;
    private double probabilityOfPrecipitation;
    private LocalDateTime timestamp;

    public HistoryEntry() {
        this.timestamp = LocalDateTime.now();
    }

    public HistoryEntry(String city, float temperature, String description,
                        int humidity, int pressure, float windSpeed,
                        String windDirection, double probabilityOfPrecipitation,
                        LocalDateTime timestamp) {
        this();
        setCity(city);
        setTemperature(temperature);
        setDescription(description);
        setHumidity(humidity);
        setPressure(pressure);
        setWindSpeed(windSpeed);
        setWindDirection(windDirection);
        setProbabilityOfPrecipitation(probabilityOfPrecipitation);
        setTimestamp(timestamp);
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        if (humidity < 0 || humidity > 100) {
            throw new IllegalArgumentException("Вологість повинна бути в межах від 0 до 100");
        }
        this.humidity = humidity;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public float getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(float windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }

    public double getProbabilityOfPrecipitation() {
        return probabilityOfPrecipitation;
    }

    public void setProbabilityOfPrecipitation(double probability) {
        this.probabilityOfPrecipitation = Math.max(0.0, Math.min(1.0, probability));
    }

    public int getProbabilityPercentage() {
        return (int) Math.round(probabilityOfPrecipitation * 100);
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // Builder class
    public static final class Builder {
        private int id;
        private String city;
        private float temperature;
        private String description;
        private int humidity;
        private int pressure;
        private float windSpeed;
        private String windDirection;
        private double probabilityOfPrecipitation;
        private LocalDateTime timestamp;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder temperature(float temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder humidity(int humidity) {
            this.humidity = humidity;
            return this;
        }

        public Builder pressure(int pressure) {
            this.pressure = pressure;
            return this;
        }

        public Builder windSpeed(float windSpeed) {
            this.windSpeed = windSpeed;
            return this;
        }

        public Builder windDirection(String windDirection) {
            this.windDirection = windDirection;
            return this;
        }

        public Builder probabilityOfPrecipitation(double probability) {
            this.probabilityOfPrecipitation = probability;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public HistoryEntry build() {
            return new HistoryEntry(
                    city, temperature, description,
                    humidity, pressure, windSpeed,
                    windDirection, probabilityOfPrecipitation,
                    timestamp != null ? timestamp : LocalDateTime.now()
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoryEntry that = (HistoryEntry) o;
        return id == that.id &&
                Float.compare(that.temperature, temperature) == 0 &&
                humidity == that.humidity &&
                pressure == that.pressure &&
                Float.compare(that.windSpeed, windSpeed) == 0 &&
                Double.compare(that.probabilityOfPrecipitation, probabilityOfPrecipitation) == 0 &&
                Objects.equals(city, that.city) &&
                Objects.equals(description, that.description) &&
                Objects.equals(windDirection, that.windDirection) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, city, temperature, description, humidity,
                pressure, windSpeed, windDirection, probabilityOfPrecipitation, timestamp);
    }

    @Override
    public String toString() {
        return String.format(
                "HistoryEntry{id=%d, city='%s', temperature=%.1f°C, description='%s', " +
                        "humidity=%d%%, pressure=%dhPa, windSpeed=%.1fm/s, windDirection='%s', " +
                        "probability=%.0f%%, timestamp=%s}",
                id, city, temperature, description, humidity, pressure,
                windSpeed, windDirection, probabilityOfPrecipitation * 100, timestamp
        );
    }
}