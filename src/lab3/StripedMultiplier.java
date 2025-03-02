package lab3;

import java.util.ArrayList;
import java.util.concurrent.*;

public class StripedMultiplier {

    public static Matrix multiplyStriped(Matrix m1, Matrix m2, int numOfThreads) {
        // Перевірка, чи можна перемножити матриці
        if (m1.getCols() != m2.getRows()) {
            throw new IllegalArgumentException("Matrices cannot be multiplied - invalid dimensions");
        }

        // Створюємо результуючу матрицю з необхідними розмірами
        Matrix result = new Matrix(m1.getRows(), m2.getCols());

        // Транспонуємо другу матрицю, щоб полегшити доступ до її стовпців
        Matrix transMatrix2 = m2.transpose();

        // Створюємо пул потоків із заданою кількістю потоків
        ExecutorService executor = Executors.newFixedThreadPool(numOfThreads);

        // Список для збереження обчислюваних значень
        ArrayList<Future<Double>> futures = new ArrayList<>();

        // Перебираємо всі елементи результуючої матриці
        for (int i = 0; i < m1.getRows(); i++) {
            for (int j = 0; j < m2.getCols(); j++) {
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
            for (int i = 0; i < result.getRows(); i++) {
                for (int j = 0; j < result.getCols(); j++) {
                    // Отримуємо результат обчислення з потоку та вставляємо у результат
                    result.getData()[i][j] = futures.get(i * result.getCols() + j).get();
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
