package app.service;

import app.repository.AnalysisRepository;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class AnalysisService {
    private final AnalysisRepository analysisRepository;
    private final List<Supplier<String>> analysisPool;

    public AnalysisService() {
        this.analysisRepository = new AnalysisRepository();
        
        // Method Reference digunakan di sini untuk inisialisasi
        this.analysisPool = Arrays.asList(
            this::analyzeHabitConsistency,
            this::analyzeHabitsWithHighMood,
            this::analyzeHabitsWithLowMood,
            this::analyzeHighestMoodDay,
            this::analyzeLowestMoodDay,
            this::generatePositiveImpactRecommendation,
            this::generateConsistencyRecommendation
        );
    }

    public List<String> getSevenRandomAnalyses() {
        List<String> analyses = new ArrayList<>();
        List<Supplier<String>> shuffledPool = new ArrayList<>(analysisPool);
        Collections.shuffle(shuffledPool);

        for (Supplier<String> analysisSupplier : shuffledPool) {
            if (analyses.size() >= 7) break;
            
            String result = analysisSupplier.get();
            if (result != null && !result.isEmpty()) {
                analyses.add(result);
            }
        }
        return analyses;
    }

    // GENERIC METHOD CORE
    private <T> String executeAnalysis(Supplier<T> dataSupplier, 
                                       Predicate<T> validator, 
                                       Function<T, String> resultFormatter) {
        try {
            T data = dataSupplier.get();
            if (validator.test(data)) {
                return resultFormatter.apply(data);
            }
        } catch (Exception e) {
            e.printStackTrace(); 
        }
        return null;
    }


    // 1. Kasus T = Habit
    private String analyzeHabitConsistency() {
        return executeAnalysis(
            analysisRepository::getRandomHabit,
            
            Objects::nonNull,
            
            habit -> {
                LocalDate endDate = LocalDate.now();
                LocalDate startDate = endDate.minusDays(6);
                int count = analysisRepository.countHabitLogs(habit.getId(), startDate, endDate);
                
                if (count > 0) {
                    long percentage = (count * 100) / 7;
                    return String.format("Konsistensi: '%s' dilakukan %d dari 7 hari terakhir (%d%%).",
                            habit.getName(), count, percentage);
                }
                return null; 
            }
        );
    }

    // 2. Kasus T = List<String>
    private String analyzeHabitsWithHighMood() {
        return executeAnalysis(
            () -> {
                LocalDate end = LocalDate.now();
                return analysisRepository.getHabitsByMood(true, 3, end.minusWeeks(1), end);
            },
            list -> !list.isEmpty(),
            list -> "Saat mood sedang baik, Anda sering melakukan: " + String.join(", ", list) + "."
        );
    }

    // 3. Kasus T = List<String> (Low Mood)
    private String analyzeHabitsWithLowMood() {
        return executeAnalysis(
            () -> {
                LocalDate end = LocalDate.now();
                return analysisRepository.getHabitsByMood(false, 3, end.minusWeeks(1), end);
            },
            list -> !list.isEmpty(),
            list -> "Saat mood sedang kurang baik, Anda tercatat melakukan: " + String.join(", ", list) + "."
        );
    }

    // 4. Kasus T = Map<DayOfWeek, Double>
    private String analyzeHighestMoodDay() {
        return executeAnalysis(
            () -> {
                LocalDate end = LocalDate.now();
                return analysisRepository.getAverageMoodByDayOfWeek(end.minusWeeks(1), end);
            },
            
            map -> !map.isEmpty(),
            
            map -> map.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(entry -> {
                        String dayName = entry.getKey().getDisplayName(TextStyle.FULL, new Locale("id", "ID"));
                        return "Pola mingguan menunjukkan mood tertinggi Anda sering terjadi pada hari " + dayName + ".";
                    })
                    .orElse(null)
        );
    }

    // 5. Kasus T = Map<DayOfWeek, Double> (Lowest)
    private String analyzeLowestMoodDay() {
        return executeAnalysis(
            () -> {
                LocalDate end = LocalDate.now();
                return analysisRepository.getAverageMoodByDayOfWeek(end.minusWeeks(1), end);
            },
            map -> !map.isEmpty(),
            
            map -> map.entrySet().stream()
                    .min(Map.Entry.comparingByValue())
                    .map(entry -> {
                        String dayName = entry.getKey().getDisplayName(TextStyle.FULL, new Locale("id", "ID"));
                        return "Hari " + dayName + " cenderung menjadi hari yang lebih berat untuk Anda minggu ini.";
                    })
                    .orElse(null)
        );
    }

    // 6. Kasus T = List<String> (Rekomendasi)
    private String generatePositiveImpactRecommendation() {
        return executeAnalysis(
            () -> {
                LocalDate end = LocalDate.now();
                return analysisRepository.getHabitsByMood(true, 1, end.minusWeeks(1), end);
            },
            list -> !list.isEmpty(),
            list -> String.format("Rekomendasi: Melakukan '%s' terbukti membantu menjaga mood tetap stabil. Pertahankan!", list.get(0))
        );
    }

    // 7. Kasus T = Habit (Saran Konsistensi)
    private String generateConsistencyRecommendation() {
        return executeAnalysis(
            analysisRepository::getRandomHabit,
            
            Objects::nonNull,
            
            habit -> {
                LocalDate end = LocalDate.now();
                int count = analysisRepository.countHabitLogs(habit.getId(), end.minusDays(6), end);
                
                if (count > 0 && count < 3) {
                    return String.format("Saran: Untuk meningkatkan mood, coba tingkatkan frekuensi '%s' menjadi 4x/minggu.", habit.getName());
                }
                return null;
            }
        );
    }
}