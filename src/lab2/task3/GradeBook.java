package lab2.task3;

import java.util.ArrayList;
import java.util.List;

public class GradeBook {
    private final Object lock = new Object();
    private final List<Group> groups;

    public GradeBook(int numGroups, int studentsPerGroup) {
        groups = new ArrayList<>();
        for (int i = 0; i < numGroups; i++) {
            groups.add(new Group(i + 1, studentsPerGroup));
        }
    }

    public void setLecturerGrade(int week, int groupIndex, int studentIndex, int grade) {
        synchronized (lock) {
            groups.get(groupIndex).getStudents().get(studentIndex).setLecturerGrade(week, grade);
        }
    }

    public void setAssistantGrade(int week, int groupIndex, int studentIndex, int grade) {
        synchronized (lock) {
            groups.get(groupIndex).getStudents().get(studentIndex).setAssistantGrade(week, grade);
        }
    }

    public void printJournal() {
        System.out.println("\n GradeBook:");
        for (Group group : groups) {
            System.out.println("\n " + group.getName());
            System.out.printf("%-12s %-25s %-25s%n", "Student", "Lecturer", "Assistant");
            for (Student student : group.getStudents()) {
                System.out.printf("%-12s %-25s %-25s%n",
                        student.getName(),
                        student.getLecturerGrades(),
                        student.getAssistantGrades());
            }
        }
    }
}
