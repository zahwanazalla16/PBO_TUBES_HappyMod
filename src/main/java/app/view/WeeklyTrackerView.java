package app.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import app.facade.HabitFacade;
import app.facade.MoodFacade;
import app.model.Habit;
import app.model.Mood;
import app.observer.IObserver;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedList; 
import java.util.List;

public class WeeklyTrackerView extends JFrame implements IObserver {

    private final HabitFacade habitFacade = new HabitFacade();
    private final MoodFacade moodFacade = new MoodFacade();

    private JTable trackerTable;
    private DefaultTableModel tableModel;
    private List<Habit> habitList;
    
    // [BARU] DUA Text Area terpisah
    private JTextArea habitLogArea; 
    private JTextArea moodLogArea;

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
        
        // Register Observer
        habitFacade.addObserver(this);
        moodFacade.addObserver(this);
        
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
        setSize(1350, 900); 
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_MAIN);

        add(createTrackerPanel());
    }

    private JPanel createTrackerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(40, 50, 40, 50)); 

        panel.add(createHeader(), BorderLayout.NORTH);
        
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20)); 
        contentPanel.setBackground(BG_MAIN);
        
        contentPanel.add(createTablePanel(), BorderLayout.CENTER);
        contentPanel.add(createLogPanel(), BorderLayout.SOUTH); 

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    // [MODIFIKASI] Membuat Panel Log Terpisah (Kiri & Kanan)
    private JPanel createLogPanel() {
        // Container Utama untuk Log
        JPanel mainLogPanel = new JPanel(new GridLayout(1, 2, 20, 0)); // 1 Baris, 2 Kolom, Jarak 20px
        mainLogPanel.setBackground(BG_MAIN);
        mainLogPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR), 
            " Activity Logs (Session) ", 
            0, 0, 
            new Font("Poppins", Font.BOLD, 12), 
            Color.GRAY
        ));

        // 1. Setup Area Kiri (Habit)
        habitLogArea = createStyledTextArea();
        JScrollPane scrollHabit = new JScrollPane(habitLogArea);
        scrollHabit.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(), "✅ Habit Log", 0, 0, new Font("Poppins", Font.BOLD, 11), ACCENT_BROWN
        ));
        
        // 2. Setup Area Kanan (Mood)
        moodLogArea = createStyledTextArea();
        JScrollPane scrollMood = new JScrollPane(moodLogArea);
        scrollMood.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(), "🎭 Mood Log", 0, 0, new Font("Poppins", Font.BOLD, 11), ACCENT_BROWN
        ));

        mainLogPanel.add(scrollHabit);
        mainLogPanel.add(scrollMood);
        
        // Bungkus lagi biar tingginya pas (tidak terlalu tinggi)
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_MAIN);
        wrapper.add(mainLogPanel, BorderLayout.CENTER);
        wrapper.setPreferredSize(new Dimension(0, 180)); // Tinggi fix 180px
        
        return wrapper;
    }

    // Helper untuk style text area biar seragam & support emoji
    private JTextArea createStyledTextArea() {
        JTextArea area = new JTextArea();
        area.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13)); // Font Emoji
        area.setForeground(new Color(80, 80, 80));
        area.setBackground(new Color(252, 252, 252));
        area.setEditable(false); 
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        // Margin dalam text area
        area.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return area;
    }

    // [LOGIKA UPDATE TERPISAH]
    private void updateLogView() {
        // 1. Update Habit Log (Kiri)
        LinkedList<String> habitLogs = habitFacade.getActivityLog();
        StringBuilder sbHabit = new StringBuilder();
        if (habitLogs.isEmpty()) {
            sbHabit.append("- Belum ada aktivitas habit -");
        } else {
            java.util.Iterator<String> it = habitLogs.descendingIterator();
            while (it.hasNext()) {
                sbHabit.append("• ").append(it.next()).append("\n");
            }
        }
        habitLogArea.setText(sbHabit.toString());
        habitLogArea.setCaretPosition(0);

        // 2. Update Mood Log (Kanan)
        LinkedList<String> moodLogs = moodFacade.getActivityLog();
        StringBuilder sbMood = new StringBuilder();
        if (moodLogs.isEmpty()) {
            sbMood.append("- Belum ada aktivitas mood -");
        } else {
            java.util.Iterator<String> it = moodLogs.descendingIterator();
            while (it.hasNext()) {
                sbMood.append("• ").append(it.next()).append("\n");
            }
        }
        moodLogArea.setText(sbMood.toString());
        moodLogArea.setCaretPosition(0);
    }

    private Component createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 30, 0));

        JLabel title = new JLabel("Weekly Tracker");
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
        
        input.setCaretColor(TEXT_DARK);
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
        
        for(java.awt.event.FocusListener fl : field.getFocusListeners()) {
            field.removeFocusListener(fl);
        }

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_DARK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
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

                if (column >= 2 && column <= 8) {
                    if (row == getRowCount() - 1) { 
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
                    } else { 
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
        // 1. Reset Table
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
        
        // 2. Update Log View (Kiri & Kanan)
        updateLogView();
    }
}