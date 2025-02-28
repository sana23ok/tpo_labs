package lab2.task3;

import java.util.HashMap;
import java.util.Map;

public class Student {
    private final String name;
    private final Map<Integer, Integer> lecturerGrades;  // Оцінки лектора (по тижнях)
    private final Map<Integer, Integer> assistantGrades; // Оцінки асистента (по тижнях)

    public Student(String name) {
        this.name = name;
        this.lecturerGrades = new HashMap<>();
        this.assistantGrades = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setLecturerGrade(int week, int grade) {
        lecturerGrades.put(week, grade);
    }

    public void setAssistantGrade(int week, int grade) {
        assistantGrades.put(week, grade);
    }

    public Map<Integer, Integer> getLecturerGrades() {
        return lecturerGrades;
    }

    public Map<Integer, Integer> getAssistantGrades() {
        return assistantGrades;
    }
}
