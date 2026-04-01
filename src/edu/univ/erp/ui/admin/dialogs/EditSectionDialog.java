package edu.univ.erp.ui.admin.dialogs;

import edu.univ.erp.data.DataStore;
import edu.univ.erp.domain.Section;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class EditSectionDialog extends JDialog {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private final DataStore ds = DataStore.getInstance();
    private final Section section;
    private final Runnable onSuccess;

    public EditSectionDialog(Frame owner, Section section, Runnable onSuccess) {
        super(owner, "Edit Section - " + section.getId(), true);
        this.section = section;
        this.onSuccess = onSuccess;
        setSize(520, 450);
        setLocationRelativeTo(owner);
        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField dayField = new JTextField(section.getDayOfWeek(), 20);
        JTextField timeField = new JTextField(section.getTimeSlot(), 20);
        JTextField roomField = new JTextField(section.getRoom(), 20);
        JTextField capacityField = new JTextField(String.valueOf(section.getCapacity()), 20);
        JTextField semesterField = new JTextField(section.getSemester(), 20);
        JTextField yearField = new JTextField(section.getYear(), 20);
        JTextField deadlineField = new JTextField(section.getDropDeadline() == null ? "" : section.getDropDeadline().format(DATE_FMT), 20);

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Day(s):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; formPanel.add(dayField, gbc);

        row++; gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; formPanel.add(new JLabel("Time Slot:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; formPanel.add(timeField, gbc);

        row++; gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; formPanel.add(new JLabel("Room:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; formPanel.add(roomField, gbc);

        row++; gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; formPanel.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; formPanel.add(capacityField, gbc);

        row++; gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; formPanel.add(new JLabel("Semester:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; formPanel.add(semesterField, gbc);

        row++; gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; formPanel.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; formPanel.add(yearField, gbc);

        row++; gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; formPanel.add(new JLabel("Drop Deadline (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; formPanel.add(deadlineField, gbc);

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
            int capacity;
            try {
                capacity = Integer.parseInt(capacityField.getText().trim());
                if (capacity <= 0) {
                    JOptionPane.showMessageDialog(this, "Capacity must be a positive number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (capacity < section.getEnrolledUsernames().size()) {
                    JOptionPane.showMessageDialog(this, "Capacity cannot be less than current enrollment (" + section.getEnrolledUsernames().size() + ").", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Capacity must be a valid number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate deadline = null;
            if (!deadlineField.getText().trim().isEmpty()) {
                try {
                    deadline = LocalDate.parse(deadlineField.getText().trim(), DATE_FMT);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid deadline date. Use yyyy-MM-dd.", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            section.setDayOfWeek(dayField.getText().trim());
            section.setTimeSlot(timeField.getText().trim());
            section.setRoom(roomField.getText().trim());
            section.setCapacity(capacity);
            section.setSemester(semesterField.getText().trim());
            section.setYear(yearField.getText().trim());
            section.setDropDeadline(deadline);
            ds.save();
            onSuccess.run();
            JOptionPane.showMessageDialog(this, "Section updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
