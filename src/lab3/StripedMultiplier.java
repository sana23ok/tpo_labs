package lab3;

class StripedMultiplier {
    public static Matrix multiplyStriped(Matrix m1, Matrix m2, int numThreads) {
        Matrix product = new Matrix(m1.getRows(), m2.getCols());

        Thread[] threads = new Thread[numThreads];

        for (int t = 0; t < numThreads; t++) {
            final int threadIndex = t;

            threads[t] = new Thread(() -> {
                // Кожен потік обробляє певні рядки результатної матриці
                // Потік з індексом t буде обробляти всі рядки, де i = t, t + numThreads, t + 2*numThreads, ...
                for (int i = threadIndex; i < m1.getRows(); i += numThreads) {
                    // Проходимо по всіх стовпцях результатної матриці
                    for (int j = 0; j < m2.getCols(); j++) {
                        // Обчислюємо добуток для елемента результатної матриці
                        for (int k = 0; k < m1.getCols(); k++) {
                            // Сумуємо добутки елементів відповідних рядків з матриці m1 та стовпця з матриці m2
                            product.getData()[i][j] += m1.getData()[i][k] * m2.getData()[k][j];
                        }
                    }
                }
            });
            threads[t].start();
        }

        for (Thread thread : threads) {
            try {
                // Очікуємо завершення кожного потоку
                thread.join();
            } catch (InterruptedException e) {
                // Якщо потік був перерваний, виводимо помилку
                e.printStackTrace();
            }
        }

        return product;
    }
}
