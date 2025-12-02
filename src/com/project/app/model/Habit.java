package com.project.app.model;

public class Habit {
    private int id;
    private String name;

    public Habit() {}

    public Habit(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Habit(String name) {
        this.name = name;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    @Override
    public String toString() { return name; }
}
