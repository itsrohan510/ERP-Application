package edu.univ.erp.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Course implements Serializable {
    private String code;
    private String title;
    private int credits;
    private List<String> sectionIds = new ArrayList<>();

    public Course() {}
    public Course(String code, String title, int credits) {
        this.code = code; this.title = title; this.credits = credits;
    }

    public String getCode() { return code; }
    public String getTitle() { return title; }
    public int getCredits() { return credits; }

    public List<String> getSectionIds() { return sectionIds; }
    public void addSection(String id) { if (!sectionIds.contains(id)) sectionIds.add(id); }
}

