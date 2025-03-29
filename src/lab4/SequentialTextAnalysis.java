package lab4;

import java.util.*;

class SequentialTextAnalysis {
    static WordStatistics analyze(Folder folder) {
        List<Integer> wordLengths = new ArrayList<>();

        for (Folder subFolder : folder.getSubFolders()) {
            wordLengths.addAll(analyze(subFolder).getWordLengths());
        }

        for (Document document : folder.getDocuments()) {
            for (String line : document.getLines()) {
                for (String word : line.split("\\W+")) {
                    if (!word.isEmpty()) {
                        wordLengths.add(word.length());
                    }
                }
            }
        }

        return new WordStatistics(wordLengths);
    }
}

