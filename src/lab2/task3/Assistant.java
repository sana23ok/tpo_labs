package lab2.task3;

public class Assistant implements Runnable {
    private final GradeBook gradeBook;
    private final String name;
    private final int groupIndex;
    private final int weeks;

    public Assistant(GradeBook gradeBook, String name, int groupIndex, int weeks) {
        this.gradeBook = gradeBook;
        this.name = name;
        this.groupIndex = groupIndex;
        this.weeks = weeks;
    }

    public void run() {
        for (int week = 1; week <= weeks; week++) {
            System.out.println(name + " is grading group" + (groupIndex + 1) + " week " + week);
            for (int studentIndex = 0; studentIndex < 3; studentIndex++) { // Оцінка кожного студента групи
                int grade = 50 + (int) (Math.random() * 51);
                gradeBook.setAssistantGrade(week, groupIndex, studentIndex, grade);
            }
            try {
                Thread.sleep(500); // Очікування перед наступним тижнем
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}


