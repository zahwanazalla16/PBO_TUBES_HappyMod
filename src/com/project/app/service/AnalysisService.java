package com.project.app.service;

import com.project.app.model.AnalysisHabitMood;
import com.project.app.model.Habit;
import com.project.app.repository.AnalysisRepository;
import com.project.app.repository.HabitRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AnalysisService {
    private final HabitRepository habitRepository;
    private final AnalysisRepository analysisRepository;

    public AnalysisService() {
        this.habitRepository = new HabitRepository();
        this.analysisRepository = new AnalysisRepository();
    }

    public List<AnalysisHabitMood> getHabitMoodImpacts(LocalDate startDate, LocalDate endDate) {
        List<AnalysisHabitMood> impacts = new ArrayList<>();
        List<Habit> habits = habitRepository.getAllHabits();
        List<LocalDate> datesWithMoodEntries = analysisRepository.getDatesWithMoodEntries(startDate, endDate);

        if (datesWithMoodEntries.isEmpty()) {
            return impacts; // No mood data for the period
        }

        for (Habit habit : habits) {
            List<LocalDate> habitCompletedDates = analysisRepository.getHabitCompletedDates(habit.getId(), startDate, endDate);

            // Dates where habit was NOT completed BUT there was a mood entry
            List<LocalDate> habitNotCompletedDates = datesWithMoodEntries.stream()
                .filter(date -> !habitCompletedDates.contains(date))
                .collect(Collectors.toList());

            double avgMoodWhenDone = 0.0;
            if (!habitCompletedDates.isEmpty()) {
                avgMoodWhenDone = analysisRepository.getAverageMoodForDates(habitCompletedDates);
            }
            
            double avgMoodWhenNotDone = 0.0;
            if (!habitNotCompletedDates.isEmpty()) {
                avgMoodWhenNotDone = analysisRepository.getAverageMoodForDates(habitNotCompletedDates);
            }

            double moodImpact = avgMoodWhenDone - avgMoodWhenNotDone;
            
            // Only add if there's enough data to calculate an impact
            // And if the moodImpact is not zero (no change)
            if ((!habitCompletedDates.isEmpty() && !habitNotCompletedDates.isEmpty()) || moodImpact != 0.0) {
                impacts.add(new AnalysisHabitMood(habit.getName(), moodImpact));
            }
        }
        return impacts;
    }
}
