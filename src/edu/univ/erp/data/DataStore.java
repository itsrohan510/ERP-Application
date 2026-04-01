package edu.univ.erp.data;

import edu.univ.erp.auth.hash.PasswordHash;
import edu.univ.erp.auth.store.AuthStore;
import edu.univ.erp.domain.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class DataStore implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String FILE = "erp_datastore.bin";

    private Map<String, User> users = new HashMap<>();
    private Map<String, Course> courses = new HashMap<>();
    private Map<String, Section> sections = new HashMap<>();
    private List<Grade> grades = new ArrayList<>();
    private Map<String, List<Assessment>> sectionAssessments = new HashMap<>();
    private List<AssessmentScore> assessmentScores = new ArrayList<>();
    private boolean maintenanceMode = false;

    private static DataStore instance;

    private DataStore() {}

    public static synchronized DataStore getInstance() {
        if (instance == null) {
            // Trying to load from disk
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE))) {
                instance = (DataStore) ois.readObject();
                instance.ensureCollections();
            } catch (Exception e) {
                instance = new DataStore();
                instance.seedSampleData();
                instance.ensureCollections();
                instance.save();
            }
        }
        return instance;
    }

    public static synchronized void reload() {
        instance = null;
        getInstance();
    }

    public static String getDataFilePath() { return FILE; }

    public synchronized void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE))) {
            oos.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void seedSampleData() {
        // Users (no passwords - stored in AuthStore)
        users.put("student1", new Student("student1","Alice Student","S1001"));
        users.put("student2", new Student("student2","Charlie Student","S1002"));
        users.put("instructor1", new Instructor("instructor1","Dr. Bob","I9001"));
        users.put("admin", new User("admin","Administrator", Role.ADMIN));
        
        // Initializing AuthStore with default passwords
        initializeAuthStore();

        // Courses and sections
        Course c1 = new Course("CSE101","Intro to Programming",4);
        Course c2 = new Course("CSE102","Data Structures",4);
        courses.put(c1.getCode(), c1);
        courses.put(c2.getCode(), c2);

        Section s1 = new Section("CSE101-01", c1.getCode(), "instructor1", 40,
            "Mon/Wed", "09:00-10:30", "Room 101", "Fall", "2024", LocalDate.now().plusWeeks(2));
        Section s2 = new Section("CSE102-01", c2.getCode(), "instructor1", 35,
            "Tue/Thu", "11:00-12:30", "Room 202", "Fall", "2024", LocalDate.now().plusWeeks(2));
        sections.put(s1.getId(), s1);
        sections.put(s2.getId(), s2);

        c1.addSection(s1.getId());
        c2.addSection(s2.getId());

        // enroll student1 in CSE101-01
        s1.enroll("student1");
        grades.add(new Grade(s1.getId(), "student1", "A"));
    }

    // Getters and helpers
    public Map<String, User> getUsers() { return users; }
    public Map<String, Course> getCourses() { return courses; }
    public Map<String, Section> getSections() { return sections; }
    public List<Grade> getGrades() { return grades; }
    public Map<String, List<Assessment>> getSectionAssessments() { return sectionAssessments; }
    public List<AssessmentScore> getAssessmentScores() { return assessmentScores; }
    public boolean isMaintenanceMode() { return maintenanceMode; }
    public void setMaintenanceMode(boolean m) { this.maintenanceMode = m; save(); }

    private void ensureCollections() {
        if (users == null) users = new HashMap<>();
        if (courses == null) courses = new HashMap<>();
        if (sections == null) sections = new HashMap<>();
        if (grades == null) grades = new ArrayList<>();
        if (sectionAssessments == null) sectionAssessments = new HashMap<>();
        if (assessmentScores == null) assessmentScores = new ArrayList<>();
    }

    private void initializeAuthStore() {
        AuthStore authStore = AuthStore.getInstance();
        // Only initialize if AuthStore is empty (first run)
        if (!authStore.hasUser("admin")) {
            authStore.setPasswordHash("admin", PasswordHash.hashPassword("admin"));
            authStore.setPasswordHash("student1", PasswordHash.hashPassword("pass"));
            authStore.setPasswordHash("student2", PasswordHash.hashPassword("pass"));
            authStore.setPasswordHash("instructor1", PasswordHash.hashPassword("pass"));
        }
    }
}
