package edu.univ.erp.ui.instructor;

import edu.univ.erp.access.AccessGuard;
import edu.univ.erp.domain.User;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.AssessmentService;
import edu.univ.erp.service.CourseService;
import edu.univ.erp.service.EnrollmentService;
import edu.univ.erp.ui.common.ChangePasswordDialog;
import edu.univ.erp.util.GradeUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstructorDashboard extends JFrame {
    private User user;
    private CourseService cs = new CourseService();
    private EnrollmentService es = new EnrollmentService();
    private AssessmentService assessmentService = new AssessmentService();

    private DefaultTableModel sectionsTableModel;
    private DefaultTableModel studentsTableModel;
    private JTable sectionsTable;
    private JTable studentsTable;
    private JLabel maintenanceBanner;
    private JButton manageAssessmentsBtn;
    private JButton enterScoresBtn;
    private JButton importCsvBtn;

    public InstructorDashboard(User user) {
        this.user = user;
        setTitle("Instructor Dashboard - " + user.getDisplayName());
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(155, 89, 182));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel welcomeLabel = new JLabel("Welcome, " + user.getDisplayName());
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerButtons.setOpaque(false);
        
        JButton changePasswordBtn = createStyledButton("Change Password", new Color(52, 152, 219));
        changePasswordBtn.addActionListener(e -> {
            ChangePasswordDialog dialog = new ChangePasswordDialog(this, user.getUsername());
            dialog.setVisible(true);
        });
        
        JButton refreshBtn = createStyledButton("Refresh", new Color(46, 204, 113));
        refreshBtn.addActionListener(e -> {
            updateMaintenanceBanner();
            loadMySections();
            loadStudentsForSelectedSection();
        });
        
        headerButtons.add(changePasswordBtn);
        headerButtons.add(refreshBtn);
        headerPanel.add(headerButtons, BorderLayout.EAST);
        
        // Maintenance Banner
        maintenanceBanner = new JLabel("", SwingConstants.CENTER);
        maintenanceBanner.setOpaque(true);
        maintenanceBanner.setBackground(new Color(231, 76, 60));
        maintenanceBanner.setForeground(Color.WHITE);
        maintenanceBanner.setFont(new Font("Segoe UI", Font.BOLD, 12));
        maintenanceBanner.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(headerPanel, BorderLayout.NORTH);
        northPanel.add(maintenanceBanner, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);
        updateMaintenanceBanner();

        // Main Content Panel
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // My Sections Panel
        JPanel sectionsPanel = new JPanel(new BorderLayout());
        String[] sectionColumns = {"Section ID", "Course Code", "Enrolled", "Capacity"};
        sectionsTableModel = new DefaultTableModel(sectionColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        sectionsTable = new JTable(sectionsTableModel);
        sectionsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sectionsTable.setRowHeight(25);
        sectionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sectionsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        sectionsTable.getTableHeader().setBackground(new Color(236, 240, 241));
        sectionsTable.setRowSorter(new TableRowSorter<>(sectionsTableModel));
        sectionsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadStudentsForSelectedSection();
            }
        });

        JScrollPane sectionsScroll = new JScrollPane(sectionsTable);
        sectionsScroll.setBorder(BorderFactory.createTitledBorder("My Sections"));
        sectionsPanel.add(sectionsScroll, BorderLayout.CENTER);
        mainPanel.add(sectionsPanel);

        // Students Panel
        JPanel studentsPanel = new JPanel(new BorderLayout());
        String[] studentColumns = {"Username", "Current Grade"};
        studentsTableModel = new DefaultTableModel(studentColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Disable editing during maintenance mode
                return column == 1 && !AccessGuard.isMaintenanceMode();
            }
        };
        studentsTable = new JTable(studentsTableModel);
        studentsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        studentsTable.setRowHeight(25);
        studentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        studentsTable.getTableHeader().setBackground(new Color(236, 240, 241));
        studentsTable.setRowSorter(new TableRowSorter<>(studentsTableModel));

        JScrollPane studentsScroll = new JScrollPane(studentsTable);
        studentsScroll.setBorder(BorderFactory.createTitledBorder("Enrolled Students"));
        studentsPanel.add(studentsScroll, BorderLayout.CENTER);
        mainPanel.add(studentsPanel);

        add(mainPanel, BorderLayout.CENTER);

        // Action Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        actionPanel.setBorder(BorderFactory.createTitledBorder("Grade Management"));
        actionPanel.setBackground(Color.WHITE);

        JButton classStatsBtn = createStyledButton("Class Stats", new Color(142, 68, 173));
        classStatsBtn.addActionListener(e -> showClassStats());

        JButton exportCsvBtn = createStyledButton("Export CSV", new Color(22, 160, 133));
        exportCsvBtn.addActionListener(e -> exportGradesCsv());

        importCsvBtn = createStyledButton("Import CSV", new Color(192, 57, 43));
        importCsvBtn.setEnabled(!AccessGuard.isMaintenanceMode());
        importCsvBtn.addActionListener(e -> {
            if (AccessGuard.isMaintenanceMode()) {
                JOptionPane.showMessageDialog(this, "Importing grades is disabled during maintenance mode.", "Maintenance Mode", JOptionPane.WARNING_MESSAGE);
                return;
            }
            importGradesCsv();
        });

        manageAssessmentsBtn = createStyledButton("Assessments", new Color(52, 73, 94));
        manageAssessmentsBtn.setEnabled(!AccessGuard.isMaintenanceMode());
        manageAssessmentsBtn.addActionListener(e -> {
            if (AccessGuard.isMaintenanceMode()) {
                JOptionPane.showMessageDialog(this, "Assessment management is disabled during maintenance mode.", "Maintenance Mode", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int sectionRow = sectionsTable.getSelectedRow();
            if (sectionRow < 0) {
                JOptionPane.showMessageDialog(this, "Select a section first.", "No Section Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String sectionId = (String) sectionsTableModel.getValueAt(sectionRow, 0);
            AssessmentDefinitionsDialog dialog = new AssessmentDefinitionsDialog(this, sectionId);
            dialog.setVisible(true);
        });

        enterScoresBtn = createStyledButton("Enter Scores", new Color(41, 128, 185));
        enterScoresBtn.setEnabled(!AccessGuard.isMaintenanceMode());
        enterScoresBtn.addActionListener(e -> {
            if (AccessGuard.isMaintenanceMode()) {
                JOptionPane.showMessageDialog(this, "Entering scores is disabled during maintenance mode.", "Maintenance Mode", JOptionPane.WARNING_MESSAGE);
                return;
            }
            openScoresDialog();
        });

        JButton loadStudentsBtn = createStyledButton("Load Students", new Color(52, 152, 219));
        loadStudentsBtn.addActionListener(e -> loadStudentsForSelectedSection());

        actionPanel.add(loadStudentsBtn);
        actionPanel.add(classStatsBtn);
        actionPanel.add(exportCsvBtn);
        actionPanel.add(importCsvBtn);
        actionPanel.add(manageAssessmentsBtn);
        actionPanel.add(enterScoresBtn);

        add(actionPanel, BorderLayout.SOUTH);

        loadMySections();
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(140, 35));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadMySections() {
        try {
            sectionsTableModel.setRowCount(0);
            List<Section> secs = cs.listSections();
            for (Section s : secs) {
                if (user.getUsername().equals(s.getInstructorUsername())) {
                    sectionsTableModel.addRow(new Object[]{
                        s.getId(),
                        s.getCourseCode(),
                        s.getEnrolledUsernames().size(),
                        s.getCapacity()
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading sections: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadStudentsForSelectedSection() {
        try {
            studentsTableModel.setRowCount(0);
            int row = sectionsTable.getSelectedRow();
            if (row < 0) {
                return;
            }
            
            String sectionId = (String) sectionsTableModel.getValueAt(row, 0);
            Section section = cs.getSection(sectionId);
            if (section == null) {
                return;
            }

            Map<String, String> gradeMap = getGradesForSection(sectionId);

            for (String username : section.getEnrolledUsernames()) {
                String grade = gradeMap.getOrDefault(username, "Not Set");
                studentsTableModel.addRow(new Object[]{username, grade});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading students: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openScoresDialog() {
        Section section = requireSelectedSection("Enter Scores");
        if (section == null) {
            return;
        }
        int studentRow = studentsTable.getSelectedRow();
        if (studentRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a student first.", "No Student Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String username = (String) studentsTableModel.getValueAt(studentRow, 0);
        if (assessmentService.getAssessments(section.getId()).isEmpty()) {
            JOptionPane.showMessageDialog(this, "Define assessments before entering scores.", "No Assessments", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        AssessmentScoresDialog dialog = new AssessmentScoresDialog(this, section.getId(), username);
        dialog.setVisible(true);
        loadStudentsForSelectedSection();
    }

    private void showClassStats() {
        Section section = requireSelectedSection("Class Stats");
        if (section == null) return;
        Map<String, String> gradeMap = getGradesForSection(section.getId());
        ClassStatsDialog dialog = new ClassStatsDialog(this, section, gradeMap);
        dialog.setVisible(true);
    }

    private void exportGradesCsv() {
        Section section = requireSelectedSection("Export Grades");
        if (section == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(section.getId() + "-grades.csv"));
        int option = chooser.showSaveDialog(this);
        if (option != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getParentFile() == null ? "" : file.getParentFile().getAbsolutePath(),
                file.getName() + ".csv");
        }
        Map<String, String> gradeMap = getGradesForSection(section.getId());
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.println("username,grade");
            for (String username : section.getEnrolledUsernames()) {
                String grade = gradeMap.getOrDefault(username, "");
                writer.println(username + "," + grade);
            }
            JOptionPane.showMessageDialog(this, "Grades exported to " + file.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to export grades: " + ex.getMessage(), "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void importGradesCsv() {
        Section section = requireSelectedSection("Import Grades");
        if (section == null) return;
        JFileChooser chooser = new JFileChooser();
        int option = chooser.showOpenDialog(this);
        if (option != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        Map<String, String> imported = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            boolean headerHandled = false;
            while ((line = reader.readLine()) != null) {
                if (!headerHandled && line.toLowerCase().contains("username")) {
                    headerHandled = true;
                    continue;
                }
                headerHandled = true;
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", 2);
                if (parts.length < 2) continue;
                String username = parts[0].trim();
                String grade = parts[1].trim();
                imported.put(username, grade);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to read file: " + ex.getMessage(), "Import Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (imported.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No grade data found in CSV.", "Import", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int applied = 0;
        int errors = 0;
        for (String username : section.getEnrolledUsernames()) {
            String grade = imported.get(username);
            if (grade != null && !grade.isBlank()) {
                try {
                    Double score = GradeUtils.extractScore(grade);
                    String value = score != null ? GradeUtils.formatGrade(score) : grade;
                    if (es.setGrade(section.getId(), username, value)) {
                        applied++;
                    } else {
                        errors++;
                    }
                } catch (Exception e) {
                    errors++;
                }
            }
        }
        loadStudentsForSelectedSection();
        String message = applied > 0 
            ? String.format("Applied %d grade updates.", applied) + (errors > 0 ? " " + errors + " errors occurred." : "")
            : "No matching students found in CSV.";
        JOptionPane.showMessageDialog(this,
            message,
            "Import Complete",
            applied > 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
    }

    private Map<String, String> getGradesForSection(String sectionId) {
        return es.getGradesForSection(sectionId);
    }

    private Section requireSelectedSection(String actionTitle) {
        try {
            int sectionRow = sectionsTable.getSelectedRow();
            if (sectionRow < 0) {
                JOptionPane.showMessageDialog(this, "Please select a section first.", actionTitle, JOptionPane.WARNING_MESSAGE);
                return null;
            }
            String sectionId = (String) sectionsTableModel.getValueAt(sectionRow, 0);
            Section section = cs.getSection(sectionId);
            if (section == null) {
                JOptionPane.showMessageDialog(this, "Selected section could not be found.", actionTitle, JOptionPane.ERROR_MESSAGE);
                return null;
            }
            return section;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error: " + e.getMessage(),
                actionTitle,
                JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void updateMaintenanceBanner() {
        if (maintenanceBanner == null) return;
        try {
            boolean maintenanceMode = AccessGuard.isMaintenanceMode();
            if (maintenanceMode) {
                maintenanceBanner.setText("MAINTENANCE MODE ENABLED - Viewing only. Changes are disabled.");
                maintenanceBanner.setVisible(true);
            } else {
                maintenanceBanner.setText("");
                maintenanceBanner.setVisible(false);
            }
            // Update button states
            if (manageAssessmentsBtn != null) manageAssessmentsBtn.setEnabled(!maintenanceMode);
            if (enterScoresBtn != null) enterScoresBtn.setEnabled(!maintenanceMode);
            if (importCsvBtn != null) importCsvBtn.setEnabled(!maintenanceMode);
            // Update table editability
            if (studentsTableModel != null) {
                studentsTableModel.fireTableStructureChanged();
            }
        } catch (Exception e) {
            maintenanceBanner.setVisible(false);
        }
    }
}
