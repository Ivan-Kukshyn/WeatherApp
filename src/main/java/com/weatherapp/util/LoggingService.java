package com.weatherapp.util;

import com.weatherapp.repository.WeatherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingService {
    private final WeatherRepository weatherRepository;
    private final Logger logger = LoggerFactory.getLogger(LoggingService.class);

    public LoggingService(WeatherRepository weatherRepository) {
        this.weatherRepository = weatherRepository;
    }

    public void logInfo(String message) {
        weatherRepository.saveLog("ІНФО", message, null);
        logger.info(message);
    }

    public void logError(String message, Throwable throwable) {
        weatherRepository.saveLog("ПОМИЛКА", message, throwable);
        logger.error(message, throwable);
    }
}