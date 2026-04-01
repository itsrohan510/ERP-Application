package edu.univ.erp.ui.student;

import edu.univ.erp.domain.*;
import edu.univ.erp.service.CourseService;
import edu.univ.erp.service.EnrollmentService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class StudentTranscriptExporter {
    
    public static void exportTranscript(Frame parent, String username, String displayName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(username + "-transcript.csv"));
        int option = chooser.showSaveDialog(parent);
        if (option != JFileChooser.APPROVE_OPTION) return;
        
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            File parentDir = file.getParentFile();
            file = new File(parentDir == null ? file.getName() + ".csv" : parentDir.getAbsolutePath() + File.separator + file.getName() + ".csv");
        }
        
        EnrollmentService es = new EnrollmentService();
        CourseService cs = new CourseService();
        List<Grade> grades = es.listGradesForStudent(username);
        
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.println("Section ID,Course Code,Course Title,Final Grade");
            for (Grade g : grades) {
                Section section = cs.getSection(g.getSectionId());
                Course course = section != null ? cs.getCourse(section.getCourseCode()) : null;
                String title = course != null ? course.getTitle() : "N/A";
                String courseCode = section != null ? section.getCourseCode() : "N/A";
                String safeTitle = title == null ? "N/A" : title;
                String gradeValue = g.getGrade() == null ? "N/A" : g.getGrade();
                writer.println(String.join(",",
                    g.getSectionId(),
                    courseCode,
                    "\"" + safeTitle.replace("\"", "\"\"") + "\"",
                    "\"" + gradeValue.replace("\"", "\"\"") + "\""
                ));
            }
            JOptionPane.showMessageDialog(parent, "Transcript saved to " + file.getAbsolutePath(), "Transcript Ready", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Failed to export transcript: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

