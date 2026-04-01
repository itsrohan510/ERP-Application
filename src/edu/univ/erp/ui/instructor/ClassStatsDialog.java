package edu.univ.erp.ui.instructor;

import edu.univ.erp.domain.Section;
import edu.univ.erp.util.GradeUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

public class ClassStatsDialog extends JDialog {
    public ClassStatsDialog(Frame owner, Section section, Map<String, String> gradeMap) {
        super(owner, "Class Stats - " + section.getId(), true);
        setSize(450, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        Stats stats = computeStats(section, gradeMap);
        JPanel summaryPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Summary"));
        summaryPanel.add(createSummaryLabel("Enrolled", section.getEnrolledUsernames().size()));
        summaryPanel.add(createSummaryLabel("Graded", stats.gradedCount));
        summaryPanel.add(createSummaryLabel("Average", stats.averageText));
        summaryPanel.add(createSummaryLabel("Min / Max", stats.rangeText));
        add(summaryPanel, BorderLayout.NORTH);

        DefaultTableModel tableModel = new DefaultTableModel(new String[]{"Student", "Grade"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        for (String username : section.getEnrolledUsernames()) {
            String grade = gradeMap.getOrDefault(username, "Not Set");
            tableModel.addRow(new Object[]{username, grade});
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Student Grades"));
        add(scrollPane, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.add(closeBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private JLabel createSummaryLabel(String title, Object value) {
        JLabel label = new JLabel(title + ": " + value);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return label;
    }

    private Stats computeStats(Section section, Map<String, String> gradeMap) {
        double total = 0;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        int count = 0;

        for (String username : section.getEnrolledUsernames()) {
            String gradeStr = gradeMap.get(username);
            Double gradeVal = GradeUtils.extractScore(gradeStr);
            if (gradeVal == null) {
                continue;
            }
            total += gradeVal;
            min = Math.min(min, gradeVal);
            max = Math.max(max, gradeVal);
            count++;
        }

        String averageText = count == 0 ? "N/A" : String.format("%.2f", total / count);
        String rangeText = count == 0 ? "N/A" : String.format("%.2f / %.2f", min, max);
        return new Stats(count, averageText, rangeText);
    }

    private static class Stats {
        int gradedCount;
        String averageText;
        String rangeText;

        Stats(int gradedCount, String averageText, String rangeText) {
            this.gradedCount = gradedCount;
            this.averageText = averageText;
            this.rangeText = rangeText;
        }
    }
}

