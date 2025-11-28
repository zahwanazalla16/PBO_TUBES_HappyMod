package com.project.app.view;

import com.project.app.facade.HabitFacade;
import com.project.app.facade.MoodFacade;
import com.project.app.model.Habit;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class MainDashboard extends JFrame implements com.project.app.observer.IObserver {

    private HabitFacade habitFacade = new HabitFacade();
    private MoodFacade moodFacade = new MoodFacade();

    // Komponen Global agar bisa diakses dari method lain
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Habit> habitList; // Untuk menyimpan referensi ID habit yang tampil
    private LocalDate startOfWeek;

    // Warna Modern
    private final Color PRIMARY_COLOR = new Color(54, 59, 78);
    private final Color ACCENT_COLOR = new Color(100, 149, 237);
    private final Color BG_COLOR = new Color(245, 247, 250);

    public MainDashboard() {
        setTitle("Habit & Mood Tracker");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        
        // Tentukan tanggal awal minggu (Senin)
        startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.addTab("Weekly Tracker", createTrackerPanel());
        tabbedPane.addTab("Mood Logger", createMoodPanel());
        tabbedPane.addTab("Analysis", createAnalysisPanel());

        add(tabbedPane);

        habitFacade.addObserver(this);
        
        // Load data pertama kali
        loadHabitData();
    }

    @Override
    public void onDataChanged() {
        // Apa yang dilakukan kalau ada perubahan data? Refresh tabel!
        loadHabitData(); 
    }

    // --- TAB 1: WEEKLY TRACKER ---
    private JPanel createTrackerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);

        // Setup Tabel
        String[] columns = {"Habit", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? String.class : Boolean.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0; // Kolom nama habit (0) tidak bisa diedit langsung (harus klik kanan)
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(PRIMARY_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // --- FITUR 1: KLIK KANAN (CONTEXT MENU) ---
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem editItem = new JMenuItem("Ubah Nama Habit");
        JMenuItem deleteItem = new JMenuItem("Hapus Habit");
        
        // Ikon opsional (kalau mau tampilan lebih bagus, bisa dihapus kalau error)
        // editItem.setIcon(UIManager.getIcon("FileView.fileIcon")); 

        editItem.addActionListener(e -> actionEditHabit());
        deleteItem.addActionListener(e -> actionDeleteHabit());

        popupMenu.add(editItem);
        popupMenu.add(deleteItem);
        
        // Pasang menu ke tabel
        table.setComponentPopupMenu(popupMenu);

        // Agar saat klik kanan, barisnya juga terpilih (selected)
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < table.getRowCount()) {
                        table.setRowSelectionInterval(row, row);
                    }
                }
            }
        });

        // Listener Checkbox
        tableModel.addTableModelListener(e -> {
            if (e.getColumn() > 0 && e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                
                // Cegah error index out of bounds saat refresh data
                if (row < habitList.size()) { 
                    boolean isChecked = (boolean) tableModel.getValueAt(row, col);
                    Habit h = habitList.get(row);
                    LocalDate targetDate = startOfWeek.plusDays(col - 1);
                    
                    habitFacade.updateHabitStatus(h.getId(), targetDate, isChecked);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel Input
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtHabit = new JTextField(20);
        JButton btnAdd = new JButton("Tambah Habit");
        styleButton(btnAdd);

        btnAdd.addActionListener(e -> {
            if (!txtHabit.getText().isEmpty()) {
                habitFacade.addHabit(txtHabit.getText());
                txtHabit.setText("");
            }
        });

        inputPanel.add(new JLabel("Habit Baru: "));
        inputPanel.add(txtHabit);
        inputPanel.add(btnAdd);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // --- LOGIC LOAD DATA ---
    private void loadHabitData() {
        // Kosongkan tabel
        tableModel.setRowCount(0);
        
        // Ambil data terbaru dari database
        habitList = habitFacade.getHabits();

        for (Habit h : habitList) {
            Object[] rowData = new Object[8];
            rowData[0] = h.getName();

            // Cek status ceklis
            for (int i = 0; i < 7; i++) {
                LocalDate dateCheck = startOfWeek.plusDays(i);
                rowData[i + 1] = habitFacade.getHabitStatus(h.getId(), dateCheck);
            }
            tableModel.addRow(rowData);
        }
    }

    // --- LOGIC EDIT & DELETE ---
    
    private void actionEditHabit() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            Habit selectedHabit = habitList.get(selectedRow);
            
            // Tampilkan Popup Input
            String newName = JOptionPane.showInputDialog(this, 
                    "Edit Nama Habit:", 
                    selectedHabit.getName());

            if (newName != null && !newName.trim().isEmpty()) {
                habitFacade.updateHabit(selectedHabit.getId(), newName);
                loadHabitData(); // Refresh UI
                JOptionPane.showMessageDialog(this, "Berhasil diupdate!");
            }
        }
    }

    private void actionDeleteHabit() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            Habit selectedHabit = habitList.get(selectedRow);

            int confirm = JOptionPane.showConfirmDialog(this, 
                    "Yakin ingin menghapus habit '" + selectedHabit.getName() + "'?",
                    "Konfirmasi Hapus",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                habitFacade.deleteHabit(selectedHabit.getId());
                loadHabitData(); // Refresh UI
            }
        }
    }

    // --- TAB 2: MOOD LOGGER ---
    private JPanel createMoodPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_COLOR);

        JLabel lblTitle = new JLabel("Bagaimana perasaanmu hari ini?");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel emojiPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        emojiPanel.setBackground(BG_COLOR);

        String[] emojis = {"😭", "😕", "😐", "🙂", "😀"};
        for (int i = 0; i < emojis.length; i++) {
            final int value = i + 1;
            JButton btn = new JButton(emojis[i]);
            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
            btn.setFocusPainted(false);
            btn.setBackground(Color.WHITE);

            btn.addActionListener(e -> {
                moodFacade.addMood(value, LocalDate.now().toString());
                JOptionPane.showMessageDialog(this, "Mood tercatat!");
            });
            emojiPanel.add(btn);
        }

        panel.add(Box.createVerticalStrut(100));
        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(30));
        panel.add(emojiPanel);
        return panel;
    }

    // --- TAB 3: ANALYSIS ---
    private JPanel createAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea area = new JTextArea();
        area.setFont(new Font("Monospaced", Font.PLAIN, 14));
        area.setEditable(false);
        area.setMargin(new Insets(20, 20, 20, 20));
        area.setText("Analisis akan muncul di sini...");
        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        return panel;
    }

    private void styleButton(JButton btn) {
        btn.setBackground(ACCENT_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }
}