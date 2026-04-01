package edu.univ.erp.ui.instructor;

import edu.univ.erp.access.AccessGuard;
import edu.univ.erp.domain.Assessment;
import edu.univ.erp.service.AssessmentService;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AssessmentDefinitionsDialog extends JDialog {
    private final String sectionId;
    private final AssessmentService assessmentService = new AssessmentService();
    private final DefaultTableModel tableModel;
    private final JLabel totalWeightLabel = new JLabel();

    public AssessmentDefinitionsDialog(Frame owner, String sectionId) {
        super(owner, "Assessments for " + sectionId, true);
        this.sectionId = sectionId;
        setSize(500, 450);
        setLocationRelativeTo(owner);

        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel tablePanel = new JPanel(new BorderLayout());
        String[] columns = {"Assessment", "Weight (%)", "Max Score"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return String.class;
                return Double.class;
            }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Assessments"));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                refreshTotalWeight();
            }
        });

        JPanel formPanel = new JPanel(new GridLayout(2, 4, 10, 5));
        JTextField nameField = new JTextField();
        JTextField weightField = new JTextField();
        JTextField maxField = new JTextField("100");

        formPanel.add(new JLabel("Name"));
        formPanel.add(new JLabel("Weight (%)"));
        formPanel.add(new JLabel("Max Score"));
        formPanel.add(new JLabel());

        formPanel.add(nameField);
        formPanel.add(weightField);
        formPanel.add(maxField);

        JButton addBtn = new JButton("Add/Update");
        addBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Assessment name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            double weight;
            double maxScore;
            try {
                weight = Double.parseDouble(weightField.getText().trim());
                maxScore = Double.parseDouble(maxField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Weight and Max Score must be numbers.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (weight <= 0 || maxScore <= 0) {
                JOptionPane.showMessageDialog(this, "Weight and Max Score must be positive.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean updated = false;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (name.equalsIgnoreCase((String) tableModel.getValueAt(i, 0))) {
                    tableModel.setValueAt(name, i, 0);
                    tableModel.setValueAt(weight, i, 1);
                    tableModel.setValueAt(maxScore, i, 2);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                tableModel.addRow(new Object[]{name, weight, maxScore});
            }
            nameField.setText("");
            weightField.setText("");
            maxField.setText("100");
        });
        formPanel.add(addBtn);
        tablePanel.add(formPanel, BorderLayout.SOUTH);

        add(tablePanel, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        refreshTotalWeight();
        footer.add(totalWeightLabel, BorderLayout.WEST);

        JPanel footerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton deleteBtn = new JButton("Remove Selected");
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                tableModel.removeRow(row);
            }
        });

        JButton saveBtn = new JButton("Save & Close");
        saveBtn.addActionListener(e -> {
            if (AccessGuard.isMaintenanceMode()) {
                JOptionPane.showMessageDialog(this, "Cannot save assessments during maintenance mode.", "Maintenance Mode", JOptionPane.WARNING_MESSAGE);
                return;
            }
            double totalWeight = calculateTotalWeight();
            if (totalWeight != 100) {
                int option = JOptionPane.showConfirmDialog(this,
                    "Total weight is " + totalWeight + "%. Do you still want to save?",
                    "Weight Not 100%", JOptionPane.YES_NO_OPTION);
                if (option != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            List<Assessment> updated;
            try {
                updated = buildAssessmentList();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                assessmentService.saveAssessments(sectionId, updated);
                dispose();
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Save Failed", JOptionPane.WARNING_MESSAGE);
            }
        });

        footerButtons.add(deleteBtn);
        footerButtons.add(saveBtn);
        footer.add(footerButtons, BorderLayout.EAST);

        add(footer, BorderLayout.SOUTH);

        loadExistingAssessments();
    }

    private void loadExistingAssessments() {
        List<Assessment> assessments = assessmentService.getAssessments(sectionId);
        for (Assessment a : assessments) {
            tableModel.addRow(new Object[]{a.getName(), a.getWeightPercentage(), a.getMaxScore()});
        }
        refreshTotalWeight();
    }

    private List<Assessment> buildAssessmentList() {
        List<Assessment> list = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String name = (String) tableModel.getValueAt(i, 0);
            Object weightObj = tableModel.getValueAt(i, 1);
            Object maxObj = tableModel.getValueAt(i, 2);
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Assessment name cannot be empty.");
            }
            if (!(weightObj instanceof Number) || !(maxObj instanceof Number)) {
                throw new IllegalArgumentException("Weight and Max Score must be filled for all rows.");
            }
            double weight = ((Number) weightObj).doubleValue();
            double maxScore = ((Number) maxObj).doubleValue();
            if (weight <= 0 || maxScore <= 0) {
                throw new IllegalArgumentException("Weight and Max Score must be positive.");
            }
            list.add(new Assessment(sectionId, name, weight, maxScore));
        }
        return list;
    }

    private double calculateTotalWeight() {
        double total = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object value = tableModel.getValueAt(i, 1);
            if (value instanceof Number) {
                total += ((Number) value).doubleValue();
            }
        }
        return Math.round(total * 100.0) / 100.0;
    }

    private void refreshTotalWeight() {
        double total = calculateTotalWeight();
        totalWeightLabel.setText("Total Weight: " + total + "%");
        totalWeightLabel.setForeground(Math.abs(total - 100) < 0.01 ? new Color(39, 174, 96) : new Color(211, 84, 0));
    }
}

