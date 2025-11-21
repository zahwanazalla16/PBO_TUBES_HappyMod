package com.project.app.repository;

import com.project.app.config.DatabaseConnection;
import com.project.app.model.Habit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HabitRepository {

    private final Connection conn;

    public HabitRepository() {
        conn = DatabaseConnection.getInstance().getConnection();
    }

    public boolean createHabit(Habit habit) {
        String sql = "INSERT INTO habits (name, description) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, habit.getName());
            stmt.setString(2, habit.getDescription());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Habit getHabitById(int id) {
        String sql = "SELECT * FROM habits WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Habit(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Habit> getAllHabits() {
        List<Habit> habits = new ArrayList<>();
        String sql = "SELECT * FROM habits";

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                habits.add(new Habit(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return habits;
    }

    public boolean updateHabit(Habit habit) {
        String sql = "UPDATE habits SET name = ?, description = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, habit.getName());
            stmt.setString(2, habit.getDescription());
            stmt.setInt(3, habit.getId());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

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
}
