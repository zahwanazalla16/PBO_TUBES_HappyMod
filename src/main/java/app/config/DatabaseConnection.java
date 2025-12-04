package app.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import io.github.cdimascio.dotenv.Dotenv; 

// memberitahu SonarQube agar mengabaikan peringatan Singleton
@SuppressWarnings("java:S6548")
public class DatabaseConnection {
    
    // Setup Logger
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    
    private static DatabaseConnection instance;
    private Connection connection;

    private final Dotenv dotenv = Dotenv.load(); 

    private final String dbUrl = dotenv.get("DB_URL");
    private final String dbUser = dotenv.get("DB_USERNAME");
    private final String dbPassword = dotenv.get("DB_PASSWORD");

    private DatabaseConnection() {
        try {
            // Validasi URL dan User
            if (dbUrl == null || dbUser == null) {
                LOGGER.log(Level.WARNING, "Gagal membaca .env! Pastikan file .env ada dan isinya benar.");
            }

            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            
            LOGGER.info("Connected to PostgreSQL!");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection failed", e);
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