package app.facade;

import app.model.Mood;
import app.observer.IObserver;
import app.repository.MoodRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;    // [JCF 2]
import java.util.LinkedList; // [JCF 3]
import java.util.Map;

public class MoodFacade {

    private MoodRepository repo;
    
    // [JCF 2] Cache Mood
    private Map<LocalDate, Mood> moodCache = new HashMap<>();

    // [JCF 3] Log Aktivitas Session
    private LinkedList<String> activityLog = new LinkedList<>();

    // [KEMBALI KE EMOJI]
    private final String[] MOOD_EMOJIS = {"", "😭", "😞", "😐", "😊", "😄"};

    private List<IObserver> observers = new ArrayList<>();

    public MoodFacade() {
        this.repo = new MoodRepository();
    }

    public MoodFacade(MoodRepository repo) {
        this.repo = repo;
    }

    public void addObserver(IObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers() {
        for (IObserver observer : observers) {
            observer.onDataChanged();
        }
    }

    public LinkedList<String> getActivityLog() {
        return activityLog;
    }

    // --- LOGIC UTAMA ---

    public void saveMood(int moodValue, LocalDate date) {
        // Validasi input 1-5
        if (moodValue < 1 || moodValue > 5) return;

        // 1. Simpan ke Database
        boolean success = repo.upsertMood(moodValue, date);
        
        // 2. Jika sukses, update Cache dan Log
        if (success) {
            // Update Cache
            Mood newMood = new Mood(moodValue, date.toString());
            moodCache.put(date, newMood);

            // [PAKAI EMOJI LAGI DISINI]
            String emoji = MOOD_EMOJIS[moodValue]; 
            
            String tgl = date.getDayOfMonth() + "/" + date.getMonthValue();
            
            // Hasil Log: "Input Mood: 😊 (30/11)"
            String logPesan = "Input Mood: " + emoji + " (" + tgl + ")";
            
            activityLog.add(logPesan);

            notifyObservers(); 
        }
    }

    public Mood getMood(LocalDate date) {
        if (moodCache.containsKey(date)) {
            return moodCache.get(date);
        }
        Mood m = repo.getMoodByDate(date);
        if (m != null) {
            moodCache.put(date, m);
        }
        return m;
    }

    // ... Sisa method (addMood, updateMood, deleteMood, getAllMood) sama ...
    public void addMood(int moodValue, String date) {
        if (moodValue < 1 || moodValue > 5) return;
        repo.createMood(new Mood(moodValue, date));
        notifyObservers();
    }
    public void updateMood(int id, int newValue) {
        if (newValue < 1 || newValue > 5) return;
        repo.updateMood(id, newValue);
        notifyObservers();
    }
    public void deleteMood(int id) {
        repo.deleteMood(id);
        notifyObservers();
    }
    public List<Mood> getAllMood() {
        return repo.getAllMood();
    }
}