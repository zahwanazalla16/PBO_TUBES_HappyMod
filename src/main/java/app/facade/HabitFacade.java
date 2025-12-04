package app.facade;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;     // [JCF 2] HashMap
import java.util.LinkedList;  // [JCF 3] LinkedList
import java.util.Map;

import app.model.Habit;
import app.observer.IObserver;
import app.repository.HabitRepository;

public class HabitFacade {

    private HabitRepository repository;
    
    // [JCF 2] Cache Habit
    private Map<Integer, Habit> habitCache = new HashMap<>();

    // [JCF 3] Log Aktivitas (Session)
    private LinkedList<String> activityLog = new LinkedList<>();

    // [JCF 1] List Observer
    private List<IObserver> observers = new ArrayList<>();

    public HabitFacade() {
        this.repository = new HabitRepository();
    }

    public HabitFacade(HabitRepository repository) {
        this.repository = repository;
    }

    public void addObserver(IObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers() {
        for (IObserver observer : observers) {
            observer.onDataChanged();
        }
    }

    // --- LOGIC CRUD ---

    public boolean addHabit(String name) {
        if (name == null || name.trim().isEmpty()) return false;

        Habit habit = new Habit(name); 
        boolean isSuccess = repository.createHabit(habit);
        
        if (isSuccess) {
            activityLog.add("Menambahkan habit baru: " + name); // Log Add
            habitCache.clear();
            notifyObservers();
        }
        return isSuccess;
    }

    public boolean updateHabit(int id, String name) {
        Habit habit = new Habit(id, name); 
        boolean isSuccess = repository.updateHabit(habit);
        
        if (isSuccess) {
            activityLog.add("Mengupdate habit ID " + id); // Log Update
            if (habitCache.containsKey(id)) {
                habitCache.put(id, habit);
            }
            notifyObservers();
        }
        return isSuccess;
    }

    public boolean deleteHabit(int id) {
        // Ambil nama dulu sebelum dihapus biar log-nya bagus
        Habit h = getHabit(id);
        String habitName = (h != null) ? h.getName() : "ID " + id;

        boolean isSuccess = repository.deleteHabit(id);
        
        if (isSuccess) {
            activityLog.add("Menghapus habit: " + habitName); // Log Delete
            habitCache.remove(id);
            notifyObservers();
        }
        return isSuccess;
    }

    public List<Habit> getHabits() {
        List<Habit> habits = repository.getAllHabits();
        for (Habit h : habits) {
            habitCache.put(h.getId(), h); 
        }
        return habits;
    }

    public Habit getHabit(int id) {
        if (habitCache.containsKey(id)) {
            return habitCache.get(id);
        }
        Habit h = repository.getHabitById(id);
        if (h != null) {
            habitCache.put(id, h);
        }
        return h;
    }
    
    // Method untuk mengambil Log ke View
    public LinkedList<String> getActivityLog() {
        return activityLog;
    }

    // --- TRACKING HARIAN (CEKLIS/UNCEKLIS) ---
    
    public boolean getHabitStatus(int habitId, LocalDate date) {
        return repository.isHabitDone(habitId, date);
    }

    // [MODIFIKASI PENTING DISINI]
    public void updateHabitStatus(int habitId, LocalDate date, boolean isCompleted) {
        // 1. Simpan ke Database
        boolean success = repository.setHabitStatus(habitId, date, isCompleted);
        
        // 2. Jika sukses simpan, Catat Log ke LinkedList
        if(success) {
            // Ambil nama habit dari Cache biar cepat
            Habit h = getHabit(habitId);
            String habitName = (h != null) ? h.getName() : "Habit ID " + habitId;
            
            // Format tanggal jadi simpel (misal: 04/12)
            String tgl = date.getDayOfMonth() + "/" + date.getMonthValue();
            
            // Tentukan pesan log (Ceklis atau Batal Ceklis)
            String pesan;
            if (isCompleted) {
                pesan = "[v] Selesai: " + habitName + " (" + tgl + ")";
            } else {
                pesan = "[x] Batal: " + habitName + " (" + tgl + ")";
            }
            
            // Masukkan ke LinkedList
            activityLog.add(pesan);
            
            // 3. Update View (Table & Log Panel)
            notifyObservers();
        }
    }
}