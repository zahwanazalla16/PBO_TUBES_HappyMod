package com.project.app;

import com.project.app.view.MainDashboard; // Pastikan import Dashboard baru
import javax.swing.SwingUtilities;

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