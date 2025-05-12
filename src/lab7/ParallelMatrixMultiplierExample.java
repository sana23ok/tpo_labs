package lab7;

import mpi.MPI;
//MPJ_HOME=C:\mpj
//-jar C:/mpj\lib\starter.jar -np 4 -dev multicore lab7.CollectiveMpi

public class ParallelMatrixMultiplierExample {
    private static final int MATRIX_A_ROWS = 1000;
    private static final int MATRIX_A_COLS = 1000;
    private static final int MATRIX_B_COLS = 1000;
    private static final int MASTER_RANK = 0;

    public static void main(String[] args) throws Exception {
        // Ініціалізація MPI середовища
        MPI.Init(args);
        int currentProcessId = MPI.COMM_WORLD.Rank();
        int totalProcessesCount = MPI.COMM_WORLD.Size();

        // Розподіл рядків матриці A між процесами
        int[] rowsDistribution = calculateRowsDistribution(totalProcessesCount, MATRIX_A_ROWS);
        int[] rowDisplacements = calculateRowDisplacements(rowsDistribution);
        int localRows = rowsDistribution[currentProcessId];

        // Ініціалізація локальних та глобальних матриць
        double[][] matrixA = null;
        double[][] matrixB = new double[MATRIX_A_COLS][MATRIX_B_COLS];
        double[][] localSubMatrixA = new double[localRows][MATRIX_A_COLS];
        double[][] localResultMatrix = new double[localRows][MATRIX_B_COLS];
        double[][] finalResultMatrix = null;

        // Замірювання часу виконання на головному процесі
        long startTime = 0;
        if (currentProcessId == MASTER_RANK) {
            matrixA = initializeMatrix(MATRIX_A_ROWS, MATRIX_A_COLS);
            initializeMatrix(matrixB, MATRIX_A_COLS, MATRIX_B_COLS);
            startTime = System.currentTimeMillis();
        }

        // Розсилка матриці B всім процесам
        // MPI_Bcast: Один процес (MASTER_RANK) надсилає матрицю B всім іншим процесам.
        MPI.COMM_WORLD.Bcast(matrixB, 0, MATRIX_A_COLS * MATRIX_B_COLS, MPI.DOUBLE, MASTER_RANK);

        // Розподіл рядків матриці A між процесами
        // MPI_Scatterv: Головний процес розподіляє різні частини матриці A (рядки) між усіма процесами.
        // sendCounts: масив, що вказує кількість елементів для відправки кожному процесу.
        // displacements: масив, що вказує зміщення у вихідному буфері для кожного процесу.
        MPI.COMM_WORLD.Scatterv(
                (currentProcessId == MASTER_RANK) ? flattenMatrix(matrixA) : null, 0, rowsDistribution, rowDisplacements, MPI.DOUBLE,
                flattenMatrix(localSubMatrixA), 0, localRows * MATRIX_A_COLS, MPI.DOUBLE, MASTER_RANK
        );

        // Множення локальних підматриць
        multiplyLocalMatrices(localSubMatrixA, matrixB, localResultMatrix);

        // Збір результатів (рядків матриці C) на головному процесі
        // MPI_Gatherv: Кожен процес надсилає свою локальну частину результуючої матриці на головний процес.
        // recvCounts: масив, що вказує кількість елементів, які очікуються від кожного процесу.
        // displacements: масив, що вказує зміщення у буфері приймання для даних від кожного процесу.
        if (currentProcessId == MASTER_RANK) {
            finalResultMatrix = new double[MATRIX_A_ROWS][MATRIX_B_COLS];
        }
        MPI.COMM_WORLD.Gatherv(
                flattenMatrix(localResultMatrix), 0, localRows * MATRIX_B_COLS, MPI.DOUBLE,
                (currentProcessId == MASTER_RANK) ? flattenMatrix(finalResultMatrix) : null, 0, rowsDistribution, rowDisplacements, MPI.DOUBLE, MASTER_RANK
        );

        // Завершення обчислень та валідація на головному процесі
        if (currentProcessId == MASTER_RANK) {
            long endTime = System.currentTimeMillis();
            double executionTime = (endTime - startTime) / 1000.0;

            System.out.println("Загальний час виконання: " + executionTime + " секунд");
            System.out.println("Обчислення завершено. Перевірка результатів...");
            validateMultiplicationResult(matrixA, matrixB, finalResultMatrix);
        }

        // Завершення MPI середовища
        MPI.Finalize();
    }

    // Функція для ініціалізації матриці випадковими значеннями
    private static double[][] initializeMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextDouble() * 10; // Заповнення випадковими числами від 0 до 10
            }
        }
        return matrix;
    }

    // Перевантажена функція для ініціалізації існуючої матриці випадковими значеннями
    private static void initializeMatrix(double[][] matrix, int rows, int cols) {
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextDouble() * 10;
            }
        }
    }

    // Функція для розрахунку розподілу рядків між процесами
    private static int[] calculateRowsDistribution(int totalProcesses, int totalRows) {
        int baseRows = totalRows / totalProcesses;
        int remainingRows = totalRows % totalProcesses;
        int[] distribution = new int[totalProcesses];
        for (int i = 0; i < totalProcesses; i++) {
            distribution[i] = baseRows + (i < remainingRows ? 1 : 0);
        }
        return distribution;
    }

    // Функція для розрахунку зміщень рядків для Scatterv та Gatherv
    private static int[] calculateRowDisplacements(int[] distribution) {
        int[] displacements = new int[distribution.length];
        int offset = 0;
        for (int i = 0; i < distribution.length; i++) {
            displacements[i] = offset;
            offset += distribution[i];
        }
        return displacements;
    }

    // Функція для множення локальних підматриць
    private static void multiplyLocalMatrices(double[][] localA, double[][] matrixB, double[][] localC) {
        int rowsA = localA.length;
        int colsA = localA[0].length;
        int colsB = matrixB[0].length;
        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                localC[i][j] = 0;
                for (int k = 0; k < colsA; k++) {
                    localC[i][j] += localA[i][k] * matrixB[k][j];
                }
            }
        }
    }

    // Функція для валідації результату множення матриць
    private static void validateMultiplicationResult(double[][] matrixA, double[][] matrixB, double[][] matrixC) {
        int rowsA = matrixA.length;
        int colsA = matrixA[0].length;
        int colsB = matrixB[0].length;
        double[][] expectedResult = new double[rowsA][colsB];
        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                expectedResult[i][j] = 0;
                for (int k = 0; k < colsA; k++) {
                    expectedResult[i][j] += matrixA[i][k] * matrixB[k][j];
                }
            }
        }

        boolean isCorrect = true;
        double tolerance = 1e-9;
        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                if (Math.abs(matrixC[i][j] - expectedResult[i][j]) > tolerance) {
                    System.out.println("Помилка в елементі [" + i + "][" + j + "]: Отримано " + matrixC[i][j] + ", Очікувано " + expectedResult[i][j]);
                    isCorrect = false;
                    break;
                }
            }
            if (!isCorrect) break;
        }

        if (isCorrect) {
            System.out.println("Валідація пройдена успішно!");
        } else {
            System.out.println("Валідація виявила помилки!");
        }
    }

    // Допоміжна функція для згортання двовимірного масиву в одновимірний
    private static double[] flattenMatrix(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[] flattened = new double[rows * cols];
        int index = 0;
        for (double[] row : matrix) {
            for (double element : row) {
                flattened[index++] = element;
            }
        }
        return flattened;
    }
}
