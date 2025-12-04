package app.service;

import app.model.Habit;
import app.repository.AnalysisRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AnalysisService {
    private final AnalysisRepository analysisRepository;
    private final List<Supplier<String>> analysisPool;

    public AnalysisService() {
        this.analysisRepository = new AnalysisRepository();
        // Initialize the pool of analysis methods
        this.analysisPool = Arrays.asList(
            this::analyzeHabitConsistency,
            this::analyzeHabitsWithHighMood,
            this::analyzeHabitsWithLowMood,
            this::analyzeHighestMoodDay,
            this::analyzeLowestMoodDay,
            this::generatePositiveImpactRecommendation,
            this::generateConsistencyRecommendation
            // Add more analysis method references here
        );
    }

    /**
     * Gets 7 unique, randomly selected analyses from the pool.
     * It will try to generate analyses until 7 are collected or the pool is exhausted.
     * @return A list of 7 unique analysis strings.
     */
    public List<String> getSevenRandomAnalyses() {
        List<String> analyses = new ArrayList<>();
        List<Supplier<String>> shuffledPool = new ArrayList<>(analysisPool);
        Collections.shuffle(shuffledPool);

        for (Supplier<String> analysisSupplier : shuffledPool) {
            if (analyses.size() >= 7) {
                break;
            }
            String result = analysisSupplier.get();
            if (result != null && !result.isEmpty()) {
                analyses.add(result);
            }
        }
        return analyses;
    }

    // --- Analysis Method Implementations ---

    private String analyzeHabitConsistency() {
        Habit habit = analysisRepository.getRandomHabit();
        if (habit == null) return null;

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6); // Last 7 days
        int count = analysisRepository.countHabitLogs(habit.getId(), startDate, endDate);
        
        if (count > 0) {
            long totalDays = 7;
            long percentage = (count * 100) / totalDays;
            return String.format("Konsistensi: '%s' dilakukan %d dari %d hari terakhir (%d%%).",
                habit.getName(), count, totalDays, percentage);
        }
        return null;
    }

    private String analyzeHabitsWithHighMood() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusWeeks(1);
        List<String> habits = analysisRepository.getHabitsByMood(true, 3, startDate, endDate);

        if (!habits.isEmpty()) {
            return "Saat mood sedang baik, Anda sering melakukan: " + String.join(", ", habits) + ".";
        }
        return null;
    }

    private String analyzeHabitsWithLowMood() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusWeeks(1);
        List<String> habits = analysisRepository.getHabitsByMood(false, 3, startDate, endDate);

        if (!habits.isEmpty()) {
            return "Saat mood sedang kurang baik, Anda tercatat melakukan: " + String.join(", ", habits) + ".";
        }
        return null;
    }

    private String analyzeHighestMoodDay() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusWeeks(1);
        Map<DayOfWeek, Double> moodByDay = analysisRepository.getAverageMoodByDayOfWeek(startDate, endDate);

        Optional<Map.Entry<DayOfWeek, Double>> highestMoodDay = moodByDay.entrySet().stream()
            .max(Map.Entry.comparingByValue());

        if (highestMoodDay.isPresent()) {
            String dayName = highestMoodDay.get().getKey().getDisplayName(TextStyle.FULL, new Locale("id", "ID"));
            return "Pola mingguan menunjukkan mood tertinggi Anda sering terjadi pada hari " + dayName + ".";
        }
        return null;
    }
    
    private String analyzeLowestMoodDay() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusWeeks(1);
        Map<DayOfWeek, Double> moodByDay = analysisRepository.getAverageMoodByDayOfWeek(startDate, endDate);

        Optional<Map.Entry<DayOfWeek, Double>> lowestMoodDay = moodByDay.entrySet().stream()
            .min(Map.Entry.comparingByValue());

        if (lowestMoodDay.isPresent()) {
            String dayName = lowestMoodDay.get().getKey().getDisplayName(TextStyle.FULL, new Locale("id", "ID"));
            return "Hari " + dayName + " cenderung menjadi hari yang lebih berat untuk Anda minggu ini.";
        }
        return null;
    }
    
    private String generatePositiveImpactRecommendation() {
        // This is a simplified version of the original impact analysis
        // It finds a habit done on high-mood days and suggests it.
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusWeeks(1);
        List<String> habits = analysisRepository.getHabitsByMood(true, 1, startDate, endDate);
        if (!habits.isEmpty()) {
            return String.format("Rekomendasi: Melakukan '%s' terbukti membantu menjaga mood tetap stabil. Pertahankan!", habits.get(0));
        }
        return null;
    }
    
    private String generateConsistencyRecommendation() {
        Habit habit = analysisRepository.getRandomHabit();
        if (habit == null) return null;

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        int count = analysisRepository.countHabitLogs(habit.getId(), startDate, endDate);

        if (count > 0 && count < 3) { // Recommend if consistency is low but not zero
            return String.format("Saran: Untuk meningkatkan mood, coba tingkatkan frekuensi '%s' menjadi 4x/minggu.", habit.getName());
        }
        return null;
    }
}