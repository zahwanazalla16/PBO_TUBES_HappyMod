package app.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import app.facade.HabitFacade;
import app.facade.MoodFacade;
import app.model.Habit;
import app.model.Mood;
import app.observer.IObserver;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WeeklyTrackerView extends JFrame implements IObserver {

    private static final long serialVersionUID = 1L; 

    private final transient HabitFacade habitFacade = new HabitFacade();
    private final transient MoodFacade moodFacade = new MoodFacade();

    private static final String FONT_POPPINS = "Poppins";
    private static final String FONT_EMOJI = "Segoe UI Emoji";
    private static final String TXT_INPUT_HABIT = "Input habit baru...";
    private static final String TXT_SELECT = "Select ▼";
    
    private static final String[] MOOD_OPTIONS = {"", "😭", "😞", "😐", "😊", "😄"};

    private JTable trackerTable;
    private DefaultTableModel tableModel;
    private transient List<Habit> habitList; 
    
    private JTextArea habitLogArea; 
    private JTextArea moodLogArea;

    private LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

    // Colors
    private static final Color BG_MAIN = Color.WHITE; 
    private static final Color BG_HEADER = new Color(248, 249, 250); 
    private static final Color BORDER_COLOR = new Color(220, 220, 220); 
    private static final Color TEXT_DARK = new Color(33, 37, 41); 
    private static final Color ACCENT_BROWN = new Color(139, 115, 85); 
    private static final Color BG_MOOD_ROW = new Color(250, 248, 245); 
    private static final Color TEXT_PLACEHOLDER = Color.GRAY;
    private static final Color BG_SELECTION = new Color(240, 240, 240);
    private static final Color REF_RED = new Color(255, 100, 120); 

    public WeeklyTrackerView() {
        setupLookAndFeel();
        initFrame();
        
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
        } catch (Exception e) {
            Logger.getLogger(WeeklyTrackerView.class.getName()).log(Level.WARNING, "Look and Feel setup failed", e);
        }
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

    private JPanel createLogPanel() {
        JPanel mainLogPanel = new JPanel(new GridLayout(1, 2, 20, 0)); 
        mainLogPanel.setBackground(BG_MAIN);
        mainLogPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR), 
            " Activity Logs (Session) ", 
            0, 0, 
            new Font(FONT_POPPINS, Font.BOLD, 12), 
            Color.GRAY
        ));

        habitLogArea = createStyledTextArea();
        JScrollPane scrollHabit = new JScrollPane(habitLogArea);
        scrollHabit.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(), "✅ Habit Log", 0, 0, new Font(FONT_POPPINS, Font.BOLD, 11), ACCENT_BROWN
        ));
        
        moodLogArea = createStyledTextArea();
        JScrollPane scrollMood = new JScrollPane(moodLogArea);
        scrollMood.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(), "🎭 Mood Log", 0, 0, new Font(FONT_POPPINS, Font.BOLD, 11), ACCENT_BROWN
        ));

        mainLogPanel.add(scrollHabit);
        mainLogPanel.add(scrollMood);
        
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_MAIN);
        wrapper.add(mainLogPanel, BorderLayout.CENTER);
        wrapper.setPreferredSize(new Dimension(0, 180)); 
        
        return wrapper;
    }

    private JTextArea createStyledTextArea() {
        JTextArea area = new JTextArea();
        area.setFont(new Font(FONT_EMOJI, Font.PLAIN, 13)); 
        area.setForeground(new Color(80, 80, 80));
        area.setBackground(new Color(252, 252, 252));
        area.setEditable(false); 
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return area;
    }

    private void updateLogView() {
        updateSingleLogArea(habitFacade.getActivityLog(), habitLogArea, "- Belum ada aktivitas habit -");
        updateSingleLogArea(moodFacade.getActivityLog(), moodLogArea, "- Belum ada aktivitas mood -");
    }

    private void updateSingleLogArea(List<String> logs, JTextArea area, String emptyMsg) {
        StringBuilder sb = new StringBuilder();
        if (logs.isEmpty()) {
            sb.append(emptyMsg);
        } else {
            ListIterator<String> it = logs.listIterator(logs.size());
            while (it.hasPrevious()) {
                sb.append("• ").append(it.previous()).append("\n");
            }
        }
        area.setText(sb.toString());
        area.setCaretPosition(0);
    }

    private Component createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 30, 0));

        JLabel title = new JLabel("Weekly Tracker");
        title.setFont(new Font(FONT_POPPINS, Font.BOLD, 32));
        title.setForeground(TEXT_DARK);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);
        
        JTextField input = new JTextField(25);
        input.setFont(new Font(FONT_POPPINS, Font.PLAIN, 14));
        input.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(1, 1, 1, 1, BORDER_COLOR), 
            new EmptyBorder(5, 10, 5, 10))
        );
        
        input.setCaretColor(TEXT_DARK);
        setupPlaceholder(input, TXT_INPUT_HABIT);
        
        JButton addBtn = new JButton("+ New Habit");
        addBtn.setBackground(ACCENT_BROWN);
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font(FONT_POPPINS, Font.BOLD, 14));
        addBtn.setFocusPainted(false);
        addBtn.setBorder(new EmptyBorder(8, 20, 8, 20));
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        addBtn.addActionListener(e -> {
            String text = input.getText().trim();
            if (!text.isEmpty() && !text.equals(TXT_INPUT_HABIT)) {
                habitFacade.addHabit(text);
                input.setText("");
                setupPlaceholder(input, TXT_INPUT_HABIT); 
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

        // [FIX] Menggunakan Inner Class TrackerTableModel
        tableModel = new TrackerTableModel(columns, 0);

        trackerTable = new JTable(tableModel);
        trackerTable.setDefaultRenderer(Object.class, new TrackerCellRenderer());
        trackerTable.setDefaultEditor(Object.class, new TrackerCellEditor());

        trackerTable.setRowHeight(60);
        trackerTable.setShowGrid(true); 
        trackerTable.setShowVerticalLines(true);
        trackerTable.setGridColor(BORDER_COLOR);
        trackerTable.setIntercellSpacing(new Dimension(1, 1));
        
        trackerTable.getTableHeader().setBackground(BG_HEADER); 
        trackerTable.getTableHeader().setForeground(TEXT_DARK);
        trackerTable.getTableHeader().setFont(new Font(FONT_POPPINS, Font.BOLD, 14));
        trackerTable.getTableHeader().setPreferredSize(new Dimension(0, 50)); 
        trackerTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR));

        TableColumn deleteCol = trackerTable.getColumnModel().getColumn(9);
        deleteCol.setMaxWidth(80); 
        
        trackerTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
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

    // --- INNER CLASS: Custom Table Model (Extracted) ---
    private static class TrackerTableModel extends DefaultTableModel {
        private static final long serialVersionUID = 1L;

        public TrackerTableModel(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return Object.class;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column >= 2 && column <= 8; 
        }
    }

    // --- INNER CLASS: Custom Renderer ---
    private class TrackerCellRenderer extends DefaultTableCellRenderer {
        private final Font emojiFont = new Font(FONT_EMOJI, Font.PLAIN, 28);
        private final Font moodHeaderFont = new Font(FONT_POPPINS, Font.BOLD, 16);
        private final Font cellFont = new Font(FONT_POPPINS, Font.PLAIN, 15);
        private final JCheckBox checkBox = new JCheckBox();
        private final JButton deleteBtn = new JButton("×");

        public TrackerCellRenderer() {
            checkBox.setHorizontalAlignment(SwingConstants.CENTER);
            
            deleteBtn.setFont(new Font(FONT_POPPINS, Font.BOLD, 24)); 
            deleteBtn.setForeground(REF_RED); 
            deleteBtn.setFocusPainted(false);
            deleteBtn.setBorderPainted(false);
            deleteBtn.setContentAreaFilled(false); 
            deleteBtn.setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // [FIX] Mengurangi kompleksitas dengan memecah method
            JComponent c = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            boolean isMoodRow = (row == table.getRowCount() - 1);
            
            configureBackground(c, isSelected, isMoodRow);
            
            if (c instanceof JLabel label) {
                label.setForeground(isMoodRow ? ACCENT_BROWN : TEXT_DARK);
            }

            if (column == 0 || column == 1) {
                return configureLabelColumn((JLabel) c, isMoodRow, column);
            }

            if (column == 9) {
                return configureDeleteButton(c, isMoodRow, isSelected);
            }

            if (isMoodRow) {
                return configureMoodCell((JLabel) c, value);
            } else {
                return configureCheckboxCell(value, isSelected);
            }
        }

        // Helper Method 1
        private void configureBackground(JComponent c, boolean isSelected, boolean isMoodRow) {
            Color bgColor;
            if (isSelected) {
                bgColor = BG_SELECTION;
            } else if (isMoodRow) {
                bgColor = BG_MOOD_ROW;
            } else {
                bgColor = BG_MAIN;
            }
            c.setBackground(bgColor);
            c.setBorder(isMoodRow ? BorderFactory.createMatteBorder(2, 0, 0, 0, BORDER_COLOR) : null);
        }

        // Helper Method 2
        private JLabel configureLabelColumn(JLabel l, boolean isMoodRow, int column) {
            l.setFont(isMoodRow ? moodHeaderFont : cellFont);
            l.setHorizontalAlignment(column == 0 ? SwingConstants.CENTER : SwingConstants.LEFT);
            if (column == 1) l.setBorder(BorderFactory.createCompoundBorder(l.getBorder(), new EmptyBorder(0, 10, 0, 0)));
            return l;
        }

        // Helper Method 3
        private Component configureDeleteButton(JComponent c, boolean isMoodRow, boolean isSelected) {
            if (isMoodRow) return c; 
            deleteBtn.setBackground(isSelected ? BG_SELECTION : BG_MAIN);
            return deleteBtn;
        }

        // Helper Method 4
        private JLabel configureMoodCell(JLabel l, Object value) {
            l.setHorizontalAlignment(SwingConstants.CENTER);
            String text = (String) value;
            if (text == null || text.trim().isEmpty()) {
                l.setFont(new Font(FONT_POPPINS, Font.ITALIC, 12));
                l.setForeground(Color.GRAY);
                l.setText(TXT_SELECT);
            } else {
                l.setFont(emojiFont);
                l.setText(text);
            }
            return l;
        }

        // Helper Method 5
        private JCheckBox configureCheckboxCell(Object value, boolean isSelected) {
            checkBox.setBackground(isSelected ? BG_SELECTION : BG_MAIN);
            checkBox.setSelected(Boolean.TRUE.equals(value));
            return checkBox;
        }
    }

    private class TrackerCellEditor extends DefaultCellEditor {
        public TrackerCellEditor() {
            super(new JCheckBox());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (row == table.getRowCount() - 1) {
                JComboBox<String> combo = new JComboBox<>(MOOD_OPTIONS);
                combo.setFont(new Font(FONT_EMOJI, Font.PLAIN, 28));
                ((JLabel)combo.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
                return combo;
            }
            
            JCheckBox cb = (JCheckBox) super.getTableCellEditorComponent(table, value, isSelected, row, column);
            cb.setHorizontalAlignment(SwingConstants.CENTER);
            cb.setBackground(BG_MAIN);
            return cb;
        }
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
        
        updateLogView();
    }
}