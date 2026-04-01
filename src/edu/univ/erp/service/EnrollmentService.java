package edu.univ.erp.service;

import edu.univ.erp.access.AccessGuard;
import edu.univ.erp.data.DataStore;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Section;

import java.util.List;
import java.util.Optional;

public class EnrollmentService {
    private DataStore ds = DataStore.getInstance();

    public boolean enroll(String username, String sectionId) {
        if (!AccessGuard.canStudentsTransact()) return false;
        Section s = ds.getSections().get(sectionId);
        if (s==null) return false;
        boolean ok = s.enroll(username);
        if (ok) ds.save();
        return ok;
    }

    public boolean drop(String username, String sectionId) {
        if (!AccessGuard.canStudentsTransact()) return false;
        Section s = ds.getSections().get(sectionId);
        if (s==null) return false;
        boolean ok = s.drop(username);
        if (ok) ds.save();
        return ok;
    }

    public List<Grade> listGradesForStudent(String username) {
        return ds.getGrades().stream().filter(g->g.getStudentUsername().equals(username)).toList();
    }

    public java.util.Map<String, String> getGradesForSection(String sectionId) {
        java.util.Map<String, String> gradeMap = new java.util.HashMap<>();
        for (Grade g : ds.getGrades()) {
            if (g.getSectionId().equals(sectionId)) {
                gradeMap.put(g.getStudentUsername(), g.getGrade());
            }
        }
        return gradeMap;
    }

    public boolean setGrade(String sectionId, String studentUsername, String gradeStr) {
        if (!AccessGuard.canInstructorsTransact()) {
            return false;
        }
        try {
            Optional<Grade> gOpt = ds.getGrades().stream().filter(g->g.getSectionId().equals(sectionId) && g.getStudentUsername().equals(studentUsername)).findFirst();
            if (gOpt.isPresent()) {
                gOpt.get().setGrade(gradeStr);
            } else {
                ds.getGrades().add(new Grade(sectionId, studentUsername, gradeStr));
            }
            ds.save();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
