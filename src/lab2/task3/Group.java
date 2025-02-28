package lab2.task3;

import java.util.ArrayList;
import java.util.List;

class Group {
    private final int groupId;
    private final List<Student> students;

    public Group(int groupId, int numStudents) {
        this.groupId = groupId;
        this.students = new ArrayList<>();
        for (int i = 0; i < numStudents; i++) {
            students.add(new Student("Student " + (i + 1)));
        }
    }

    public List<Student> getStudents() {
        return students;
    }

    public int getName() {
        return groupId;
    }
}

