package app;

import javax.swing.SwingUtilities;
import java.util.logging.Logger;
import app.view.MainDashboard;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private Main() {
        throw new IllegalStateException("Main class");
    }

    public static void main(String[] args) {
        
        LOGGER.info("=== Starting MoodFlow Application ===");

        SwingUtilities.invokeLater(() -> {
            MainDashboard dashboard = new MainDashboard();
            dashboard.setVisible(true);
        });
    }
}