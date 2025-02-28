package lab2.task3;

import java.util.ArrayList;
import java.util.List;

public class ElectronicJournal {
    public static void main(String[] args) {
        int weeks = 2;
        int numGroups = 3;
        int studentsPerGroup = 3;

        GradeBook gradeBook = new GradeBook(numGroups, studentsPerGroup);

        Thread lecturer = new Thread(new Lecturer(gradeBook, "Lecturer", weeks));
        lecturer.start();

        List<Thread> assistants = new ArrayList<>();
        for (int i = 0; i < numGroups; i++) {
            Thread assistant = new Thread(new Assistant(gradeBook, "Assistant " + (i + 1), i, weeks));
            assistants.add(assistant);
            assistant.start();
        }

        try {
            lecturer.join();
            for (Thread assistant : assistants) {
                assistant.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        gradeBook.printJournal();
    }
}
