package edu.univ.erp.domain;

public class Instructor extends User {
    private String instructorId;

    public Instructor() { super(); }
    public Instructor(String username, String displayName, String instructorId) {
        super(username, displayName, Role.INSTRUCTOR);
        this.instructorId = instructorId;
    }

    public String getInstructorId() { return instructorId; }
}

