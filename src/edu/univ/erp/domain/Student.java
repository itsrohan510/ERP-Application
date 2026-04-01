package edu.univ.erp.domain;

public class Student extends User {
    private String studentId;

    public Student() { super(); }
    public Student(String username, String displayName, String studentId) {
        super(username, displayName, Role.STUDENT);
        this.studentId = studentId;
    }

    public String getStudentId() { return studentId; }
}

