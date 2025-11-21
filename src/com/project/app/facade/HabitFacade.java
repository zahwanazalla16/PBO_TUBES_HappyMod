package com.project.app.facade;

import com.project.app.model.Habit;
import com.project.app.repository.HabitRepository;

import java.util.List;

public class HabitFacade {

    private HabitRepository repository;

    public HabitFacade() {
        this.repository = new HabitRepository();
    }

    public boolean addHabit(String name, String description) {
        Habit habit = new Habit(name, description);
        return repository.createHabit(habit);
    }

    public List<Habit> getHabits() {
        return repository.getAllHabits();
    }

    public Habit getHabit(int id) {
        return repository.getHabitById(id);
    }

    public boolean updateHabit(int id, String name, String description) {
        Habit habit = new Habit(id, name, description);
        return repository.updateHabit(habit);
    }

    public boolean deleteHabit(int id) {
        return repository.deleteHabit(id);
    }
}
