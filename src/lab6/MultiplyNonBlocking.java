package lab6;

import mpi.*;
import java.util.Arrays;

import static lab6.Constants.*;
import static lab6.MatrixOperations.*;

public class MultiplyNonBlocking {

    public static void main(String[] args) throws Exception {
        MPI.Init(args);
        int currentProcessId = MPI.COMM_WORLD.Rank();
        int totalProcesses = MPI.COMM_WORLD.Size();
        int numWorkers = totalProcesses - 1;

        if (numWorkers < 1) {
            System.out.println("Need at least 2 processes. Quitting...");
            MPI.Finalize();
            System.exit(1);
        }

        double[][] matrixA = new double[MATRIX_A_ROWS][MATRIX_A_COLS];
        double[][] matrixB = new double[MATRIX_A_COLS][MATRIX_B_COLS];
        double[][] matrixC = new double[MATRIX_A_ROWS][MATRIX_B_COLS];

        long startTimeInMillis = System.currentTimeMillis();

        if (currentProcessId == MASTER_PROCESS) {
            initializeMatrices(matrixA, matrixB);
            distributeWork(numWorkers, matrixA, matrixB);
            collectResultsFromWorkers(numWorkers, matrixC);

            long endTimeInMillis = System.currentTimeMillis();
            double timeElapsedInSeconds = (endTimeInMillis - startTimeInMillis) / 1000.0;

            System.out.println("MultiplyNonBlocking execution time: " + timeElapsedInSeconds  + " s.");
            //System.out.println("Checking if result is correct....");
            //validateResults(matrixA, matrixB, matrixC);
        } else {
            executeWorkerTask();
        }
        MPI.Finalize();
    }


    private static void distributeWork(int numWorkers, double[][] matrixA, double[][] matrixB) throws MPIException {
        int rowsPerWorker = MATRIX_A_ROWS / numWorkers;
        int remainingRows = MATRIX_A_ROWS % numWorkers;
        int currentRow = 0;

        Request[] metadataRequests = new Request[numWorkers];
        Request[] matrixARequests = new Request[numWorkers];
        Request[] matrixBRequests = new Request[numWorkers];

        for (int dest = 1; dest <= numWorkers; dest++) {
            int rowsForWorker = (dest <= remainingRows) ? rowsPerWorker + 1 : rowsPerWorker;

            metadataRequests[dest - 1] = MPI.COMM_WORLD.Isend(new int[]{currentRow, rowsForWorker}, 0, 2, MPI.INT, dest, DATA_TAG);

            matrixARequests[dest - 1] = MPI.COMM_WORLD.Isend(matrixA, currentRow, rowsForWorker, MPI.OBJECT, dest, DATA_TAG);

            matrixBRequests[dest - 1] = MPI.COMM_WORLD.Isend(matrixB, 0, MATRIX_A_COLS, MPI.OBJECT, dest, DATA_TAG);

            currentRow += rowsForWorker;
        }

        Request.Waitall(metadataRequests);
        Request.Waitall(matrixARequests);
        Request.Waitall(matrixBRequests);

        System.out.println("All tasks distributed");
    }

    private static void collectResultsFromWorkers(int numWorkers, double[][] resultMatrix) throws MPIException {
        int[][] metadata = new int[numWorkers][2];
        Request[] metadataRequests = new Request[numWorkers];

        for (int i = 0; i < numWorkers; i++) {
            metadataRequests[i] = MPI.COMM_WORLD.Irecv(metadata[i], 0, 2, MPI.INT, i + 1, RESULT_TAG);
        }

        Request.Waitall(metadataRequests);

        Request[] resultRequests = new Request[numWorkers];
        for (int i = 0; i < numWorkers; i++) {
            int startRow = metadata[i][0];
            int rowsToReceive = metadata[i][1];
            resultRequests[i] = MPI.COMM_WORLD.Irecv(resultMatrix, startRow, rowsToReceive, MPI.OBJECT, i + 1, RESULT_TAG);
        }

        Request.Waitall(resultRequests);

        System.out.println("All results collected");
    }

    private static void executeWorkerTask() throws MPIException {
        int[] taskMetadata = new int[2];

        Request metadataRequest = MPI.COMM_WORLD.Irecv(taskMetadata, 0, 2, MPI.INT, MASTER_PROCESS, DATA_TAG);
        metadataRequest.Wait();

        int startRow = taskMetadata[0];
        int rowsToProcess = taskMetadata[1];

        double[][] localMatrixA = new double[rowsToProcess][MATRIX_A_COLS];
        double[][] localMatrixB = new double[MATRIX_A_COLS][MATRIX_B_COLS];
        double[][] localMatrixC = new double[rowsToProcess][MATRIX_B_COLS];

        for (double[] row : localMatrixC) {
            Arrays.fill(row, 0.0);
        }

        Request requestA = MPI.COMM_WORLD.Irecv(localMatrixA, 0, rowsToProcess, MPI.OBJECT, MASTER_PROCESS, DATA_TAG);
        Request requestB = MPI.COMM_WORLD.Irecv(localMatrixB, 0, MATRIX_A_COLS, MPI.OBJECT, MASTER_PROCESS, DATA_TAG);

        Request[] requests = {requestA, requestB};
        Request.Waitall(requests);

        performMatrixMultiplication(localMatrixA, localMatrixB, localMatrixC);

        MPI.COMM_WORLD.Send(taskMetadata, 0, 2, MPI.INT, MASTER_PROCESS, RESULT_TAG);
        MPI.COMM_WORLD.Send(localMatrixC, 0, rowsToProcess, MPI.OBJECT, MASTER_PROCESS, RESULT_TAG);
    }
}
