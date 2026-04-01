package edu.univ.erp.domain;

import java.io.Serializable;

public class Grade implements Serializable {
    private String sectionId;
    private String studentUsername;
    private String grade; 

    public Grade() {}
    public Grade(String sectionId, String studentUsername, String grade) {
        this.sectionId = sectionId; this.studentUsername = studentUsername; this.grade = grade;
    }

    public String getSectionId() { return sectionId; }
    public String getStudentUsername() { return studentUsername; }
    public String getGrade() { return grade; }
    public void setGrade(String g) { this.grade = g; }
}

