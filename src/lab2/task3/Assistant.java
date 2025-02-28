package lab2.task3;

class Assistant implements Runnable {
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
            System.out.println(name + " is grading group " + (groupIndex + 1) + " week " + week);
            for (int studentIndex = 0; studentIndex < 3; studentIndex++) {
                int grade = 50 + (int) (Math.random() * 51);
                gradeBook.addGrade(week, groupIndex, studentIndex, grade, name);
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}


