package edu.univ.erp.ui.admin.dialogs;

import edu.univ.erp.data.DataStore;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AssignInstructorDialog extends JDialog {
    private final DataStore ds = DataStore.getInstance();
    private final Section section;
    private final List<User> instructors;
    private final Runnable onSuccess;

    public AssignInstructorDialog(Frame owner, Section section, List<User> instructors, Runnable onSuccess) {
        super(owner, "Assign Instructor - " + section.getId(), true);
        this.section = section;
        this.instructors = instructors;
        this.onSuccess = onSuccess;
        setSize(400, 200);
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

        JComboBox<String> instructorCombo = new JComboBox<>();
        instructorCombo.addItem("Unassigned");
        for (User instructor : instructors) {
            instructorCombo.addItem(instructor.getUsername() + " - " + instructor.getDisplayName());
            if (instructor.getUsername().equals(section.getInstructorUsername())) {
                instructorCombo.setSelectedItem(instructor.getUsername() + " - " + instructor.getDisplayName());
            }
        }

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Instructor:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(instructorCombo, gbc);

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
            String selection = (String) instructorCombo.getSelectedItem();
            String username = null;
            if (selection != null && !selection.equals("Unassigned")) {
                username = selection.split(" - ")[0];
            }
            section.setInstructorUsername(username);
            ds.save();
            onSuccess.run();
            JOptionPane.showMessageDialog(this, "Instructor assignment updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
