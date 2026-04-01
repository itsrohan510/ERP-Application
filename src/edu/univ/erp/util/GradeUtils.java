package edu.univ.erp.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GradeUtils {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("(-?\\d+(?:\\.\\d+)?)");

    private GradeUtils() {}

    public static String formatGrade(double score) {
        double bounded = boundScore(score);
        return letterForScore(bounded) + " (" + formatScore(bounded) + ")";
    }

    public static String letterForScore(double score) {
        double bounded = boundScore(score);
        if (bounded >= 91) return "A+";
        if (bounded >= 71) return "A";
        if (bounded >= 51) return "B";
        if (bounded >= 41) return "C";
        if (bounded >= 30) return "D";
        return "F";
    }

    public static Double extractScore(String gradeValue) {
        if (gradeValue == null) return null;
        String trimmed = gradeValue.trim();
        if (trimmed.isEmpty()) return null;
        try {
            return boundScore(Double.parseDouble(trimmed));
        } catch (NumberFormatException ignored) {
        }
        Matcher matcher = NUMBER_PATTERN.matcher(trimmed);
        if (matcher.find()) {
            try {
                return boundScore(Double.parseDouble(matcher.group(1)));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private static double boundScore(double score) {
        if (score < 0) return 0;
        if (score > 100) return 100;
        return score;
    }

    private static String formatScore(double score) {
        return String.format(Locale.US, "%.2f", score);
    }
}

