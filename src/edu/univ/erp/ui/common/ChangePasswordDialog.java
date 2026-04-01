package edu.univ.erp.ui.common;

import edu.univ.erp.auth.AuthService;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {
    private final AuthService authService = new AuthService();
    private final String username;
    private JPasswordField oldPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    public ChangePasswordDialog(Frame owner, String username) {
        super(owner, "Change Password", true);
        this.username = username;
        setSize(400, 250);
        setLocationRelativeTo(owner);
        init();
    }

    private void init() {
        setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.anchor = GridBagConstraints.WEST;

        oldPasswordField = new JPasswordField(20);
        newPasswordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Current Password:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(oldPasswordField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(newPasswordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Confirm New Password:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(confirmPasswordField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveBtn = new JButton("Change Password");
        saveBtn.setBackground(new Color(46, 204, 113));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBorderPainted(false);
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(new Color(149, 165, 166));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setBorderPainted(false);

        saveBtn.addActionListener(e -> {
            String oldPass = new String(oldPasswordField.getPassword());
            String newPass = new String(newPasswordField.getPassword());
            String confirmPass = new String(confirmPasswordField.getPassword());

            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(this, "New passwords do not match", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (newPass.length() < 4) {
                JOptionPane.showMessageDialog(this, "Password must be at least 4 characters long", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                boolean success = authService.changePassword(username, oldPass, newPass);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Password changed successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Current password is incorrect", "Error", JOptionPane.ERROR_MESSAGE);
                    oldPasswordField.setText("");
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error changing password: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}

