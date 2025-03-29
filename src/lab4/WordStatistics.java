package lab4;

import java.util.*;

class WordStatistics {
    private final List<Integer> wordLengths;

    WordStatistics(List<Integer> wordLengths) {
        this.wordLengths = wordLengths;
    }

    List<Integer> getWordLengths() {
        return wordLengths;
    }

    double getMean() {
        return wordLengths.stream().mapToInt(Integer::intValue).average().orElse(0);
    }

    int getMin() {
        return wordLengths.stream().min(Integer::compare).orElse(0);
    }

    int getMax() {
        return wordLengths.stream().max(Integer::compare).orElse(0);
    }

    double getStandardDeviation() {
        double mean = getMean();
        return Math.sqrt(wordLengths.stream().mapToDouble(l -> Math.pow(l - mean, 2)).average().orElse(0));
    }

    @Override
    public String toString() {
        return "Min: " + getMin() + ", Max: " + getMax() + ", Mean: " + getMean() + ", Std Dev: " + getStandardDeviation();
    }
}

