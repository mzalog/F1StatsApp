package F1Stats.app;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.util.stream.Collectors;
import java.sql.SQLException;
import java.util.Comparator;

public class RaceTablePanel extends JPanel {
    private JTable raceTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private int currentYear;

    public RaceTablePanel() {
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        setLayout(new MigLayout("fill", "[grow]", "[][grow][]"));

        JButton loadRaceDataButton = new JButton("Load Race Data for Year");
        loadRaceDataButton.setPreferredSize(new Dimension(180, 30));
        loadRaceDataButton.addActionListener(e -> loadRaceDataForYear());

        JButton showRaceDataButton = new JButton("Show Race Data for Year");
        showRaceDataButton.setPreferredSize(new Dimension(180, 30));
        showRaceDataButton.addActionListener(e -> showRaceDataForYearPrompt());

        JButton addRaceButton = new JButton("Add New Race");
        addRaceButton.setPreferredSize(new Dimension(180, 30));
        addRaceButton.addActionListener(e -> addNewRace());

        JButton editRaceButton = new JButton("Edit Selected Race");
        editRaceButton.setPreferredSize(new Dimension(180, 30));
        editRaceButton.addActionListener(e -> editSelectedRace());

        JButton deleteRaceButton = new JButton("Delete Selected Race");
        deleteRaceButton.setPreferredSize(new Dimension(180, 30));
        deleteRaceButton.addActionListener(e -> deleteSelectedRace());

        JButton deleteRaceTableButton = new JButton("Delete Race Tables");
        deleteRaceTableButton.setPreferredSize(new Dimension(180, 30));
        deleteRaceTableButton.addActionListener(e -> showDeleteRaceTableDialog());

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

        String[] sortOptions = {"ID", "Name", "Date", "Location"};
        JComboBox<String> sortComboBox = new JComboBox<>(sortOptions);
        sortComboBox.setPreferredSize(new Dimension(150, 30));
        sortComboBox.addActionListener(e -> sortTable((String) sortComboBox.getSelectedItem()));

        JPanel buttonPanel = new JPanel(new MigLayout("wrap 3", "[]20[]20[]", "[]20[]"));
        buttonPanel.add(loadRaceDataButton);
        buttonPanel.add(showRaceDataButton);
        buttonPanel.add(addRaceButton);
        buttonPanel.add(editRaceButton);
        buttonPanel.add(deleteRaceButton);
        buttonPanel.add(deleteRaceTableButton);

        JPanel searchSortPanel = new JPanel(new MigLayout("fillx", "[][]20[grow]"));
        searchSortPanel.add(new JLabel("Search:"), "split 2, right");
        searchSortPanel.add(searchField, "growx, wrap");
        searchSortPanel.add(new JLabel("Sort by:"), "split 2, right");
        searchSortPanel.add(sortComboBox, "growx, wrap");

        add(buttonPanel, "north");
        add(searchSortPanel, "span, wrap, growx, gapbottom 10");

        tableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Date", "Location"}, 0);
        raceTable = new JTable(tableModel) {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? getBackground() : new Color(240, 240, 240));
                }
                return c;
            }
        };
        raceTable.setFillsViewportHeight(true);
        raceTable.setRowHeight(25);
        raceTable.setShowGrid(false);
        raceTable.setIntercellSpacing(new Dimension(0, 0));
        sorter = new TableRowSorter<>(tableModel);
        raceTable.setRowSorter(sorter);
        JScrollPane scrollPane = new JScrollPane(raceTable);
        scrollPane.setPreferredSize(new Dimension(980, 300));

        add(scrollPane, "grow");
    }

    private void loadRaceDataForYear() {
        String year = JOptionPane.showInputDialog(this, "Enter the year to load race data (1950-2024):", "Load Race Data", JOptionPane.PLAIN_MESSAGE);
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
            db.createRaceTableForYear(currentYear);
            String data = apiClient.getSeasonRaces(currentYear);
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(data, JsonObject.class);
            JsonArray racesArray = jsonObject.getAsJsonObject("MRData").getAsJsonObject("RaceTable").getAsJsonArray("Races");

            for (int i = 0; i < racesArray.size(); i++) {
                JsonObject raceObject = racesArray.get(i).getAsJsonObject();
                String raceId = raceObject.get("round").getAsString();
                String raceName = raceObject.get("raceName").getAsString();
                String raceDate = raceObject.get("date").getAsString();
                String raceLocation = raceObject.getAsJsonObject("Circuit").getAsJsonObject("Location").get("locality").getAsString();

                ModelRace modelRace = new ModelRace();
                modelRace.setId(Integer.parseInt(raceId));
                modelRace.setRaceName(raceName);
                modelRace.setDate(raceDate);
                modelRace.setCircuitName(raceLocation);
                db.insertRace(currentYear, modelRace);
            }

            showRaceDataForYear(currentYear);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid year format.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showRaceDataForYearPrompt() {
        String year = JOptionPane.showInputDialog(this, "Enter the year to show race data (1950-2024):", "Show Race Data", JOptionPane.PLAIN_MESSAGE);
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
            showRaceDataForYear(currentYear);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid year format.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showRaceDataForYear(int year) {
        Database db = new Database();
        try {
            List<ModelRace> races = db.getAllRacesForYear(year);
            tableModel.setRowCount(0);
            for (ModelRace race : races) {
                tableModel.addRow(new Object[]{race.getId(), race.getRaceName(), race.getDate(), race.getCircuitName()});
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading race data from database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addNewRace() {
        if (currentYear == 0) {
            JOptionPane.showMessageDialog(this, "Please load or show race data for a specific year first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        EditRaceDialog dialog = new EditRaceDialog((Frame) SwingUtilities.getWindowAncestor(this), new ModelRace());
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            String raceId = dialog.getRaceId();
            String name = dialog.getName();
            String date = dialog.getDate();
            String location = String.valueOf(dialog.getLocation());

            if (validateRaceData(raceId, name, date, location)) {
                ModelRace race = new ModelRace();
                race.setId(Integer.parseInt(raceId));
                race.setRaceName(name);
                race.setDate(date);
                race.setCircuitName(location);

                try {
                    Database db = new Database();
                    db.insertRace(currentYear, race);
                    JOptionPane.showMessageDialog(this, "Race added successfully!");
                    showRaceDataForYear(currentYear);
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error adding race: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void editSelectedRace() {
        int selectedRow = raceTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "No race selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String raceId = (String) tableModel.getValueAt(raceTable.convertRowIndexToModel(selectedRow), 0);

        try {
            Database db = new Database();
            ModelRace race = db.getRaceById(currentYear, Integer.parseInt(raceId));
            if (race != null) {
                EditRaceDialog dialog = new EditRaceDialog((Frame) SwingUtilities.getWindowAncestor(this), race);
                dialog.setVisible(true);

                if (dialog.isConfirmed()) {
                    String name = dialog.getName();
                    String date = dialog.getDate();
                    String location = String.valueOf(dialog.getLocation());

                    if (validateRaceData(raceId, name, date, location)) {
                        race.setRaceName(name);
                        race.setDate(date);
                        race.setCircuitName(location);
                        db.updateRace(currentYear, race);
                        JOptionPane.showMessageDialog(this, "Race updated successfully!");
                        showRaceDataForYear(currentYear);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Race with ID " + raceId + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating race: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedRace() {
        int selectedRow = raceTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "No race selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String raceId = (String) tableModel.getValueAt(raceTable.convertRowIndexToModel(selectedRow), 0);

        try {
            int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete Race with ID " + raceId + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (response == JOptionPane.YES_OPTION) {
                Database db = new Database();
                db.deleteRace(currentYear, Integer.parseInt(raceId));
                JOptionPane.showMessageDialog(this, "Race deleted successfully!");
                showRaceDataForYear(currentYear);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting race: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDeleteRaceTableDialog() {
        Database db = new Database();
        try {
            List<String> raceTables = db.getAllRaceTables();
            JCheckBox[] checkBoxes = new JCheckBox[raceTables.size()];
            JPanel panel = new JPanel(new GridLayout(raceTables.size(), 1));
            for (int i = 0; i < raceTables.size(); i++) {
                checkBoxes[i] = new JCheckBox(raceTables.get(i));
                panel.add(checkBoxes[i]);
            }
            int result = JOptionPane.showConfirmDialog(this, panel, "Select Race Tables to Delete", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                for (JCheckBox checkBox : checkBoxes) {
                    if (checkBox.isSelected()) {
                        String tableName = checkBox.getText();
                        int year = Integer.parseInt(tableName.split("_")[1]);
                        db.deleteRaceTable(year);
                        JOptionPane.showMessageDialog(this, "Race table for year " + year + " deleted successfully!");
                        if (currentYear == year) {
                            tableModel.setRowCount(0);
                            currentYear = -1; // Reset the current year
                        }
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching race tables: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateRaceData(String raceId, String name, String date, String location) {
        if (raceId == null || raceId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Race ID cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (date == null || date.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Date cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (location == null || location.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Location cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
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
            case "Date":
                columnIndex = 2;
                break;
            case "Location":
                columnIndex = 3;
                break;
        }
        sorter.setComparator(columnIndex, Comparator.naturalOrder());
        raceTable.getRowSorter().toggleSortOrder(columnIndex);
    }
}
