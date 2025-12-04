package com.project.app.repository;

import com.project.app.config.DatabaseConnection;
import com.project.app.model.Habit;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if (dates == null || dates.isEmpty()) {
            return 0.0;
        }
        String sql = "SELECT AVG(mood_value) FROM mood WHERE date IN (";
        StringBuilder sb = new StringBuilder(sql);
        for (int i = 0; i < dates.size(); i++) {
            sb.append("?");
            if (i < dates.size() - 1) {
                sb.append(",");
            }
        }
        sb.append(")");

        try (PreparedStatement stmt = conn.prepareStatement(sb.toString())) {
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

    public Habit getRandomHabit() {
        String sql = "SELECT id, name FROM habits ORDER BY RANDOM() LIMIT 1";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new Habit(rs.getInt("id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int countHabitLogs(int habitId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COUNT(*) FROM habit_logs WHERE habit_id = ? AND date BETWEEN ? AND ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, habitId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public List<String> getHabitsByMood(boolean isHighMood, int limit, LocalDate startDate, LocalDate endDate) {
        List<String> habitNames = new ArrayList<>();
        // High mood > 3, Low mood < 3
        String moodCondition = isHighMood ? "m.mood_value > 3" : "m.mood_value < 3";
        String sql = "SELECT h.name, COUNT(h.id) as habit_count " +
                     "FROM habits h " +
                     "JOIN habit_logs hl ON h.id = hl.habit_id " +
                     "JOIN mood m ON hl.date = m.date " +
                     "WHERE " + moodCondition + " AND m.date BETWEEN ? AND ? " +
                     "GROUP BY h.name " +
                     "ORDER BY habit_count DESC " +
                     "LIMIT ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            stmt.setInt(3, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                habitNames.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return habitNames;
    }

    public Map<DayOfWeek, Double> getAverageMoodByDayOfWeek(LocalDate startDate, LocalDate endDate) {
        Map<DayOfWeek, Double> moodByDay = new HashMap<>();
        // In PostgreSQL, EXTRACT(ISODOW FROM date) returns 1 for Monday through 7 for Sunday.
        String sql = "SELECT EXTRACT(ISODOW FROM date) as day_of_week, AVG(mood_value) as avg_mood " +
                     "FROM mood " +
                     "WHERE date BETWEEN ? AND ? " +
                     "GROUP BY day_of_week";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                DayOfWeek day = DayOfWeek.of(rs.getInt("day_of_week"));
                moodByDay.put(day, rs.getDouble("avg_mood"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return moodByDay;
    }
}