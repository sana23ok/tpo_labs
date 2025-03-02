package lab3;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class FoxMultiplier {
    public static Result multiplyFox(Matrix m1, Matrix m2, int blockSize, int numThreads) {
        int n = m1.getRows();
        Result product = new Result(n, n);

        Matrix[][] blocksM1 = Matrix.splitIntoBlocks(m1, blockSize);
        Matrix[][] blocksM2 = Matrix.splitIntoBlocks(m2, blockSize);
        Matrix[][] blocksProduct = new Matrix[n / blockSize][n / blockSize];

        // Ініціалізуємо блоки результатів
        for (int i = 0; i < blocksProduct.length; i++) {
            for (int j = 0; j < blocksProduct.length; j++) {
                blocksProduct[i][j] = new Matrix(blockSize, blockSize);
            }
        }

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int stage = 0; stage < n / blockSize; stage++) {
            Future<?>[] futures = new Future<?>[numThreads];
            int taskIndex = 0;

            for (int i = 0; i < n / blockSize; i++) {
                for (int j = 0; j < n / blockSize; j++) {
                    int k = (i + stage) % (n / blockSize);

                    Matrix A = blocksM1[i][k];
                    Matrix B = blocksM2[k][j];
                    Matrix C = blocksProduct[i][j];

                    futures[taskIndex++] = executor.submit(() -> multiplyBlock(A, B, C));

                    if (taskIndex >= numThreads) {
                        waitForTasks(futures);
                        taskIndex = 0;
                    }
                }
            }

            waitForTasks(futures);
        }

        executor.shutdown();

        return combineBlocks(blocksProduct, n, blockSize);
    }

    private static void multiplyBlock(Matrix A, Matrix B, Matrix C) {
        int size = A.getRows();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                double sum = 0;
                for (int k = 0; k < size; k++) {
                    sum += A.getData()[i][k] * B.getData()[k][j];
                }
                C.getData()[i][j] += sum;
            }
        }
    }

    private static void waitForTasks(Future<?>[] futures) {
        for (Future<?> future : futures) {
            if (future != null) {
                try {
                    future.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static Result combineBlocks(Matrix[][] blocks, int n, int blockSize) {
        Result result = new Result(n, n);
        double[][] resultData = result.getData();

        for (int i = 0; i < n / blockSize; i++) {
            for (int j = 0; j < n / blockSize; j++) {
                double[][] blockData = blocks[i][j].getData();
                for (int k = 0; k < blockSize; k++) {
                    System.arraycopy(blockData[k], 0, resultData[i * blockSize + k], j * blockSize, blockSize);
                }
            }
        }

        return result;
    }
}
