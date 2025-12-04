package app.repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

import app.config.DatabaseConnection;
import app.model.Mood;

public class MoodRepository {

    private static final Logger LOGGER = Logger.getLogger(MoodRepository.class.getName());
    private Connection conn;

    public MoodRepository() {
        conn = DatabaseConnection.getInstance().getConnection();
    }

    public Mood getMoodByDate(LocalDate date) {
        String sql = "SELECT id, mood_value, date FROM mood WHERE date = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) { 
                if (rs.next()) {
                    return new Mood(rs.getInt("id"), rs.getInt("mood_value"), rs.getString("date"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting mood by date", e);
        }
        return null;
    }

    public boolean upsertMood(int moodValue, LocalDate date) {
        String deleteSql = "DELETE FROM mood WHERE date = ?";
        String insertSql = "INSERT INTO mood (mood_value, date) VALUES (?, ?)";

        try {
            try (PreparedStatement delStmt = conn.prepareStatement(deleteSql)) {
                delStmt.setDate(1, Date.valueOf(date));
                delStmt.executeUpdate();
            }

            if (moodValue > 0) {
                try (PreparedStatement insStmt = conn.prepareStatement(insertSql)) {
                    insStmt.setInt(1, moodValue);
                    insStmt.setDate(2, Date.valueOf(date));
                    insStmt.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) { 
            LOGGER.log(Level.SEVERE, "Error upserting mood", e);
            return false;
        }
    }
}