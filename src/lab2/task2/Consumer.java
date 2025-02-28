package lab2.task2;

import java.util.Random;

public class Consumer implements Runnable {
    private final Drop drop;

    public Consumer(Drop drop) {
        this.drop = drop;
    }

    @Override
    public void run() {
        Random random = new Random();
        while (true) {
            int value = drop.take();
            if (value == -1) { // Перевірка на завершення
                break;
            }
            System.out.println("Consumed: " + value+1);
            try {
                Thread.sleep(random.nextInt(10)); // Затримка для симуляції
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Consumer finished.");
    }
}
