package com.weatherapp.repository;

import com.weatherapp.model.HistoryEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WeatherRepository {
    private static final Logger logger = LoggerFactory.getLogger(WeatherRepository.class);
    private static final String DB_URL = "jdbc:mysql://localhost:3306/weather_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    // SQL Запити
    private static final String INSERT_HISTORY = """
            INSERT INTO weather_history\s
            (city, temperature, description, humidity, pressure, wind_speed, wind_direction, pop, timestamp)\s
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""";

    private static final String SELECT_ALL_HISTORY = "SELECT * FROM weather_history ORDER BY timestamp DESC";
    private static final String DELETE_HISTORY = "DELETE FROM weather_history WHERE id = ?";
    private static final String INSERT_LOG = """
            INSERT INTO weather_logs (timestamp, level, message, logger, exception)\s
            VALUES (?, ?, ?, ?, ?)""";
    private static final String SELECT_ALL_LOGS = "SELECT * FROM weather_logs ORDER BY timestamp DESC";
    private static final String CLEAR_LOGS = "DELETE FROM weather_logs";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public void saveHistory(HistoryEntry entry) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_HISTORY, Statement.RETURN_GENERATED_KEYS)) {

            setHistoryStatementParameters(stmt, entry);
            stmt.executeUpdate();
            setGeneratedId(stmt, entry);

            saveLog("INFO", "Successfully saved history for city: " + entry.getCity(), null);

        } catch (SQLException e) {
            saveLog("ERROR", "Failed to save history for city: " + entry.getCity(), e);
            logger.error("Не вдалося зберегти запис історії: {}", e.getMessage(), e);
            throw new DataAccessException("Не вдалося зберегти запис історії", e);
        }
    }

    private void setHistoryStatementParameters(PreparedStatement stmt, HistoryEntry entry) throws SQLException {
        stmt.setString(1, entry.getCity());
        stmt.setFloat(2, entry.getTemperature());
        stmt.setString(3, entry.getDescription());
        stmt.setInt(4, entry.getHumidity());
        stmt.setInt(5, entry.getPressure());
        stmt.setFloat(6, entry.getWindSpeed());
        stmt.setString(7, entry.getWindDirection());
        stmt.setDouble(8, entry.getProbabilityOfPrecipitation());
        stmt.setTimestamp(9, Timestamp.valueOf(entry.getTimestamp()));
    }

    private void setGeneratedId(PreparedStatement stmt, HistoryEntry entry) throws SQLException {
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                entry.setId(generatedKeys.getInt(1));
            }
        }
    }

    public List<HistoryEntry> getAllHistory() {
        List<HistoryEntry> history = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_HISTORY)) {

            while (rs.next()) {
                history.add(mapResultSetToHistoryEntry(rs));
            }
        } catch (SQLException e) {
            logger.error("Не вдалося завантажити історію: {}", e.getMessage(), e);
            throw new DataAccessException("Не вдалося завантажити історію", e);
        }
        return history;
    }

    private HistoryEntry mapResultSetToHistoryEntry(ResultSet rs) throws SQLException {
        HistoryEntry entry = new HistoryEntry(
                rs.getString("city"),
                rs.getFloat("temperature"),
                rs.getString("description"),
                rs.getInt("humidity"),
                rs.getInt("pressure"),
                rs.getFloat("wind_speed"),
                rs.getString("wind_direction"),
                rs.getDouble("pop"),
                rs.getTimestamp("timestamp").toLocalDateTime()
        );
        entry.setId(rs.getInt("id"));
        return entry;
    }

    public void deleteHistoryEntry(int id) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_HISTORY)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Не вдалося видалити ID запису історії {}: {}", id, e.getMessage(), e);
            throw new DataAccessException("Не вдалося видалити запис історії", e);
        }
    }

    public void saveLog(String level, String message, Throwable throwable) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_LOG)) {

            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setString(2, level);
            stmt.setString(3, message);
            stmt.setString(4, logger.getName());
            stmt.setString(5, throwable != null ? getStackTraceAsString(throwable) : null);
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Не вдалося зберегти запис журналу: {}", e.getMessage(), e);
            throw new DataAccessException("Не вдалося зберегти запис журналу", e);
        }
    }

    private String getStackTraceAsString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    public List<LogEntry> getAllLogs() {
        List<LogEntry> logs = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_LOGS)) {

            while (rs.next()) {
                logs.add(new LogEntry(
                        rs.getInt("id"),
                        rs.getTimestamp("timestamp"),
                        rs.getString("level"),
                        rs.getString("message"),
                        rs.getString("logger"),
                        rs.getString("exception")
                ));
            }
        } catch (SQLException e) {
            logger.error("Не вдалося завантажити журнали: {}", e.getMessage(), e);
            throw new DataAccessException("Не вдалося завантажити журнали", e);
        }
        return logs;
    }

    public void clearLogs() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(CLEAR_LOGS);

        } catch (SQLException e) {
            logger.error("Не вдалося очистити журнали: {}", e.getMessage(), e);
            throw new DataAccessException("Не вдалося очистити журнали", e);
        }
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(2);
        } catch (SQLException e) {
            logger.warn("Тест на підключення до бази даних завершився невдало: {}", e.getMessage());
            return false;
        }
    }

    public record LogEntry(
            int id,
            Timestamp timestamp,
            String level,
            String message,
            String logger,
            String exception
    ) {
    }

    public static class DataAccessException extends RuntimeException {
        public DataAccessException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}