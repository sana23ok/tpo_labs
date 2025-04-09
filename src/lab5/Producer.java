package lab5;

import java.util.Random;

/**
 * Потік, який імітує генерацію завдань у чергу.
 */
public class Producer extends Thread {
    private final Manager queueController; // Менеджер, який керує чергою

    public Producer(Manager queueController) {
        this.queueController = queueController;
    }

    @Override
    public void run() {
        Random numberGenerator = new Random();
        long simulationStart = System.currentTimeMillis();

        while (System.currentTimeMillis() - simulationStart < 10_000) { // 10 секунд симуляції
            queueController.addItem(numberGenerator.nextInt(100)); // Додаємо випадкове завдання

            try {
                Thread.sleep(numberGenerator.nextInt(15)); // Імітація інтервалу генерації
            } catch (InterruptedException ignored) {}
        }

        queueController.setSimulationDone(); // Позначаємо завершення симуляції
    }
}
