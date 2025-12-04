package app;

import javax.swing.SwingUtilities;
import java.util.logging.Logger;
import app.view.MainDashboard;

public class Main {

    // 1. Setup Logger
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    // 2. Tambahkan private constructor agar kelas ini tidak bisa di-new Main()
    // Ini menghilangkan warning "Utility classes should not have public constructors"
    private Main() {
        throw new IllegalStateException("Main class");
    }

    public static void main(String[] args) {
        
        // 3. Ganti System.out.println dengan Logger
        LOGGER.info("=== Starting MoodFlow Application ===");

        // --- MENJALANKAN GUI ---
        SwingUtilities.invokeLater(() -> {
            // Membuka Dashboard Utama (Grafik)
            MainDashboard dashboard = new MainDashboard();
            dashboard.setVisible(true);
        });
    }
}