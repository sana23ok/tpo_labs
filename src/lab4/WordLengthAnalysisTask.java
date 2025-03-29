package lab4;

import java.util.concurrent.RecursiveTask;
import java.util.*;

class WordLengthAnalysisTask extends RecursiveTask<WordStatistics> {
    private final Document document;

    WordLengthAnalysisTask(Document document) {
        this.document = document;
    }

    @Override
    protected WordStatistics compute() {
        List<Integer> wordLengths = new ArrayList<>();
        for (String line : document.getLines()) {
            for (String word : line.split("\\W+")) {
                if (!word.isEmpty()) {
                    wordLengths.add(word.length());
                }
            }
        }
        return new WordStatistics(wordLengths);
    }
}
