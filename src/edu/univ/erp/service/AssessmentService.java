package edu.univ.erp.service;

import edu.univ.erp.access.AccessGuard;
import edu.univ.erp.data.DataStore;
import edu.univ.erp.domain.Assessment;
import edu.univ.erp.domain.AssessmentScore;

import java.util.*;
import java.util.stream.Collectors;

public class AssessmentService {
    private final DataStore ds = DataStore.getInstance();

    public List<Assessment> getAssessments(String sectionId) {
        return new ArrayList<>(ds.getSectionAssessments().getOrDefault(sectionId, new ArrayList<>()));
    }

    public void saveAssessments(String sectionId, List<Assessment> updatedList) {
        if (!AccessGuard.canInstructorsTransact()) {
            throw new IllegalStateException("Cannot modify assessments during maintenance mode.");
        }
        ds.getSectionAssessments().put(sectionId, new ArrayList<>(updatedList));
        cleanupScores(sectionId, updatedList.stream().map(Assessment::getName).collect(Collectors.toSet()));
        ds.save();
    }

    public boolean deleteAssessment(String sectionId, String assessmentName) {
        if (!AccessGuard.canInstructorsTransact()) {
            return false;
        }
        List<Assessment> assessments = ds.getSectionAssessments().get(sectionId);
        if (assessments == null) return false;
        boolean removed = assessments.removeIf(a -> a.getName().equalsIgnoreCase(assessmentName));
        if (removed) {
            ds.getAssessmentScores().removeIf(s ->
                s.getSectionId().equals(sectionId) &&
                s.getAssessmentName().equalsIgnoreCase(assessmentName)
            );
            ds.save();
        }
        return removed;
    }

    public Map<String, Double> getScores(String sectionId, String studentUsername) {
        return ds.getAssessmentScores().stream()
            .filter(s -> s.getSectionId().equals(sectionId) && s.getStudentUsername().equals(studentUsername))
            .collect(Collectors.toMap(
                AssessmentScore::getAssessmentName,
                AssessmentScore::getScore,
                (existing, replacement) -> replacement
            ));
    }

    public void saveScores(String sectionId, String studentUsername, Map<String, Double> scores) {
        if (!AccessGuard.canInstructorsTransact()) {
            throw new IllegalStateException("Cannot modify scores during maintenance mode.");
        }
        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            AssessmentScore existing = findScore(sectionId, studentUsername, entry.getKey());
            if (existing != null) {
                existing.setScore(entry.getValue());
            } else {
                ds.getAssessmentScores().add(new AssessmentScore(sectionId, studentUsername, entry.getKey(), entry.getValue()));
            }
        }
        ds.save();
    }

    public double computeFinalScore(String sectionId, String studentUsername) {
        Map<String, Double> scores = getScores(sectionId, studentUsername);
        return computeFinalScore(sectionId, scores);
    }

    public double computeFinalScore(String sectionId, Map<String, Double> scores) {
        List<Assessment> assessments = getAssessments(sectionId);
        double finalScore = 0;
        for (Assessment a : assessments) {
            Double score = scores.get(a.getName());
            if (score == null) continue;
            double max = a.getMaxScore() <= 0 ? 100 : a.getMaxScore();
            double boundedScore = Math.max(0, Math.min(score, max));
            finalScore += (boundedScore / max) * a.getWeightPercentage();
        }
        return Math.round(finalScore * 100.0) / 100.0;
    }

    public double getTotalWeight(String sectionId) {
        return getAssessments(sectionId).stream().mapToDouble(Assessment::getWeightPercentage).sum();
    }

    private AssessmentScore findScore(String sectionId, String studentUsername, String assessmentName) {
        for (AssessmentScore s : ds.getAssessmentScores()) {
            if (s.getSectionId().equals(sectionId)
                && s.getStudentUsername().equals(studentUsername)
                && s.getAssessmentName().equals(assessmentName)) {
                return s;
            }
        }
        return null;
    }

    private void cleanupScores(String sectionId, Set<String> validAssessmentNames) {
        ds.getAssessmentScores().removeIf(s ->
            s.getSectionId().equals(sectionId) &&
            !validAssessmentNames.contains(s.getAssessmentName())
        );
    }
}

