package com.project.app.repository;

import com.project.app.config.DatabaseConnection;
import com.project.app.model.Habit;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HabitRepository {

    private final Connection conn;

    public HabitRepository() {
        conn = DatabaseConnection.getInstance().getConnection();
    }

    // --- CREATE ---
    public boolean createHabit(Habit habit) {
        String sql = "INSERT INTO habits (name) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, habit.getName());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- READ (Single) - INI YANG TADI ERROR ---
    public Habit getHabitById(int id) {
        String sql = "SELECT * FROM habits WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Perhatikan: Constructor Habit sekarang cuma (id, name)
                return new Habit(
                        rs.getInt("id"),
                        rs.getString("name")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- READ (All) ---
    public List<Habit> getAllHabits() {
        List<Habit> habits = new ArrayList<>();
        String sql = "SELECT * FROM habits ORDER BY id ASC";

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                habits.add(new Habit(
                        rs.getInt("id"),
                        rs.getString("name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return habits;
    }

    // --- UPDATE ---
    public boolean updateHabit(Habit habit) {
        // Hapus update description, sisa name saja
        String sql = "UPDATE habits SET name = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, habit.getName());
            stmt.setInt(2, habit.getId());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- DELETE ---
    public boolean deleteHabit(int id) {
        String sql = "DELETE FROM habits WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==========================================
    // FITUR BARU: TRACKING / LOGGING
    // ==========================================

    // Cek status Habit di tanggal tertentu
    public boolean isHabitDone(int habitId, LocalDate date) {
        String sql = "SELECT is_completed FROM habit_logs WHERE habit_id = ? AND date = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, habitId);
            stmt.setDate(2, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("is_completed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Toggle Ceklis Habit (Upsert: Insert or Update)
    public void setHabitStatus(int habitId, LocalDate date, boolean status) {
        String sql = "INSERT INTO habit_logs (habit_id, date, is_completed) VALUES (?, ?, ?) " +
                     "ON CONFLICT (habit_id, date) DO UPDATE SET is_completed = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, habitId);
            stmt.setDate(2, Date.valueOf(date));
            stmt.setBoolean(3, status);
            stmt.setBoolean(4, status);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}