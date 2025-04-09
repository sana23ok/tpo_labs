package lab5;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Клас QueueCallable виконує роль задачі, що моделює роботу багатоканальної
 * системи масового обслуговування з обмеженою чергою.
 * Використовує пул потоків для запуску декількох споживачів.
 */
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
        Manager dispatcher = new Manager(maxQueueLimit); // Менеджер черги
        ExecutorService threadPool = Executors.newFixedThreadPool(numberOfHandlers); // Пул потоків

        // Запускаємо обробники заявок (Consumers)
        for (int i = 0; i < numberOfHandlers; i++) {
            threadPool.execute(new Consumer(dispatcher));
        }

        Analyzer statsCollector = new Analyzer(dispatcher);
        Producer taskGenerator = new Producer(dispatcher);

        statsCollector.start();
        taskGenerator.start();

        try {
            // Чекаємо завершення генерації заявок
            taskGenerator.join();

            // Після завершення генерації — закриваємо пул потоків (споживачі завершать роботу)
            threadPool.shutdown();

            // Чекаємо завершення аналітика
            statsCollector.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return statsCollector; // Повертаємо результати аналізу
    }
}
