package edu.univ.erp.ui.admin.dialogs;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.data.DataStore;
import edu.univ.erp.domain.*;

import javax.swing.*;
import java.awt.*;

public class AddUserDialog extends JDialog {
    private final AuthService auth = new AuthService();
    private final DataStore ds = DataStore.getInstance();
    private final Runnable onSuccess;

    public AddUserDialog(Frame owner, Runnable onSuccess) {
        super(owner, "Add New User", true);
        this.onSuccess = onSuccess;
        setSize(450, 350);
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

        JTextField usernameField = new JTextField(20);
        JTextField displayNameField = new JTextField(20);
        JComboBox<Role> roleCombo = new JComboBox<>(Role.values());
        JPasswordField passwordField = new JPasswordField(20);

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Display Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(displayNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(roleCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(passwordField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveBtn = new JButton("Save");
        saveBtn.setBackground(new Color(46, 204, 113));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBorderPainted(false);
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(new Color(149, 165, 166));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setBorderPainted(false);

        saveBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String displayName = displayNameField.getText().trim();
            Role role = (Role) roleCombo.getSelectedItem();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || displayName.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                if (password.length() < 4) {
                    JOptionPane.showMessageDialog(this, "Password must be at least 4 characters long", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                User u;
                if (role == Role.STUDENT) {
                    u = new Student(username, displayName, "S" + (1000 + ds.getUsers().size()));
                } else if (role == Role.INSTRUCTOR) {
                    u = new Instructor(username, displayName, "I" + (9000 + ds.getUsers().size()));
                } else {
                    u = new User(username, displayName, role);
                }
                boolean ok = auth.addUser(u, password);
                if (ok) {
                    onSuccess.run();
                    JOptionPane.showMessageDialog(this, "User added successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
