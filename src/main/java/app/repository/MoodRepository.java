package app.repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
        String deleteSql = "DELETE FROM mood WHERE date = ?";
        String insertSql = "INSERT INTO mood (mood_value, date) VALUES (?, ?)";

        try {
            // 1. Hapus data lama (jika ada)
            PreparedStatement delStmt = conn.prepareStatement(deleteSql);
            delStmt.setDate(1, Date.valueOf(date));
            delStmt.executeUpdate();

            // 2. Insert baru (hanya jika value valid)
            if (moodValue > 0) {
                PreparedStatement insStmt = conn.prepareStatement(insertSql);
                insStmt.setInt(1, moodValue);
                insStmt.setDate(2, Date.valueOf(date));
                insStmt.executeUpdate();
            }
            return true; // Sukses
        } catch (Exception e) { 
            e.printStackTrace(); 
            return false; // Gagal
        }
    }

    public void createMood(Mood mood) {
        String sql = "INSERT INTO mood (mood_value, date) VALUES (?, ?)";

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, mood.getMoodValue());
            stmt.setDate(2, Date.valueOf(mood.getDate()));
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Mood> getAllMood() {
        List<Mood> list = new ArrayList<>();
        String sql = "SELECT * FROM mood ORDER BY date DESC";

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                list.add(new Mood(
                        rs.getInt("id"),
                        rs.getInt("mood_value"),
                        rs.getString("date")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public void updateMood(int id, int newValue) {
        String sql = "UPDATE mood SET mood_value = ? WHERE id = ?";

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, newValue);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteMood(int id) {
        String sql = "DELETE FROM mood WHERE id = ?";

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
