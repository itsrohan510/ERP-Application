package edu.univ.erp.ui.admin;

import edu.univ.erp.data.DataStore;
import edu.univ.erp.domain.*;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.CourseService;
import edu.univ.erp.ui.admin.dialogs.*;
import edu.univ.erp.ui.common.ChangePasswordDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class AdminDashboard extends JFrame {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private final AdminService adminService = new AdminService();
    private final CourseService cs = new CourseService();
    private final DataStore ds = DataStore.getInstance();
    private final User admin;
    
    private DefaultTableModel userTableModel;
    private DefaultTableModel courseTableModel;
    private DefaultTableModel sectionsTableModel;
    private JTable userTable;
    private JTable courseTable;
    private JTable sectionsTable;
    private JLabel maintenanceBanner;

    public AdminDashboard(User admin) {
        this.admin = admin;
        setTitle("Admin Dashboard - " + admin.getDisplayName());
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        init();
    }

    private void showAssignInstructorDialog() {
        Section section = requireSectionSelection("Assign Instructor");
        if (section == null) return;

        List<User> instructors = adminService.getInstructors();
        if (instructors.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No instructors exist yet. Create one in Users tab first.", "Assign Instructor", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AssignInstructorDialog dialog = new AssignInstructorDialog(this, section, instructors, () -> {
            loadSections();
            loadCourses();
        });
        dialog.setVisible(true);
    }

    private void showEditSectionDialog() {
        Section section = requireSectionSelection("Edit Section");
        if (section == null) return;

        EditSectionDialog dialog = new EditSectionDialog(this, section, () -> {
            loadSections();
            loadCourses();
        });
        dialog.setVisible(true);
    }

    private void performBackup() {
        AdminBackupRestore.performBackup(this, null);
    }

    private void performRestore() {
        AdminBackupRestore.performRestore(this, () -> {
            loadUsers();
            loadCourses();
            loadSections();
            updateMaintenanceBanner();
        });
    }

    private Section requireSectionSelection(String action) {
        if (sectionsTable == null) {
            JOptionPane.showMessageDialog(this, "Sections tab not ready yet.", action, JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int row = sectionsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a section first.", action, JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String sectionId = (String) sectionsTableModel.getValueAt(row, 0);
        Section section = ds.getSections().get(sectionId);
        if (section == null) {
            JOptionPane.showMessageDialog(this, "Section could not be found.", action, JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return section;
    }

    private void init() {
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        User adminUser = adminService.getAdminUser();
        JLabel titleLabel = new JLabel("Administrator Dashboard" + (adminUser != null ? " - " + adminUser.getDisplayName() : ""));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerButtons.setOpaque(false);
        
        JButton changePasswordBtn = new JButton("Change Password");
        changePasswordBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        changePasswordBtn.setBackground(new Color(52, 152, 219));
        changePasswordBtn.setForeground(Color.WHITE);
        changePasswordBtn.setBorderPainted(false);
        changePasswordBtn.setFocusPainted(false);
        changePasswordBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        changePasswordBtn.addActionListener(e -> {
            ChangePasswordDialog dialog = new ChangePasswordDialog(this, admin.getUsername());
            dialog.setVisible(true);
        });
        
        JButton toggleMaintenance = new JButton(adminService.isMaintenanceMode() ? "Disable Maintenance" : "Enable Maintenance");
        toggleMaintenance.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        toggleMaintenance.setBackground(adminService.isMaintenanceMode() ? new Color(231, 76, 60) : new Color(46, 204, 113));
        toggleMaintenance.setForeground(Color.WHITE);
        toggleMaintenance.setBorderPainted(false);
        toggleMaintenance.setFocusPainted(false);
        toggleMaintenance.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleMaintenance.addActionListener(e -> {
            try {
                adminService.setMaintenanceMode(!adminService.isMaintenanceMode());
                toggleMaintenance.setText(adminService.isMaintenanceMode() ? "Disable Maintenance" : "Enable Maintenance");
                toggleMaintenance.setBackground(adminService.isMaintenanceMode() ? new Color(231, 76, 60) : new Color(46, 204, 113));
                updateMaintenanceBanner();
                JOptionPane.showMessageDialog(this,
                    "Maintenance mode is now: " + (adminService.isMaintenanceMode() ? "ENABLED" : "DISABLED"),
                    "Maintenance Mode",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Error toggling maintenance mode: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        headerButtons.add(changePasswordBtn);
        headerButtons.add(toggleMaintenance);
        headerPanel.add(headerButtons, BorderLayout.EAST);

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

        // Main Content Panel with Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Users Tab
        JPanel usersPanel = createUsersPanel();
        tabbedPane.addTab("Users", usersPanel);

        // Courses Tab
        JPanel coursesPanel = createCoursesPanel();
        tabbedPane.addTab("Courses", coursesPanel);

        // Sections Tab
        JPanel sectionsPanel = createSectionsPanel();
        tabbedPane.addTab("Sections", sectionsPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Action Buttons Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        actionPanel.setBorder(BorderFactory.createTitledBorder("Quick Actions"));
        actionPanel.setBackground(Color.WHITE);

        JButton addUserBtn = createStyledButton("Add User", new Color(52, 152, 219));
        JButton addCourseBtn = createStyledButton("Add Course", new Color(155, 89, 182));
        JButton addSectionBtn = createStyledButton("Add Section", new Color(241, 196, 15));
        JButton assignInstructorBtn = createStyledButton("Assign Instructor", new Color(22, 160, 133));
        JButton editScheduleBtn = createStyledButton("Edit Section Schedule", new Color(52, 73, 94));
        JButton backupBtn = createStyledButton("Backup DB", new Color(142, 68, 173));
        JButton restoreBtn = createStyledButton("Restore DB", new Color(192, 57, 43));
        JButton refreshBtn = createStyledButton("Refresh All", new Color(149, 165, 166));

        addUserBtn.addActionListener(e -> showAddUserDialog());
        addCourseBtn.addActionListener(e -> showAddCourseDialog());
        addSectionBtn.addActionListener(e -> showAddSectionDialog());
        assignInstructorBtn.addActionListener(e -> showAssignInstructorDialog());
        editScheduleBtn.addActionListener(e -> showEditSectionDialog());
        backupBtn.addActionListener(e -> performBackup());
        restoreBtn.addActionListener(e -> performRestore());
        refreshBtn.addActionListener(e -> {
            loadUsers();
            loadCourses();
            loadSections();
            JOptionPane.showMessageDialog(this, "Data refreshed", "Refresh", JOptionPane.INFORMATION_MESSAGE);
        });

        actionPanel.add(addUserBtn);
        actionPanel.add(addCourseBtn);
        actionPanel.add(addSectionBtn);
        actionPanel.add(assignInstructorBtn);
        actionPanel.add(editScheduleBtn);
        actionPanel.add(backupBtn);
        actionPanel.add(restoreBtn);
        actionPanel.add(refreshBtn);

        add(actionPanel, BorderLayout.SOUTH);
    }

    private void updateMaintenanceBanner() {
        if (maintenanceBanner == null) return;
        try {
            if (adminService.isMaintenanceMode()) {
                maintenanceBanner.setText("MAINTENANCE MODE ENABLED - Student/Instructor actions are blocked.");
                maintenanceBanner.setVisible(true);
            } else {
                maintenanceBanner.setText("");
                maintenanceBanner.setVisible(false);
            }
        } catch (Exception e) {
            maintenanceBanner.setVisible(false);
        }
    }

    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] userColumns = {"Username", "Display Name", "Role", "ID"};
        userTableModel = new DefaultTableModel(userColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(userTableModel);
        userTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userTable.setRowHeight(25);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        userTable.getTableHeader().setBackground(new Color(236, 240, 241));
        userTable.setRowSorter(new TableRowSorter<>(userTableModel));
        
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("System Users"));
        panel.add(scrollPane, BorderLayout.CENTER);

        loadUsers();
        return panel;
    }

    private JPanel createCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] courseColumns = {"Course Code", "Title", "Credits", "Sections"};
        courseTableModel = new DefaultTableModel(courseColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        courseTable = new JTable(courseTableModel);
        courseTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        courseTable.setRowHeight(25);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        courseTable.getTableHeader().setBackground(new Color(236, 240, 241));
        courseTable.setRowSorter(new TableRowSorter<>(courseTableModel));
        
        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Courses"));
        panel.add(scrollPane, BorderLayout.CENTER);

        loadCourses();
        return panel;
    }

    private JPanel createSectionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] sectionColumns = {"Section ID", "Course", "Title", "Instructor", "Day(s)", "Time", "Room", "Capacity", "Enrolled", "Semester", "Year", "Drop Deadline"};
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

        JScrollPane scrollPane = new JScrollPane(sectionsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Sections & Timetable"));
        panel.add(scrollPane, BorderLayout.CENTER);

        loadSections();
        return panel;
    }

    private void loadUsers() {
        try {
            userTableModel.setRowCount(0);
            for (Map.Entry<String, User> e : ds.getUsers().entrySet()) {
                User u = e.getValue();
                String id = "";
                if (u instanceof Student) {
                    id = ((Student) u).getStudentId();
                } else if (u instanceof Instructor) {
                    id = ((Instructor) u).getInstructorId();
                }
                userTableModel.addRow(new Object[]{e.getKey(), u.getDisplayName(), u.getRole(), id});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading users: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCourses() {
        try {
            courseTableModel.setRowCount(0);
            for (Course c : cs.listCourses()) {
                courseTableModel.addRow(new Object[]{
                    c.getCode(), 
                    c.getTitle(), 
                    c.getCredits(), 
                    c.getSectionIds().size()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading courses: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSections() {
        try {
            if (sectionsTableModel == null) return;
            sectionsTableModel.setRowCount(0);
            for (Section s : ds.getSections().values()) {
                Course course = cs.getCourse(s.getCourseCode());
                sectionsTableModel.addRow(new Object[]{
                    s.getId(),
                    s.getCourseCode(),
                    course != null ? course.getTitle() : "N/A",
                    s.getInstructorUsername() == null ? "Unassigned" : s.getInstructorUsername(),
                    s.getDayOfWeek(),
                    s.getTimeSlot(),
                    s.getRoom(),
                    s.getCapacity(),
                    s.getEnrolledUsernames().size(),
                    s.getSemester(),
                    s.getYear(),
                    s.getDropDeadline() == null ? "N/A" : s.getDropDeadline().format(DATE_FMT)
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading sections: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
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

    private void showAddUserDialog() {
        AddUserDialog dialog = new AddUserDialog(this, () -> loadUsers());
        dialog.setVisible(true);
    }

    private void showAddCourseDialog() {
        AddCourseDialog dialog = new AddCourseDialog(this, () -> loadCourses());
        dialog.setVisible(true);
    }

    private void showAddSectionDialog() {
        AddSectionDialog dialog = new AddSectionDialog(this, () -> {
            loadCourses();
            loadSections();
        });
        dialog.setVisible(true);
    }
}
