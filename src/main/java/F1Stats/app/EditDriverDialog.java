package F1Stats.app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EditDriverDialog extends JDialog {
    private JTextField driverIdField;
    private JTextField nameField;
    private JTextField pointsField;
    private JTextField winsField;
    private JTextField nationalityField;
    private boolean confirmed = false;

    public EditDriverDialog(Frame parent, ModelDriver driver) {
        super(parent, "Edit Driver", true);
        initUI(driver);
    }

    private void initUI(ModelDriver driver) {
        JPanel panel = new JPanel(new GridLayout(6, 2));

        panel.add(new JLabel("Driver ID:"));
        driverIdField = new JTextField(driver.getDriverId());
        panel.add(driverIdField);

        panel.add(new JLabel("Name:"));
        nameField = new JTextField(driver.getName());
        panel.add(nameField);

        panel.add(new JLabel("Points:"));
        pointsField = new JTextField(String.valueOf(driver.getPoints()));
        panel.add(pointsField);

        panel.add(new JLabel("Wins:"));
        winsField = new JTextField(String.valueOf(driver.getWins()));
        panel.add(winsField);

        panel.add(new JLabel("Nationality:"));
        nationalityField = new JTextField(driver.getNationality());
        panel.add(nationalityField);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = true;
                setVisible(false);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        panel.add(okButton);
        panel.add(cancelButton);

        getContentPane().add(panel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getParent());
    }

    public String getDriverId() {
        return driverIdField.getText().trim();
    }

    public String getName() {
        return nameField.getText().trim();
    }

    public int getPoints() {
        return Integer.parseInt(pointsField.getText().trim());
    }

    public int getWins() {
        return Integer.parseInt(winsField.getText().trim());
    }

    public String getNationality() {
        return nationalityField.getText().trim();
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
