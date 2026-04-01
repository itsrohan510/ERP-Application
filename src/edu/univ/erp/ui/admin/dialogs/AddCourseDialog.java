package edu.univ.erp.ui.admin.dialogs;

import edu.univ.erp.domain.Course;
import edu.univ.erp.service.CourseService;

import javax.swing.*;
import java.awt.*;

public class AddCourseDialog extends JDialog {
    private final CourseService cs = new CourseService();
    private final Runnable onSuccess;

    public AddCourseDialog(Frame owner, Runnable onSuccess) {
        super(owner, "Add New Course", true);
        this.onSuccess = onSuccess;
        setSize(450, 250);
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

        JTextField codeField = new JTextField(20);
        JTextField titleField = new JTextField(20);
        JTextField creditsField = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Course Code:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(codeField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Credits:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(creditsField, gbc);

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
            String code = codeField.getText().trim();
            String title = titleField.getText().trim();
            String creds = creditsField.getText().trim();

            if (code.isEmpty() || title.isEmpty() || creds.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int credits = Integer.parseInt(creds);
                if (credits <= 0) {
                    JOptionPane.showMessageDialog(this, "Credits must be a positive number", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Course course = new Course(code, title, credits);
                boolean ok = cs.createCourse(course);
                if (ok) {
                    onSuccess.run();
                    JOptionPane.showMessageDialog(this, "Course created successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Course code already exists", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid credits value. Must be a number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
