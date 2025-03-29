package lab4;

import java.util.concurrent.RecursiveTask;
import java.util.*;

class FolderAnalysisTask extends RecursiveTask<WordStatistics> {
    private final Folder folder;

    FolderAnalysisTask(Folder folder) {
        this.folder = folder;
    }

    @Override
    protected WordStatistics compute() {
        List<RecursiveTask<WordStatistics>> forks = new ArrayList<>();
        List<Integer> allWordLengths = new ArrayList<>();

        for (Folder subFolder : folder.getSubFolders()) {
            FolderAnalysisTask task = new FolderAnalysisTask(subFolder);
            forks.add(task);
            task.fork();
        }

        for (Document document : folder.getDocuments()) {
            WordLengthAnalysisTask task = new WordLengthAnalysisTask(document);
            forks.add(task);
            task.fork();
        }

        for (RecursiveTask<WordStatistics> task : forks) {
            allWordLengths.addAll(task.join().getWordLengths());
        }

        return new WordStatistics(allWordLengths);
    }
}

