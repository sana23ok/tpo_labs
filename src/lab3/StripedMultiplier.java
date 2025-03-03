package lab3;

import java.util.ArrayList;
import java.util.concurrent.*;

public class StripedMultiplier {

    public static Result multiplyStriped(Matrix m1, Matrix m2, int numOfThreads) {
        // Перевірка, чи можна перемножити матриці
        if (m1.getCols() != m2.getRows()) {
            throw new IllegalArgumentException("Matrices cannot be multiplied - invalid dimensions");
        }

        // Створюємо результуючий об'єкт класу Result з необхідними розмірами
        int rows = m1.getRows();
        int cols = m2.getCols();
        Result result = new Result(rows, cols);

        // Транспонуємо другу матрицю, щоб полегшити доступ до її стовпців
        Matrix transMatrix2 = m2.transpose();

        // Створюємо пул потоків із заданою кількістю потоків
        ExecutorService executor = Executors.newFixedThreadPool(numOfThreads);

        // Список для збереження обчислюваних значень
        ArrayList<Future<Double>> futures = new ArrayList<>();

        // Перебираємо всі елементи результуючої матриці
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // Отримуємо i-й рядок з першої матриці
                double[] row = m1.extractRow(i);

                // Отримуємо j-й стовпець з другої транспонованої матриці (тобто рядок)
                double[] col = transMatrix2.extractRow(j);

                // Передаємо операцію обчислення скалярного добутку у потік
                futures.add(executor.submit(() -> getProduct(row, col)));
            }
        }

        // Завершуємо прийом нових задач у executor
        executor.shutdown();

        try {
            // Записуємо отримані значення у результуючу матрицю
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    // Отримуємо результат обчислення з потоку та вставляємо у результат
                    result.getData()[i][j] = futures.get(i * cols + j).get();
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    // Метод для обчислення скалярного добутку двох векторів (рядок і стовпець)
    private static Double getProduct(double[] row, double[] column) {
        double result = 0;
        for (int k = 0; k < row.length; k++) {
            result += row[k] * column[k];
        }
        return result;
    }
}
