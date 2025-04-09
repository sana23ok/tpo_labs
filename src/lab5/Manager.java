package lab5;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Клас Manager реалізує логіку управління чергою для багатоканальної системи обслуговування.
 * Він зберігає чергу, дозволяє додавати/отримувати елементи, відстежує кількість оброблених і пропущених заявок.
 */
public class Manager {
    private final int maxQueueCapacity;
    private final Queue<Integer> taskQueue;
    private boolean isSimulationFinished = false;

    public int rejectedTasks = 0;
    public int processedTasks = 0;

    public Manager(int maxQueueCapacity) {
        this.maxQueueCapacity = maxQueueCapacity;
        this.taskQueue = new ArrayDeque<>();
    }

    public synchronized void addItem(int newTask) {
        if (taskQueue.size() >= maxQueueCapacity) {
            rejectedTasks++;// Відмова через переповнення черги
        } else {
            taskQueue.add(newTask);
            notifyAll();// Сповіщаємо потоки, які очікують
        }
    }

    /**
     * Повертає елемент із черги для обробки. Очікує, поки черга не стане непорожньою.
     * Повертає null, якщо симуляція завершена і нових завдань не буде.
     */
    public synchronized Integer getItem() {
        while (taskQueue.isEmpty()) {
            if (isSimulationFinished) return null; // Симуляція завершена, повертаємо null

            try {
                wait(); // Очікуємо на появу нових заявок у черзі
            } catch (InterruptedException ignored) {}
        }
        return taskQueue.poll(); // Повертаємо заявку з початку черги
    }

    public synchronized void incCompleted() {
        processedTasks++;
    }

    public synchronized int getQueueSize() {
        return taskQueue.size();
    }

    public synchronized void setSimulationDone() {
        isSimulationFinished = true;
        notifyAll(); // Сповіщаємо всі потоки, які могли очікувати
    }

    public synchronized boolean isSimulationDone() {
        return isSimulationFinished;
    }
}
