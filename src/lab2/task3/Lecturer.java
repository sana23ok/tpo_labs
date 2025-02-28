package lab2.task3;

public class Lecturer implements Runnable {
    private final GradeBook gradeBook;
    private final String name;
    private final int weeks;

    public Lecturer(GradeBook gradeBook, String name, int weeks) {
        this.gradeBook = gradeBook;
        this.name = name;
        this.weeks = weeks;
    }

    public void run() {
        for (int week = 1; week <= weeks; week++) {
            for (int groupIndex = 0; groupIndex < 3; groupIndex++) { // Проходимо по групах
                System.out.println(name + " is grading group" + (groupIndex + 1) + " week " + week);
                for (int studentIndex = 0; studentIndex < 3; studentIndex++) { // Проходимо по студентах
                    int grade = 50 + (int) (Math.random() * 51);
                    gradeBook.setLecturerGrade(week, groupIndex, studentIndex, grade);
                }
            }
            try {
                Thread.sleep(500); // Очікування перед наступним тижнем
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}



