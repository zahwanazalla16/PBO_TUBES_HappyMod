package app.facade;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import app.model.Habit;
import app.observer.IObserver;
import app.repository.HabitRepository;

public class HabitFacade {

    private HabitRepository repository;
    
    // Cache & Log
    private Map<Integer, Habit> habitCache = new HashMap<>();
    private LinkedList<String> activityLog = new LinkedList<>();
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

    public LinkedList<String> getActivityLog() {
        return activityLog;
    }

    // --- LOGIC CRUD ---

    public boolean addHabit(String name) {
        if (name == null || name.trim().isEmpty()) return false;

        Habit habit = new Habit(name); 
        boolean isSuccess = repository.createHabit(habit);
        
        if (isSuccess) {
            activityLog.add("Menambahkan habit baru: " + name);
            habitCache.clear(); // Clear cache agar reload ulang dari DB saat getHabits
            notifyObservers();
        }
        return isSuccess;
    }

    public boolean deleteHabit(int id) {
        Habit h = getHabit(id);
        String habitName = (h != null) ? h.getName() : "ID " + id;

        boolean isSuccess = repository.deleteHabit(id);
        
        if (isSuccess) {
            activityLog.add("Menghapus habit: " + habitName);
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

    // Helper internal untuk mengambil nama habit (dipakai saat log status update)
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
    
    // --- TRACKING STATUS ---
    
    public boolean getHabitStatus(int habitId, LocalDate date) {
        return repository.isHabitDone(habitId, date);
    }

    public void updateHabitStatus(int habitId, LocalDate date, boolean isCompleted) {
        boolean success = repository.setHabitStatus(habitId, date, isCompleted);
        
        if(success) {
            Habit h = getHabit(habitId);
            String habitName = (h != null) ? h.getName() : "Habit ID " + habitId;
            String tgl = date.getDayOfMonth() + "/" + date.getMonthValue();
            String pesan;
            
            if (isCompleted) {
                pesan = "[v] Selesai: " + habitName + " (" + tgl + ")";
            } else {
                pesan = "[x] Batal: " + habitName + " (" + tgl + ")";
            }
            
            activityLog.add(pesan);
            notifyObservers();
        }
    }
}