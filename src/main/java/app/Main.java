package app;

import javax.swing.SwingUtilities;

import app.view.MainDashboard;

public class Main {
    public static void main(String[] args) {
        
        System.out.println("=== Starting MoodFlow Application ===");

        // --- MENJALANKAN GUI ---
        SwingUtilities.invokeLater(() -> {
            // Membuka Dashboard Utama (Grafik)
            MainDashboard dashboard = new MainDashboard();
            dashboard.setVisible(true);
        });
    }
}