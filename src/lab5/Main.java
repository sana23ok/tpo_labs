package lab5;

import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        task1();
    }

    public static void task1() {
        System.out.println("- - - Starting task 1 - - -");
        QueueCallable simulation = new QueueCallable(5, 100); // 5 каналів, черга на 100
        Analyzer analyzer = simulation.call();

        System.out.printf("Skipped Messages %%: %.2f%%\n", analyzer.skippedChance() * 100);
        System.out.printf("Average Queue Length: %.2f\n", analyzer.getAvgQueueValue());
    }
}
