package lab4.task4;

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


class KeywordSearchTask extends RecursiveTask<Map<String, Map<File, Integer>>> {
    private final Document document;
    private final File file;
    private static final Set<String> IT_KEYWORDS = Set.of("thread", "main", "execution", "guard", "python");

    KeywordSearchTask(Document document, File file) {
        this.document = document;
        this.file = file;
    }

    @Override
    protected Map<String, Map<File, Integer>> compute() {
        Map<String, Map<File, Integer>> wordOccurrences = new HashMap<>();

        // Проходимо кожен рядок документа
        for (String line : document.getLines()) {
            for (String word : WordLengthAnalyzer.wordsIn(line)) {
                // Якщо слово є у списку IT_KEYWORDS, додаємо його в мапу
                if (IT_KEYWORDS.contains(word.toLowerCase())) {
                    wordOccurrences.putIfAbsent(word.toLowerCase(), new HashMap<>());
                    wordOccurrences.get(word.toLowerCase()).merge(file, 1, Integer::sum);
                }
            }
        }
        return wordOccurrences;
    }
}


class FolderKeywordSearchTask extends RecursiveTask<Map<String, Map<File, Integer>>> {
    private final Folder folder;
    private final File directory;

    FolderKeywordSearchTask(Folder folder, File directory) {
        this.folder = folder;
        this.directory = directory;
    }

    @Override
    protected Map<String, Map<File, Integer>> compute() {
        List<RecursiveTask<Map<String, Map<File, Integer>>>> forks = new ArrayList<>();
        Map<String, Map<File, Integer>> result = new HashMap<>();

        File[] files = directory.listFiles();
        if (files == null) return result;

        for (File file : files) {
            if (file.isDirectory()) {
                try {
                    Folder subFolder = Folder.fromDirectory(file);
                    FolderKeywordSearchTask task = new FolderKeywordSearchTask(subFolder, file);
                    forks.add(task);
                    task.fork(); // Створюємо асинхронне виконання підзадачі
                } catch (IOException e) {
                    System.err.println("Warning: Unable to process directory " + file.getAbsolutePath() + " - " + e.getMessage());
                }
            } else if (file.getName().toLowerCase().endsWith(".txt")) {
                Document doc = Document.fromFile(file);
                KeywordSearchTask task = new KeywordSearchTask(doc, file);
                forks.add(task);
                task.fork(); // Запускаємо паралельну обробку файлу
            }
        }

        // Очікуємо завершення всіх задач і об'єднуємо результати
        for (RecursiveTask<Map<String, Map<File, Integer>>> task : forks) {
            Map<String, Map<File, Integer>> partialResult = task.join(); // Блокує потік, поки задача не завершиться
            for (Map.Entry<String, Map<File, Integer>> entry : partialResult.entrySet()) {
                result.putIfAbsent(entry.getKey(), new HashMap<>());
                Map<File, Integer> fileMap = result.get(entry.getKey());
                entry.getValue().forEach((file, count) -> fileMap.merge(file, count, Integer::sum));
            }
        }
        return result;
    }
}


public class DocumentFinder {
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    public Map<String, Map<File, Integer>> findDocuments(Folder folder, File directory) {
        return forkJoinPool.invoke(new FolderKeywordSearchTask(folder, directory));
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
            DocumentFinder finder = new DocumentFinder();

            long startTime = System.nanoTime();
            Map<String, Map<File, Integer>> wordOccurrences = finder.findDocuments(folder, dir);
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000;

            System.out.println("Word occurrences:");
            wordOccurrences.forEach((word, fileMap) -> {
                System.out.println("Word: " + word);
                fileMap.forEach((file, count) -> System.out.println("  File: " + file.getAbsolutePath() + ", Count: " + count));
            });

            System.out.println("Execution time: " + duration + " ms");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}

