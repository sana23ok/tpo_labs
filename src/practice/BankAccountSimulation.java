package practice;

import java.util.Random;
import java.util.concurrent.*;

/*
Напишіть код, в якому 100 підзадач одночасно здійснюють 10000 операцій збільшення або зменшення рахунку
банківського клієнта на задану суму коштів. Задачі завантажуються в пул 8 потоків.
Результат операцій виведіть на консоль.
 */

public class BankAccountSimulation {
    static class BankAccount {
        private int balance = 0;

        // Синхронізований метод для зміни балансу
        public synchronized void changeBalance(int amount) {
            balance += amount;
        }

        public synchronized int getBalance() {
            return balance;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final int NUM_TASKS = 100;
        final int OPERATIONS_PER_TASK = 10_000;
        final int THREAD_POOL_SIZE = 8;

        BankAccount account = new BankAccount();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        CountDownLatch latch = new CountDownLatch(NUM_TASKS);

        for (int i = 0; i < NUM_TASKS; i++) {
            executor.submit(() -> {
                Random rand = new Random();
                for (int j = 0; j < OPERATIONS_PER_TASK; j++) {
                    int amount = rand.nextInt(201) - 100;  // від -100 до +100
                    account.changeBalance(amount);
                }
                latch.countDown();
            });
        }

        // Чекаємо завершення всіх задач
        latch.await();

        // Завершення пулу потоків
        executor.shutdown();

        // Вивід кінцевого балансу
        System.out.println("Кінцевий баланс рахунку: " + account.getBalance());
    }
}

