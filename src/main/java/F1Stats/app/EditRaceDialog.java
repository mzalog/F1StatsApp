package F1Stats.app;

import javax.swing.*;
import org.jdesktop.swingx.JXDatePicker;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;

public class EditRaceDialog extends JDialog {
    private JTextField raceIdField;
    private JTextField raceNameField;
    private JXDatePicker datePicker;
    private JTextField circuitNameField;
    private boolean confirmed;

    public EditRaceDialog(Frame parent, ModelRace race) {
        super(parent, "Edit Race", true);
        setLayout(new BorderLayout());

        raceNameField = new JTextField(race.getRaceName(), 20);
        datePicker = new JXDatePicker();
        datePicker.setFormats(new SimpleDateFormat("yyyy-MM-dd"));
        try {
            datePicker.setDate(new SimpleDateFormat("yyyy-MM-dd").parse(race.getDate()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        circuitNameField = new JTextField(race.getCircuitName(), 20);

        JPanel inputPanel = new JPanel(new GridLayout(3, 2));
        inputPanel.add(new JLabel("Race Name:"));
        inputPanel.add(raceNameField);
        inputPanel.add(new JLabel("Date:"));
        inputPanel.add(datePicker);
        inputPanel.add(new JLabel("Circuit Name:"));
        inputPanel.add(circuitNameField);

        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = true;
                setVisible(false);
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = false;
                setVisible(false);
            }
        });

        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(parent);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getRaceName() {
        return raceNameField.getText();
    }

    public String getDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(datePicker.getDate());
    }

    public String getCircuitName() {
        return circuitNameField.getText();
    }

    public String getRaceId() {
        return raceIdField.getText();
    }
}
