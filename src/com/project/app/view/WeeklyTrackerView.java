package com.project.app.view;

import com.project.app.facade.HabitFacade;
import com.project.app.facade.MoodFacade;
import com.project.app.model.Habit;
import com.project.app.model.Mood;
import com.project.app.observer.IObserver;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class WeeklyTrackerView extends JFrame implements IObserver {

    private final HabitFacade habitFacade = new HabitFacade();
    private final MoodFacade moodFacade = new MoodFacade();

    private JTable trackerTable;
    private DefaultTableModel tableModel;
    private List<Habit> habitList;
    
    private LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

    // --- PALET WARNA ---
    private static final Color BG_MAIN = Color.WHITE; 
    private static final Color BG_HEADER = new Color(248, 249, 250); 
    private static final Color BORDER_COLOR = new Color(220, 220, 220); 
    private static final Color TEXT_DARK = new Color(33, 37, 41); 
    private static final Color ACCENT_BROWN = new Color(139, 115, 85); 
    private static final Color BG_MOOD_ROW = new Color(250, 248, 245); 
    private static final Color TEXT_PLACEHOLDER = Color.GRAY;
    private static final Color BG_SELECTION = new Color(240, 240, 240);
    private static final Color REF_RED = new Color(255, 100, 120); 

    private final String[] MOOD_OPTIONS = {"", "😭", "😞", "😐", "😊", "😄"}; 

    public WeeklyTrackerView() {
        setupLookAndFeel();
        initFrame();
        habitFacade.addObserver(this);
        loadData();
    }

    private void setupLookAndFeel() {
        try {
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("Table.showVerticalLines", true); 
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.gridColor", BORDER_COLOR);
        } catch (Exception e) {}
    }

    private void initFrame() {
        setTitle("MoodFlow • Weekly Tracker");
        setSize(1350, 850);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // Agar Main Menu tidak ikut tertutup
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_MAIN);

        add(createTrackerPanel());
    }

    private JPanel createTrackerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(40, 50, 50, 50)); 

        panel.add(createHeader(), BorderLayout.NORTH);
        panel.add(createTablePanel(), BorderLayout.CENTER);

        return panel;
    }

    private Component createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 30, 0));

        JLabel title = new JLabel("Weekly Overview");
        title.setFont(new Font("Poppins", Font.BOLD, 32));
        title.setForeground(TEXT_DARK);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);
        
        JTextField input = new JTextField(25);
        input.setFont(new Font("Poppins", Font.PLAIN, 14));
        input.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(1, 1, 1, 1, BORDER_COLOR), 
            new EmptyBorder(5, 10, 5, 10))
        );
        
        // --- FITUR CURSOR KEDIP-KEDIP ---
        input.setCaretColor(TEXT_DARK); // Memastikan garis cursor berwarna gelap agar terlihat
        
        // Setup Placeholder
        setupPlaceholder(input, "Input habit baru...");
        
        JButton addBtn = new JButton("+ New Habit");
        addBtn.setBackground(ACCENT_BROWN);
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font("Poppins", Font.BOLD, 14));
        addBtn.setFocusPainted(false);
        addBtn.setBorder(new EmptyBorder(8, 20, 8, 20));
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        addBtn.addActionListener(e -> {
            String text = input.getText().trim();
            if (!text.isEmpty() && !text.equals("Input habit baru...")) {
                habitFacade.addHabit(text);
                input.setText("");
                setupPlaceholder(input, "Input habit baru..."); 
                addBtn.requestFocusInWindow(); 
            }
        });

        right.add(input);
        right.add(Box.createHorizontalStrut(15));
        right.add(addBtn);

        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private void setupPlaceholder(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(TEXT_PLACEHOLDER);
        
        // Bersihkan listener lama (jika ada) untuk mencegah duplikasi event
        for(java.awt.event.FocusListener fl : field.getFocusListeners()) {
            field.removeFocusListener(fl);
        }

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                // Saat diklik, jika isinya masih placeholder, kosongkan
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_DARK);
                }
                // Saat teks kosong, Java Swing otomatis menampilkan cursor (caret) kedip-kedip
            }
            @Override
            public void focusLost(FocusEvent e) {
                // Saat ditinggalkan, jika kosong, kembalikan placeholder
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(TEXT_PLACEHOLDER);
                }
            }
        });
    }

    private JScrollPane createTablePanel() {
        setupTable();
        JScrollPane scroll = new JScrollPane(trackerTable);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1)); 
        scroll.getViewport().setBackground(BG_MAIN);
        return scroll;
    }

    private void setupTable() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE dd/MM");
        String[] columns = new String[10];
        columns[0] = "No";
        columns[1] = "Activity"; 
        for (int i = 0; i < 7; i++) {
            columns[i + 2] = weekStart.plusDays(i).format(formatter);
        }
        columns[9] = "Action"; 

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return Object.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 2 && column <= 8; 
            }
        };

        trackerTable = new JTable(tableModel) {
            Font emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 28);
            Font normalFont = new Font("Poppins", Font.PLAIN, 14);
            Font moodHeaderFont = new Font("Poppins", Font.BOLD, 16);
            Font cellFont = new Font("Poppins", Font.PLAIN, 15);

            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                // A. LOGIKA UNTUK MOOD ROW (BARIS PALING BAWAH)
                if (row == getRowCount() - 1) {
                    if (column == 0 || column == 1) {
                        return new DefaultTableCellRenderer() {
                            @Override
                            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                                l.setBackground(BG_MOOD_ROW); 
                                l.setFont(moodHeaderFont);
                                l.setForeground(ACCENT_BROWN);
                                l.setHorizontalAlignment(column == 1 ? SwingConstants.LEFT : SwingConstants.CENTER);
                                if (column == 0) l.setText(""); 
                                l.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, BORDER_COLOR));
                                return l;
                            }
                        };
                    } else if (column == 9) {
                        return new DefaultTableCellRenderer() {
                             @Override
                             public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                                 JPanel p = new JPanel();
                                 p.setBackground(BG_MOOD_ROW);
                                 p.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, BORDER_COLOR));
                                 return p;
                             }
                        };
                    }
                }

                // B. ISI DATA
                if (column >= 2 && column <= 8) {
                    if (row == getRowCount() - 1) { // MOOD EMOJI
                        return new DefaultTableCellRenderer() {
                            @Override
                            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                                l.setHorizontalAlignment(SwingConstants.CENTER);
                                l.setBackground(BG_MOOD_ROW);
                                l.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, BORDER_COLOR));
                                
                                String text = (String) value;
                                if (text == null || text.trim().isEmpty()) {
                                    l.setFont(new Font("Poppins", Font.ITALIC, 12));
                                    l.setForeground(Color.GRAY);
                                    l.setText("Select ▼");
                                } else {
                                    l.setFont(emojiFont);
                                    l.setForeground(TEXT_DARK);
                                    l.setText(text);
                                }
                                return l;
                            }
                        };
                    } else { // HABIT CHECKBOX
                        return new TableCellRenderer() {
                            final JCheckBox cb = new JCheckBox();
                            @Override
                            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                                cb.setHorizontalAlignment(JLabel.CENTER);
                                cb.setBackground(isSelected ? BG_SELECTION : BG_MAIN);
                                if (value instanceof Boolean) {
                                    cb.setSelected((Boolean) value);
                                } else {
                                    cb.setSelected(false);
                                }
                                return cb;
                            }
                        };
                    }
                }
                
                // C. Kolom Text Biasa
                if (column == 0 || column == 1) {
                    return new DefaultTableCellRenderer() {
                        @Override
                        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                             JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                             l.setFont(cellFont);
                             l.setForeground(TEXT_DARK);
                             l.setBackground(isSelected ? BG_SELECTION : BG_MAIN);
                             if(column == 1) l.setBorder(new EmptyBorder(0, 10, 0, 0));
                             if(column == 0) l.setHorizontalAlignment(CENTER);
                             return l;
                        }
                    };
                }

                return super.getCellRenderer(row, column);
            }

            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if (column >= 2 && column <= 8) {
                    if (row == getRowCount() - 1) {
                        JComboBox<String> combo = new JComboBox<>(MOOD_OPTIONS);
                        combo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
                        ((JLabel)combo.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
                        return new DefaultCellEditor(combo);
                    } else {
                        JCheckBox cb = new JCheckBox();
                        cb.setHorizontalAlignment(JLabel.CENTER);
                        cb.setBackground(BG_MAIN);
                        return new DefaultCellEditor(cb);
                    }
                }
                return super.getCellEditor(row, column);
            }
        };

        // --- STYLE TABLE ---
        trackerTable.setRowHeight(60);
        trackerTable.setShowGrid(true); 
        trackerTable.setShowVerticalLines(true);
        trackerTable.setGridColor(BORDER_COLOR);
        trackerTable.setIntercellSpacing(new Dimension(1, 1));
        
        trackerTable.getTableHeader().setBackground(BG_HEADER); 
        trackerTable.getTableHeader().setForeground(TEXT_DARK);
        trackerTable.getTableHeader().setFont(new Font("Poppins", Font.BOLD, 14));
        trackerTable.getTableHeader().setPreferredSize(new Dimension(0, 50)); 
        ((JComponent)trackerTable.getTableHeader()).setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR));

        // === TOMBOL DELETE MERAH ===
        TableColumn deleteCol = trackerTable.getColumnModel().getColumn(9);
        deleteCol.setMaxWidth(80); 
        
        deleteCol.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (row == table.getRowCount() - 1) {
                    JPanel p = new JPanel();
                    p.setBackground(BG_MOOD_ROW);
                    p.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, BORDER_COLOR));
                    return p;
                }
                JButton btn = new JButton("×");
                btn.setFont(new Font("Poppins", Font.BOLD, 24)); 
                btn.setForeground(REF_RED); 
                btn.setBackground(isSelected ? BG_SELECTION : BG_MAIN); 
                btn.setFocusPainted(false);
                btn.setBorderPainted(false);
                btn.setContentAreaFilled(false); 
                btn.setOpaque(true);
                return btn;
            }
        });

        // Event Listener
        trackerTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = trackerTable.rowAtPoint(evt.getPoint());
                int col = trackerTable.columnAtPoint(evt.getPoint());
                if (col == 9 && row >= 0 && row < habitList.size()) {
                    confirmAndDelete(row);
                }
            }
        });

        tableModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                if (col >= 2 && col <= 8) {
                    handleDataChange(row, col);
                }
            }
        });
    }

    private void handleDataChange(int row, int col) {
        int dayIndex = col - 2;
        LocalDate date = weekStart.plusDays(dayIndex);

        if (row == tableModel.getRowCount() - 1) {
            String emoji = (String) tableModel.getValueAt(row, col);
            int moodVal = 0;
            for (int i=1; i<MOOD_OPTIONS.length; i++) {
                if (MOOD_OPTIONS[i].equals(emoji)) {
                    moodVal = i;
                    break;
                }
            }
            moodFacade.saveMood(moodVal, date);
        } else if (row < habitList.size()) {
            boolean isChecked = Boolean.TRUE.equals(tableModel.getValueAt(row, col));
            Habit h = habitList.get(row);
            habitFacade.updateHabitStatus(h.getId(), date, isChecked);
        }
    }

    private void confirmAndDelete(int row) {
        Habit h = habitList.get(row);
        int choice = JOptionPane.showConfirmDialog(this, 
            "Apakah Anda yakin ingin menghapus habit '" + h.getName() + "'?", 
            "Konfirmasi Hapus", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (choice == JOptionPane.YES_OPTION) {
            habitFacade.deleteHabit(h.getId());
        }
    }

    @Override
    public void onDataChanged() {
        SwingUtilities.invokeLater(this::loadData);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        habitList = habitFacade.getHabits();
        int no = 1;
        for (Habit h : habitList) {
            Object[] row = new Object[10];
            row[0] = no++;
            row[1] = h.getName();
            for (int i = 0; i < 7; i++) {
                row[i + 2] = habitFacade.getHabitStatus(h.getId(), weekStart.plusDays(i));
            }
            row[9] = ""; 
            tableModel.addRow(row);
        }

        Object[] moodRow = new Object[10];
        moodRow[0] = "";
        moodRow[1] = "Daily Mood"; 
        
        for (int i = 0; i < 7; i++) {
            LocalDate d = weekStart.plusDays(i);
            Mood m = moodFacade.getMood(d);
            if (m != null && m.getMoodValue() >= 1 && m.getMoodValue() <= 5) {
                moodRow[i+2] = MOOD_OPTIONS[m.getMoodValue()];
            } else {
                moodRow[i+2] = ""; 
            }
        }
        moodRow[9] = "";
        tableModel.addRow(moodRow);
    }
}