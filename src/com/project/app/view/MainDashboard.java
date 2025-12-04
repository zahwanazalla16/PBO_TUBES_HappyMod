package com.project.app.view;

import com.project.app.facade.HabitFacade;
import com.project.app.facade.MoodFacade;
import com.project.app.model.Habit;
import com.project.app.observer.IObserver;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class MainDashboard extends JFrame implements IObserver {

    private final HabitFacade habitFacade = new HabitFacade();
    private final MoodFacade moodFacade = new MoodFacade();

    private JTable habitTable;
    private DefaultTableModel tableModel;
    private List<Habit> habitList;
    private LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);

    private static final Color PASTEL_BLUE    = new Color(173, 216, 255);
    private static final Color PASTEL_YELLOW  = new Color(244, 238, 177);
    private static final Color BG_WHITE       = new Color(252, 252, 255);
    private static final Color ACCENT_BLUE    = new Color(100, 180, 255);
    private static final Color ACCENT_YELLOW  = new Color(255, 220, 100);
    private static final Color TEXT_DARK      = new Color(40, 50, 80);
    private static final Color RED         = new Color(255, 100, 120);

    public MainDashboard() {
        setupLookAndFeel();
        initFrame();
        habitFacade.addObserver(this);
        loadHabitData();
    }

    private void setupLookAndFeel() {
        try {
            // UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            UIManager.put("Button.arc", 16);
            UIManager.put("Component.arc", 12);
            UIManager.put("Table.rowHeight", 60);
            UIManager.put("TitlePane.background", PASTEL_YELLOW);
            UIManager.put("TitlePane.foreground", TEXT_DARK);
        } catch (Exception e) {
            System.err.println("FlatLaf tidak ditemukan!");
        }
    }

    private void initFrame() {
        setTitle("MoodFlow • Your Daily Habit & Mood Companion");
        setSize(1350, 850);
        setMinimumSize(new Dimension(1000, 600));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Poppins", Font.BOLD, 17));
        tabs.setBackground(PASTEL_YELLOW);
        tabs.setForeground(TEXT_DARK);

        tabs.addTab("Weekly Tracker", createTrackerTab());
        tabs.addTab("Mood Logger", createMoodTab());
        tabs.addTab("Analysis", createAnalysisTab());

        add(tabs);
    }

    private JPanel createTrackerTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PASTEL_YELLOW);
        panel.setBorder(new EmptyBorder(30, 35, 35, 35));

        panel.add(createHeader(), BorderLayout.NORTH);
        panel.add(createTablePanel(), BorderLayout.CENTER);

        return panel;
    }

    private Component createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("My Weekly Habits");
        title.setFont(new Font("Poppins", Font.BOLD, 36));
        title.setForeground(TEXT_DARK);

        JTextField input = new JTextField(22);
        input.setFont(new Font("Poppins", Font.PLAIN, 16));

        JButton addBtn = new JButton("Add New Habit");
        addBtn.setBackground(PASTEL_BLUE);
        addBtn.setForeground(TEXT_DARK);
        addBtn.setFont(new Font("Poppins", Font.BOLD, 15));
        addBtn.setFocusPainted(false);
        addBtn.addActionListener(e -> {
            String name = input.getText().trim();
            if (!name.isEmpty()) {
                habitFacade.addHabit(name);
                input.setText("");
            }
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);
        right.add(new JLabel("New habit: "));
        right.add(input);
        right.add(Box.createHorizontalStrut(10));
        right.add(addBtn);

        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JScrollPane createTablePanel() {
        setupTable();
        JScrollPane scroll = new JScrollPane(habitTable);
        scroll.setBorder(BorderFactory.createLineBorder(PASTEL_BLUE, 3, true));
        scroll.getViewport().setBackground(PASTEL_BLUE);
        return scroll;
    }

    private void setupTable() {
        String[] columns = {"No", "Habit Name", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun", ""};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex >= 2 && columnIndex <= 8 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 2 && column <= 8;
            }
        };

        habitTable = new JTable(tableModel);
        habitTable.setRowHeight(65);
        habitTable.setFont(new Font("Poppins", Font.PLAIN, 16));
        habitTable.setGridColor(PASTEL_BLUE);
        habitTable.setShowGrid(true);

        // Header
        habitTable.getTableHeader().setBackground(PASTEL_BLUE);
        habitTable.getTableHeader().setForeground(TEXT_DARK);
        habitTable.getTableHeader().setFont(new Font("Poppins", Font.BOLD, 15));

        // Semua Kolom Hari Menggunakan Checkbox
        for (int i = 2; i <= 8; i++) {
            habitTable.getColumnModel().getColumn(i).setCellRenderer(habitTable.getDefaultRenderer(Boolean.class));
            habitTable.getColumnModel().getColumn(i).setCellEditor(habitTable.getDefaultEditor(Boolean.class));
            habitTable.getColumnModel().getColumn(i).setPreferredWidth(90);
            habitTable.getColumnModel().getColumn(i).setMaxWidth(100);
        }

        // Kolom Delete
        TableColumn deleteCol = habitTable.getColumnModel().getColumn(9);
        deleteCol.setMaxWidth(70);
        deleteCol.setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
            JButton btn = new JButton("×");
            btn.setFont(new Font("Poppins", Font.BOLD, 24));
            btn.setForeground(RED);
            btn.setBackground(BG_WHITE);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return btn;
        });

        // Add a mouse listener to the table to handle delete button clicks
        habitTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = habitTable.rowAtPoint(evt.getPoint());
                int col = habitTable.columnAtPoint(evt.getPoint());
                if (row >= 0 && col == 9) {
                    confirmAndDelete(row);
                }
            }
        });

        // Listener checkbox
        tableModel.addTableModelListener(e -> {
            if (e.getColumn() >= 2 && e.getColumn() <= 8 && e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int dayIndex = e.getColumn() - 2;
                boolean done = Boolean.TRUE.equals(tableModel.getValueAt(row, e.getColumn()));
                Habit h = habitList.get(row);
                LocalDate date = weekStart.plusDays(dayIndex);
                habitFacade.updateHabitStatus(h.getId(), date, done);
            }
        });
    }

    private void confirmAndDelete(int row) {
        Habit h = habitList.get(row);
        
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure want to delete this habit?\n\n" +
            "Habit: " + h.getName() + "\n" +
            "This action cannot be undone.",
            "Delete Habit",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            habitFacade.deleteHabit(h.getId());
        }
    }

    // Mood Tab
    private JPanel createMoodTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(PASTEL_BLUE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(25, 25, 25, 25);

        JLabel title = new JLabel("How are you feeling today?");
        title.setFont(new Font("Poppins", Font.BOLD, 34));
        title.setForeground(TEXT_DARK);
        gbc.gridwidth = 5; gbc.gridx = 0; gbc.gridy = 0;
        p.add(title, gbc);

        String[] labels = {"Crying", "Sad", "Neutral", "Happy", "Excited"};
        String[] emojis = {"😭", "😞", "😐", "😊", "😄"}; // Unicode emoji characters
        Color[] colors = {
            new Color(255, 150, 150), new Color(255, 200, 150),
            new Color(255, 230, 150), PASTEL_YELLOW, new Color(180, 255, 180)
        };

        gbc.gridwidth = 1; gbc.gridy = 1;
        for (int i = 0; i < 5; i++) {
            final int val = i + 1;
            JButton b = new JButton("<html><center><font size=+20>" + emojis[i] + "</font><br><b>" + labels[i] + "</b></center></html>");
            b.setPreferredSize(new Dimension(170, 170));
            b.setBackground(colors[i]);
            b.setForeground(TEXT_DARK);
            b.setFont(new Font("Poppins", Font.BOLD, 18));
            b.setFocusPainted(false);
            b.addActionListener(e -> {
                moodFacade.addMood(val, LocalDate.now().toString());
                JOptionPane.showMessageDialog(this, "Mood recorded! Keep going!", "Success", JOptionPane.INFORMATION_MESSAGE);
            });
            gbc.gridx = i;
            p.add(b, gbc);
        }
        return p;
    }

    private JPanel createAnalysisTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(PASTEL_YELLOW);
        p.setBorder(new EmptyBorder(80, 80, 80, 80));

        JLabel lbl = new JLabel("<html><h1>Analysis & Charts Coming Next!</h1><p>Get ready for beautiful insights...</p></html>");
        lbl.setFont(new Font("Poppins", Font.BOLD, 40));
        lbl.setForeground(ACCENT_BLUE);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    @Override
    public void onDataChanged() {
        SwingUtilities.invokeLater(this::loadHabitData);
    }

    private void loadHabitData() {
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
            row[9] = "Delete";
            tableModel.addRow(row);
        }
    }
}
