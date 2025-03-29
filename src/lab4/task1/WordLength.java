package lab4.task1;

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

    static Map<Integer, Integer> analyzeWordLengths(Document document) {
        Map<Integer, Integer> wordLengthCounts = new HashMap<>();
        for (String line : document.getLines()) {
            for (String word : wordsIn(line)) {
                int length = word.length();
                if (length > 0) wordLengthCounts.put(length, wordLengthCounts.getOrDefault(length, 0) + 1);
            }
        }
        return wordLengthCounts;
    }

    static double calculateMean(Map<Integer, Integer> wordLengthCounts) {
        long totalLength = 0;
        long totalWords = 0;
        for (Map.Entry<Integer, Integer> entry : wordLengthCounts.entrySet()) {
            totalLength += (long) entry.getKey() * entry.getValue();
            totalWords += entry.getValue();
        }
        return (double) totalLength / totalWords;
    }

    static double calculateVariance(Map<Integer, Integer> wordLengthCounts, double mean) {
        double variance = 0;
        long totalWords = 0;
        for (Map.Entry<Integer, Integer> entry : wordLengthCounts.entrySet()) {
            variance += entry.getValue() * Math.pow(entry.getKey() - mean, 2);
            totalWords += entry.getValue();
        }
        return variance / totalWords;
    }

    static double calculateStandardDeviation(double variance) {
        return Math.sqrt(variance);
    }
}

class DocumentWordLengthTask extends RecursiveTask<Map<Integer, Integer>> {
    private final Document document;

    DocumentWordLengthTask(Document document) {
        this.document = document;
    }

    @Override
    protected Map<Integer, Integer> compute() {
        return WordLengthAnalyzer.analyzeWordLengths(document);
    }
}

class FolderWordLengthTask extends RecursiveTask<Map<Integer, Integer>> {
    private final Folder folder;

    FolderWordLengthTask(Folder folder) {
        this.folder = folder;
    }

    @Override
    protected Map<Integer, Integer> compute() {
        Map<Integer, Integer> combinedCounts = new HashMap<>();
        List<RecursiveTask<Map<Integer, Integer>>> forks = new LinkedList<>();

        for (Folder subFolder : folder.getSubFolders()) {
            FolderWordLengthTask task = new FolderWordLengthTask(subFolder);
            forks.add(task);
            task.fork();
        }

        for (Document document : folder.getDocuments()) {
            DocumentWordLengthTask task = new DocumentWordLengthTask(document);
            forks.add(task);
            task.fork();
        }

        for (RecursiveTask<Map<Integer, Integer>> task : forks) {
            Map<Integer, Integer> result = task.join();
            result.forEach((length, count) -> combinedCounts.merge(length, count, Integer::sum));
        }

        return combinedCounts;
    }
}


class WordLength {
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    public Map<Integer, Integer> analyzeFolderWordLengths(Folder folder) {
        return forkJoinPool.invoke(new FolderWordLengthTask(folder));
    }

    public Map<Integer, Integer> analyzeFolderWordLengthsSingleThread(Folder folder) {
        Map<Integer, Integer> combinedCounts = new HashMap<>();
        List<Folder> folders = new ArrayList<>();
        folders.add(folder);

        while (!folders.isEmpty()) {
            Folder currentFolder = folders.remove(0);
            folders.addAll(currentFolder.getSubFolders());

            for (Document document : currentFolder.getDocuments()) {
                Map<Integer, Integer> result = WordLengthAnalyzer.analyzeWordLengths(document);
                result.forEach((length, count) -> combinedCounts.merge(length, count, Integer::sum));
            }
        }
        return combinedCounts;
    }

    public static void main(String[] args) {
        String directoryPath = args.length > 0 ? args[0] : "src/lab4/files";

        File dir = new File(directoryPath);
        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("Error: Directory does not exist or is not a folder.");
            System.exit(1);
        }

        try {
            WordLength statistic = new WordLength();
            Folder folder = Folder.fromDirectory(dir);

            long startTimeSingle = System.nanoTime();
            Map<Integer, Integer> singleThreadResults = statistic.analyzeFolderWordLengthsSingleThread(folder);
            long endTimeSingle = System.nanoTime();
            long durationSingle = (endTimeSingle - startTimeSingle) / 1_000_000;

            long startTimeParallel = System.nanoTime();
            Map<Integer, Integer> parallelResults = statistic.analyzeFolderWordLengths(folder);
            long endTimeParallel = System.nanoTime();
            long durationParallel = (endTimeParallel - startTimeParallel) / 1_000_000;

            double meanSingle = WordLengthAnalyzer.calculateMean(singleThreadResults);
            double varianceSingle = WordLengthAnalyzer.calculateVariance(singleThreadResults, meanSingle);
            double stdDevSingle = WordLengthAnalyzer.calculateStandardDeviation(varianceSingle);

            double meanParallel = WordLengthAnalyzer.calculateMean(parallelResults);
            double varianceParallel = WordLengthAnalyzer.calculateVariance(parallelResults, meanParallel);
            double stdDevParallel = WordLengthAnalyzer.calculateStandardDeviation(varianceParallel);

            System.out.println("\nSingle Thread Word Length Frequency: " + singleThreadResults);
            System.out.println("Single Thread Word Length Relative Frequency: " + calculateRelativeFrequency(singleThreadResults));

            System.out.println("Single Thread Duration: " + durationSingle + " ms");
            System.out.println("Single Thread Mean: " + meanSingle);
            System.out.println("Single Thread Standard Deviation: " + stdDevSingle);

            System.out.println("\n\nParallel Thread Word Length Frequency: " + parallelResults);
            System.out.println("Parallel Thread Word Length Relative Frequency: " + calculateRelativeFrequency(parallelResults));

            System.out.println("\nParallel Thread Duration: " + durationParallel + " ms");
            System.out.println("Parallel Thread Mean: " + meanParallel);
            System.out.println("Parallel Thread Standard Deviation: " + stdDevParallel);

            // Прискорення та ефективність
            double speedup = (double) durationSingle / durationParallel;
            int availableProcessors = Runtime.getRuntime().availableProcessors();
            double efficiency = speedup / availableProcessors;

            System.out.println("\n\nSpeedup (Single Thread / Parallel Thread): " + speedup);
            System.out.println("Efficiency (Speedup / Available Processors): " + efficiency);

            // Відображення гістограми для обох методів
            System.out.println("\nDisplaying Histogram for Single Thread Analysis...");
            //WordLengthHistogram.displayHistogram(singleThreadResults);

            System.out.println("\nDisplaying Histogram for Parallel Thread Analysis...");
            WordLengthHistogram.displayHistogram(parallelResults);

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static Map<Integer, Double> calculateRelativeFrequency(Map<Integer, Integer> wordLengthCounts) {
        long totalWords = wordLengthCounts.values().stream().mapToLong(Integer::longValue).sum();
        Map<Integer, Double> relativeFrequency = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : wordLengthCounts.entrySet()) {
            relativeFrequency.put(entry.getKey(), Math.round((double) entry.getValue() / totalWords * 10000.0) / 10000.0);
        }
        return relativeFrequency;
    }
}
