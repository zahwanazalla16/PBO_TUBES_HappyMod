package app.facade;

import app.model.Mood;
import app.observer.IObserver;
import app.repository.MoodRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MoodFacade {

    private MoodRepository repo;
    
    // 1. Constructor Default (Dipakai Aplikasi Asli)
    public MoodFacade() {
        this.repo = new MoodRepository();
    }

    // 2. Constructor untuk Testing (Agar bisa masukin Mock)
    public MoodFacade(MoodRepository repo) {
        this.repo = repo;
    }

    private List<IObserver> observers = new ArrayList<>();

    public void addObserver(IObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers() {
        for (IObserver observer : observers) {
            observer.onDataChanged();
        }
    }

    // --- BAGIAN CRUD ---

    public void saveMood(int moodValue, LocalDate date) {
        // Validasi: Mood harus 1-5 (Emoji)
        if (moodValue < 1 || moodValue > 5) {
            return; // GAGAL: Tidak simpan ke DB, tidak notify observer
        }
        repo.upsertMood(moodValue, date);
        notifyObservers(); 
    }

    public void addMood(int moodValue, String date) {
        // Validasi
        if (moodValue < 1 || moodValue > 5) {
            return;
        }
        repo.createMood(new Mood(moodValue, date));
        notifyObservers();
    }

    public void updateMood(int id, int newValue) {
        if (newValue < 1 || newValue > 5) {
            return;
        }
        repo.updateMood(id, newValue);
        notifyObservers();
    }

    public void deleteMood(int id) {
        repo.deleteMood(id);
        notifyObservers();
    }

    // --- BAGIAN READ ---

    public Mood getMood(LocalDate date) {
        return repo.getMoodByDate(date);
    }

    public List<Mood> getAllMood() {
        return repo.getAllMood();
    }
}