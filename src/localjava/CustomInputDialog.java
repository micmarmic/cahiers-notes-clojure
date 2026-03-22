package customdialog;

import javax.swing.*;
import java.awt.*;

public class CustomInputDialog extends JDialog {
    private static final long serialVersionUID = 1L;
	private String inputValue;

    public CustomInputDialog(Frame parent, String title, String message, String currentValue) {
        super(parent, title, true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Create the main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create the message label
        JLabel messageLabel = new JLabel(message);
        mainPanel.add(messageLabel, BorderLayout.NORTH);

        // Create the input text field
        JTextField inputField = new JTextField(currentValue, 20);
        mainPanel.add(inputField, BorderLayout.CENTER);

        // Create the button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            inputValue = inputField.getText();
            setVisible(false);
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> setVisible(false));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add the main panel to the dialog
        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(parent);
    }

    public String showDialog() {
        setVisible(true);
        return inputValue;
    }
}
