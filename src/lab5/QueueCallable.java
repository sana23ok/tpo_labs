package lab5;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QueueCallable implements Callable<Analyzer> {

    private final int numberOfHandlers; // Кількість обслуговуючих потоків (Consumer)
    private final int maxQueueLimit;

    // Конструктор за замовчуванням: 5 обслуговуючих потоків, розмір черги — 100
    public QueueCallable() {
        this(5, 100);
    }

    // Конструктор із параметрами
    public QueueCallable(int numberOfHandlers, int maxQueueLimit) {
        this.numberOfHandlers = numberOfHandlers;
        this.maxQueueLimit = maxQueueLimit;
    }

    /**
     * Метод call() запускає моделювання: створює диспетчера (Manager),
     * пул потоків для обробників, запускає продюсера і аналітика.
     * Після завершення повертає аналітик (Analyzer), який містить результати.
     */
    @Override
    public Analyzer call() {
        Manager dispatcher = new Manager(maxQueueLimit);
        ExecutorService threadPool = Executors.newFixedThreadPool(numberOfHandlers);

        for (int i = 0; i < numberOfHandlers; i++) {
            threadPool.execute(new Consumer(dispatcher));
        }

        Analyzer statsCollector = new Analyzer(dispatcher);
        Producer taskGenerator = new Producer(dispatcher);
        Monitor monitor = new Monitor(dispatcher);

        statsCollector.start();
        monitor.start();
        taskGenerator.start();

        try {
            taskGenerator.join();
            threadPool.shutdown();
            statsCollector.join();
            monitor.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return statsCollector;
    }
}
