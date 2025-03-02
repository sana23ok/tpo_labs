package lab3;

class FoxMultiplier {
    // Алгоритм Фокса для множення матриць
    public static Result multiplyFox(Matrix m1, Matrix m2, int blockSize, int numThreads) {
        int resultRows = m1.getRows();
        int resultCols = m2.getCols();

        Result product = new Result(resultRows, resultCols);

        Matrix[][] blocksM1 = Matrix.splitIntoBlocks(m1, blockSize);
        Matrix[][] blocksM2 = Matrix.splitIntoBlocks(m2, blockSize);
        Matrix[][] blocksProduct = new Matrix[resultRows / blockSize][resultCols / blockSize];

        // Створення масиву потоків
        Thread[] threads = new Thread[numThreads];

        for (int stage = 0; stage < resultRows / blockSize; stage++) {
            final int currentStage = stage;

            // Робимо копії блоків для поточного етапу, щоб уникнути проблеми змінних у лямбда-виразах
            Matrix[][] currentBlocksM1 = blocksM1;
            Matrix[][] currentBlocksM2 = blocksM2;

            for (int t = 0; t < numThreads; t++) {
                final int threadIndex = t;

                threads[t] = new Thread(() -> {
                    for (int i = threadIndex * blockSize; i < Math.min((threadIndex + 1) * blockSize, resultRows); i++) {
                        for (int j = 0; j < resultCols; j++) {
                            for (int k = 0; k < m1.getCols(); k++) {
                                product.getData()[i][j] += currentBlocksM1[i / blockSize][k / blockSize].getData()[i % blockSize][k % blockSize]
                                        * currentBlocksM2[k / blockSize][j / blockSize].getData()[k % blockSize][j % blockSize];
                            }
                        }
                    }
                });

                threads[t].start();
            }

            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            blocksM1 = swapBlocks(currentBlocksM1, currentStage, blockSize);
            blocksM2 = swapBlocks(currentBlocksM2, currentStage, blockSize);
        }

        return product;
    }

    // Функція для обміну блоками між матрицями
    private static Matrix[][] swapBlocks(Matrix[][] blocks, int stage, int blockSize) {
        int n = blocks.length;
        Matrix[][] swapped = new Matrix[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                swapped[i][j] = blocks[(i + stage) % n][(j + stage) % n];
            }
        }
        return swapped;
    }
}
