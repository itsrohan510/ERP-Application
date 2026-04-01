package edu.univ.erp.ui.instructor;

import edu.univ.erp.access.AccessGuard;
import edu.univ.erp.domain.Assessment;
import edu.univ.erp.service.AssessmentService;
import edu.univ.erp.service.EnrollmentService;
import edu.univ.erp.util.GradeUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssessmentScoresDialog extends JDialog {
    private final String sectionId;
    private final String studentUsername;
    private final AssessmentService assessmentService = new AssessmentService();
    private final EnrollmentService enrollmentService = new EnrollmentService();
    private final DefaultTableModel tableModel;
    private final JLabel finalScoreLabel = new JLabel("Final Grade: n/a");

    public AssessmentScoresDialog(Frame owner, String sectionId, String studentUsername) {
        super(owner, "Scores for " + studentUsername, true);
        this.sectionId = sectionId;
        this.studentUsername = studentUsername;
        setSize(500, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        String[] columns = {"Assessment", "Weight (%)", "Max Score", "Score"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Disable editing during maintenance mode
                return column == 3 && !AccessGuard.isMaintenanceMode();
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return String.class;
                return Double.class;
            }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Enter Scores"));
        add(scrollPane, BorderLayout.CENTER);

        List<Assessment> assessments = assessmentService.getAssessments(sectionId);
        if (assessments.isEmpty()) {
            JOptionPane.showMessageDialog(owner, "No assessments defined for this section yet.", "No Assessments", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            return;
        }

        loadExistingScores(assessments);

        JPanel footer = new JPanel(new BorderLayout());
        footer.add(finalScoreLabel, BorderLayout.WEST);

        JButton saveBtn = new JButton("Save Scores");
        saveBtn.addActionListener(e -> handleSave());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveBtn);
        footer.add(buttonPanel, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }

    private void loadExistingScores(List<Assessment> assessments) {
        Map<String, Double> scores = assessmentService.getScores(sectionId, studentUsername);
        for (Assessment a : assessments) {
            Double score = scores.get(a.getName());
            tableModel.addRow(new Object[]{
                a.getName(),
                a.getWeightPercentage(),
                a.getMaxScore(),
                score == null ? null : score
            });
        }
        double finalScore = assessmentService.computeFinalScore(sectionId, studentUsername);
        updateFinalScoreLabel(finalScore);
    }

    private void handleSave() {
        if (AccessGuard.isMaintenanceMode()) {
            JOptionPane.showMessageDialog(this, "Cannot save scores during maintenance mode.", "Maintenance Mode", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Map<String, Double> scores = new HashMap<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object scoreObj = tableModel.getValueAt(i, 3);
            String assessmentName = (String) tableModel.getValueAt(i, 0);
            double maxScore = ((Number) tableModel.getValueAt(i, 2)).doubleValue();

            if (scoreObj == null) {
                continue;
            }
            double score;
            if (scoreObj instanceof Number n) {
                score = n.doubleValue();
            } else {
                try {
                    score = Double.parseDouble(scoreObj.toString());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid score for " + assessmentName, "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            if (score < 0 || score > maxScore) {
                JOptionPane.showMessageDialog(this, "Score for " + assessmentName + " must be between 0 and " + maxScore, "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            scores.put(assessmentName, score);
        }
        if (scores.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter at least one score before saving.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            assessmentService.saveScores(sectionId, studentUsername, scores);
            double finalScore = assessmentService.computeFinalScore(sectionId, scores);
            if (!enrollmentService.setGrade(sectionId, studentUsername, GradeUtils.formatGrade(finalScore))) {
                JOptionPane.showMessageDialog(this, "Failed to update final grade. System may be in maintenance mode.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
            updateFinalScoreLabel(finalScore);
            JOptionPane.showMessageDialog(this, "Scores saved and final grade updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Save Failed", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateFinalScoreLabel(double finalScore) {
        if (Double.isNaN(finalScore)) {
            finalScoreLabel.setText("Final Grade: n/a");
            return;
        }
        finalScoreLabel.setText("Final Grade: " + GradeUtils.formatGrade(finalScore));
    }
}

