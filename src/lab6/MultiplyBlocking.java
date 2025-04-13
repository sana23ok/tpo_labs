package lab6;

import mpi.*;

import static lab6.Constants.*;
import static lab6.MatrixOperations.*;

public class MultiplyBlocking {

    public static void main(String[] args) throws Exception {
        MPI.Init(args);
        int currentProcessId = MPI.COMM_WORLD.Rank();
        int totalProcesses = MPI.COMM_WORLD.Size();
        int numWorkers = totalProcesses - 1;

        double[][] matrixA = new double[MATRIX_A_ROWS][MATRIX_A_COLS];
        double[][] matrixB = new double[MATRIX_A_COLS][MATRIX_B_COLS];
        double[][] matrixC = new double[MATRIX_A_ROWS][MATRIX_B_COLS];

        long startTime = System.currentTimeMillis();

        if (currentProcessId == MASTER_PROCESS) {
            initializeMatrices(matrixA, matrixB);
            distributeWorkToWorkers(numWorkers, matrixA, matrixB);
            gatherResultsFromWorkers(numWorkers, matrixC);

            long endTime = System.currentTimeMillis();
            double executionTimeInSeconds = (endTime - startTime) / 1000.0;

            System.out.println("MultiplyBlocking execution time: " + executionTimeInSeconds + " s.");
            System.out.println("Checking if result is correct....");
            validateResults(matrixA, matrixB, matrixC);
        } else {
            performWorkerTask();
        }
        MPI.Finalize();
    }

//    private static void initializeMatrices(double[][] matrixA, double[][] matrixB) {
//        int value = 1;
//        for (double[] row : matrixA)
//            Arrays.fill(row, value++);
//        for (double[] row : matrixB)
//            Arrays.fill(row, value++);
//    }

    private static void distributeWorkToWorkers(int numWorkers, double[][] matrixA, double[][] matrixB) {
        int rowsPerWorker = MATRIX_A_ROWS / numWorkers;
        int remainingRows = MATRIX_A_ROWS % numWorkers;
        int startRowIndex = 0;

        for (int workerId = 1; workerId <= numWorkers; workerId++) {
            int rowsForCurrentWorker = (workerId <= remainingRows) ? rowsPerWorker + 1 : rowsPerWorker;
            MPI.COMM_WORLD.Send(new int[]{startRowIndex, rowsForCurrentWorker}, 0, 2, MPI.INT, workerId, DATA_TAG);
            MPI.COMM_WORLD.Send(matrixA, startRowIndex, rowsForCurrentWorker, MPI.OBJECT, workerId, DATA_TAG);
            MPI.COMM_WORLD.Send(matrixB, 0, MATRIX_A_COLS, MPI.OBJECT, workerId, DATA_TAG);
            startRowIndex += rowsForCurrentWorker;
        }
    }

    private static void gatherResultsFromWorkers(int numWorkers, double[][] resultMatrix) {
        for (int workerId = 1; workerId <= numWorkers; workerId++) {
            int[] resultMetadata = new int[2];
            MPI.COMM_WORLD.Recv(resultMetadata, 0, 2, MPI.INT, workerId, RESULT_TAG);
            MPI.COMM_WORLD.Recv(resultMatrix, resultMetadata[0], resultMetadata[1], MPI.OBJECT, workerId, RESULT_TAG);
            System.out.println("Received results from process " + workerId);
        }
    }

    private static void performWorkerTask() {
        int[] taskMetadata = new int[2];
        MPI.COMM_WORLD.Recv(taskMetadata, 0, 2, MPI.INT, MASTER_PROCESS, DATA_TAG);
        int startRowIndex = taskMetadata[0], rowsToProcess = taskMetadata[1];

        double[][] localMatrixA = new double[rowsToProcess][MATRIX_A_COLS];
        double[][] localMatrixB = new double[MATRIX_A_COLS][MATRIX_B_COLS];
        double[][] localMatrixC = new double[rowsToProcess][MATRIX_B_COLS];

        MPI.COMM_WORLD.Recv(localMatrixA, 0, rowsToProcess, MPI.OBJECT, MASTER_PROCESS, DATA_TAG);
        MPI.COMM_WORLD.Recv(localMatrixB, 0, MATRIX_A_COLS, MPI.OBJECT, MASTER_PROCESS, DATA_TAG);

        performMatrixMultiplication(localMatrixA, localMatrixB, localMatrixC);

        MPI.COMM_WORLD.Send(taskMetadata, 0, 2, MPI.INT, MASTER_PROCESS, RESULT_TAG);
        MPI.COMM_WORLD.Send(localMatrixC, 0, rowsToProcess, MPI.OBJECT, MASTER_PROCESS, RESULT_TAG);
    }

//    private static void performMatrixMultiplication(double[][] matrixA, double[][] matrixB, double[][] resultMatrix) {
//        for (int i = 0; i < matrixA.length; i++) {
//            for (int j = 0; j < matrixB[0].length; j++) {
//                for (int k = 0; k < matrixB.length; k++) {
//                    resultMatrix[i][j] += matrixA[i][k] * matrixB[k][j];
//                }
//            }
//        }
//    }
//
//    private static void checkResults(double[][] matrixA, double[][] matrixB, double[][] resultMatrix) {
//        double[][] expectedResult = new double[matrixA.length][matrixB[0].length];
//        performMatrixMultiplication(matrixA, matrixB, expectedResult);
//
//        for (int i = 0; i < matrixA.length; i++) {
//            if (!Arrays.equals(resultMatrix[i], expectedResult[i])) {
//                throw new RuntimeException("Matrix multiplication result is incorrect!");
//            }
//        }
//        System.out.println("Matrix multiplication result is correct!");
//    }
}

