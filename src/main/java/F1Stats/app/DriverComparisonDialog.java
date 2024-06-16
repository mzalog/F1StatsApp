package F1Stats.app;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DriverComparisonDialog extends JDialog {
    private JList<String> driverList;
    private JCheckBox pointsCheckBox;
    private JCheckBox winsCheckBox;
    private boolean confirmed;

    public DriverComparisonDialog(Frame parent, List<String> driverNames) {
        super(parent, "Select Drivers and Parameters", true);
        setLayout(new BorderLayout());

        driverList = new JList<>(driverNames.toArray(new String[0]));
        driverList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        pointsCheckBox = new JCheckBox("Points");
        winsCheckBox = new JCheckBox("Wins");

        JPanel parameterPanel = new JPanel();
        parameterPanel.add(pointsCheckBox);
        parameterPanel.add(winsCheckBox);

        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        okButton.addActionListener(e -> {
            confirmed = true;
            setVisible(false);
        });

        cancelButton.addActionListener(e -> {
            confirmed = false;
            setVisible(false);
        });

        add(new JScrollPane(driverList), BorderLayout.CENTER);
        add(parameterPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);
        setSize(300, 400);
        setLocationRelativeTo(parent);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public List<String> getSelectedDrivers() {
        return driverList.getSelectedValuesList();
    }

    public boolean isPointsSelected() {
        return pointsCheckBox.isSelected();
    }

    public boolean isWinsSelected() {
        return winsCheckBox.isSelected();
    }
}
