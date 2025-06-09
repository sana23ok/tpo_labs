package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


import java.util.Random;

class ResultMatrix extends Matrix {
    public ResultMatrix(int rows, int cols) {
        super(rows, cols);
    }

    // You can add specific methods if needed, like result validation or formatting
    public void print() {
        for (int i = 0; i < getRows(); i++) {
            for (int j = 0; j < getCols(); j++) {
                System.out.print(get(i, j) + " ");
            }
            System.out.println();
        }
    }

    public static boolean compareMatrices(ResultMatrix matrix1, ResultMatrix matrix2) {
        if (matrix1.getRows() != matrix2.getRows() || matrix1.getCols() != matrix2.getCols()) {
            return false;
        }
        for (int i = 0; i < matrix1.getRows(); i++) {
            for (int j = 0; j < matrix1.getCols(); j++) {
                if (matrix1.get(i, j) != matrix2.get(i, j)) {
                    return false;
                }
            }
        }
        return true;
    }
}

class Matrix {
    private final int[][] data;
    private final int rows;
    private final int cols;

    public Matrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.data = new int[rows][cols];
    }

    public static ResultMatrix multiplySequential(Matrix matrixA, Matrix matrixB) {
        int rowsA = matrixA.getRows();
        int colsA = matrixA.getCols();
        int colsB = matrixB.getCols();
        ResultMatrix resultMatrix = new ResultMatrix(rowsA, colsB);

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                int sum = 0;
                for (int k = 0; k < colsA; k++) {
                    sum += matrixA.get(i, k) * matrixB.get(k, j);
                }
                resultMatrix.set(i, j, sum);
            }
        }
        return resultMatrix;
    }

    public static Matrix generate(int rows, int cols) {
        Random random = new Random();
        Matrix matrix = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix.data[i][j] = random.nextInt(10);
            }
        }
        return matrix;
    }

    public int get(int row, int col) {
        return data[row][col];
    }

    public void set(int row, int col, int value) {
        data[row][col] = value;
    }

    public synchronized int getRows() {
        return rows;
    }

    public synchronized int getCols() {
        return cols;
    }

    // Метод для транспонування матриці
    public Matrix transpose() {
        Matrix transposed = new Matrix(cols, rows);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposed.set(j, i, data[i][j]);
            }
        }
        return transposed;
    }
    public synchronized void addToCell(int row, int col, int value) {
        data[row][col] += value;
    }

    public synchronized int[][] getBlock(int rowBlock, int colBlock, int blockSize) {
        int[][] block = new int[blockSize][blockSize];
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                int row = rowBlock * blockSize + i;
                int col = colBlock * blockSize + j;
                if (row < rows && col < cols) { // Ensure we are within the bounds of the matrix
                    block[i][j] = data[row][col];
                } else {
                    block[i][j] = 0; // If out of bounds, fill with 0 (for non-square matrices)
                }
            }
        }
        return block;
    }


    public synchronized void addBlock(int[][] block, int rowBlock, int colBlock) {
        int blockSize = block.length;
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                int row = rowBlock * blockSize + i;
                int col = colBlock * blockSize + j;
                if (row < rows && col < cols) { // Ensure the indices are within bounds
                    data[row][col] += block[i][j]; // Add the block value to the corresponding position
                }
            }
        }
    }

    // Метод для отримання блоку, доданий для блочних алгоритмів
    public int[][] getBlockData(int startRow, int startCol, int blockSize) {
        int[][] block = new int[blockSize][blockSize];
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                int row = startRow + i;
                int col = startCol + j;
                if (row < rows && col < cols) {
                    block[i][j] = data[row][col];
                } else {
                    block[i][j] = 0; // Заповнюємо нулями, якщо виходимо за межі
                }
            }
        }
        return block;
    }

    // Метод для встановлення блоку даних у матрицю
    public synchronized void setBlockData(int[][] block, int startRow, int startCol) {
        int blockSize = block.length;
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                int row = startRow + i;
                int col = startCol + j;
                if (row < rows && col < cols) {
                    data[row][col] = block[i][j];
                }
            }
        }
    }

    // Метод для додавання блоку даних до існуючих значень у матриці
    public synchronized void addBlockData(int[][] block, int startRow, int startCol) {
        int blockSize = block.length;
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                int row = startRow + i;
                int col = startCol + j;
                if (row < rows && col < cols) {
                    data[row][col] += block[i][j];
                }
            }
        }
    }
}

class FoxMatrixMultiplication {
    public static ResultMatrix multiply(Matrix matrixA, Matrix matrixB, int threadCount) {
        int matrixSize = matrixA.getRows();
        int blockSize = Math.max(1, matrixSize / (int) Math.sqrt(threadCount));
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        ResultMatrix resultMatrix = new ResultMatrix(matrixA.getRows(), matrixA.getCols());

        for (int rowBlock = 0; rowBlock < matrixSize; rowBlock += blockSize) {
            for (int colBlock = 0; colBlock < matrixSize; colBlock += blockSize) {
                for (int k = 0; k < matrixA.getCols(); k += blockSize) {
                    final int[][] blockA = matrixA.getBlock(rowBlock / blockSize, k / blockSize, blockSize);
                    final int[][] blockB = matrixB.getBlock(k / blockSize, colBlock / blockSize, blockSize);
                    final int blockRow = rowBlock / blockSize;
                    final int blockCol = colBlock / blockSize;

                    executor.execute(() -> {
                        int[][] blockResult = multiplyBlock(blockA, blockB, blockSize);
                        resultMatrix.addBlock(blockResult, blockRow, blockCol);
                    });
                }
            }
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        return resultMatrix;
    }

    private static int[][] multiplyBlock(int[][] blockA, int[][] blockB, int blockSize) {
        int[][] blockResult = new int[blockSize][blockSize];


        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                int sum = 0;
                for (int x = 0; x < blockSize; x++) {
                    sum += blockA[i][x] * blockB[x][j];
                }
                blockResult[i][j] = sum;
            }
        }
        return blockResult;
    }


    public static ResultMatrix multiplyForkJoin(Matrix matrixA, Matrix matrixB, int threadCount) {
        int matrixSize = matrixA.getRows();
        int minBlockSize = 32;
        int blockSize = Math.max(minBlockSize, matrixSize / (int) Math.sqrt(threadCount));
        ForkJoinPool forkJoinPool = new ForkJoinPool(threadCount);
        ResultMatrix resultMatrix = new ResultMatrix(matrixA.getRows(), matrixA.getCols());

        forkJoinPool.invoke(new FoxMultiplyTask(matrixA, matrixB, resultMatrix, blockSize));
        forkJoinPool.shutdown();

        return resultMatrix;
    }

    static class FoxMultiplyTask extends RecursiveAction {
        private final Matrix matrixA;
        private final Matrix matrixB;
        private final ResultMatrix resultMatrix;
        private final int blockSize;

        FoxMultiplyTask(Matrix matrixA, Matrix matrixB, ResultMatrix resultMatrix, int blockSize) {
            this.matrixA = matrixA;
            this.matrixB = matrixB;
            this.resultMatrix = resultMatrix;
            this.blockSize = blockSize;
        }

        @Override
        protected void compute() {
            int matrixSize = matrixA.getRows();
            List<RecursiveAction> subtasks = new ArrayList<>();

            for (int rowBlock = 0; rowBlock < matrixSize; rowBlock += blockSize) {
                for (int colBlock = 0; colBlock < matrixSize; colBlock += blockSize) {
                    for (int k = 0; k < matrixA.getCols(); k += blockSize) {
                        final int blockRow = rowBlock / blockSize;
                        final int blockCol = colBlock / blockSize;
                        final int blockK = k / blockSize;

                        subtasks.add(new RecursiveAction() {
                            @Override
                            protected void compute() {
                                int[][] blockA = matrixA.getBlock(blockRow, blockK, blockSize);
                                int[][] blockB = matrixB.getBlock(blockK, blockCol, blockSize);
                                int[][] blockResult = multiplyBlock(blockA, blockB, blockSize);
                                resultMatrix.addBlock(blockResult, blockRow, blockCol);
                            }
                        });
                    }
                }
            }

            ForkJoinTask.invokeAll(subtasks);
        }


    }
}

class StripedMatrixMultiplication {

    /**
     * Виконує стрічкове паралельне множення матриць A і B.
     * Кожен потік обчислює частину рядків результуючої матриці.
     *
     * @param matrixA Перша матриця.
     * @param matrixB Друга матриця.
     * @param threadCount Кількість потоків для використання.
     * @return Результуюча матриця.
     * @throws InterruptedException Якщо потік перервано під час очікування.
     */
    public static ResultMatrix multiply(Matrix matrixA, Matrix matrixB, int threadCount) throws InterruptedException {
        int rowsA = matrixA.getRows();
        int colsA = matrixA.getCols(); // Також дорівнює rowsB
        int colsB = matrixB.getCols();

        if (colsA != matrixB.getRows()) {
            throw new IllegalArgumentException("Кількість стовпців Matrix A повинна дорівнювати кількості рядків Matrix B.");
        }

        ResultMatrix resultMatrix = new ResultMatrix(rowsA, colsB);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        // Визначаємо кількість рядків, які буде обробляти кожен потік
        int rowsPerThread = (int) Math.ceil((double) rowsA / threadCount);


        for (int i = 0; i < threadCount; i++) {
            final int startRow = i * rowsPerThread;
            final int endRow = Math.min((i + 1) * rowsPerThread, rowsA);

            if (startRow >= rowsA) {
                break; // Якщо рядки вже розподілені
            }

            // Кожен потік обробляє свою "стрічку" матриці A і всю матрицю B
            futures.add(executor.submit(() -> {
                for (int r = startRow; r < endRow; r++) { // Обробляємо рядки матриці A
                    for (int c = 0; c < colsB; c++) {     // Обробляємо стовпці матриці B
                        int sum = 0;
                        for (int k = 0; k < colsA; k++) { // Проходимо по спільній розмірності
                            sum += matrixA.get(r, k) * matrixB.get(k, c);
                        }
                        resultMatrix.set(r, c, sum);
                    }
                }
            }));
        }

        // Очікуємо завершення всіх потоків
        for (Future<?> future : futures) {
            try {
                future.get(); // Блокуємо, доки потік не завершиться
            } catch (ExecutionException e) {
                System.err.println("Помилка виконання потоку: " + e.getCause());
            }
        }

        executor.shutdown();
        return resultMatrix;
    }
}

// --- Новий клас для множення матриць за методом Кеннона ---
// --- Новий клас для множення матриць за методом Кеннона (Виправлена імітація) ---
// Ця імітація буде більш надійною, але все одно є спрощенням справжнього розподіленого алгоритму.
// Вона не виконує фізичного обміну, а повторно обчислює, який блок має бути в даному "віртуальному процесі"
// на даному кроці, що може бути менш ефективним, ніж реальний алгоритм Кеннона з обміном.
class CannonMatrixMultiplication {

    private static int[][] multiplyBlock(int[][] blockA, int[][] blockB) {
        int blockSize = blockA.length;
        int[][] blockResult = new int[blockSize][blockSize];

        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                int sum = 0;
                for (int x = 0; x < blockSize; x++) {
                    sum += blockA[i][x] * blockB[x][j];
                }
                blockResult[i][j] = sum;
            }
        }
        return blockResult;
    }

    public static ResultMatrix multiply(Matrix matrixA, Matrix matrixB, int threadCount) {
        int matrixSize = matrixA.getRows();
        int numBlocksPerSide = (int) Math.sqrt(threadCount);

        // Перевірки
        if (numBlocksPerSide * numBlocksPerSide != threadCount) {
            System.err.println("Попередження: Кількість потоків не є повним квадратом. Метод Кеннона буде працювати неоптимально або некоректно.");
            return null; // Або кинути виняток
        }
        if (matrixSize % numBlocksPerSide != 0) {
            System.err.println("Попередження: Розмір матриці не кратний кореню з кількості потоків. Метод Кеннона може працювати некоректно.");
            return null; // Або кинути виняток
        }
        if (matrixA.getRows() != matrixA.getCols() || matrixB.getRows() != matrixB.getCols() || matrixA.getCols() != matrixB.getRows()) {
            System.err.println("Попередження: Алгоритм Кеннона найкраще працює з квадратними матрицями однакового розміру.");
            return null; // Або кинути виняток
        }

        int blockSize = matrixSize / numBlocksPerSide;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        ResultMatrix resultMatrix = new ResultMatrix(matrixSize, matrixSize);

        List<Callable<Void>> tasks = new ArrayList<>();


        // Створюємо ImmutableMatrix для початкових зміщених блоків
        // Це імітація того, що кожен "процесор" отримує свою початкову пару блоків
        // і далі зсуває їх. Ми не будемо фізично зсувати ці блоки, а повторно звертатися
        // до початкових матриць з коректними індексами, які симулюють зсув.
        // Для кожного "віртуального процесора" (i,j) буде створено своє завдання
        for (int i = 0; i < numBlocksPerSide; i++) {
            for (int j = 0; j < numBlocksPerSide; j++) {
                final int currentProcRow = i;
                final int currentProcCol = j;
                final int startRowC = i * blockSize;
                final int startColC = j * blockSize;

                tasks.add(() -> {
                    int[][] currentResultBlock = new int[blockSize][blockSize];

                    // Імітація початкових зсувів для поточного "процесора"
                    // Блок A для процесора (i,j) на першому кроці: A[i][(j+i)%P]
                    int initialBlockACol = (currentProcCol + currentProcRow) % numBlocksPerSide;
                    int[][] currentBlockA = matrixA.getBlockData(currentProcRow * blockSize, initialBlockACol * blockSize, blockSize);

                    // Блок B для процесора (i,j) на першому кроці: B[(i+j)%P][j]
                    int initialBlockBRow = (currentProcRow + currentProcCol) % numBlocksPerSide;
                    int[][] currentBlockB = matrixB.getBlockData(initialBlockBRow * blockSize, currentProcCol * blockSize, blockSize);

                    for (int k = 0; k < numBlocksPerSide; k++) { // P ітерацій
                        // 1. Множення поточних блоків
                        int[][] multiplied = multiplyBlock(currentBlockA, currentBlockB);

                        // Додаємо результат до накопичувального блоку C
                        for (int r = 0; r < blockSize; r++) {
                            for (int c = 0; c < blockSize; c++) {
                                currentResultBlock[r][c] += multiplied[r][c];
                            }
                        }

                        // 2. Обчислення індексів для наступних зсунутих блоків
                        // Це найважливіша частина для коректної імітації без фізичного обміну
                        // Якщо ми на останній ітерації, нам не потрібно зсувати блоки
                        if (k < numBlocksPerSide - 1) {
                            // Блок A (поточний) зсувається вліво. Його отримує процесор (currentProcRow, (currentProcCol-1+P)%P)
                            // Отже, для поточного процесора (currentProcRow, currentProcCol), його наступний блок A
                            // приходить з (currentProcCol + 1) % numBlocksPerSide
                            int nextBlockACol = (initialBlockACol - 1 + numBlocksPerSide) % numBlocksPerSide; // Зсув вліво

                            // Блок B (поточний) зсувається вгору. Його отримує процесор ((currentProcRow-1+P)%P, currentProcCol)
                            // Отже, для поточного процесора (currentProcRow, currentProcCol), його наступний блок B
                            // приходить з (initialBlockBRow + 1) % numBlocksPerSide
                            int nextBlockBRow = (initialBlockBRow - 1 + numBlocksPerSide) % numBlocksPerSide; // Зсув вгору

                            currentBlockA = matrixA.getBlockData(currentProcRow * blockSize, nextBlockACol * blockSize, blockSize);
                            currentBlockB = matrixB.getBlockData(nextBlockBRow * blockSize, currentProcCol * blockSize, blockSize);


                            // ОНОВЛЕННЯ: Помилка була в тому, що initialBlockACol та initialBlockBRow не оновлювались.
                            // Для імітації, ми повинні імітувати стан "наступного" блоку.
                            // Насправді, це означає, що початкові зміщені індекси повинні бути збережені
                            // і далі від них виконуються циклічні зсуви.
                            // Це складніше, ніж просто recalculate initial shifts.
                            // Давайте переробимо, щоб це було більш коректно, але все ще імітація.
                            // Ідея: на кожній ітерації `k`, блок A, який використовує процесор (i,j) це A[i][(j+i+k)%P]
                            // а блок B це B[(i+j+k)%P][j]. Це більш схоже на модель, де блоки "переміщуються" через індекси.
                            initialBlockACol = (initialBlockACol - 1 + numBlocksPerSide) % numBlocksPerSide;
                            initialBlockBRow = (initialBlockBRow - 1 + numBlocksPerSide) % numBlocksPerSide;
                        }
                    }
                    resultMatrix.setBlockData(currentResultBlock, startRowC, startColC);
                    return null;
                });
            }
        }

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Cannon Matrix Multiplication interrupted: " + e.getMessage());
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                    System.err.println("Executor did not terminate in time for Cannon Matrix Multiplication.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Cannon Matrix Multiplication executor shutdown interrupted: " + e.getMessage());
            }
        }
        return resultMatrix;
    }
}


public class MatrixMultiplicationComparison {
    public static void main(String[] args) {
        int[] matrixSizes = {500, 800};
        int[] threadCounts = {16};

        for (int matrixSize : matrixSizes) {

            Matrix matrixA = Matrix.generate(matrixSize, matrixSize);
            Matrix matrixB = Matrix.generate(matrixSize, matrixSize);

            long startTimeSequential = System.nanoTime();
            ResultMatrix resultSequential = Matrix.multiplySequential(matrixA, matrixB);
            long endTimeSequential = System.nanoTime();
            long durationSequential = (endTimeSequential - startTimeSequential) / 1_000_000;

            for (int threadCount : threadCounts) {
                System.out.println("\nMatrix Size: " + matrixSize + ", Threads: " + threadCount);

                long startTimeExecutor = System.nanoTime();
                ResultMatrix resultExecutor = FoxMatrixMultiplication.multiply(matrixA, matrixB, threadCount);
                long endTimeExecutor = System.nanoTime();
                long durationExecutor = (endTimeExecutor - startTimeExecutor) / 1_000_000;

                long startTimeForkJoin = System.nanoTime();
                ResultMatrix resultForkJoin = FoxMatrixMultiplication.multiplyForkJoin(matrixA, matrixB, threadCount);
                long endTimeForkJoin = System.nanoTime();
                long durationForkJoin = (endTimeForkJoin - startTimeForkJoin) / 1_000_000;

                long startTimeStriped = System.nanoTime();
                ResultMatrix resultStriped = null;
                try {
                    resultStriped = StripedMatrixMultiplication.multiply(matrixA, matrixB, threadCount);
                } catch (InterruptedException e) {
                    System.err.println("Множення перервано: " + e.getMessage());
                }
                long endTimeStriped = System.nanoTime();
                long durationStriped = (endTimeStriped - startTimeStriped) / 1_000_000;


                long startTimeCannon = System.nanoTime();
                ResultMatrix resultCannon = CannonMatrixMultiplication.multiply(matrixA, matrixB, threadCount);
                long endTimeCannon = System.nanoTime();
                long durationCannon = (endTimeCannon - startTimeCannon) / 1_000_000;

                System.out.println("  Sequential time: " + durationSequential + " ms");
                System.out.println("  ExecutorService (Fox) time: " + durationExecutor + " ms");
                System.out.println("  ForkJoinFramework (Fox) time: " + durationForkJoin + " ms");
                System.out.println("  Striped Multiplication time: " + durationStriped + " ms");
                System.out.println("  Cannon's Algorithm time: " + durationCannon + " ms");

                System.out.println("  Speedup Executor (Fox) - Sequential: " + (double) durationSequential / durationExecutor);
                System.out.println("  Speedup ForkJoin (Fox) - Sequential: " + (double) durationSequential / durationForkJoin);
                System.out.println("  Speedup Striped - Sequential: " + (double) durationSequential / durationStriped);
                System.out.println("  Speedup Cannon - Sequential: " + (double) durationSequential / durationCannon);

                if (ResultMatrix.compareMatrices(resultSequential, resultExecutor)) {
                    System.out.println("  Results Executor (Fox) are equal!");
                } else {
                    System.out.println("  Results Executor (Fox) are NOT equal!");
                }
                if (ResultMatrix.compareMatrices(resultSequential, resultForkJoin)) {
                    System.out.println("  Results ForkJoin (Fox) are equal!");
                } else {
                    System.out.println("  Results ForkJoin (Fox) are NOT equal!");
                }
                if (ResultMatrix.compareMatrices(resultSequential, resultStriped)) {
                    System.out.println("  Results Striped are equal!");
                } else {
                    System.out.println("  Results Striped are NOT equal!");
                }
                if (ResultMatrix.compareMatrices(resultSequential, resultCannon)) {
                    System.out.println("  Results Cannon are equal!");
                } else {
                    System.out.println("  Results Cannon are NOT equal!");
                }
            }
        }
    }
}
