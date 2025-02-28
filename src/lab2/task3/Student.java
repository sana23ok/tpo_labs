package lab2.task3;

import java.util.HashMap;
import java.util.Map;

class Student {
    private final String name;
    private final Map<Integer, Map<String, Integer>> grades;

    public Student(String name) {
        this.name = name;
        this.grades = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void addGrade(int week, int grade, String teacher) {
        grades.putIfAbsent(week, new HashMap<>());
        grades.get(week).put(teacher, grade);
    }

    public Map<Integer, Map<String, Integer>> getGrades() {
        return grades;
    }
}