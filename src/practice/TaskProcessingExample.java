package practice;

import java.util.concurrent.*;
/*
Напишіть фрагмент коду, в якому створюється пул десяти потоків, створюються та завантажуються
в пул сто підзадач, 5 з яких створюють завдання на обчислення та розміщують їх у буфер,
а 95 – як тільки завдання на обчислення з’являється, витягують його з буфера та виконують обчислення.
Припустіть, що завдання на обчислення задані типом Т, а обчислення задані в методі act() цього типу.
 */

class T {
    private final int value;

    public T(int value) {
        this.value = value;
    }

    public void act() {
        // Імітація обчислення
        System.out.println(Thread.currentThread().getName() + " обчислює: " + (value * value));
    }
}


public class TaskProcessingExample {
    public static void main(String[] args) {
        // Буфер для обміну завданнями
        BlockingQueue<T> buffer = new LinkedBlockingQueue<>();

        // Пул з 10 потоків
        ExecutorService pool = Executors.newFixedThreadPool(10);

        // 5 продюсерів
        for (int i = 0; i < 5; i++) {
            pool.submit(() -> {
                for (int j = 0; j < 20; j++) { // кожен створює 20 задач = 5*20=100
                    try {
                        T task = new T(j);
                        buffer.put(task); // додати завдання в буфер
                        System.out.println(Thread.currentThread().getName() + " додав завдання " + j);
                        Thread.sleep(50); // затримка для імітації процесу
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }

        // 95 споживачів
        for (int i = 0; i < 95; i++) {
            pool.submit(() -> {
                while (true) {
                    try {
                        T task = buffer.take(); // чекає, поки з’явиться завдання
                        task.act();             // виконує обчислення
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }

        // Завершення роботи пулу після деякого часу
        pool.shutdown();
        try {
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
        }
    }
}

