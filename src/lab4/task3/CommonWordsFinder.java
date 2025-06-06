package lab4.task3;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

class Folder {
    private final List<Folder> subFolders;
    private final List<Document> documents;

    Folder(List<Folder> subFolders, List<Document> documents) {
        this.subFolders = subFolders;
        this.documents = documents;
    }

    List<Folder> getSubFolders() {
        return this.subFolders;
    }

    List<Document> getDocuments() {
        return this.documents;
    }

    static Folder fromDirectory(File dir) throws IOException {
        List<Document> documents = new LinkedList<>();
        List<Folder> subFolders = new LinkedList<>();
        if (dir.listFiles() == null) {
            return new Folder(subFolders, documents);
        }
        for (File entry : dir.listFiles()) {
            if (entry.isDirectory()) {
                subFolders.add(Folder.fromDirectory(entry));
            } else {
                documents.add(Document.fromFile(entry));
            }
        }
        return new Folder(subFolders, documents);
    }
}

class Document {
    private final List<String> lines;

    Document(List<String> lines) {
        this.lines = lines;
    }

    List<String> getLines() {
        return this.lines;
    }

    static Document fromFile(File file) {
        List<String> lines = new LinkedList<>();
        if (!file.getName().toLowerCase().endsWith(".txt")) {
            System.out.println("Warning: Skipping non-txt file " + file.getAbsolutePath());
            return new Document(lines);
        }

        System.out.println("Reading file " + file.getAbsolutePath());
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.out.println("Warning: Cannot read file " + file.getAbsolutePath() + " - " + e.getMessage());
        }
        return new Document(lines);
    }
}

class WordLengthAnalyzer {
    static String[] wordsIn(String line) {
        return line.trim().split("(\\s|\\p{Punct})+");
    }
}

class CommonWordsTask extends RecursiveTask<Set<String>> {
    private final Document document;

    CommonWordsTask(Document document) {
        this.document = document;
    }

    @Override
    protected Set<String> compute() {
        Set<String> words = new HashSet<>();
        for (String line : document.getLines()) {
            for (String word : WordLengthAnalyzer.wordsIn(line)) {
                if (word.length() > 2) { // Фільтр слів довше 2 символів
                    words.add(word);
                }
            }
        }
        return words;
    }
}

// Завдання для обробки папки та об'єднання результатів документів і підпапок
class FolderCommonWordsTask extends RecursiveTask<Set<String>> {
    private final Folder folder;

    FolderCommonWordsTask(Folder folder) {
        this.folder = folder;
    }

    @Override
    protected Set<String> compute() {
        List<RecursiveTask<Set<String>>> forks = new ArrayList<>(); // Список задач для підпапок і документів
        Set<String> commonWords = null;

        // Обробка підпапок
        for (Folder subFolder : folder.getSubFolders()) {
            FolderCommonWordsTask task = new FolderCommonWordsTask(subFolder);
            forks.add(task); // Додаємо задачу до списку
            task.fork(); // Запускаємо задачу у новому потоці
        }

        // Обробка документів у поточній папці
        for (Document document : folder.getDocuments()) {
            CommonWordsTask task = new CommonWordsTask(document);
            forks.add(task); // Додаємо задачу до списку
            task.fork(); // Запускаємо задачу у новому потоці
        }

        // Очікуємо завершення всіх задач та об'єднуємо результати
        for (RecursiveTask<Set<String>> task : forks) {
            Set<String> words = task.join(); // Отримуємо результат виконання
            if (commonWords == null) {
                commonWords = new HashSet<>(words);
            } else {
                commonWords.retainAll(words); // Знаходимо спільні слова у всіх документах
            }
        }
        return commonWords == null ? Collections.emptySet() : commonWords;
    }
}

public class CommonWordsFinder {
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    public Set<String> findCommonWords(Folder folder) {
        return forkJoinPool.invoke(new FolderCommonWordsTask(folder));
    }

    public static void main(String[] args) {
        String directoryPath = args.length > 0 ? args[0] : "src/lab4/files";
        File dir = new File(directoryPath);

        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("Error: Directory does not exist or is not a folder.");
            System.exit(1);
        }

        try {
            Folder folder = Folder.fromDirectory(dir);
            CommonWordsFinder finder = new CommonWordsFinder();

            long startTime = System.nanoTime();
            Set<String> commonWords = finder.findCommonWords(folder);
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000;

            System.out.println("Common words in all documents:");
            int count = 0;
            for (String word : commonWords) {
                System.out.print(word);
                System.out.print(", \t");
                count++;
                if (count % 10 == 0) {
                    System.out.println(); // Новий рядок кожні 5 слів
                }
            }
            if (count % 5 != 0) {
                System.out.println(); // Додати новий рядок, якщо не завершився рядок
            }
            System.out.println("Execution time: " + duration + " ms");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
