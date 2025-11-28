package com.project.app;

import com.project.app.facade.HabitFacade;
import com.project.app.facade.MoodFacade;
import com.project.app.view.MainDashboard; // Pastikan import GUI-nya
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        
        // --- BAGIAN 1: TEST BACKEND (Opsional, hanya untuk cek koneksi) ---
        System.out.println("=== Starting Backend Check ===");
        
        HabitFacade habitFacade = new HabitFacade();

        // Contoh input data dummy (Parameter description SUDAH DIHAPUS)
        // Uncomment baris di bawah ini jika ingin mengisi data awal via kodingan
        // habitFacade.addHabit("Olahraga");
        // habitFacade.addHabit("Ngoding Java");

        System.out.println("List Habits di Database: " + habitFacade.getHabits());

        // Update juga tanpa description
        // habitFacade.updateHabit(1, "Olahraga Update");

        
        MoodFacade mf = new MoodFacade();
        // mf.addMood(5, "2025-11-21"); 
        
        System.out.println("List Mood: " + mf.getAllMood());
        System.out.println("=== Backend OK, Launching GUI... ===");


        // --- BAGIAN 2: MENJALANKAN GUI ---
        // Ini perintah untuk membuka window MainDashboard
        SwingUtilities.invokeLater(() -> {
            MainDashboard dashboard = new MainDashboard();
            dashboard.setVisible(true);
        });
    }
}