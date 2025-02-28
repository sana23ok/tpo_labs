package lab2.task3;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ElectronicJournal {
    public static void main(String[] args) {
//        Scanner scanner = new Scanner(System.in);
//        System.out.print("Введіть кількість тижнів: ");
//        int weeks = scanner.nextInt();
//        scanner.close();
        int weeks = 2;

        int numGroups = 3;
        int studentsPerGroup = 3;
        GradeBook gradeBook = new GradeBook(numGroups, studentsPerGroup);

        // Створення та запуск лектора
        Thread lecturer = new Thread(new Lecturer(gradeBook, "Lecturer", weeks));
        lecturer.start();

        // Створення та запуск асистентів
        List<Thread> assistants = new ArrayList<>();
        for (int i = 0; i < numGroups; i++) {
            Thread assistant = new Thread(new Assistant(gradeBook, "Assistant " + (i + 1), i, weeks));
            assistants.add(assistant);
            assistant.start();
        }

        // Очікування завершення всіх потоків
        try {
            lecturer.join();
            for (Thread assistant : assistants) {
                assistant.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Виведення результатів оцінювання
        gradeBook.printJournal();
    }
}
