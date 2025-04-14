package lab6;

import mpi.*;

import static lab6.Constants.*;
import static lab6.MatrixOperations.*;

public class MultiplyBlocking {

    public static void main(String[] args) throws Exception {
        MPI.Init(args); // Ініціалізація MPI

        int currentProcessId = MPI.COMM_WORLD.Rank();// Отримання поточного процесу (рангу)
        int totalProcesses = MPI.COMM_WORLD.Size();// Загальна кількість процесів
        int numWorkers = totalProcesses - 1;// Кількість воркерів (крім майстра)

        double[][] matrixA = new double[MATRIX_A_ROWS][MATRIX_A_COLS];
        double[][] matrixB = new double[MATRIX_A_COLS][MATRIX_B_COLS];
        double[][] matrixC = new double[MATRIX_A_ROWS][MATRIX_B_COLS];

        long startTime = System.currentTimeMillis();

        if (isMaster(currentProcessId)) {
            fill(matrixA, matrixB);
            distributeWorkBlocking(numWorkers, matrixA, matrixB);
            collectResultsBlocking(numWorkers, matrixC);

            long endTime = System.currentTimeMillis();
            double executionTimeInSeconds = (endTime - startTime) / 1000.0;
            System.out.println("MultiplyBlocking execution time: " + executionTimeInSeconds + " s.");
        } else {
            computeWorkerBlocking();
        }

        MPI.Finalize(); // Завершення MPI
    }

    // Перевірка, чи процес є головним
    private static boolean isMaster(int processId) {
        return processId == MASTER_PROCESS;
    }

    private static void distributeWorkBlocking(int numWorkers, double[][] matrixA, double[][] matrixB) {
        int baseRows = MATRIX_A_ROWS / numWorkers;
        int extraRows = MATRIX_A_ROWS % numWorkers;

        int start = 0;

        for (int worker = 1; worker <= numWorkers; worker++) {
            int rowsToSend = (worker <= extraRows) ? baseRows + 1 : baseRows;

            // meta date
            MPI.COMM_WORLD.Send(new int[]{start, rowsToSend}, 0, 2, MPI.INT, worker, DATA_TAG);

            // rows A
            MPI.COMM_WORLD.Send(matrixA, start, rowsToSend, MPI.OBJECT, worker, DATA_TAG);

            // B
            MPI.COMM_WORLD.Send(matrixB, 0, MATRIX_A_COLS, MPI.OBJECT, worker, DATA_TAG);

            start += rowsToSend;
        }
    }

    private static void collectResultsBlocking(int numWorkers, double[][] matrixC) {
        for (int worker = 1; worker <= numWorkers; worker++) {
            int[] metadata = new int[2];

            MPI.COMM_WORLD.Recv(metadata, 0, 2, MPI.INT, worker, RESULT_TAG);
            // get parts of result
            MPI.COMM_WORLD.Recv(matrixC, metadata[0], metadata[1], MPI.OBJECT, worker, RESULT_TAG);

            System.out.println("Received results from process " + worker);
        }
    }

    private static void computeWorkerBlocking() {
        int[] metadata = new int[2];
        MPI.COMM_WORLD.Recv(metadata, 0, 2, MPI.INT, MASTER_PROCESS, DATA_TAG);

        int startRow = metadata[0];
        int rowsToCompute = metadata[1];

        double[][] partA = new double[rowsToCompute][MATRIX_A_COLS];
        double[][] partB = new double[MATRIX_A_COLS][MATRIX_B_COLS];
        double[][] partC = new double[rowsToCompute][MATRIX_B_COLS];


        MPI.COMM_WORLD.Recv(partA, 0, rowsToCompute, MPI.OBJECT, MASTER_PROCESS, DATA_TAG);
        MPI.COMM_WORLD.Recv(partB, 0, MATRIX_A_COLS, MPI.OBJECT, MASTER_PROCESS, DATA_TAG);

        multiply(partA, partB, partC);

        MPI.COMM_WORLD.Send(metadata, 0, 2, MPI.INT, MASTER_PROCESS, RESULT_TAG);
        MPI.COMM_WORLD.Send(partC, 0, rowsToCompute, MPI.OBJECT, MASTER_PROCESS, RESULT_TAG);
    }
}


