package lab6;

import mpi.*;

import static lab6.Constants.*;
import static lab6.MatrixOperations.*;

public class MultiplyNonBlocking {

    public static void main(String[] args) throws Exception {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        int workers = size - 1;

        if (workers < 1) {
            System.out.println("Need at least 2 processes. Quitting...");
            MPI.Finalize();
            return;
        }

        double[][] A = new double[MATRIX_A_ROWS][MATRIX_A_COLS];
        double[][] B = new double[MATRIX_A_COLS][MATRIX_B_COLS];
        double[][] C = new double[MATRIX_A_ROWS][MATRIX_B_COLS];

        long start = System.currentTimeMillis();

        if (rank == MASTER_PROCESS) {
            fill(A, B);
            distributeNonBlocking(workers, A, B);
            receiveResultsNonBlocking(workers, C);

            long end = System.currentTimeMillis();
            System.out.println("MultiplyNonBlocking execution time: " + (end - start) / 1000.0 + " s.");
        } else {
            computeWorkerNonBlocking(); // Обчислення на стороні воркера
        }

        MPI.Finalize(); // Завершення MPI
    }

    // Неблокуючий розподіл даних до воркерів
    private static void distributeNonBlocking(int workers, double[][] A, double[][] B) throws MPIException {
        int base = MATRIX_A_ROWS / workers;
        int extra = MATRIX_A_ROWS % workers;
        int start = 0;

        Request[] metaReq = new Request[workers];
        Request[] aReq = new Request[workers];
        Request[] bReq = new Request[workers];

        for (int i = 0; i < workers; i++) {
            int rows = (i + 1 <= extra) ? base + 1 : base;

            metaReq[i] = MPI.COMM_WORLD.Isend(new int[]{start, rows}, 0, 2, MPI.INT, i + 1, DATA_TAG);
            aReq[i] = MPI.COMM_WORLD.Isend(A, start, rows, MPI.OBJECT, i + 1, DATA_TAG);
            bReq[i] = MPI.COMM_WORLD.Isend(B, 0, MATRIX_A_COLS, MPI.OBJECT, i + 1, DATA_TAG);

            start += rows;
        }

        Request.Waitall(metaReq); // Очікуємо, поки всі мета-дані будуть надіслані
        Request.Waitall(aReq);    // Очікуємо надсилання частин A
        Request.Waitall(bReq);    // Очікуємо надсилання B

        System.out.println("All tasks distributed");
    }

    // Неблокуючий прийом результатів від воркерів
    private static void receiveResultsNonBlocking(int workers, double[][] C) throws MPIException {
        int[][] metadata = new int[workers][2];
        Request[] metaReq = new Request[workers];

        for (int i = 0; i < workers; i++) {
            metaReq[i] = MPI.COMM_WORLD.Irecv(metadata[i], 0, 2, MPI.INT, i + 1, RESULT_TAG);
        }

        Request.Waitall(metaReq); // Очікуємо мета-дані

        Request[] resultReq = new Request[workers];
        for (int i = 0; i < workers; i++) {
            int startRow = metadata[i][0];
            int numRows = metadata[i][1];
            resultReq[i] = MPI.COMM_WORLD.Irecv(C, startRow, numRows, MPI.OBJECT, i + 1, RESULT_TAG);
        }

        Request.Waitall(resultReq); // Очікуємо всі частини результату
        System.out.println("All results collected");
    }

    // Неблокуюче обчислення на стороні воркера
    private static void computeWorkerNonBlocking() throws MPIException {
        int[] task = new int[2];

        MPI.COMM_WORLD.Irecv(task, 0, 2, MPI.INT, MASTER_PROCESS, DATA_TAG).Wait();

        int start = task[0], rows = task[1];

        double[][] partA = new double[rows][MATRIX_A_COLS];
        double[][] partB = new double[MATRIX_A_COLS][MATRIX_B_COLS];
        double[][] partC = new double[rows][MATRIX_B_COLS];

        Request aReq = MPI.COMM_WORLD.Irecv(partA, 0, rows, MPI.OBJECT, MASTER_PROCESS, DATA_TAG);
        Request bReq = MPI.COMM_WORLD.Irecv(partB, 0, MATRIX_A_COLS, MPI.OBJECT, MASTER_PROCESS, DATA_TAG);

        Request.Waitall(new Request[]{aReq, bReq});

        multiply(partA, partB, partC);

        MPI.COMM_WORLD.Send(task, 0, 2, MPI.INT, MASTER_PROCESS, RESULT_TAG);
        MPI.COMM_WORLD.Send(partC, 0, rows, MPI.OBJECT, MASTER_PROCESS, RESULT_TAG);
    }
}

