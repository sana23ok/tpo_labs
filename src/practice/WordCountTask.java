package practice;

import java.util.ArrayList;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

/*
Створіть та запустіть на виконання пул потоків, що виконують підрахунок кількості слів «algorithm»
в заданому масиві ArrayList<String> words з рекурсивним поділом на підзадачі.
 */

public class WordCountTask extends RecursiveTask<Integer> {
    private static final int THRESHOLD = 100; // межа для розділення задачі
    private final ArrayList<String> words;
    private final int start;
    private final int end;

    public WordCountTask(ArrayList<String> words, int start, int end) {
        this.words = words;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer compute() {
        if (end - start <= THRESHOLD) {
            // Базовий випадок: підрахунок в межах порогу
            int count = 0;
            for (int i = start; i < end; i++) {
                if ("algorithm".equalsIgnoreCase(words.get(i))) {
                    count++;
                }
            }
            return count;
        } else {
            // Рекурсивне розбиття задачі
            int mid = (start + end) / 2;
            WordCountTask leftTask = new WordCountTask(words, start, mid);
            WordCountTask rightTask = new WordCountTask(words, mid, end);

            leftTask.fork(); // запустити ліву задачу асинхронно
            int rightResult = rightTask.compute(); // обчислити праву задачу
            int leftResult = leftTask.join(); // дочекатись завершення лівої задачі

            return leftResult + rightResult;
        }
    }

    // Головний метод для запуску
    public static void main(String[] args) {
        ArrayList<String> words = new ArrayList<>();

        // Додавання тестових даних
        for (int i = 0; i < 1000; i++) {
            words.add(i % 10 == 0 ? "algorithm" : "data");
        }

        ForkJoinPool pool = new ForkJoinPool();
        WordCountTask task = new WordCountTask(words, 0, words.size());

        int result = pool.invoke(task);
        System.out.println("Кількість слів 'algorithm': " + result);
    }
}

