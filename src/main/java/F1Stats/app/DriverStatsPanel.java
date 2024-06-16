package F1Stats.app;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.util.stream.Collectors;
import java.sql.SQLException;
import java.util.Comparator;

public class DriverStatsPanel extends JPanel {
    private JTable driverTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JPanel chartPanelContainer;
    private int currentYear;
    private JFreeChart currentChart;

    public DriverStatsPanel() {
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        setLayout(new MigLayout("fill", "[grow]", "[][grow][]"));

        JButton loadDriverStatsButton = new JButton("Load Driver Stats for Year");
        loadDriverStatsButton.setPreferredSize(new Dimension(180, 30));
        loadDriverStatsButton.addActionListener(e -> loadDriverStatsForYear());

        JButton showDriverStatsButton = new JButton("Show Driver Stats for Year");
        showDriverStatsButton.setPreferredSize(new Dimension(180, 30));
        showDriverStatsButton.addActionListener(e -> showDriverStatsForYearPrompt());

        JButton generateStatsButton = new JButton("Generate Driver Comparison Chart");
        generateStatsButton.setPreferredSize(new Dimension(250, 30));
        generateStatsButton.addActionListener(e -> {
            try {
                showDriverComparisonDialog();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        JButton exportPdfButton = new JButton("Export Chart to PDF");
        exportPdfButton.setPreferredSize(new Dimension(180, 30));
        exportPdfButton.addActionListener(e -> exportChartToPDF());

        JButton addDriverButton = new JButton("Add New Driver");
        addDriverButton.setPreferredSize(new Dimension(180, 30));
        addDriverButton.addActionListener(e -> addNewDriver());

        JButton editDriverButton = new JButton("Edit Selected Driver");
        editDriverButton.setPreferredSize(new Dimension(180, 30));
        editDriverButton.addActionListener(e -> editSelectedDriver());

        JButton deleteDriverButton = new JButton("Delete Selected Driver");
        deleteDriverButton.setPreferredSize(new Dimension(180, 30));
        deleteDriverButton.addActionListener(e -> deleteSelectedDriver());

        JButton deleteDriverTableButton = new JButton("Delete Driver Tables");
        deleteDriverTableButton.setPreferredSize(new Dimension(180, 30));
        deleteDriverTableButton.addActionListener(e -> showDeleteDriverTableDialog());

        JTextField searchField = new JTextField(20);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                search(searchField.getText());
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                search(searchField.getText());
            }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                search(searchField.getText());
            }
        });

        String[] sortOptions = {"ID", "Name", "Points", "Wins", "Nationality"};
        JComboBox<String> sortComboBox = new JComboBox<>(sortOptions);
        sortComboBox.setPreferredSize(new Dimension(150, 30));
        sortComboBox.addActionListener(e -> sortTable((String) sortComboBox.getSelectedItem()));

        JPanel buttonPanel = new JPanel(new MigLayout("wrap 3", "[]20[]20[]", "[]20[]"));
        buttonPanel.add(loadDriverStatsButton);
        buttonPanel.add(showDriverStatsButton);
        buttonPanel.add(generateStatsButton);
        buttonPanel.add(exportPdfButton);
        buttonPanel.add(addDriverButton);
        buttonPanel.add(editDriverButton);
        buttonPanel.add(deleteDriverButton);
        buttonPanel.add(deleteDriverTableButton);

        JPanel searchSortPanel = new JPanel(new MigLayout("fillx", "[][]20[grow]"));
        searchSortPanel.add(new JLabel("Search:"), "split 2, right");
        searchSortPanel.add(searchField, "growx, wrap");
        searchSortPanel.add(new JLabel("Sort by:"), "split 2, right");
        searchSortPanel.add(sortComboBox, "growx, wrap");

        add(buttonPanel, "north");
        add(searchSortPanel, "span, wrap, growx, gapbottom 10");

        tableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Points", "Wins", "Nationality"}, 0);
        driverTable = new JTable(tableModel) {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? getBackground() : new Color(240, 240, 240));
                }
                return c;
            }
        };
        driverTable.setFillsViewportHeight(true);
        driverTable.setRowHeight(25);
        driverTable.setShowGrid(false);
        driverTable.setIntercellSpacing(new Dimension(0, 0));
        sorter = new TableRowSorter<>(tableModel);
        driverTable.setRowSorter(sorter);
        JScrollPane scrollPane = new JScrollPane(driverTable);
        scrollPane.setPreferredSize(new Dimension(980, 300));

        chartPanelContainer = new JPanel(new BorderLayout());

        add(scrollPane, "grow");
        add(chartPanelContainer, "south, grow");
    }

    private void loadDriverStatsForYear() {
        String year = JOptionPane.showInputDialog(this, "Enter the year to load driver stats (1950-2024):", "Load Driver Stats", JOptionPane.PLAIN_MESSAGE);
        if (year == null || year.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Year cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            currentYear = Integer.parseInt(year);
            if (currentYear < 1950 || currentYear > 2024) {
                JOptionPane.showMessageDialog(this, "Year must be between 1950 and 2024.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            ApiClient apiClient = new ApiClient();
            Database db = new Database();
            db.createDriverTableForYear(currentYear);
            String data = apiClient.getDriverStandingsForYear(currentYear);
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(data, JsonObject.class);
            JsonArray standingsArray = jsonObject.getAsJsonObject("MRData").getAsJsonObject("StandingsTable").getAsJsonArray("StandingsLists").get(0).getAsJsonObject().getAsJsonArray("DriverStandings");

            for (int i = 0; i < standingsArray.size(); i++) {
                JsonObject driverStanding = standingsArray.get(i).getAsJsonObject();
                String driverId = driverStanding.getAsJsonObject("Driver").get("driverId").getAsString();
                String givenName = driverStanding.getAsJsonObject("Driver").get("givenName").getAsString();
                String familyName = driverStanding.getAsJsonObject("Driver").get("familyName").getAsString();
                String nationality = driverStanding.getAsJsonObject("Driver").get("nationality").getAsString();
                int points = driverStanding.get("points").getAsInt();
                int wins = driverStanding.get("wins").getAsInt();

                ModelDriver modelDriver = new ModelDriver();
                modelDriver.setDriverId(driverId);
                modelDriver.setName(givenName + " " + familyName);
                modelDriver.setNationality(nationality);
                modelDriver.setPoints(points);
                modelDriver.setWins(wins);
                db.insertDriver(currentYear, modelDriver);
            }

            showDriverStatsForYear(currentYear);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid year format.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDriverStatsForYearPrompt() {
        String year = JOptionPane.showInputDialog(this, "Enter the year to show driver stats (1950-2024):", "Show Driver Stats", JOptionPane.PLAIN_MESSAGE);
        if (year == null || year.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Year cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            currentYear = Integer.parseInt(year);
            if (currentYear < 1950 || currentYear > 2024) {
                JOptionPane.showMessageDialog(this, "Year must be between 1950 and 2024.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            showDriverStatsForYear(currentYear);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid year format.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDriverStatsForYear(int year) {
        Database db = new Database();
        try {
            List<ModelDriver> drivers = db.getAllDriversForYear(year);
            tableModel.setRowCount(0); // Clear existing rows
            for (ModelDriver driver : drivers) {
                tableModel.addRow(new Object[]{driver.getDriverId(), driver.getName(), driver.getPoints(), driver.getWins(), driver.getNationality()});
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading driver stats from database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addNewDriver() {
        if (currentYear == 0) {
            JOptionPane.showMessageDialog(this, "Please load or show driver stats for a specific year first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        EditDriverDialog dialog = new EditDriverDialog((Frame) SwingUtilities.getWindowAncestor(this), new ModelDriver());
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            String driverId = dialog.getDriverId();
            String name = dialog.getName();
            int points = dialog.getPoints();
            int wins = dialog.getWins();
            String nationality = dialog.getNationality();

            if (validateDriverData(driverId, name, points, wins, nationality)) {
                ModelDriver driver = new ModelDriver();
                driver.setDriverId(driverId);
                driver.setName(name);
                driver.setPoints(points);
                driver.setWins(wins);
                driver.setNationality(nationality);

                try {
                    Database db = new Database();
                    db.insertDriver(currentYear, driver);
                    JOptionPane.showMessageDialog(this, "Driver added successfully!");
                    showDriverStatsForYear(currentYear);
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error adding driver: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void editSelectedDriver() {
        int selectedRow = driverTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "No driver selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String driverId = (String) tableModel.getValueAt(driverTable.convertRowIndexToModel(selectedRow), 0);

        try {
            Database db = new Database();
            ModelDriver driver = db.getDriverById(currentYear, driverId);
            if (driver != null) {
                EditDriverDialog dialog = new EditDriverDialog((Frame) SwingUtilities.getWindowAncestor(this), driver);
                dialog.setVisible(true);

                if (dialog.isConfirmed()) {
                    String name = dialog.getName();
                    int points = dialog.getPoints();
                    int wins = dialog.getWins();
                    String nationality = dialog.getNationality();

                    if (validateDriverData(driverId, name, points, wins, nationality)) {
                        driver.setName(name);
                        driver.setPoints(points);
                        driver.setWins(wins);
                        driver.setNationality(nationality);
                        db.updateDriver(currentYear, driver);
                        JOptionPane.showMessageDialog(this, "Driver updated successfully!");
                        showDriverStatsForYear(currentYear);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Driver with ID " + driverId + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating driver: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedDriver() {
        int selectedRow = driverTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "No driver selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String driverId = (String) tableModel.getValueAt(driverTable.convertRowIndexToModel(selectedRow), 0);

        try {
            int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete Driver with ID " + driverId + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (response == JOptionPane.YES_OPTION) {
                Database db = new Database();
                db.deleteDriver(currentYear, driverId);
                JOptionPane.showMessageDialog(this, "Driver deleted successfully!");
                showDriverStatsForYear(currentYear);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting driver: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDeleteDriverTableDialog() {
        Database db = new Database();
        try {
            List<String> driverTables = db.getAllDriverTables();
            JCheckBox[] checkBoxes = new JCheckBox[driverTables.size()];
            JPanel panel = new JPanel(new GridLayout(driverTables.size(), 1));
            for (int i = 0; i < driverTables.size(); i++) {
                checkBoxes[i] = new JCheckBox(driverTables.get(i));
                panel.add(checkBoxes[i]);
            }
            int result = JOptionPane.showConfirmDialog(this, panel, "Select Driver Tables to Delete", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                for (JCheckBox checkBox : checkBoxes) {
                    if (checkBox.isSelected()) {
                        String tableName = checkBox.getText();
                        int year = Integer.parseInt(tableName.split("_")[1]);
                        db.deleteDriverTable(year);
                        JOptionPane.showMessageDialog(this, "Driver table for year " + year + " deleted successfully!");
                        if (currentYear == year) {
                            tableModel.setRowCount(0);
                            chartPanelContainer.removeAll();
                            chartPanelContainer.revalidate();
                            chartPanelContainer.repaint();
                            currentYear = -1;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching driver tables: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateDriverData(String driverId, String name, int points, int wins, String nationality) {
        if (driverId == null || driverId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Driver ID cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (points < 0) {
            JOptionPane.showMessageDialog(this, "Points cannot be negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (wins < 0) {
            JOptionPane.showMessageDialog(this, "Wins cannot be negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (nationality == null || nationality.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nationality cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void showDriverComparisonDialog() throws SQLException {
        Database db = new Database();
        List<ModelDriver> drivers = db.getAllDriversForYear(currentYear);
        List<String> driverNames = drivers.stream().map(ModelDriver::getName).collect(Collectors.toList());

        DriverComparisonDialog dialog = new DriverComparisonDialog((Frame) SwingUtilities.getWindowAncestor(this), driverNames);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            List<String> selectedDrivers = dialog.getSelectedDrivers();
            boolean comparePoints = dialog.isPointsSelected();
            boolean compareWins = dialog.isWinsSelected();

            generateDriverComparisonChart(selectedDrivers, comparePoints, compareWins);
        }
    }

    private void generateDriverComparisonChart(List<String> selectedDrivers, boolean comparePoints, boolean compareWins) throws SQLException {
        Database db = new Database();
        List<ModelDriver> drivers = db.getDriversByNames(currentYear, selectedDrivers);

        StatisticsService statsService = new StatisticsService();
        currentChart = statsService.createDriverComparisonChart(drivers, comparePoints, compareWins);
        ChartPanel chartPanel = new ChartPanel(currentChart);
        chartPanel.setPreferredSize(new Dimension(980, 200));

        chartPanelContainer.removeAll();
        chartPanelContainer.add(chartPanel, BorderLayout.CENTER);
        chartPanelContainer.revalidate();
        chartPanelContainer.repaint();
    }

    private void exportChartToPDF() {
        if (currentChart == null) {
            JOptionPane.showMessageDialog(this, "No chart available to export.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        PdfExporter pdfExporter = new PdfExporter();
        pdfExporter.exportChartToPDF(currentChart, "driver_comparison_chart.pdf");
        JOptionPane.showMessageDialog(this, "Chart exported to PDF!");
    }

    private void search(String query) {
        RowFilter<DefaultTableModel, Object> rf = null;
        try {
            rf = RowFilter.regexFilter("(?i)" + query);
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
        sorter.setRowFilter(rf);
    }

    private void sortTable(String criterion) {
        int columnIndex = 0;
        switch (criterion) {
            case "Name":
                columnIndex = 1;
                break;
            case "Points":
                columnIndex = 2;
                break;
            case "Wins":
                columnIndex = 3;
                break;
            case "Nationality":
                columnIndex = 4;
                break;
        }
        sorter.setComparator(columnIndex, Comparator.naturalOrder());
        driverTable.getRowSorter().toggleSortOrder(columnIndex);
    }
}
