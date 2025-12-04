
package com.project.app.view;

import com.project.app.facade.MoodFacade;
import com.project.app.model.AnalysisHabitMood; // Import the new model
import com.project.app.model.Mood;
import com.project.app.service.AnalysisService; // Import the new service
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MainDashboard extends JFrame {

    private final MoodFacade moodFacade = new MoodFacade();
    private final AnalysisService analysisService = new AnalysisService(); // Instantiate AnalysisService
    private LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    
    // Panel to hold analysis results
    private JPanel analysisResultPanel;

    // --- PALET WARNA LIGHT THEME (SERAGAM DENGAN TRACKER) ---
    private static final Color BG_MAIN = Color.WHITE;
    private static final Color TEXT_DARK = new Color(33, 37, 41);
    private static final Color ACCENT_BROWN = new Color(139, 115, 85);
    private static final Color GRAPH_LINE = new Color(139, 115, 85); // Garis Coklat
    private static final Color GRAPH_POINT = new Color(220, 53, 69); // Titik Merah
    private static final Color GRID_COLOR = new Color(230, 230, 230); // Abu-abu muda
    // New color for positive/negative impact
    private static final Color POSITIVE_IMPACT_COLOR = new Color(40, 167, 69); // Green
    private static final Color NEGATIVE_IMPACT_COLOR = new Color(220, 53, 69); // Red

    public MainDashboard() {
        setTitle("MoodFlow • Dashboard");
        setSize(1200, 700); // Increased width to accommodate analysis panel
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_MAIN);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(new MoodGraphPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
        
        analysisResultPanel = createAnalysisPanel(); // Initialize analysis panel
        add(analysisResultPanel, BorderLayout.EAST); // Add analysis panel to the EAST
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_MAIN);
        header.setBorder(new EmptyBorder(30, 40, 10, 40));

        JLabel title = new JLabel("Weekly Mood Analysis");
        title.setFont(new Font("Poppins", Font.BOLD, 36));
        title.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel("Overview of your emotional journey this week (" + 
                weekStart.format(DateTimeFormatter.ofPattern("dd MMM")) + " - " + 
                weekStart.plusDays(6).format(DateTimeFormatter.ofPattern("dd MMM")) + ")");
        subtitle.setFont(new Font("Poppins", Font.PLAIN, 16));
        subtitle.setForeground(Color.GRAY);

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        return header;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(20, 0, 40, 0));

        JButton btnOpenTracker = new JButton("Manage Habits & Log Mood");
        btnOpenTracker.setFont(new Font("Poppins", Font.BOLD, 18));
        btnOpenTracker.setBackground(ACCENT_BROWN);
        btnOpenTracker.setForeground(Color.WHITE);
        btnOpenTracker.setFocusPainted(false);
        btnOpenTracker.setBorder(new EmptyBorder(15, 30, 15, 30));
        btnOpenTracker.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnOpenTracker.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                WeeklyTrackerView tracker = new WeeklyTrackerView();
                tracker.setVisible(true);
                
                tracker.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                        repaint(); // Refresh grafik saat tracker ditutup
                        refreshAnalysisPanel(); // Refresh analysis panel saat tracker ditutup
                    }
                });
            });
        });

        panel.add(btnOpenTracker);
        return panel;
    }

    private JPanel createAnalysisPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(30, 20, 30, 40)); // Padding for the panel

        JLabel title = new JLabel("Habit Impact on Mood");
        title.setFont(new Font("Poppins", Font.BOLD, 24));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT); // Align title to the left
        panel.add(title);
        panel.add(Box.createVerticalStrut(20)); // Space below title

        updateAnalysisContent(panel); // Populate with initial analysis

        return panel;
    }

    private void updateAnalysisContent(JPanel panel) {
        // Clear previous content, except the title
        for (int i = panel.getComponentCount() - 1; i > 1; i--) { // Keep title and strut
            panel.remove(i);
        }

        List<AnalysisHabitMood> impacts = analysisService.getHabitMoodImpacts(weekStart, weekStart.plusDays(6));

        if (impacts.isEmpty()) {
            JLabel noDataLabel = new JLabel("No sufficient data for habit-mood analysis this week.");
            noDataLabel.setFont(new Font("Poppins", Font.PLAIN, 14));
            noDataLabel.setForeground(Color.GRAY);
            noDataLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(noDataLabel);
        } else {
            for (AnalysisHabitMood impact : impacts) {
                JLabel habitLabel = new JLabel();
                habitLabel.setFont(new Font("Poppins", Font.PLAIN, 15));
                habitLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

                String impactText;
                Color impactColor;
                if (impact.getMoodImpact() > 0) {
                    impactText = String.format("<html><b>%s</b> increases mood by <font color='#%s'>+%.2f</font> points.</html>", 
                                                impact.getHabitName(), 
                                                Integer.toHexString(POSITIVE_IMPACT_COLOR.getRGB()).substring(2),
                                                impact.getMoodImpact());
                    impactColor = POSITIVE_IMPACT_COLOR;
                } else if (impact.getMoodImpact() < 0) {
                    impactText = String.format("<html><b>%s</b> decreases mood by <font color='#%s'>%.2f</font> points.</html>", 
                                                impact.getHabitName(),
                                                Integer.toHexString(NEGATIVE_IMPACT_COLOR.getRGB()).substring(2),
                                                impact.getMoodImpact());
                    impactColor = NEGATIVE_IMPACT_COLOR;
                } else {
                    impactText = String.format("<html><b>%s</b> has no noticeable impact on mood.</html>", impact.getHabitName());
                    impactColor = TEXT_DARK;
                }
                habitLabel.setText(impactText);
                habitLabel.setForeground(TEXT_DARK); // Set base color, HTML will override for numbers
                panel.add(habitLabel);
                panel.add(Box.createVerticalStrut(5)); // Small space between habits
            }
        }
        panel.revalidate();
        panel.repaint();
    }
    
    private void refreshAnalysisPanel() {
        updateAnalysisContent(analysisResultPanel);
    }

    // --- CUSTOM COMPONENT: PANEL GRAFIK MOOD ---
    private class MoodGraphPanel extends JPanel {
        
        public MoodGraphPanel() {
            setBackground(Color.WHITE); // Background Grafik Putih
            setBorder(new EmptyBorder(20, 20, 20, 20));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int padding = 60;
            int graphW = w - (2 * padding);
            int graphH = h - (2 * padding);

            // 1. Gambar Sumbu
            g2.setColor(Color.LIGHT_GRAY);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(padding, h - padding, padding, padding); // Y Axis
            g2.drawLine(padding, h - padding, w - padding, h - padding); // X Axis

            // 2. Gambar Grid & Label Y (Mood 1-5)
            String[] emojis = {"", "😭", "😞", "😐", "😊", "😄"};
            for (int i = 1; i <= 5; i++) {
                int y = (h - padding) - (i * graphH / 6);
                
                // Grid Line Halus
                g2.setColor(GRID_COLOR);
                g2.setStroke(new BasicStroke(1));
                g2.drawLine(padding, y, w - padding, y);

                // Label Emoji
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
                g2.setColor(TEXT_DARK);
                g2.drawString(emojis[i], padding - 45, y + 10);
            }

            // 3. Gambar Data
            int[] xPoints = new int[7];
            int[] yPoints = new int[7];
            boolean[] hasData = new boolean[7];

            g2.setFont(new Font("Poppins", Font.BOLD, 14));
            DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEE");

            for (int i = 0; i < 7; i++) {
                LocalDate date = weekStart.plusDays(i);
                Mood mood = moodFacade.getMood(date);

                int x = padding + (i * graphW / 6);
                xPoints[i] = x;

                // Label Hari
                g2.setColor(Color.GRAY);
                g2.drawString(date.format(dayFmt), x - 15, h - padding + 25);

                if (mood != null && mood.getMoodValue() > 0) {
                    int val = mood.getMoodValue();
                    int y = (h - padding) - (val * graphH / 6);
                    yPoints[i] = y;
                    hasData[i] = true;
                } else {
                    hasData[i] = false;
                }
            }

            // Gambar Garis Penghubung
            g2.setColor(GRAPH_LINE);
            g2.setStroke(new BasicStroke(3f));
            for (int i = 0; i < 6; i++) {
                if (hasData[i] && hasData[i+1]) {
                    g2.drawLine(xPoints[i], yPoints[i], xPoints[i+1], yPoints[i+1]);
                }
            }

            // Gambar Titik
            for (int i = 0; i < 7; i++) {
                if (hasData[i]) {
                    g2.setColor(GRAPH_POINT);
                    g2.fill(new Ellipse2D.Double(xPoints[i] - 6, yPoints[i] - 6, 12, 12));
                    g2.setColor(BG_MAIN);
                    g2.draw(new Ellipse2D.Double(xPoints[i] - 6, yPoints[i] - 6, 12, 12));
                }
            }
        }
    }
}