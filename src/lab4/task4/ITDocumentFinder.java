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

class KeywordSearchTask extends RecursiveTask<List<File>> {
    private final Document document;
    private final File file;
    private static final Set<String> IT_KEYWORDS = Set.of("thread", "Java", "Python");

    KeywordSearchTask(Document document, File file) {
        this.document = document;
        this.file = file;
    }

    @Override
    protected List<File> compute() {
        for (String line : document.getLines()) {
            for (String word : WordLengthAnalyzer.wordsIn(line)) {
                if (IT_KEYWORDS.contains(word.toLowerCase())) {
                    return List.of(file);
                }
            }
        }
        return Collections.emptyList();
    }
}

class FolderKeywordSearchTask extends RecursiveTask<List<File>> {
    private final Folder folder;
    private final File directory;

    FolderKeywordSearchTask(Folder folder, File directory) {
        this.folder = folder;
        this.directory = directory;
    }

    @Override
    protected List<File> compute() {
        List<RecursiveTask<List<File>>> forks = new ArrayList<>();
        List<File> result = new ArrayList<>();

        File[] files = directory.listFiles();
        if (files == null) return result;

        for (File file : files) {
            if (file.isDirectory()) {
                try {
                    Folder subFolder = Folder.fromDirectory(file);
                    FolderKeywordSearchTask task = new FolderKeywordSearchTask(subFolder, file);
                    forks.add(task);
                    task.fork();
                } catch (IOException e) {
                    System.err.println("Warning: Unable to process directory " + file.getAbsolutePath() + " - " + e.getMessage());
                }
            } else if (file.getName().toLowerCase().endsWith(".txt")) {
                Document doc = Document.fromFile(file);
                KeywordSearchTask task = new KeywordSearchTask(doc, file);
                forks.add(task);
                task.fork();
            }
        }

        for (RecursiveTask<List<File>> task : forks) {
            result.addAll(task.join());
        }
        return result;
    }

}

public class ITDocumentFinder {
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    public List<File> findITDocuments(Folder folder, File directory) {
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
            ITDocumentFinder finder = new ITDocumentFinder();

            long startTime = System.nanoTime();
            List<File> itDocuments = finder.findITDocuments(folder, dir);
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000;

            System.out.println("Documents related to Information Technology: " + itDocuments);
            System.out.println("Execution time: " + duration + " ms");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

