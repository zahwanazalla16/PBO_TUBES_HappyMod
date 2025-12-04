package app.repository;

import java.sql.*;
import java.time.LocalDate;
import app.config.DatabaseConnection;
import app.model.Mood;

public class MoodRepository {

    private Connection conn;

    public MoodRepository() {
        conn = DatabaseConnection.getInstance().getConnection();
    }

    public Mood getMoodByDate(LocalDate date) {
        String sql = "SELECT * FROM mood WHERE date = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Mood(rs.getInt("id"), rs.getInt("mood_value"), rs.getString("date"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean upsertMood(int moodValue, LocalDate date) {
        // Logic: Hapus dulu yang lama di tanggal itu (kalau ada), lalu insert baru
        // Ini menangani kasus Insert maupun Update sekaligus
        String deleteSql = "DELETE FROM mood WHERE date = ?";
        String insertSql = "INSERT INTO mood (mood_value, date) VALUES (?, ?)";

        try {
            // 1. Hapus data lama
            PreparedStatement delStmt = conn.prepareStatement(deleteSql);
            delStmt.setDate(1, Date.valueOf(date));
            delStmt.executeUpdate();

            // 2. Insert baru (hanya jika value valid > 0)
            if (moodValue > 0) {
                PreparedStatement insStmt = conn.prepareStatement(insertSql);
                insStmt.setInt(1, moodValue);
                insStmt.setDate(2, Date.valueOf(date));
                insStmt.executeUpdate();
            }
            return true;
        } catch (Exception e) { 
            e.printStackTrace(); 
            return false;
        }
    }
}