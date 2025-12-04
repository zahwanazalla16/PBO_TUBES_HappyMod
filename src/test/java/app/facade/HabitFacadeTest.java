package app.facade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Import Package Aplikasi
import app.model.Habit;
import app.observer.IObserver;
import app.repository.HabitRepository;

// Import Java Utilities
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

// Import JUnit & Mockito Static
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@DisplayName("Test Lengkap HabitFacade (Add, Delete, Tracking)")
class HabitFacadeTest {

    // 1. Siapkan Objek Mock (Palsu)
    private HabitRepository repositoryMock;
    private IObserver observerMock;

    // 2. Siapkan Objek Asli yang mau dites
    private HabitFacade habitFacade;

    @BeforeEach
    void setUp() {
        // Inisialisasi Mock
        repositoryMock = mock(HabitRepository.class);
        observerMock = mock(IObserver.class);

        // Masukkan Repository Mock ke dalam Facade (Constructor Injection)
        habitFacade = new HabitFacade(repositoryMock);

        // Daftarkan Observer Mock agar kita bisa cek notifikasi
        habitFacade.addObserver(observerMock);
    }

    // ==========================================
    // 1. TEST FITUR ADD HABIT
    // ==========================================

    @Test
    @DisplayName("ADD SUKSES: Data valid & tersimpan di DB -> Observer harus bunyi")
    void testAddHabit_Success() {
        // SKENARIO: Repository berhasil simpan (return true)
        when(repositoryMock.createHabit(any(Habit.class))).thenReturn(true);

        // EKSEKUSI
        boolean result = habitFacade.addHabit("Belajar Coding");

        // VERIFIKASI
        assertTrue(result, "Seharusnya return true");
        verify(repositoryMock, times(1)).createHabit(any(Habit.class)); // Pastikan repo dipanggil
        verify(observerMock, times(1)).onDataChanged(); // Observer WAJIB dipanggil
    }

    @Test
    @DisplayName("ADD GAGAL: Nama kosong (Validasi Bisnis) -> Repo & Observer jangan disentuh")
    void testAddHabit_Fail_EmptyName() {
        // EKSEKUSI: Input nama kosong
        boolean result = habitFacade.addHabit(""); 

        // VERIFIKASI
        assertFalse(result, "Seharusnya return false karena nama kosong");
        verify(repositoryMock, never()).createHabit(any()); // Repo tidak boleh dipanggil
        verify(observerMock, never()).onDataChanged();      // Observer tidak boleh bunyi
    }

    @Test
    @DisplayName("ADD GAGAL: Database Error -> Observer jangan bunyi")
    void testAddHabit_Fail_DatabaseError() {
        // SKENARIO: Repository gagal simpan (misal koneksi putus)
        when(repositoryMock.createHabit(any(Habit.class))).thenReturn(false);

        // EKSEKUSI
        boolean result = habitFacade.addHabit("Lari Pagi");

        // VERIFIKASI
        assertFalse(result, "Seharusnya false karena DB error");
        verify(observerMock, never()).onDataChanged(); // Kalau gagal simpan, UI jangan diupdate
    }

    // ==========================================
    // 2. TEST FITUR DELETE HABIT
    // ==========================================

    @Test
    @DisplayName("DELETE SUKSES: ID ada -> Observer harus bunyi")
    void testDeleteHabit_Success() {
        // SKENARIO: Repo berhasil delete
        when(repositoryMock.deleteHabit(1)).thenReturn(true);

        // EKSEKUSI
        boolean result = habitFacade.deleteHabit(1);

        // VERIFIKASI
        assertTrue(result);
        verify(observerMock, times(1)).onDataChanged();
    }

    @Test
    @DisplayName("DELETE GAGAL: ID salah / DB Error -> Observer diam")
    void testDeleteHabit_Fail() {
        // SKENARIO: Repo gagal delete
        when(repositoryMock.deleteHabit(1)).thenReturn(false);

        // EKSEKUSI
        boolean result = habitFacade.deleteHabit(1);

        // VERIFIKASI
        assertFalse(result);
        verify(observerMock, never()).onDataChanged();
    }

    // ==========================================
    // 3. TEST FITUR GET (READ DATA)
    // ==========================================

    @Test
    @DisplayName("GET ALL: Mengambil semua data")
    void testGetHabits() {
        // Setup data palsu
        List<Habit> dummyList = new ArrayList<>();
        dummyList.add(new Habit(1, "Test 1"));
        dummyList.add(new Habit(2, "Test 2"));

        when(repositoryMock.getAllHabits()).thenReturn(dummyList);

        // EKSEKUSI
        List<Habit> result = habitFacade.getHabits();

        // VERIFIKASI
        assertEquals(2, result.size());
        assertEquals("Test 1", result.get(0).getName());
    }

    @Test
    @DisplayName("GET BY ID: Data ditemukan vs Tidak ditemukan")
    void testGetHabitById() {
        // Skenario 1: Ketemu
        Habit h = new Habit(10, "Found Me");
        when(repositoryMock.getHabitById(10)).thenReturn(h);
        
        Habit resultFound = habitFacade.getHabit(10);
        assertNotNull(resultFound);
        assertEquals("Found Me", resultFound.getName());

        // Skenario 2: Tidak Ketemu (Null)
        when(repositoryMock.getHabitById(99)).thenReturn(null);
        
        Habit resultNull = habitFacade.getHabit(99);
        assertNull(resultNull);
    }

    // ==========================================
    // 4. TEST FITUR TRACKING (CHECKBOX)
    // ==========================================

    @Test
    @DisplayName("TRACKING: Cek Status Habit (True/False)")
    void testGetHabitStatus() {
        LocalDate today = LocalDate.now();

        // Skenario: Sudah diceklis
        when(repositoryMock.isHabitDone(1, today)).thenReturn(true);
        assertTrue(habitFacade.getHabitStatus(1, today));

        // Skenario: Belum diceklis
        when(repositoryMock.isHabitDone(1, today)).thenReturn(false);
        assertFalse(habitFacade.getHabitStatus(1, today));
    }

    @Test
    @DisplayName("TRACKING: Update Status (Centang/Uncentang)")
    void testUpdateHabitStatus() {
        LocalDate today = LocalDate.now();
        // Setup repo sukses
        when(repositoryMock.setHabitStatus(anyInt(), any(), anyBoolean())).thenReturn(true);

        // EKSEKUSI: Centang (True)
        habitFacade.updateHabitStatus(1, today, true);

        // VERIFIKASI: Pastikan method di repo dipanggil dengan parameter yg benar
        verify(repositoryMock).setHabitStatus(1, today, true);
        verify(observerMock).onDataChanged(); // UI update
    }


    // ==========================================
    // 5. TEST KHUSUS JCF (HashMap & LinkedList)
    // ==========================================

    @Test
    @DisplayName("JCF LINKEDLIST: Activity Log bertambah saat ada aksi")
    void testActivityLog_LinkedList() {
        // Setup Repository selalu sukses
        when(repositoryMock.createHabit(any())).thenReturn(true);
        when(repositoryMock.deleteHabit(anyInt())).thenReturn(true);

        // 1. Awalnya log harus kosong
        assertEquals(0, habitFacade.getActivityLog().size());

        // 2. Lakukan Aksi Tambah
        habitFacade.addHabit("Habit A");
        
        // Cek apakah LinkedList nambah?
        LinkedList<String> log = habitFacade.getActivityLog();
        assertEquals(1, log.size(), "Log harusnya berisi 1 item");
        assertTrue(log.getLast().contains("Menambahkan"), "Isi log harus benar");

        // 3. Lakukan Aksi Hapus
        habitFacade.deleteHabit(1);

        // Cek lagi
        assertEquals(2, log.size(), "Log harusnya berisi 2 item");
        assertTrue(log.getLast().contains("Menghapus"), "Isi log terakhir harus delete");
    }

    @Test
    @DisplayName("JCF HASHMAP: Caching berfungsi (Get kedua tidak panggil DB)")
    void testCache_HashMap() {
        int testId = 100;
        Habit habitMock = new Habit(testId, "Test Cache");
        
        // Skenario: DB punya data ini
        when(repositoryMock.getHabitById(testId)).thenReturn(habitMock);

        // --- PEMANGGILAN PERTAMA (Cache Miss) ---
        // Karena cache kosong, dia harus nanya ke Repository (DB)
        Habit result1 = habitFacade.getHabit(testId);
        
        assertNotNull(result1);
        // Pastikan repo dipanggil 1 kali
        verify(repositoryMock, times(1)).getHabitById(testId);

        // --- PEMANGGILAN KEDUA (Cache Hit) ---
        // Sekarang data harusnya sudah ada di HashMap
        Habit result2 = habitFacade.getHabit(testId);
        
        assertNotNull(result2);
        // POIN PENTING: Verify tetap times(1). 
        // Artinya pada panggilan kedua, dia TIDAK memanggil repositoryMock lagi.
        // Dia ambil langsung dari HashMap.
        verify(repositoryMock, times(1)).getHabitById(testId);
    }
}