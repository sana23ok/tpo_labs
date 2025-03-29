package lab4;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;

public class Main {
    private static final String DIRECTORY_PATH = "src/lab4/files"; // Аналіз усіх файлів у "files" (включно з вкладеними папками)

    public static void main(String[] args) throws IOException {
        File directory = new File(DIRECTORY_PATH);
        System.out.println("Analyzing directory: " + directory.getAbsolutePath());

        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Invalid directory: " + directory.getAbsolutePath());
            return;
        }

        // Побудова ієрархії файлів та папок (рекурсивно)
        Folder folder = Folder.fromDirectory(directory);
        ForkJoinPool pool = new ForkJoinPool();

        // Запускаємо паралельний аналіз за допомогою ForkJoinPool
        WordStatistics statsParallel = pool.invoke(new FolderAnalysisTask(folder));
        System.out.println("Parallel Analysis: " + statsParallel);

        // Запускаємо послідовний аналіз
        WordStatistics statsSequential = SequentialTextAnalysis.analyze(folder);
        System.out.println("Sequential Analysis: " + statsSequential);
    }
}
