package app.facade;

import app.model.Mood;
import app.observer.IObserver;
import app.repository.MoodRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MoodFacade {

    private MoodRepository repo;
    
    // Cache & Log
    private Map<LocalDate, Mood> moodCache = new HashMap<>();
    
    private List<String> activityLog = new LinkedList<>();
    
    private final String[] moodEmojis = {"", "😭", "😞", "😐", "😊", "😄"};
    
    private List<IObserver> observers = new ArrayList<>();

    public MoodFacade() {
        this.repo = new MoodRepository();
    }

    public MoodFacade(MoodRepository repo) {
        this.repo = repo;
    }

    public void addObserver(IObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers() {
        for (IObserver observer : observers) {
            observer.onDataChanged();
        }
    }

    public List<String> getActivityLog() {
        return activityLog;
    }


    public void saveMood(int moodValue, LocalDate date) {
        if (moodValue < 0 || moodValue > 5) return; 

        boolean success = repo.upsertMood(moodValue, date);
        
        if (success) {
            if (moodValue > 0) {
                Mood newMood = new Mood(moodValue, date.toString());
                moodCache.put(date, newMood);
                
                String emoji = moodEmojis[moodValue]; 
                String tgl = date.getDayOfMonth() + "/" + date.getMonthValue();
                String logPesan = "Input Mood: " + emoji + " (" + tgl + ")";
                activityLog.add(logPesan);
            } else {
                moodCache.remove(date); 
            }
            notifyObservers(); 
        }
    }

    public Mood getMood(LocalDate date) {
        if (moodCache.containsKey(date)) {
            return moodCache.get(date);
        }
        Mood m = repo.getMoodByDate(date);
        if (m != null) {
            moodCache.put(date, m);
        }
        return m;
    }
}