package com.project.app.view;

import com.project.app.facade.MoodFacade;
import com.project.app.model.Mood;
import com.project.app.service.AnalysisService;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    
    private JPanel analysisContentPanel; // Panel to hold the dynamic analysis labels

    private static final Color BG_MAIN = Color.WHITE;
    private static final Color TEXT_DARK = new Color(33, 37, 41);
    private static final Color ACCENT_BROWN = new Color(139, 115, 85);
    private static final Color GRAPH_LINE = new Color(139, 115, 85);
    private static final Color GRAPH_POINT = new Color(220, 53, 69);
    private static final Color GRID_COLOR = new Color(230, 230, 230);
    private static final Color ACCENT_BLUE = new Color(13, 110, 253);

    public MainDashboard() {
        setTitle("MoodFlow • Dashboard");
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_MAIN);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(new MoodGraphPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
        add(createAnalysisPanel(), BorderLayout.EAST);
        
        // Initial load of analyses
        SwingUtilities.invokeLater(this::loadRandomAnalyses);
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
                        repaint(); // Refresh graph
                        loadRandomAnalyses(); // Refresh analyses
                    }
                });
            });
        });

        panel.add(btnOpenTracker);
        return panel;
    }

    private JPanel createAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(30, 20, 30, 40));
        panel.setPreferredSize(new Dimension(350, 0));

        // Header (Title + Reload Button)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_MAIN);

        JLabel title = new JLabel("Analisis Cerdas");
        title.setFont(new Font("Poppins", Font.BOLD, 24));
        title.setForeground(TEXT_DARK);
        
        JLabel reloadLabel = new JLabel("Muat Ulang ↻");
        reloadLabel.setFont(new Font("Poppins", Font.BOLD, 14));
        reloadLabel.setForeground(ACCENT_BLUE);
        reloadLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        reloadLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                loadRandomAnalyses();
            }
        });

        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(reloadLabel, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);

        // Content Panel for the analysis results
        analysisContentPanel = new JPanel();
        analysisContentPanel.setLayout(new BoxLayout(analysisContentPanel, BoxLayout.Y_AXIS));
        analysisContentPanel.setBackground(BG_MAIN);
        
        JScrollPane scrollPane = new JScrollPane(analysisContentPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BG_MAIN);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadRandomAnalyses() {
        analysisContentPanel.removeAll();
        
        List<String> analyses = analysisService.getSevenRandomAnalyses();

        if (analyses.isEmpty()) {
            JLabel noDataLabel = new JLabel("<html><p style='width:250px;'>Belum ada cukup data untuk dianalisis. Terus catat mood dan kebiasaanmu setiap hari!</p></html>");
            noDataLabel.setFont(new Font("Poppins", Font.PLAIN, 14));
            noDataLabel.setForeground(Color.GRAY);
            analysisContentPanel.add(noDataLabel);
        } else {
            for (String analysisText : analyses) {
                JLabel analysisLabel = new JLabel("<html><p style='width:250px;'>• " + analysisText + "</p></html>");
                analysisLabel.setFont(new Font("Poppins", Font.PLAIN, 15));
                analysisLabel.setForeground(TEXT_DARK);
                analysisLabel.setBorder(new EmptyBorder(0, 0, 10, 0)); // Space between items
                analysisContentPanel.add(analysisLabel);
            }
        }
        analysisContentPanel.revalidate();
        analysisContentPanel.repaint();
    }

    // --- CUSTOM COMPONENT: MOOD GRAPH PANEL ---
    private class MoodGraphPanel extends JPanel {
        
        public MoodGraphPanel() {
            setBackground(Color.WHITE);
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

            g2.setColor(Color.LIGHT_GRAY);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(padding, h - padding, padding, padding); // Y Axis
            g2.drawLine(padding, h - padding, w - padding, h - padding); // X Axis

            String[] emojis = {"", "😭", "😞", "😐", "😊", "😄"};
            for (int i = 1; i <= 5; i++) {
                int y = (h - padding) - (i * graphH / 6);
                g2.setColor(GRID_COLOR);
                g2.setStroke(new BasicStroke(1));
                g2.drawLine(padding, y, w - padding, y);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
                g2.setColor(TEXT_DARK);
                g2.drawString(emojis[i], padding - 45, y + 10);
            }

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

            g2.setColor(GRAPH_LINE);
            g2.setStroke(new BasicStroke(3f));
            for (int i = 0; i < 6; i++) {
                if (hasData[i] && hasData[i+1]) {
                    g2.drawLine(xPoints[i], yPoints[i], xPoints[i+1], yPoints[i+1]);
                }
            }

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