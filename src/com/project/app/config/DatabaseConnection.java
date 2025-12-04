package com.project.app.config;

import java.sql.Connection;
import java.sql.DriverManager;
// Pastikan import ini tidak merah
// import io.github.cdimascio.dotenv.Dotenv; 

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    // // --- TAMBAHKAN BARIS INI ---
    // // Kita harus memuat file .env nya dulu ke dalam variabel bernama 'dotenv'
    // private final Dotenv dotenv = Dotenv.load(); 
    // // ---------------------------

    // // Sekarang variabel 'dotenv' sudah dikenali, jadi baris di bawah ini tidak akan merah lagi
    // private final String URL = dotenv.get("DB_URL");
    // private final String USER = dotenv.get("DB_USERNAME");
    // private final String PASSWORD = dotenv.get("DB_PASSWORD");

    private final String URL = "jdbc:postgresql://localhost:5432/mood_habit_analysis";
    private final String USER = "postgres";
    private final String PASSWORD = "postgre";


    private DatabaseConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            // Pastikan URL, USER, PASSWORD tidak null
            if(URL == null || USER == null) {
                System.out.println("Gagal membaca .env! Pastikan file .env ada dan isinya benar.");
            }
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to PostgreSQL!");
        } catch (Exception e) {
            e.printStackTrace();
        }
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