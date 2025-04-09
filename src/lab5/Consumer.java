package lab5;

import java.util.Random;

/**
 * Потік, що імітує споживача (канал обслуговування) в системі.
 */
public class Consumer extends Thread {
    private final Manager queueManager;

    public Consumer(Manager queueManager) {
        this.queueManager = queueManager;
    }

    @Override
    public void run() {
        Random delayGenerator = new Random();

        while (true) {
            Integer task = queueManager.getItem(); // Отримання елементу з черги
            if (task == null) break; // Якщо немає більше елементів і симуляція завершена

            try {
                Thread.sleep(delayGenerator.nextInt(100)); // Затримка – імітація обробки завдання
            } catch (InterruptedException ignored) {}

            queueManager.incCompleted(); // Після обробки збільшуємо лічильник виконаних
        }
    }
}
