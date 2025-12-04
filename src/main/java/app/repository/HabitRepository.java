package app.repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import app.config.DatabaseConnection;
import app.model.Habit;

public class HabitRepository {

    private static final Logger LOGGER = Logger.getLogger(HabitRepository.class.getName());
    private final Connection conn;

    public HabitRepository() {
        conn = DatabaseConnection.getInstance().getConnection();
    }

    public boolean createHabit(Habit habit) {
        String sql = "INSERT INTO habits (name) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, habit.getName());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating habit", e);
            return false;
        }
    }

    public Habit getHabitById(int id) {
        String sql = "SELECT id, name FROM habits WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Habit(rs.getInt("id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting habit by id", e);
        }
        return null;
    }

    public List<Habit> getAllHabits() {
        List<Habit> habits = new ArrayList<>();
        String sql = "SELECT id, name FROM habits ORDER BY id ASC";

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                habits.add(new Habit(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all habits", e);
        }
        return habits;
    }

    public boolean deleteHabit(int id) {
        String sql = "DELETE FROM habits WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting habit", e);
            return false;
        }
    }

    public boolean isHabitDone(int habitId, LocalDate date) {
        String sql = "SELECT 1 FROM habit_logs WHERE habit_id = ? AND date = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, habitId);
            stmt.setDate(2, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking habit status", e);
        }
        return false;
    }

    public boolean setHabitStatus(int habitId, LocalDate date, boolean status) {
        if (status) {
            String sql = "INSERT INTO habit_logs (habit_id, date) VALUES (?, ?) ON CONFLICT DO NOTHING";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, habitId);
                stmt.setDate(2, Date.valueOf(date));
                stmt.executeUpdate();
                return true;
            } catch (SQLException e) { 
                LOGGER.log(Level.SEVERE, "Error setting habit status (insert)", e);
                return false; 
            }
        } else {
            String sql = "DELETE FROM habit_logs WHERE habit_id = ? AND date = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, habitId);
                stmt.setDate(2, Date.valueOf(date));
                stmt.executeUpdate();
                return true;
            } catch (SQLException e) { 
                LOGGER.log(Level.SEVERE, "Error setting habit status (delete)", e);
                return false; 
            }
        }
    }
}