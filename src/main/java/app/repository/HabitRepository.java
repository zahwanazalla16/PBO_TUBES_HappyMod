package app.repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import app.config.DatabaseConnection;
import app.model.Habit;

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

    // --- READ (Single) ---
    public Habit getHabitById(int id) {
        String sql = "SELECT * FROM habits WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Constructor Habit hanya ID dan Name (Sesuai request, tanpa is_completed)
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
    // TRACKING / LOGGING (Habit Logs Table)
    // ==========================================

    public boolean isHabitDone(int habitId, LocalDate date) {
        String sql = "SELECT 1 FROM habit_logs WHERE habit_id = ? AND date = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, habitId);
            stmt.setDate(2, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // [UBAH] Return boolean agar Facade tau sukses/gagal
    public boolean setHabitStatus(int habitId, LocalDate date, boolean status) {
        if (status) {
            // INSERT (Tandai Selesai)
            String sql = "INSERT INTO habit_logs (habit_id, date) VALUES (?, ?) ON CONFLICT DO NOTHING";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, habitId);
                stmt.setDate(2, Date.valueOf(date));
                stmt.executeUpdate();
                return true; // Sukses Insert
            } catch (SQLException e) { 
                e.printStackTrace(); 
                return false;
            }
        } else {
            // DELETE (Hapus Tanda Selesai / Unchecklist)
            String sql = "DELETE FROM habit_logs WHERE habit_id = ? AND date = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, habitId);
                stmt.setDate(2, Date.valueOf(date));
                stmt.executeUpdate();
                return true; // Sukses Delete
            } catch (SQLException e) { 
                e.printStackTrace(); 
                return false;
            }
        }
    }
}