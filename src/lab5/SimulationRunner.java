package lab5;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class SimulationRunner {

    private final int simulationsCount;
    private final int numberOfHandlers;
    private final int maxQueueLimit;

    public SimulationRunner(int simulationsCount, int numberOfHandlers, int maxQueueLimit) {
        this.simulationsCount = simulationsCount;
        this.numberOfHandlers = numberOfHandlers;
        this.maxQueueLimit = maxQueueLimit;
    }

    public void runSimulations() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(simulationsCount);
        List<Future<Analyzer>> futures = new ArrayList<>();

        for (int i = 0; i < simulationsCount; i++) {
            QueueCallable simulation = new QueueCallable(numberOfHandlers, maxQueueLimit);
            Future<Analyzer> future = executor.submit(() -> {
                System.out.println("Queue in process...");
                return simulation.call();
            });
            futures.add(future);
        }

        double totalSkippedChance = 0.0;
        double totalQueueLength = 0.0;

        for (Future<Analyzer> future : futures) {
            Analyzer analyzer = future.get();
            totalSkippedChance += analyzer.skippedChance();
            totalQueueLength += analyzer.getAvgQueueValue();
        }

        executor.shutdown();

        double avgSkippedChance = totalSkippedChance / simulationsCount;
        double avgQueueLength = totalQueueLength / simulationsCount;

        // Вивід з форматуванням
        System.out.printf("Skipped Messages %% After %d Parallel Runs: %.3f%n", simulationsCount, avgSkippedChance * 100);
        System.out.printf("Average Queue Value After %d Parallel Runs: %.3f%n", simulationsCount, avgQueueLength);
    }
}
