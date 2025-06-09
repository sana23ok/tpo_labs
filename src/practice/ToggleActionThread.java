package practice;
/*
Потік А виконує дію actionEmpty за умови isEmpty та дію actionFull за умови isFull.
Змінювання умови з isEmpty на isFull і навпаки відбувається кожного разу при завершенні виконання дії.
Напишіть код такого потоку, використовуючи об’єкти типу Condition.
 */

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ToggleActionThread {

    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    private boolean isEmpty = true; // Початковий стан

    public void actionEmpty() {
        System.out.println("Executing actionEmpty");
    }

    public void actionFull() {
        System.out.println("Executing actionFull");
    }

    public void run() {
        while (true) {
            lock.lock();
            try {
                if (isEmpty) {
                    actionEmpty();
                } else {
                    actionFull();
                }

                // Змінюємо стан
                isEmpty = !isEmpty;

                // Повідомляємо можливих очікувачів (не обов’язково в однопотоковому прикладі, але хороша практика)
                condition.signalAll();

                // Для прикладу зробимо паузу, щоби побачити чергування
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) {
        ToggleActionThread obj = new ToggleActionThread();
        Thread thread = new Thread(obj::run);
        thread.start();
    }
}

