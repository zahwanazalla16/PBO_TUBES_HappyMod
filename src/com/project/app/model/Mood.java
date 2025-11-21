package com.project.app.model;

public class Mood {
    private int id;
    private int moodValue;
    private String date;

    public Mood(int id, int moodValue, String date) {
        this.id = id;
        this.moodValue = moodValue;
        this.date = date;
    }

    public Mood(int moodValue, String date) {
        this.moodValue = moodValue;
        this.date = date;
    }

    public int getId() { return id; }
    public int getMoodValue() { return moodValue; }
    public String getDate() { return date; }

    @Override
    public String toString() {
        return "Mood{" +
                "id=" + id +
                ", moodValue=" + moodValue +
                ", date='" + date + '\'' +
                '}';
    }
}
