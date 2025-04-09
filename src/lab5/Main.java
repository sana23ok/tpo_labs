package lab5;

import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //task1();
        task2();
    }

    public static void task1() {
        System.out.println("- - - Starting task 1 - - -");
        QueueCallable simulation = new QueueCallable(5, 100); // 5 каналів, черга на 100
        Analyzer analyzer = simulation.call();

        System.out.printf("Skipped Messages %%: %.2f%%\n", analyzer.skippedChance() * 100);
        System.out.printf("Average Queue Length: %.2f\n", analyzer.getAvgQueueValue());
    }

    public static void task2() throws ExecutionException, InterruptedException {
        System.out.println("- - - Starting parallel simulation task 2 - - -");
        int numberOfSimulations = 10;
        int channels = 5;
        int queueSize = 100;

        SimulationRunner runner = new SimulationRunner(numberOfSimulations, channels, queueSize);
        runner.runSimulations();
    }
}
