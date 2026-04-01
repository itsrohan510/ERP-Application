package edu.univ.erp.ui.student;

import edu.univ.erp.access.AccessGuard;
import edu.univ.erp.domain.*;
import edu.univ.erp.service.AssessmentService;
import edu.univ.erp.service.CourseService;
import edu.univ.erp.service.EnrollmentService;
import edu.univ.erp.ui.common.ChangePasswordDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentDashboard extends JFrame {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private final User user;
    private CourseService cs = new CourseService();
    private EnrollmentService es = new EnrollmentService();
    private AssessmentService assessmentService = new AssessmentService();

    private DefaultTableModel sectionsTableModel;
    private DefaultTableModel enrolledTableModel;
    private DefaultTableModel gradesTableModel;
    private DefaultTableModel timetableTableModel;
    private DefaultTableModel assessmentTableModel;

    private JTable sectionsTable;
    private JTable enrolledTable;
    private JTable gradesTable;
    private JTable timetableTable;
    private JTable assessmentTable;

    private JLabel finalGradeLabel = new JLabel("Final Grade: N/A");
    private Map<String, String> finalGradeMap = new HashMap<>();
    private JLabel maintenanceBanner;
    private JButton enrollBtn;
    private JButton dropBtn;

    public StudentDashboard(User user) {
        this.user = user;
        setTitle("Student Dashboard - " + user.getDisplayName());
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 152, 219));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel welcomeLabel = new JLabel("Welcome, " + user.getDisplayName());
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerButtons.setOpaque(false);
        
        JButton changePasswordBtn = createStyledButton("Change Password", new Color(142, 68, 173));
        changePasswordBtn.addActionListener(e -> {
            ChangePasswordDialog dialog = new ChangePasswordDialog(this, user.getUsername());
            dialog.setVisible(true);
        });
        
        JButton refreshBtn = createStyledButton("Refresh", new Color(46, 204, 113));
        refreshBtn.addActionListener(e -> {
            updateMaintenanceBanner();
            loadSections();
            loadEnrolledSections();
            loadGrades();
            loadTimetable();
            JOptionPane.showMessageDialog(this, "Data refreshed", "Refresh", JOptionPane.INFORMATION_MESSAGE);
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

        // Main Content with Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Enrollment Tab
        JPanel enrollmentPanel = createEnrollmentPanel();
        tabbedPane.addTab("Course Enrollment", enrollmentPanel);

        // My Courses Tab
        JPanel myCoursesPanel = createMyCoursesPanel();
        tabbedPane.addTab("My Courses", myCoursesPanel);

        // Timetable Tab
        JPanel timetablePanel = createTimetablePanel();
        tabbedPane.addTab("Timetable", timetablePanel);

        // Grades Tab
        JPanel gradesPanel = createGradesPanel();
        tabbedPane.addTab("Grades", gradesPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createEnrollmentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Sections Table
        String[] columns = {"Section ID", "Course Code", "Title", "Credits", "Capacity", "Enrolled", "Instructor", "Schedule", "Status"};
        sectionsTableModel = new DefaultTableModel(columns, 0) {
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

        JScrollPane scrollPane = new JScrollPane(sectionsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Available Sections"));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        actionPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
        actionPanel.setBackground(Color.WHITE);

        enrollBtn = createStyledButton("Enroll in Selected Section", new Color(46, 204, 113));
        enrollBtn.setEnabled(!AccessGuard.isMaintenanceMode());
        enrollBtn.addActionListener(e -> {
            if (AccessGuard.isMaintenanceMode()) {
                JOptionPane.showMessageDialog(this, "Enrollment is disabled during maintenance mode.", "Maintenance Mode", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int row = sectionsTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a section to enroll", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String sectionId = (String) sectionsTableModel.getValueAt(row, 0);
            Section section = cs.getSection(sectionId);
            if (section == null) {
                JOptionPane.showMessageDialog(this, "Section not found.", "Enrollment Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (section.getEnrolledUsernames().contains(user.getUsername())) {
                JOptionPane.showMessageDialog(this, "You are already enrolled in this section.", "Enrollment Failed", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (section.getEnrolledUsernames().size() >= section.getCapacity()) {
                JOptionPane.showMessageDialog(this, "No seats available in this section.", "Enrollment Failed", JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean ok = es.enroll(user.getUsername(), sectionId);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Successfully enrolled in " + sectionId, "Enrollment Success", JOptionPane.INFORMATION_MESSAGE);
                loadSections();
                loadEnrolledSections();
                loadTimetable();
            } else {
                JOptionPane.showMessageDialog(this, "Could not enroll. Section may be full, you may already be enrolled, or system is in maintenance mode.", "Enrollment Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        actionPanel.add(enrollBtn);
        panel.add(actionPanel, BorderLayout.SOUTH);

        loadSections();
        return panel;
    }

    private JPanel createMyCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Section ID", "Course Code", "Title", "Instructor", "Enrolled", "Capacity", "Drop Deadline", "Schedule"};
        enrolledTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        enrolledTable = new JTable(enrolledTableModel);
        enrolledTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        enrolledTable.setRowHeight(25);
        enrolledTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        enrolledTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        enrolledTable.getTableHeader().setBackground(new Color(236, 240, 241));
        enrolledTable.setRowSorter(new TableRowSorter<>(enrolledTableModel));

        JScrollPane scrollPane = new JScrollPane(enrolledTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("My Enrolled Sections"));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Drop Button
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        actionPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
        actionPanel.setBackground(Color.WHITE);

        dropBtn = createStyledButton("Drop Selected Section", new Color(231, 76, 60));
        dropBtn.setEnabled(!AccessGuard.isMaintenanceMode());
        dropBtn.addActionListener(e -> {
            if (AccessGuard.isMaintenanceMode()) {
                JOptionPane.showMessageDialog(this, "Dropping courses is disabled during maintenance mode.", "Maintenance Mode", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int row = enrolledTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a section to drop", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String sectionId = (String) enrolledTableModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to drop " + sectionId + "?", 
                "Confirm Drop", 
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                Section section = cs.getSection(sectionId);
                if (section == null) {
                    JOptionPane.showMessageDialog(this, "Section not found.", "Drop Failed", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                LocalDate deadline = section.getDropDeadline();
                if (deadline != null && LocalDate.now().isAfter(deadline)) {
                    JOptionPane.showMessageDialog(this, "Drop deadline has passed (" + deadline.format(DATE_FMT) + ").", "Drop Not Allowed", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                boolean ok = es.drop(user.getUsername(), sectionId);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Successfully dropped " + sectionId, "Drop Success", JOptionPane.INFORMATION_MESSAGE);
                    loadSections();
                    loadEnrolledSections();
                    loadTimetable();
                } else {
                    JOptionPane.showMessageDialog(this, "Could not drop. You may not be enrolled or system is in maintenance mode.", "Drop Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        actionPanel.add(dropBtn);
        panel.add(actionPanel, BorderLayout.SOUTH);

        loadEnrolledSections();
        return panel;
    }

    private JPanel createGradesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Section ID", "Course Code", "Grade"};
        gradesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        gradesTable = new JTable(gradesTableModel);
        gradesTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gradesTable.setRowHeight(25);
        gradesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        gradesTable.getTableHeader().setBackground(new Color(236, 240, 241));
        gradesTable.setRowSorter(new TableRowSorter<>(gradesTableModel));
        gradesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = gradesTable.getSelectedRow();
                if (row >= 0) {
                    String sectionId = (String) gradesTableModel.getValueAt(row, 0);
                    loadAssessmentBreakdown(sectionId);
                } else {
                    clearAssessmentBreakdown();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(gradesTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("My Grades"));

        assessmentTableModel = new DefaultTableModel(new String[]{"Assessment", "Weight (%)", "Max Score", "My Score"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        assessmentTable = new JTable(assessmentTableModel);
        assessmentTable.setRowHeight(24);
        assessmentTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        assessmentTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        assessmentTable.setRowSorter(new TableRowSorter<>(assessmentTableModel));

        JScrollPane assessmentScroll = new JScrollPane(assessmentTable);
        assessmentScroll.setPreferredSize(new Dimension(0, 180));
        assessmentScroll.setBorder(BorderFactory.createTitledBorder("Assessment Components"));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        bottomPanel.setPreferredSize(new Dimension(0, 220));
        bottomPanel.add(assessmentScroll, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        finalGradeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel hintLabel = new JLabel("Select a course above to see assessment details.");
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        infoPanel.add(finalGradeLabel);
        infoPanel.add(hintLabel);

        JButton transcriptBtn = createStyledButton("Download Transcript (CSV)", new Color(39, 174, 96));
        transcriptBtn.setPreferredSize(new Dimension(250, 35));
        transcriptBtn.addActionListener(e -> StudentTranscriptExporter.exportTranscript(this, user.getUsername(), user.getDisplayName()));
        infoPanel.add(transcriptBtn);

        bottomPanel.add(infoPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, bottomPanel);
        splitPane.setResizeWeight(0.55);
        splitPane.setDividerLocation(0.55);
        splitPane.setDividerSize(6);
        splitPane.setBorder(null);
        panel.add(splitPane, BorderLayout.CENTER);

        loadGrades();
        return panel;
    }

    private JPanel createTimetablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        timetableTableModel = new DefaultTableModel(new String[]{"Day(s)", "Time", "Section", "Course", "Instructor"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        timetableTable = new JTable(timetableTableModel);
        timetableTable.setRowHeight(25);
        timetableTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        timetableTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        timetableTable.setRowSorter(new TableRowSorter<>(timetableTableModel));

        JScrollPane scrollPane = new JScrollPane(timetableTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Weekly Timetable"));
        panel.add(scrollPane, BorderLayout.CENTER);

        loadTimetable();
        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(200, 35));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadSections() {
        if (sectionsTableModel == null) return;
        sectionsTableModel.setRowCount(0);
        List<Section> secs = cs.listSections();
        for (Section s : secs) {
            boolean isEnrolled = s.getEnrolledUsernames().contains(user.getUsername());
            String status = isEnrolled ? "Enrolled" : (s.getEnrolledUsernames().size() >= s.getCapacity() ? "Full" : "Available");
            Course course = cs.getCourse(s.getCourseCode());
            sectionsTableModel.addRow(new Object[]{
                s.getId(),
                s.getCourseCode(),
                course != null ? course.getTitle() : "N/A",
                course != null ? course.getCredits() : "-",
                s.getCapacity(),
                s.getEnrolledUsernames().size(),
                s.getInstructorUsername() == null ? "TBD" : s.getInstructorUsername(),
                s.getDayOfWeek() + " " + s.getTimeSlot(),
                status
            });
        }
    }

    private void loadEnrolledSections() {
        if (enrolledTableModel == null) return;
        enrolledTableModel.setRowCount(0);
        List<Section> secs = cs.listSections();
        for (Section s : secs) {
            if (s.getEnrolledUsernames().contains(user.getUsername())) {
                Course course = cs.getCourse(s.getCourseCode());
                LocalDate deadline = s.getDropDeadline();
                enrolledTableModel.addRow(new Object[]{
                    s.getId(),
                    s.getCourseCode(),
                    course != null ? course.getTitle() : "N/A",
                    s.getInstructorUsername() == null ? "TBD" : s.getInstructorUsername(),
                    s.getEnrolledUsernames().size(),
                    s.getCapacity(),
                    deadline == null ? "N/A" : deadline.format(DATE_FMT),
                    s.getDayOfWeek() + " " + s.getTimeSlot()
                });
            }
        }
    }

    private void loadGrades() {
        if (gradesTableModel == null) return;
        gradesTableModel.setRowCount(0);
        finalGradeMap.clear();
        List<Grade> grades = es.listGradesForStudent(user.getUsername());
        for (Grade g : grades) {
            // Get course code from section
            Section section = cs.getSection(g.getSectionId());
            String courseCode = section != null ? section.getCourseCode() : "N/A";
            finalGradeMap.put(g.getSectionId(), g.getGrade());
            gradesTableModel.addRow(new Object[]{
                g.getSectionId(),
                courseCode,
                g.getGrade()
            });
        }
        if (gradesTableModel.getRowCount() > 0 && gradesTable != null) {
            gradesTable.setRowSelectionInterval(0, 0);
            String sectionId = (String) gradesTableModel.getValueAt(0, 0);
            loadAssessmentBreakdown(sectionId);
        } else {
            clearAssessmentBreakdown();
        }
    }

    private void loadTimetable() {
        if (timetableTableModel == null) return;
        timetableTableModel.setRowCount(0);
        List<Section> secs = cs.listSections();
        for (Section s : secs) {
            if (!s.getEnrolledUsernames().contains(user.getUsername())) continue;
            Course course = cs.getCourse(s.getCourseCode());
            timetableTableModel.addRow(new Object[]{
                s.getDayOfWeek(),
                s.getTimeSlot(),
                s.getId(),
                course != null ? course.getTitle() : s.getCourseCode(),
                s.getInstructorUsername() == null ? "TBD" : s.getInstructorUsername()
            });
        }
    }

    private void loadAssessmentBreakdown(String sectionId) {
        assessmentTableModel.setRowCount(0);
        List<Assessment> assessments = assessmentService.getAssessments(sectionId);
        Map<String, Double> scores = assessmentService.getScores(sectionId, user.getUsername());
        if (assessments.isEmpty()) {
            assessmentTableModel.addRow(new Object[]{"No assessments defined", "-", "-", "-"});
        } else {
            for (Assessment a : assessments) {
                Double score = scores.get(a.getName());
                assessmentTableModel.addRow(new Object[]{
                    a.getName(),
                    a.getWeightPercentage(),
                    a.getMaxScore(),
                    score == null ? "N/A" : String.format("%.2f", score)
                });
            }
        }
        String finalGrade = finalGradeMap.getOrDefault(sectionId, "N/A");
        finalGradeLabel.setText("Final Grade: " + finalGrade);
    }

    private void clearAssessmentBreakdown() {
        if (assessmentTableModel != null) {
            assessmentTableModel.setRowCount(0);
        }
        finalGradeLabel.setText("Final Grade: N/A");
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
            if (enrollBtn != null) enrollBtn.setEnabled(!maintenanceMode);
            if (dropBtn != null) dropBtn.setEnabled(!maintenanceMode);
        } catch (Exception e) {
            maintenanceBanner.setVisible(false);
        }
    }

}
