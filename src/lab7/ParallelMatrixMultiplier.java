package lab7;

import mpi.*;
//MPJ_HOME=C:\mpj
//-jar C:/mpj\lib\starter.jar -np 4 -dev multicore lab7.CollectiveMpi

public class ParallelMatrixMultiplier {
    static final int ROOT = 0;
    static final int TAG = 1;

    public static void main(String[] args) throws MPIException {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        int n = 1000; // Matrix size

        compareCollectiveMethods(rank, size, n);

        MPI.Finalize();
    }

    private static void compareCollectiveMethods(int rank, int size, int n) throws MPIException {
        double[][] a = null;
        double[][] b = null;
        double[][] resultBcast = null;
        double[][] resultGather = null;
        double[][] resultAlltoall = null;
        double[][] resultMixed = null;
        double[][] resultSequential = null;

        if (rank == ROOT) {
            a = MatrixOperations.createRandomMatrix(n, n);
            b = MatrixOperations.createRandomMatrix(n, n);
        }

        // Sequential
        double timeSequential = 0;
        if (rank == ROOT) {
            long start = System.nanoTime();
            resultSequential = MatrixOperations.multiplySequential(a, b);
            long end = System.nanoTime();
            timeSequential = (end - start) / 1e9;
            System.out.printf("Sequential Time: %.3f s%n", timeSequential);
        }

        // Bcast (one-to-many for B)
        double timeBcast;
        long startBcast = System.nanoTime();
        resultBcast = multiplyBcastCollective(rank, size, a, b, n);
        long endBcast = System.nanoTime();
        timeBcast = (endBcast - startBcast) / 1e9;

        // Gather (many-to-one for result)
        double timeGather;
        long startGather = System.nanoTime();
        resultGather = multiplyGatherCollective(rank, size, a, b, n);
        long endGather = System.nanoTime();
        timeGather = (endGather - startGather) / 1e9;

        // Alltoall (many-to-many)
        double timeAlltoall;
        long startAlltoall = System.nanoTime();
        resultAlltoall = multiplyAlltoallCollective(rank, size, a, b, n);
        long endAlltoall = System.nanoTime();
        timeAlltoall = (endAlltoall - startAlltoall) / 1e9;

        // Mixed (combination of Scatter/Bcast + Gatherv)
        double timeMixed;
        long startMixed = System.nanoTime();
        resultMixed = multiplyMixedCollective(rank, size, a, b, n);
        long endMixed = System.nanoTime();
        timeMixed = (endMixed - startMixed) / 1e9;

        // Report
        if (rank == ROOT) {
            System.out.println("\nSize: " + n + ", Processes: " + size);
            System.out.printf("\nSequential Time:                          %.3f s%n", timeSequential);
            System.out.printf("One to many (Bcast):                      %.3f s, Speedup: %.2f%n", timeBcast, timeSequential / timeBcast);
            System.out.printf("Many to one (Gather):                     %.3f s, Speedup: %.2f%n", timeGather, timeSequential / timeGather);
            System.out.printf("Many to many (Allgatherv):                %.3f s, Speedup: %.2f%n", timeAlltoall, timeSequential / timeAlltoall);
            System.out.printf("Mixed (Scatterv, Bcast, Gatherv):         %.3f s, Speedup: %.2f%n", timeMixed, timeSequential / timeMixed);

            System.out.println("\n\nValidation:");
            System.out.println("Bcast correct:        " + MatrixOperations.equals(resultSequential, resultBcast));
            System.out.println("Gather correct:       " + MatrixOperations.equals(resultSequential, resultGather));
            System.out.println("Alltoall correct:     " + MatrixOperations.equals(resultSequential, resultAlltoall));
            System.out.println("Mixed correct:        " + MatrixOperations.equals(resultSequential, resultMixed));
        }
    }

    // Bcast (one-to-many for B)
    private static double[][] multiplyBcastCollective(int rank, int size, double[][] localA, double[][] b, int n) throws MPIException {
        double[] flatB = new double[n * n];
        double[] flatA = new double[n * n];
        double[][] broadcastA = null;

        if (rank == ROOT) {
            flatB = MatrixOperations.flatten(b);
            flatA = MatrixOperations.flatten(localA);
            broadcastA = localA;
        }

        MPI.COMM_WORLD.Bcast(flatB, 0, flatB.length, MPI.DOUBLE, ROOT);
        double[][] broadcastB = MatrixOperations.fromFlattened(flatB, n, n);

        MPI.COMM_WORLD.Bcast(flatA, 0, flatA.length, MPI.DOUBLE, ROOT);
        broadcastA = (broadcastA == null) ? MatrixOperations.fromFlattened(flatA, n, n) : broadcastA;

        int rowsPerProcess = n / size;
        int extraRows = n % size;
        int startRow = rank * rowsPerProcess + Math.min(rank, extraRows);
        int localRows = rowsPerProcess + (rank < extraRows ? 1 : 0);

        double[][] localResult = null;
        if (localRows > 0) {
            double[][] partA = MatrixOperations.getSubmatrix(broadcastA, startRow, startRow + localRows, 0, n);
            localResult = MatrixOperations.multiplySequential(partA, broadcastB);
        }
        double[] localCflat = (localResult != null) ? MatrixOperations.flatten(localResult) : new double[0];
        int localSizeC = localCflat.length;

        double[] resultFlat = null;

        if (rank == ROOT) {
            int[] recvSizes = new int[size];
            recvSizes[ROOT] = localSizeC;

            // Receive sizes from other processes manually
            for (int srcRank = 1; srcRank < size; srcRank++) {
                int[] recvSizeBuf = new int[1];
                MPI.COMM_WORLD.Recv(recvSizeBuf, 0, 1, MPI.INT, srcRank, TAG);
                recvSizes[srcRank] = recvSizeBuf[0];
            }

            resultFlat = new double[n * n];
            double[][] receivedData = new double[size][];
            Request[] recvRequests = new Request[size - 1];
            int[] displacements = new int[size];
            int currentDisplacement = 0;

            // Store local data
            receivedData[ROOT] = localCflat;
            displacements[ROOT] = 0;
            currentDisplacement += recvSizes[ROOT];

            // Setup asynchronous receives
            for (int srcRank = 1; srcRank < size; srcRank++) {
                receivedData[srcRank] = new double[recvSizes[srcRank]];
                recvRequests[srcRank - 1] = MPI.COMM_WORLD.Irecv(receivedData[srcRank], 0, recvSizes[srcRank], MPI.DOUBLE, srcRank, TAG);
                displacements[srcRank] = currentDisplacement;
                currentDisplacement += recvSizes[srcRank];
            }

            Request.Waitall(recvRequests);

            // Combine all parts into one result
            for (int i = 0; i < size; i++) {
                System.arraycopy(receivedData[i], 0, resultFlat, displacements[i], receivedData[i].length);
            }

            return MatrixOperations.fromFlattened(resultFlat, n, n);
        } else {
            // First send your size
            MPI.COMM_WORLD.Send(new int[]{localSizeC}, 0, 1, MPI.INT, ROOT, TAG);

            // Then send your data
            MPI.COMM_WORLD.Send(localCflat, 0, localSizeC, MPI.DOUBLE, ROOT, TAG);
            return null;
        }
    }

    // Gather (many-to-one for result)
    private static double[][] multiplyGatherCollective(int rank, int size, double[][] localA, double[][] b, int n) throws MPIException {
        double[] flatB = new double[n * n];
        double[] flatA = new double[n * n];
        double[][] broadcastA = null;

        if (rank == ROOT) {
            flatB = MatrixOperations.flatten(b);
            flatA = MatrixOperations.flatten(localA);
            broadcastA = localA;

            // Send arrays to other processes
            for (int dest = 1; dest < size; dest++) {
                MPI.COMM_WORLD.Send(flatB, 0, flatB.length, MPI.DOUBLE, dest, 0);
                MPI.COMM_WORLD.Send(flatA, 0, flatA.length, MPI.DOUBLE, dest, 1);
            }
        } else {
            // Other processes receive flatB and flatA
            MPI.COMM_WORLD.Recv(flatB, 0, flatB.length, MPI.DOUBLE, ROOT, 0);
            MPI.COMM_WORLD.Recv(flatA, 0, flatA.length, MPI.DOUBLE, ROOT, 1);
        }

        double[][] broadcastB = MatrixOperations.fromFlattened(flatB, n, n);
        broadcastA = (broadcastA == null) ? MatrixOperations.fromFlattened(flatA, n, n) : broadcastA;

        int rowsPerProcess = n / size;
        int extraRows = n % size;
        int startRow = rank * rowsPerProcess + Math.min(rank, extraRows);
        int localRows = rowsPerProcess + (rank < extraRows ? 1 : 0);

        double[][] localResult = null;
        if (localRows > 0) {
            double[][] partA = MatrixOperations.getSubmatrix(broadcastA, startRow, startRow + localRows, 0, n);
            localResult = MatrixOperations.multiplySequential(partA, broadcastB);
        }
        double[] localCflat = (localResult != null) ? MatrixOperations.flatten(localResult) : new double[0];

        double[] resultFlat = null;
        int[] sendCounts = new int[size];
        int[] displs = new int[size];
        int displacement = 0;
        for (int i = 0; i < size; i++) {
            int r = n / size + (i < extraRows ? 1 : 0);
            sendCounts[i] = r * n;
            displs[i] = displacement;
            displacement += sendCounts[i];
        }
        if (rank == ROOT) {
            resultFlat = new double[n * n];
        }
        MPI.COMM_WORLD.Gatherv(localCflat, 0, localCflat.length, MPI.DOUBLE,
                resultFlat, 0, sendCounts, displs, MPI.DOUBLE, ROOT);
        return rank == ROOT ? MatrixOperations.fromFlattened(resultFlat, n, n) : null;
    }

    // Alltoall (many-to-many)
    private static double[][] multiplyAlltoallCollective(int rank, int size, double[][] a, double[][] b, int n) throws MPIException {
        int rowsPerProcess = n / size;
        int extraRows = n % size;

        int[] sendCountsA = new int[size];
        int[] sendDisplsA = new int[size];

        int sOffset = 0;
        for (int i = 0; i < size; ++i) {
            int rows = (i < extraRows) ? rowsPerProcess + 1 : rowsPerProcess;
            sendCountsA[i] = rows * n;
            sendDisplsA[i] = sOffset;
            sOffset += sendCountsA[i];
        }

        double[] localAflat = new double[sendCountsA[rank]];
        MPI.COMM_WORLD.Scatterv(rank == ROOT ? MatrixOperations.flatten(a) : null, 0, sendCountsA, sendDisplsA, MPI.DOUBLE,
                localAflat, 0, sendCountsA[rank], MPI.DOUBLE, ROOT);
        double[][] localA = MatrixOperations.fromFlattened(localAflat, sendCountsA[rank] / n, n);

        // Each process gets the full matrix B
        double[] flatB = new double[n * n];
        if (rank == ROOT) {
            flatB = MatrixOperations.flatten(b);
        }
        MPI.COMM_WORLD.Bcast(flatB, 0, flatB.length, MPI.DOUBLE, ROOT);
        double[][] fullB = MatrixOperations.fromFlattened(flatB, n, n);

        // Calculate the local part of the resulting matrix C
        double[][] localC = MatrixOperations.multiplySequential(localA, fullB);
        double[] localCflat = MatrixOperations.flatten(localC);

        int localSizeC = localCflat.length;

        int[] allSendCountsC = new int[size];
        MPI.COMM_WORLD.Allgather(new int[]{localSizeC}, 0, 1, MPI.INT, allSendCountsC, 0, 1, MPI.INT);

        int[] allDisplsC = new int[size];
        int displ = 0;
        for (int i = 0; i < size; ++i) {
            allDisplsC[i] = displ;
            displ += allSendCountsC[i];
        }

        double[] allResultsFlat = new double[n * n];
        MPI.COMM_WORLD.Allgatherv(localCflat, 0, localSizeC, MPI.DOUBLE,
                allResultsFlat, 0,allSendCountsC, allDisplsC, MPI.DOUBLE);

        return MatrixOperations.fromFlattened(allResultsFlat, n, n);
    }

    // Mixed (combination of Scatterv/Bcast + Gatherv)
    private static double[][] multiplyMixedCollective(int rank, int size, double[][] localA, double[][] b, int n) throws MPIException {
        int rowsPerProcess = n / size;
        int extra = n % size;
        int[] sendCountsA = new int[size];
        int[] sendDisplsA = new int[size];
        int sOffset = 0;
        for (int i = 0; i < size; i++) {
            int rows = rowsPerProcess + (i < extra ? 1 : 0);
            sendCountsA[i] = rows * n;
            sendDisplsA[i] = sOffset;
            sOffset += sendCountsA[i];
        }
        double[] localAflat = new double[sendCountsA[rank]];
        double[] flatB = new double[n * n];
        double[] flatFullA = new double[n * n];

        if (rank == ROOT) {
            flatB = MatrixOperations.flatten(b);
            flatFullA = MatrixOperations.flatten(localA);
        }
        MPI.COMM_WORLD.Scatterv(flatFullA, 0, sendCountsA, sendDisplsA, MPI.DOUBLE,
                localAflat, 0, sendCountsA[rank], MPI.DOUBLE, ROOT);

        MPI.COMM_WORLD.Bcast(flatB, 0, flatB.length, MPI.DOUBLE, ROOT);
        double[][] broadcastB = MatrixOperations.fromFlattened(flatB, n, n);
        double[][] localAPart = MatrixOperations.fromFlattened(localAflat, sendCountsA[rank] / n, n);
        double[][] localC = MatrixOperations.multiplySequential(localAPart, broadcastB);
        double[] localCflat = MatrixOperations.flatten(localC);

        double[] resultFlat = null;
        if (rank == ROOT) {
            resultFlat = new double[n * n];
        }
        MPI.COMM_WORLD.Gatherv(localCflat, 0, localCflat.length, MPI.DOUBLE,
                resultFlat, 0, sendCountsA, sendDisplsA, MPI.DOUBLE, ROOT);

        return rank == ROOT ? MatrixOperations.fromFlattened(resultFlat, n, n) : null;
    }
}