package F1Stats.app;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private RaceTablePanel raceTablePanel;
    private DriverStatsPanel driverStatsPanel;

    public MainWindow() {
        super("F1 Statistics");

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        raceTablePanel = new RaceTablePanel();
        driverStatsPanel = new DriverStatsPanel();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Race Tables", raceTablePanel);
        tabbedPane.addTab("Driver Stats", driverStatsPanel);

        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow mainWindow = new MainWindow();
            mainWindow.setVisible(true);
        });
    }
}
