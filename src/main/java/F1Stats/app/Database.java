package F1Stats.app;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:f1data.db";

    public void createRaceTableForYear(int year) throws SQLException {
        String tableName = "races_" + year;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (id INTEGER PRIMARY KEY AUTOINCREMENT, raceName TEXT, date TEXT, circuitName TEXT)";
            stmt.executeUpdate(sql);
        }
    }

    public void insertRace(int year, ModelRace race) throws SQLException {
        String tableName = "races_" + year;
        String sql = "INSERT INTO " + tableName + " (raceName, date, circuitName) VALUES(?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, race.getRaceName());
            pstmt.setString(2, race.getDate());
            pstmt.setString(3, race.getCircuitName());
            pstmt.executeUpdate();
        }
    }

    public List<ModelRace> getAllRacesForYear(int year) throws SQLException {
        List<ModelRace> races = new ArrayList<>();
        String tableName = "races_" + year;
        String sql = "SELECT id, raceName, date, circuitName FROM " + tableName;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ModelRace race = new ModelRace();
                race.setId(rs.getInt("id"));
                race.setRaceName(rs.getString("raceName"));
                race.setDate(rs.getString("date"));
                race.setCircuitName(rs.getString("circuitName"));
                races.add(race);
            }
        }
        return races;
    }

    public ModelRace getRaceById(int year, int id) throws SQLException {
        String tableName = "races_" + year;
        String sql = "SELECT id, raceName, date, circuitName FROM " + tableName + " WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                ModelRace race = new ModelRace();
                race.setId(rs.getInt("id"));
                race.setRaceName(rs.getString("raceName"));
                race.setDate(rs.getString("date"));
                race.setCircuitName(rs.getString("circuitName"));
                return race;
            }
        }
        return null;
    }

    public void updateRace(int year, ModelRace race) throws SQLException {
        String tableName = "races_" + year;
        String sql = "UPDATE " + tableName + " SET raceName = ?, date = ?, circuitName = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, race.getRaceName());
            pstmt.setString(2, race.getDate());
            pstmt.setString(3, race.getCircuitName());
            pstmt.setInt(4, race.getId());
            pstmt.executeUpdate();
        }
    }

    public void deleteRace(int year, int id) throws SQLException {
        String tableName = "races_" + year;
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public void deleteRaceTable(int year) throws SQLException {
        String tableName = "races_" + year;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String sql = "DROP TABLE IF EXISTS " + tableName;
            stmt.executeUpdate(sql);
        }
    }

    // Metody do obsługi kierowców
    public void createDriverTableForYear(int year) throws SQLException {
        String tableName = "drivers_" + year;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (driverId TEXT PRIMARY KEY, name TEXT, points INTEGER, wins INTEGER, nationality TEXT)";
            stmt.executeUpdate(sql);
        }
    }

    public void insertDriver(int year, ModelDriver driver) throws SQLException {
        String tableName = "drivers_" + year;
        String sql = "INSERT INTO " + tableName + " (driverId, name, points, wins, nationality) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, driver.getDriverId());
            pstmt.setString(2, driver.getName());
            pstmt.setInt(3, driver.getPoints());
            pstmt.setInt(4, driver.getWins());
            pstmt.setString(5, driver.getNationality());
            pstmt.executeUpdate();
        }
    }

    public List<ModelDriver> getAllDriversForYear(int year) throws SQLException {
        List<ModelDriver> drivers = new ArrayList<>();
        String tableName = "drivers_" + year;
        String sql = "SELECT driverId, name, points, wins, nationality FROM " + tableName;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ModelDriver driver = new ModelDriver();
                driver.setDriverId(rs.getString("driverId"));
                driver.setName(rs.getString("name"));
                driver.setPoints(rs.getInt("points"));
                driver.setWins(rs.getInt("wins"));
                driver.setNationality(rs.getString("nationality"));
                drivers.add(driver);
            }
        }
        return drivers;
    }

    public List<ModelDriver> getDriversByNames(int year, List<String> names) throws SQLException {
        List<ModelDriver> drivers = new ArrayList<>();
        String tableName = "drivers_" + year;
        String sql = "SELECT driverId, name, points, wins, nationality FROM " + tableName + " WHERE name IN (" + String.join(",", names.stream().map(name -> "?").toArray(String[]::new)) + ")";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < names.size(); i++) {
                pstmt.setString(i + 1, names.get(i));
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ModelDriver driver = new ModelDriver();
                driver.setDriverId(rs.getString("driverId"));
                driver.setName(rs.getString("name"));
                driver.setPoints(rs.getInt("points"));
                driver.setWins(rs.getInt("wins"));
                driver.setNationality(rs.getString("nationality"));
                drivers.add(driver);
            }
        }
        return drivers;
    }

    public ModelDriver getDriverById(int year, String driverId) throws SQLException {
        String tableName = "drivers_" + year;
        String sql = "SELECT driverId, name, points, wins, nationality FROM " + tableName + " WHERE driverId = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, driverId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                ModelDriver driver = new ModelDriver();
                driver.setDriverId(rs.getString("driverId"));
                driver.setName(rs.getString("name"));
                driver.setPoints(rs.getInt("points"));
                driver.setWins(rs.getInt("wins"));
                driver.setNationality(rs.getString("nationality"));
                return driver;
            }
        }
        return null;
    }

    public void updateDriver(int year, ModelDriver driver) throws SQLException {
        String tableName = "drivers_" + year;
        String sql = "UPDATE " + tableName + " SET name = ?, points = ?, wins = ?, nationality = ? WHERE driverId = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, driver.getName());
            pstmt.setInt(2, driver.getPoints());
            pstmt.setInt(3, driver.getWins());
            pstmt.setString(4, driver.getNationality());
            pstmt.setString(5, driver.getDriverId());
            pstmt.executeUpdate();
        }
    }

    public void deleteDriver(int year, String driverId) throws SQLException {
        String tableName = "drivers_" + year;
        String sql = "DELETE FROM " + tableName + " WHERE driverId = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, driverId);
            pstmt.executeUpdate();
        }
    }

    public void deleteDriverTable(int year) throws SQLException {
        String tableName = "drivers_" + year;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String sql = "DROP TABLE IF EXISTS " + tableName;
            stmt.executeUpdate(sql);
        }
    }

    public List<String> getAllDriverTables() throws SQLException {
        List<String> tables = new ArrayList<>();
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name LIKE 'drivers_%'";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tables.add(rs.getString("name"));
            }
        }
        return tables;
    }

    public List<String> getAllRaceTables() throws SQLException {
        List<String> tables = new ArrayList<>();
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name LIKE 'races_%'";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tables.add(rs.getString("name"));
            }
        }
        return tables;
    }
}
