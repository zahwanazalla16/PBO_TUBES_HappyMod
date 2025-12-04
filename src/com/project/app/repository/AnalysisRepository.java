package com.project.app.repository;

import com.project.app.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AnalysisRepository {
    private final Connection conn;

    public AnalysisRepository() {
        conn = DatabaseConnection.getInstance().getConnection();
    }

    public List<LocalDate> getDatesWithMoodEntries(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        String sql = "SELECT DISTINCT date FROM mood WHERE date BETWEEN ? AND ? ORDER BY date";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                dates.add(rs.getDate("date").toLocalDate());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dates;
    }

    public List<LocalDate> getHabitCompletedDates(int habitId, LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        String sql = "SELECT date FROM habit_logs WHERE habit_id = ? AND date BETWEEN ? AND ? ORDER BY date";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, habitId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                dates.add(rs.getDate("date").toLocalDate());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dates;
    }

    public double getAverageMoodForDates(List<LocalDate> dates) {
        if (dates.isEmpty()) {
            return 0.0;
        }

        // Convert List<LocalDate> to a comma-separated string for the IN clause
        StringBuilder datePlaceholders = new StringBuilder();
        for (int i = 0; i < dates.size(); i++) {
            datePlaceholders.append("?");
            if (i < dates.size() - 1) {
                datePlaceholders.append(",");
            }
        }

        String sql = "SELECT AVG(mood_value) FROM mood WHERE date IN (" + datePlaceholders.toString() + ")";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < dates.size(); i++) {
                stmt.setDate(i + 1, Date.valueOf(dates.get(i)));
            }
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}
