package edu.univ.erp.domain;

import java.io.Serializable;

public class Assessment implements Serializable {
    private String sectionId;
    private String name;
    private double weightPercentage; 
    private double maxScore;

    public Assessment() {}

    public Assessment(String sectionId, String name, double weightPercentage, double maxScore) {
        this.sectionId = sectionId;
        this.name = name;
        this.weightPercentage = weightPercentage;
        this.maxScore = maxScore;
    }

    public String getSectionId() { return sectionId; }
    public String getName() { return name; }
    public double getWeightPercentage() { return weightPercentage; }
    public double getMaxScore() { return maxScore; }

    public void setName(String name) { this.name = name; }
    public void setWeightPercentage(double weightPercentage) { this.weightPercentage = weightPercentage; }
    public void setMaxScore(double maxScore) { this.maxScore = maxScore; }
}

