package practice;

/*
* Напишіть код, в якому створюються та запускаються на виконання потоки S і W.
* Потік S виконує переключення з затримкою 1000 мілісекунд зі стану true у стан false і навпаки.
* Потік W очікує стану true потоку S, виводить на консоль зворотний відлік від 30
* із затримкою 100 мілісекунд та призупиняє свою дію, як тільки потік S переключено у стан false.
* Умовою завершення роботи потоків є досягнення відліку нульової відмітки.
*/

public class ThreadControl {

    private static volatile boolean sState = true; // Стан потоку S, volatile для видимості змін між потоками
    private static final Object lock = new Object(); // Об'єкт для синхронізації
    private static volatile int countdown = 30; // Зворотний відлік, volatile для видимості
    private static volatile boolean running = true; // Умова завершення роботи потоків

    public static void main(String[] args) {

        // Потік S
        Thread sThread = new Thread(() -> {
            try {
                System.out.println("Потік S: Запущено.");
                while (running) {
                    synchronized (lock) {
                        sState = !sState; // Переключення стану
                        System.out.println("Потік S: Стан переключено на " + sState);
                        lock.notifyAll(); // Повідомляємо всім, хто чекає, про зміну стану
                    }
                    Thread.sleep(1000); // Затримка 1000 мс
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Потік S: Перервано.");
            } finally {
                System.out.println("Потік S: Завершено.");
            }
        }, "Потік S");

        // Потік W
        Thread wThread = new Thread(() -> {
            try {
                System.out.println("Потік W: Запущено.");
                while (running) {
                    synchronized (lock) {
                        while (!sState && running) { // Очікуємо стану true потоку S
                            System.out.println("Потік W: Чекаю стану true від Потоку S...");
                            lock.wait(); // Призупиняємо дію, чекаємо notifyAll()
                        }
                        if (!running) break; // Виходимо, якщо програма завершується

                        // Якщо sState = true, виконуємо зворотний відлік
                        while (sState && countdown > 0 && running) {
                            System.out.println("Потік W: Зворотний відлік: " + countdown);
                            countdown--; // Зменшуємо відлік
                            if (countdown == 0) {
                                running = false; // Умова завершення роботи потоків
                                System.out.println("Потік W: Відлік досяг нуля. Завершення роботи...");
                                lock.notifyAll(); // Повідомляємо S, щоб він теж завершився
                                break; // Виходимо з внутрішнього циклу
                            }
                            Thread.sleep(100); // Затримка 100 мс

                            // Після затримки, перевіряємо, чи S переключив стан,
                            // щоб W міг призупинитися, якщо S стане false
                            if (!sState) {
                                System.out.println("Потік W: Потік S переключено у false. Призупиняю дію.");
                                break; // Виходимо з внутрішнього циклу і чекаємо знову
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Потік W: Перервано.");
            } finally {
                System.out.println("Потік W: Завершено.");
            }
        }, "Потік W");

        // Запуск потоків
        sThread.start();
        wThread.start();

        // Очікування завершення потоків
        try {
            sThread.join();
            wThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Головний потік: Перервано під час очікування завершення інших потоків.");
        }

        System.out.println("Головний потік: Всі потоки завершили роботу.");
    }
}