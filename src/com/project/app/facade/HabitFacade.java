package com.project.app.facade;

import com.project.app.model.Habit;
import com.project.app.observer.IObserver;
import com.project.app.repository.HabitRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HabitFacade {

    private HabitRepository repository;

    public HabitFacade() {
        this.repository = new HabitRepository();
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


    public boolean addHabit(String name) {
        Habit habit = new Habit(name); 
        boolean isSuccess = repository.createHabit(habit);
        
        if (isSuccess) {
            notifyObservers();
        }
        
        return isSuccess;
    }

    public boolean updateHabit(int id, String name) {
        Habit habit = new Habit(id, name); 
        boolean isSuccess = repository.updateHabit(habit);
        
        if (isSuccess) {
            notifyObservers();
        }
        
        return isSuccess;
    }

    public boolean deleteHabit(int id) {
        boolean isSuccess = repository.deleteHabit(id);
        
        if (isSuccess) {
            notifyObservers();
        }
        
        return isSuccess;
    }

    public List<Habit> getHabits() {
        return repository.getAllHabits();
    }

    public Habit getHabit(int id) {
        return repository.getHabitById(id);
    }

    // --- FITUR BARU: TRACKING HARIAN (Untuk GUI Checkbox) ---
    
    // Mengecek apakah habit X di tanggal Y sudah diceklis?
    public boolean getHabitStatus(int habitId, LocalDate date) {
        return repository.isHabitDone(habitId, date);
    }

    // Mengubah status ceklis (True/False)
    public void updateHabitStatus(int habitId, LocalDate date, boolean isCompleted) {
        repository.setHabitStatus(habitId, date, isCompleted);
    }
}
