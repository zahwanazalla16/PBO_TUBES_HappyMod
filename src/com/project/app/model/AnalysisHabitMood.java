package com.project.app.model;

public class AnalysisHabitMood {
    private String habitName;
    private double moodImpact; // Positive: habit increases mood, negative: decreases

    public AnalysisHabitMood(String habitName, double moodImpact) {
        this.habitName = habitName;
        this.moodImpact = moodImpact;
    }

    public String getHabitName() {
        return habitName;
    }

    public double getMoodImpact() {
        return moodImpact;
    }

    @Override
    public String toString() {
        return "AnalysisHabitMood{"
                + "habitName='" + habitName + "'\''" + 
                ", moodImpact=" + moodImpact +
                '}';
    }
}
