package app.facade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Import Package Aplikasi
import app.model.Mood;
import app.observer.IObserver;
import app.repository.MoodRepository;

// Import Java Utilities
import java.time.LocalDate;
import java.util.List; // Penting: Pakai List generic

// Import JUnit & Mockito
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@DisplayName("Test Lengkap MoodFacade (Save & Get Only)")
class MoodFacadeTest {

    private MoodRepository repoMock;
    private IObserver observerMock;
    private MoodFacade moodFacade;

    @BeforeEach
    void setUp() {
        // 1. Setup Mock
        repoMock = mock(MoodRepository.class);
        observerMock = mock(IObserver.class);

        // 2. Masukkan Mock ke Facade
        moodFacade = new MoodFacade(repoMock);
        
        // 3. Daftarkan Observer
        moodFacade.addObserver(observerMock);
    }

    // ==========================================
    // 1. TEST SAVE / UPSERT MOOD
    // ==========================================

    @Test
    @DisplayName("SAVE SUKSES: Input Valid -> Panggil Repo & Observer")
    void testSaveMood_Success() {
        int validMood = 5; // Happy
        LocalDate today = LocalDate.now();

        // Setup mock agar return true
        when(repoMock.upsertMood(validMood, today)).thenReturn(true);

        // EKSEKUSI
        moodFacade.saveMood(validMood, today);

        // VERIFIKASI
        verify(repoMock, times(1)).upsertMood(validMood, today);
        verify(observerMock, times(1)).onDataChanged();
    }

    @Test
    @DisplayName("SAVE GAGAL: Mood diluar range (misal -1) -> Jangan panggil Repo")
    void testSaveMood_Fail_InvalidValue() {
        int invalidMood = -1; // Invalid
        LocalDate today = LocalDate.now();

        // EKSEKUSI
        moodFacade.saveMood(invalidMood, today);

        // VERIFIKASI
        verify(repoMock, never()).upsertMood(anyInt(), any());
        verify(observerMock, never()).onDataChanged();
    }

    // ==========================================
    // 2. TEST GET (READ)
    // ==========================================

    @Test
    @DisplayName("GET BY DATE: Data Ditemukan")
    void testGetMood_Found() {
        LocalDate date = LocalDate.now();
        Mood mockMood = new Mood(1, 5, date.toString()); // ID 1, Mood 5

        // SKENARIO: Repo mengembalikan objek Mood
        when(repoMock.getMoodByDate(date)).thenReturn(mockMood);

        // EKSEKUSI
        Mood result = moodFacade.getMood(date);

        // VERIFIKASI
        assertNotNull(result);
        assertEquals(5, result.getMoodValue());
    }

    @Test
    @DisplayName("GET BY DATE: Data Tidak Ditemukan")
    void testGetMood_NotFound() {
        LocalDate date = LocalDate.now();

        // SKENARIO: Repo mengembalikan null
        when(repoMock.getMoodByDate(date)).thenReturn(null);

        // EKSEKUSI
        Mood result = moodFacade.getMood(date);

        // VERIFIKASI
        assertNull(result);
    }

    // ==========================================
    // 3. TEST KHUSUS JCF (HashMap & LinkedList)
    // ==========================================

    @Test
    @DisplayName("JCF LINKEDLIST: Activity Log bertambah saat Save Mood")
    void testActivityLog_LinkedList() {
        // Setup: Repo harus return true (sukses) agar log tercatat
        when(repoMock.upsertMood(anyInt(), any())).thenReturn(true);
        LocalDate date = LocalDate.now();

        // 1. Awalnya log harus kosong
        assertEquals(0, moodFacade.getActivityLog().size());

        // 2. Lakukan Aksi Save Mood
        moodFacade.saveMood(5, date); 
        
        // 3. Cek LinkedList
        // PERBAIKAN DISINI: Gunakan Interface List, bukan LinkedList
        List<String> log = moodFacade.getActivityLog();
        
        // Validasi Ukuran
        assertEquals(1, log.size(), "Log harusnya berisi 1 item");
        
        // Validasi Konten
        // PERBAIKAN DISINI: List tidak punya .getLast(), pakai .get(size - 1)
        String lastEntry = log.get(log.size() - 1);

        assertTrue(lastEntry.contains("Input Mood"), "Isi log harus mengandung 'Input Mood'");
        assertTrue(lastEntry.contains("😊") || lastEntry.contains("😄"), "Harus ada emojinya");
    }

    @Test
    @DisplayName("JCF HASHMAP: Caching berfungsi (Get kedua tidak panggil DB)")
    void testCache_HashMap() {
        LocalDate testDate = LocalDate.of(2023, 12, 1);
        Mood moodMock = new Mood(1, 4, testDate.toString());
        
        // Skenario: DB punya data ini
        when(repoMock.getMoodByDate(testDate)).thenReturn(moodMock);

        // --- PEMANGGILAN PERTAMA (Cache Miss) ---
        Mood result1 = moodFacade.getMood(testDate);
        
        assertNotNull(result1);
        verify(repoMock, times(1)).getMoodByDate(testDate);

        // --- PEMANGGILAN KEDUA (Cache Hit) ---
        Mood result2 = moodFacade.getMood(testDate);
        
        assertNotNull(result2);
        
        // POIN PENTING: Verify tetap times(1). 
        // Artinya panggilan kedua murni diambil dari Memory (HashMap).
        verify(repoMock, times(1)).getMoodByDate(testDate);
    }
}