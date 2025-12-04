package com.project.app.facade;

import com.project.app.model.Mood;
import com.project.app.repository.MoodRepository;

import java.time.LocalDate;
import java.util.List;

public class MoodFacade {

    private MoodRepository repo = new MoodRepository();

    public Mood getMood(LocalDate date) {
        return repo.getMoodByDate(date);
    }

    public void saveMood(int moodValue, LocalDate date) {
        repo.upsertMood(moodValue, date);
    }

    public void addMood(int moodValue, String date) {
        repo.createMood(new Mood(moodValue, date));
    }

    public List<Mood> getAllMood() {
        return repo.getAllMood();
    }

    public void updateMood(int id, int newValue) {
        repo.updateMood(id, newValue);
    }

    public void deleteMood(int id) {
        repo.deleteMood(id);
    }
}
