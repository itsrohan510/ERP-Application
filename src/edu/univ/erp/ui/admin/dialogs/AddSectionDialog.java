package edu.univ.erp.ui.admin.dialogs;

import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.CourseService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AddSectionDialog extends JDialog {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private final CourseService cs = new CourseService();
    private final Runnable onSuccess;

    public AddSectionDialog(Frame owner, Runnable onSuccess) {
        super(owner, "Add New Section", true);
        this.onSuccess = onSuccess;
        setSize(520, 500);
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

        JTextField courseCodeField = new JTextField(20);
        JTextField sectionIdField = new JTextField(20);
        JTextField instructorField = new JTextField(20);
        JTextField capacityField = new JTextField(20);
        capacityField.setText("30");
        JTextField dayField = new JTextField("Mon/Wed", 20);
        JTextField timeField = new JTextField("09:00-10:30", 20);
        JTextField roomField = new JTextField("Room 100", 20);
        JTextField semesterField = new JTextField("Fall", 20);
        JTextField yearField = new JTextField("2024", 20);
        JTextField deadlineField = new JTextField(LocalDate.now().plusWeeks(2).format(DATE_FMT), 20);

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Course Code:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(courseCodeField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Section ID:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(sectionIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Instructor (optional):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(instructorField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(capacityField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Day(s):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(dayField, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Time Slot:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(timeField, gbc);

        gbc.gridx = 0; gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Room:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(roomField, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Semester:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(semesterField, gbc);

        gbc.gridx = 0; gbc.gridy = 8; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(yearField, gbc);

        gbc.gridx = 0; gbc.gridy = 9; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Drop Deadline (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(deadlineField, gbc);

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
            String courseCode = courseCodeField.getText().trim();
            String sectionId = sectionIdField.getText().trim();
            String instructor = instructorField.getText().trim();
            String capStr = capacityField.getText().trim();

            if (courseCode.isEmpty() || sectionId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Course code and Section ID are required", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Course course = cs.getCourse(courseCode);
            if (course == null) {
                JOptionPane.showMessageDialog(this, "Course not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int capacity = 30;
            try {
                if (!capStr.isEmpty()) {
                    capacity = Integer.parseInt(capStr);
                    if (capacity <= 0) {
                        JOptionPane.showMessageDialog(this, "Capacity must be a positive number", "Validation Error", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid capacity value. Must be a number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LocalDate deadline = null;
            if (!deadlineField.getText().trim().isEmpty()) {
                try {
                    deadline = LocalDate.parse(deadlineField.getText().trim(), DATE_FMT);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid deadline date. Use yyyy-MM-dd.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            Section s = new Section(sectionId, courseCode, instructor.isEmpty() ? null : instructor, capacity,
                dayField.getText().trim().isEmpty() ? "TBD" : dayField.getText().trim(),
                timeField.getText().trim().isEmpty() ? "TBD" : timeField.getText().trim(),
                roomField.getText().trim().isEmpty() ? "TBD" : roomField.getText().trim(),
                semesterField.getText().trim().isEmpty() ? "N/A" : semesterField.getText().trim(),
                yearField.getText().trim().isEmpty() ? "N/A" : yearField.getText().trim(),
                deadline);
            boolean ok = cs.createSection(s);
            if (ok) {
                onSuccess.run();
                JOptionPane.showMessageDialog(this, "Section created successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Section ID already exists or error occurred", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
