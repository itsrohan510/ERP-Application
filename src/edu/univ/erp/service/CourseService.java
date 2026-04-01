package edu.univ.erp.service;

import edu.univ.erp.data.DataStore;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;

import java.util.ArrayList;
import java.util.List;

public class CourseService {
    private DataStore ds = DataStore.getInstance();

    public List<Course> listCourses() {
        return new ArrayList<>(ds.getCourses().values());
    }

    public List<Section> listSections() {
        return new ArrayList<>(ds.getSections().values());
    }

    public Course getCourse(String code) { return ds.getCourses().get(code); }

    public Section getSection(String id) { return ds.getSections().get(id); }

    public boolean createCourse(Course c) {
        if (ds.getCourses().containsKey(c.getCode())) return false;
        ds.getCourses().put(c.getCode(), c);
        ds.save();
        return true;
    }

    public boolean createSection(Section s) {
        if (ds.getSections().containsKey(s.getId())) return false;
        ds.getSections().put(s.getId(), s);
        Course course = ds.getCourses().get(s.getCourseCode());
        if (course!=null) course.addSection(s.getId());
        ds.save();
        return true;
    }
}
