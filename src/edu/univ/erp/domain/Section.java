package edu.univ.erp.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Section implements Serializable {
    private String id; // e.g., CSE101-01
    private String courseCode;
    private String instructorUsername; 
    private int capacity = 30;
    private List<String> enrolledUsernames = new ArrayList<>();
    private String dayOfWeek = "TBD";
    private String timeSlot = "TBD";
    private String room = "TBD";
    private String semester = "Fall";
    private String year = "2024";
    private LocalDate dropDeadline;

    public Section() {}
    public Section(String id, String courseCode, String instructorUsername, int capacity) {
        this(id, courseCode, instructorUsername, capacity, "TBD", "TBD", "TBD", "Fall", "2024", null);
    }

    public Section(String id, String courseCode, String instructorUsername, int capacity, String dayOfWeek, String timeSlot, LocalDate dropDeadline) {
        this(id, courseCode, instructorUsername, capacity, dayOfWeek, timeSlot, "TBD", "Fall", "2024", dropDeadline);
    }

    public Section(String id, String courseCode, String instructorUsername, int capacity, String dayOfWeek, String timeSlot, String room, String semester, String year, LocalDate dropDeadline) {
        this.id = id;
        this.courseCode = courseCode;
        this.instructorUsername = instructorUsername;
        this.capacity = capacity;
        this.dayOfWeek = dayOfWeek;
        this.timeSlot = timeSlot;
        this.room = room;
        this.semester = semester;
        this.year = year;
        this.dropDeadline = dropDeadline;
    }

    public String getId() { return id; }
    public String getCourseCode() { return courseCode; }
    public String getInstructorUsername() { return instructorUsername; }
    public int getCapacity() { return capacity; }
    public List<String> getEnrolledUsernames() { return enrolledUsernames; }
    public String getDayOfWeek() { return dayOfWeek == null ? "TBD" : dayOfWeek; }
    public String getTimeSlot() { return timeSlot == null ? "TBD" : timeSlot; }
    public String getRoom() { return room == null ? "TBD" : room; }
    public String getSemester() { return semester == null ? "N/A" : semester; }
    public String getYear() { return year == null ? "N/A" : year; }
    public LocalDate getDropDeadline() { return dropDeadline; }

    public void setInstructorUsername(String instructorUsername) { this.instructorUsername = instructorUsername; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    public void setRoom(String room) { this.room = room; }
    public void setSemester(String semester) { this.semester = semester; }
    public void setYear(String year) { this.year = year; }
    public void setDropDeadline(LocalDate dropDeadline) { this.dropDeadline = dropDeadline; }

    public boolean enroll(String username) {
        if (enrolledUsernames.contains(username)) return false;
        if (enrolledUsernames.size() >= capacity) return false;
        enrolledUsernames.add(username);
        return true;
    }

    public boolean drop(String username) { return enrolledUsernames.remove(username); }
}

