package com.project.app;

import com.project.app.facade.HabitFacade;
import com.project.app.facade.MoodFacade;

public class Main {
    public static void main(String[] args) {
        HabitFacade habitFacade = new HabitFacade();

        habitFacade.addHabit("Olahraga", "Lari 20 menit");
        habitFacade.addHabit("Ngoding", "Belajar Java 1 jam");

        System.out.println(habitFacade.getHabits());

        habitFacade.updateHabit(1, "Olahraga Update", "Renang 30 menit");

        habitFacade.deleteHabit(2);

        
        MoodFacade mf = new MoodFacade();

        mf.addMood(5, "2025-11-21"); // 5 = 😀 sangat senang
        mf.addMood(2, "2025-11-20"); // 2 = 😕 kurang happy

        System.out.println(mf.getAllMood());

    }
}
