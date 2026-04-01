package edu.univ.erp.domain;

import java.io.Serializable;

public class AssessmentScore implements Serializable {
    private String sectionId;
    private String studentUsername;
    private String assessmentName;
    private double score;

    public AssessmentScore() {}

    public AssessmentScore(String sectionId, String studentUsername, String assessmentName, double score) {
        this.sectionId = sectionId;
        this.studentUsername = studentUsername;
        this.assessmentName = assessmentName;
        this.score = score;
    }

    public String getSectionId() { return sectionId; }
    public String getStudentUsername() { return studentUsername; }
    public String getAssessmentName() { return assessmentName; }
    public double getScore() { return score; }

    public void setScore(double score) { this.score = score; }
}

