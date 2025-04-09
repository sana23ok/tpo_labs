package lab5;

/**
 * Клас Analyzer відповідає за збір статистики під час симуляції:
 * - середнє значення довжини черги;
 * - ймовірність відмови (відкинутих запитів).
 */
public class Analyzer extends Thread {
    private final Manager manager;
    private int totalQueueLength = 0;
    private int sampleCount = 0;

    public Analyzer(Manager manager) {
        this.manager = manager;
    }

    /**
     * Періодичні заміри довжини черги.
     */
    @Override
    public void run() {
        while (true) {
            // Перевірка, чи завершено генерацію та обслуговування запитів, і черга порожня
            synchronized (manager) {
                if (manager.isSimulationDone() && manager.getQueueSize() == 0) break;
            }

            try {
                Thread.sleep(100); // Чекаємо 100 мс перед кожним заміром
            } catch (InterruptedException ignored) {}

            // Додаємо поточну довжину черги до загальної суми
            totalQueueLength += manager.getQueueSize();
            sampleCount++; // Збільшуємо лічильник замірів
        }
    }

    public double getAvgQueueValue() {
        return sampleCount == 0 ? 0 : (double) totalQueueLength / sampleCount;
    }

    /**
     * Обчислення ймовірності відмови — частка запитів, що були відкинуті.
     */
    public double skippedChance() {
        int processedTotal = manager.processedTasks + manager.rejectedTasks; // Загальна кількість запитів
        return processedTotal == 0 ? 0 : (double) manager.rejectedTasks / processedTotal;
    }
}
