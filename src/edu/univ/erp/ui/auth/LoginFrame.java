package edu.univ.erp.ui.auth;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.domain.User;
import edu.univ.erp.ui.admin.AdminDashboard;
import edu.univ.erp.ui.instructor.InstructorDashboard;
import edu.univ.erp.ui.student.StudentDashboard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginFrame extends JFrame {
    private AuthService auth = new AuthService();
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginFrame() {
        setTitle("University ERP System - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 350);
        setLocationRelativeTo(null);
        setResizable(false);
        init();
    }

    private void init() {
        setLayout(new BorderLayout());
        
        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("University ERP System", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);

        // Main Panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 8, 12, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Username
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        mainPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        mainPanel.add(usernameField, gbc);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        mainPanel.add(passwordField, gbc);

        // Login Button
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBackground(new Color(41, 128, 185));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBorderPainted(false);
        loginButton.setFocusPainted(false);
        loginButton.setPreferredSize(new Dimension(120, 40));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(20, 8, 8, 8);
        mainPanel.add(loginButton, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Add action listeners
        loginButton.addActionListener(e -> performLogin());
        
        // Enter key support
        KeyAdapter enterKeyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };
        usernameField.addKeyListener(enterKeyAdapter);
        passwordField.addKeyListener(enterKeyAdapter);
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter both username and password", 
                "Login Error", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        User user;
        try {
            user = auth.login(username, password);
        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(this, 
                e.getMessage(), 
                "Account Locked", 
                JOptionPane.WARNING_MESSAGE);
            passwordField.setText("");
            return;
        }
        
        if (user == null) {
            JOptionPane.showMessageDialog(this, 
                "Invalid username or password", 
                "Login Failed", 
                JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            return;
        }
        
        // Open appropriate dashboard (users can always log in, even during maintenance mode)
        dispose();
        switch (user.getRole()) {
            case STUDENT -> SwingUtilities.invokeLater(() -> new StudentDashboard(user).setVisible(true));
            case INSTRUCTOR -> SwingUtilities.invokeLater(() -> new InstructorDashboard(user).setVisible(true));
            case ADMIN -> SwingUtilities.invokeLater(() -> new AdminDashboard(user).setVisible(true));
        }
    }
}
