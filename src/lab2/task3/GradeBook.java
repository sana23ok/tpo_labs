package lab2.task3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class GradeBook {
    private final Lock lock = new ReentrantLock();
    private final List<Group> groups;

    public GradeBook(int numGroups, int studentsPerGroup) {
        groups = new ArrayList<>();
        for (int i = 0; i < numGroups; i++) {
            groups.add(new Group(i + 1, studentsPerGroup));
        }
    }

    public void addGrade(int week, int groupIndex, int studentIndex, int grade, String teacher) {
        lock.lock();
        try {
            groups.get(groupIndex).getStudents().get(studentIndex).addGrade(week, grade, teacher);
//            System.out.printf("%s assigned %d points to Student %d in Group %d (Week %d)%n",
//                    teacher, grade, studentIndex + 1, groupIndex + 1, week);
        } finally {
            lock.unlock();
        }
    }

    public void printJournal() {
        System.out.println("\n GradeBook:");
        for (Group group : groups) {
            System.out.println("\n " + group.getName());
            System.out.printf("%-12s %-25s%n", "Student", "Grades");
            for (Student student : group.getStudents()) {
                System.out.printf("%-12s %-25s%n", student.getName(), student.getGrades());
            }
        }
    }
}
