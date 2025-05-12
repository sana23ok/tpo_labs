package lab7;

import mpi.*;
//MPJ_HOME=C:\mpj
//-jar C:/mpj\lib\starter.jar -np 4 -dev multicore lab7.CollectiveMpi

public class ParallelMatrixMultiplier {
    static final int ROOT = 0;

    public static void main(String[] args) throws MPIException {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        int n = 1000; // Розмір матриць

        compareCollectiveMethods(rank, size, n);

        MPI.Finalize();
    }

    private static void compareCollectiveMethods(int rank, int size, int n) throws MPIException {
        double[][] a = null;
        double[][] b = null;
        double[][] resultBcast = null;
        double[][] resultScatterGather = null;
        double[][] resultAllgather = null;

        if (rank == ROOT) {
            a = MatrixOperations.createRandomMatrix(n, n);
            b = MatrixOperations.createRandomMatrix(n, n);
        }

        double timeSequential = 0;
        if (rank == ROOT) {
            long start = System.nanoTime();
            MatrixOperations.multiplySequential(a, b);
            long end = System.nanoTime();
            timeSequential = (end - start) / 1e9;
            System.out.printf("Sequential time: %.3f с%n", timeSequential);
        }

        // Bcast
        double timeBcast;
        long startBcast = System.nanoTime();
        resultBcast = multiplyBcast(rank, size, a, b, n);
        long endBcast = System.nanoTime();
        timeBcast = (endBcast - startBcast) / 1e9;

        // Scatter/Gather
        double timeScatterGather;
        long startScatterGather = System.nanoTime();
        resultScatterGather = multiplyScatterGather(rank, size, a, b, n);
        long endScatterGather = System.nanoTime();
        timeScatterGather = (endScatterGather - startScatterGather) / 1e9;

        // Allgather
        double timeAllgather;
        long startAllgather = System.nanoTime();
        resultAllgather = multiplyAllgather(rank, size, a, b, n);
        long endAllgather = System.nanoTime();
        timeAllgather = (endAllgather - startAllgather) / 1e9;

        if (rank == ROOT) {
            System.out.println("\nSize: " + n + ", Number of processes: " + size);
            System.out.printf("Bcast Time:        %.3f s, Speedup: %.2f%n", timeBcast, timeSequential / timeBcast);
            System.out.printf("Scatter/Gather Time: %.3f s, Speedup: %.2f%n", timeScatterGather, timeSequential / timeScatterGather);
            System.out.printf("Allgather Time:      %.3f s, Speedup: %.2f%n", timeAllgather, timeSequential / timeAllgather);

            // Validation (only for one of the parallel results)
            double[][] sequentialResult = MatrixOperations.multiplySequential(a, b);
            System.out.println("\nValidation:");
            System.out.println("Bcast result correct:        " + MatrixOperations.equals(sequentialResult, resultBcast));
            System.out.println("Scatter/Gather result correct: " + MatrixOperations.equals(sequentialResult, resultScatterGather));
            System.out.println("Allgather result correct:      " + MatrixOperations.equals(sequentialResult, resultAllgather));
        }
    }

    // 2. Реалізація алгоритму паралельного множення матриць з використанням
    // колективних методів обміну повідомленнями.

    // Метод з використанням MPI.Bcast для розсилання матриці B
    private static double[][] multiplyBcast(int rank, int size, double[][] a, double[][] b, int n) throws MPIException {
        double[][] localA = null;
        double[][] broadcastB = new double[n][n];
        double[] flatB = null;

        int rowsPerProcess = n / size;
        int remainder = n % size;
        int[] sendCounts = new int[size];
        int[] displacements = new int[size];
        int offset = 0;
        for (int i = 0; i < size; i++) {
            sendCounts[i] = (i < remainder) ? (rowsPerProcess + 1) * n : rowsPerProcess * n;
            displacements[i] = offset;
            offset += sendCounts[i];
        }

        double[] recvBuf = new double[sendCounts[rank]];
        double[] flatA = null;

        if (rank == ROOT) {
            if (a == null) {
                System.err.println("Error: Matrix 'a' is null on ROOT process.");
                return null;
            }
            flatA = MatrixOperations.flatten(a);
            if (b == null) {
                System.err.println("Error: Matrix 'b' is null on ROOT process.");
                return null;
            }
            flatB = MatrixOperations.flatten(b);
        }

        MPI.COMM_WORLD.Scatterv(flatA, 0, sendCounts, displacements, MPI.DOUBLE,
                recvBuf, 0, sendCounts[rank], MPI.DOUBLE, ROOT);

        int localRows = sendCounts[rank] / n;
        localA = MatrixOperations.fromFlattened(recvBuf, localRows, n);

        MPI.COMM_WORLD.Bcast(flatB, 0, n * n, MPI.DOUBLE, ROOT);
        broadcastB = MatrixOperations.fromFlattened(flatB, n, n);

        double[][] localC = MatrixOperations.multiplySequential(localA, broadcastB);
        double[] flatLocalC = MatrixOperations.flatten(localC);
        double[] flatC = null;

        if (rank == ROOT) {
            flatC = new double[n * n];
        }

        MPI.COMM_WORLD.Gatherv(flatLocalC, 0, flatLocalC.length, MPI.DOUBLE,
                flatC, 0, sendCounts, displacements, MPI.DOUBLE, ROOT);

        return (rank == ROOT) ? MatrixOperations.fromFlattened(flatC, n, n) : null;
    }

    // Метод з використанням MPI.Scatter та MPI.Gather
    private static double[][] multiplyScatterGather(int rank, int size, double[][] a, double[][] b, int n) throws MPIException {
        int rowsPerProcess = n / size;
        int remainder = n % size;
        int[] sendCounts = new int[size];
        int[] sendDispls = new int[size];
        int[] recvCounts = new int[size];
        int[] recvDispls = new int[size];
        int sendOffset = 0;
        int recvOffset = 0;

        for (int i = 0; i < size; i++) {
            int rows = (i < remainder) ? rowsPerProcess + 1 : rowsPerProcess;
            sendCounts[i] = rows * n;
            sendDispls[i] = sendOffset;
            sendOffset += sendCounts[i];
            recvCounts[i] = rows * n;
            recvDispls[i] = recvOffset;
            recvOffset += recvCounts[i];
        }

        double[] localARows = new double[sendCounts[rank]];
        double[] flatA = null;
        double[] flatB = null;

        if (rank == ROOT) {
            flatA = MatrixOperations.flatten(a);
            flatB = MatrixOperations.flatten(b);
        }

        MPI.COMM_WORLD.Scatterv(flatA, 0, sendCounts, sendDispls, MPI.DOUBLE,
                localARows, 0, sendCounts[rank], MPI.DOUBLE, ROOT);

        double[][] localA = MatrixOperations.fromFlattened(localARows, sendCounts[rank] / n, n);
        double[][] broadcastB = new double[n][n];
        MPI.COMM_WORLD.Bcast(flatB, 0, n * n, MPI.DOUBLE, ROOT);
        broadcastB = MatrixOperations.fromFlattened(flatB, n, n);

        double[][] localC = MatrixOperations.multiplySequential(localA, broadcastB);
        double[] flatLocalC = MatrixOperations.flatten(localC);
        double[] flatC = null;

        if (rank == ROOT) {
            flatC = new double[n * n];
        }

        MPI.COMM_WORLD.Gatherv(flatLocalC, 0, flatLocalC.length, MPI.DOUBLE,
                flatC, 0, recvCounts, recvDispls, MPI.DOUBLE, ROOT);

        return (rank == ROOT) ? MatrixOperations.fromFlattened(flatC, n, n) : null;
    }

    // Метод з використанням MPI.Allgather
    private static double[][] multiplyAllgather(int rank, int size, double[][] a, double[][] b, int n) throws MPIException {
        int rowsPerProcess = n / size;
        int remainder = n % size;
        int[] sendCounts = new int[size];
        int[] sendDispls = new int[size];
        int offset = 0;
        for (int i = 0; i < size; i++) {
            sendCounts[i] = (i < remainder) ? (rowsPerProcess + 1) : rowsPerProcess;
            sendDispls[i] = offset;
            offset += sendCounts[i];
        }

        double[] localARow = new double[sendCounts[rank] * n];
        double[] flatA = null;

        if (rank == ROOT) {
            flatA = MatrixOperations.flatten(a);
        }

        MPI.COMM_WORLD.Scatterv(flatA, 0, sendCounts, sendDispls, MPI.DOUBLE,
                localARow, 0, sendCounts[rank] * n, MPI.DOUBLE, ROOT);

        double[][] localA = MatrixOperations.fromFlattened(localARow, sendCounts[rank], n);

        double[][] broadcastB = new double[n][n];
        MPI.COMM_WORLD.Bcast(MatrixOperations.flatten(b), 0, n * n, MPI.DOUBLE, ROOT);
        broadcastB = MatrixOperations.fromFlattened(MatrixOperations.flatten(b), n, n);

        double[][] localC = MatrixOperations.multiplySequential(localA, broadcastB);
        double[] flatLocalC = MatrixOperations.flatten(localC);

        int[] allSendCounts = new int[size];
        for (int i = 0; i < size; i++) {
            allSendCounts[i] = sendCounts[i] * n;
        }
        double[] allResults = new double[n * n];
        MPI.COMM_WORLD.Allgatherv(flatLocalC, 0, flatLocalC.length, MPI.DOUBLE,
                allResults, 0, allSendCounts, sendDispls, MPI.DOUBLE);

        return MatrixOperations.fromFlattened(allResults, n, n);
    }
}
