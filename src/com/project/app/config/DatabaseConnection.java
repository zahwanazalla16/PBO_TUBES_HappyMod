package com.project.app.config;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    private final String URL = "jdbc:postgresql://localhost:5432/mood_habit_analysis";
    private final String USER = "postgres";
    private final String PASSWORD = "postgre";

    private DatabaseConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to PostgreSQL!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Connection getConn() {
        return connection;
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}

