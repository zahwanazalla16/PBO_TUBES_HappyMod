package app.facade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

// Import Package Aplikasi
import app.model.Mood;
import app.observer.IObserver;
import app.repository.MoodRepository;

// Import Java Utilities
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Import JUnit & Mockito
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Test Lengkap MoodFacade (CRUD + Validasi)")
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

        // EKSEKUSI
        moodFacade.saveMood(validMood, today);

        // VERIFIKASI
        // Pastikan fungsi upsertMood dipanggil dengan parameter yang benar
        verify(repoMock, times(1)).upsertMood(validMood, today);
        // Pastikan Observer diberitahu
        verify(observerMock, times(1)).onDataChanged();
    }

    @Test
    @DisplayName("SAVE GAGAL: Mood diluar range (misal 0) -> Jangan panggil Repo")
    void testSaveMood_Fail_InvalidValue() {
        int invalidMood = 0; // Tidak ada emoji untuk 0
        LocalDate today = LocalDate.now();

        // EKSEKUSI
        moodFacade.saveMood(invalidMood, today);

        // VERIFIKASI
        // Repo tidak boleh dipanggil karena validasi di Facade mencegahnya
        verify(repoMock, never()).upsertMood(anyInt(), any());
        // Observer jangan bunyi
        verify(observerMock, never()).onDataChanged();
    }

    // ==========================================
    // 2. TEST ADD MOOD (String Date)
    // ==========================================

    @Test
    @DisplayName("ADD SUKSES: Input Valid -> Panggil CreateMood")
    void testAddMood_Success() {
        // EKSEKUSI
        moodFacade.addMood(4, "2023-10-01");

        // VERIFIKASI
        verify(repoMock, times(1)).createMood(any(Mood.class));
        verify(observerMock, times(1)).onDataChanged();
    }

    @Test
    @DisplayName("ADD GAGAL: Mood > 5 -> Validasi block")
    void testAddMood_Fail() {
        // EKSEKUSI
        moodFacade.addMood(10, "2023-10-01"); // Nilai 10 tidak valid

        // VERIFIKASI
        verify(repoMock, never()).createMood(any());
        verify(observerMock, never()).onDataChanged();
    }

    // ==========================================
    // 3. TEST UPDATE MOOD
    // ==========================================

    @Test
    @DisplayName("UPDATE SUKSES: Ubah nilai mood")
    void testUpdateMood_Success() {
        // EKSEKUSI
        moodFacade.updateMood(1, 3); // Ubah ID 1 jadi Mood 3

        // VERIFIKASI
        verify(repoMock, times(1)).updateMood(1, 3);
        verify(observerMock, times(1)).onDataChanged();
    }

    @Test
    @DisplayName("UPDATE GAGAL: Nilai baru tidak valid")
    void testUpdateMood_Fail() {
        // EKSEKUSI
        moodFacade.updateMood(1, -5); // Nilai minus

        // VERIFIKASI
        verify(repoMock, never()).updateMood(anyInt(), anyInt());
        verify(observerMock, never()).onDataChanged();
    }

    // ==========================================
    // 4. TEST DELETE MOOD
    // ==========================================

    @Test
    @DisplayName("DELETE SUKSES: Hapus data")
    void testDeleteMood_Success() {
        // EKSEKUSI
        moodFacade.deleteMood(5);

        // VERIFIKASI
        verify(repoMock, times(1)).deleteMood(5);
        verify(observerMock, times(1)).onDataChanged();
    }

    // (Delete void biasanya tidak ada skenario gagal di level Facade 
    // kecuali repo melempar exception, tapi untuk sekarang ini cukup)

    // ==========================================
    // 5. TEST GET (READ)
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

    @Test
    @DisplayName("GET ALL: Return list")
    void testGetAllMood() {
        List<Mood> list = new ArrayList<>();
        list.add(new Mood(5, "2023-10-01"));

        when(repoMock.getAllMood()).thenReturn(list);

        List<Mood> result = moodFacade.getAllMood();
        assertEquals(1, result.size());
    }
}