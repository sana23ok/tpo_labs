package lab2.task2;

public class Drop {
    private final int[] data; // Масив чисел
    private int index = 0; // Поточний індекс
    private boolean empty = true; // Стан буфера (порожній/заповнений)

    public Drop(int size) {
        data = new int[size];
    }

    public synchronized int take() {
        // Чекаємо, поки дані стануть доступними
        while (empty) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        int value = data[--index]; // Витягуємо значення
        if (index == 0) {
            empty = true;
        }
        notifyAll(); // Сповіщаємо, що можна додати дані
        return value;
    }

    public synchronized void put(int value) {
        // Чекаємо, поки буфер стане порожнім
        while (index == data.length) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        data[index++] = value; // Додаємо значення
        empty = false;
        notifyAll(); // Сповіщаємо, що можна отримати дані
    }
}
