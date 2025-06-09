package practice;

/*
З використанням колективних методів обміну повідомленнями MPJ Express напишіть фрагмент коду,
який виконує розсилку фрагментів масиву А дійсних чисел в процеси-воркери, виконує в процесах-воркерах
пошук чисел, що належать інтервалу (a,b), у переданих масивах та отримує в процесі-майстрі знайдені
значення.
 */

//MPJ_HOME=C:\mpj
//-jar C:/mpj\lib\starter.jar -np 4 -dev multicore lab7.CollectiveMpi

import mpi.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MPJScatterGather {
    public static void main(String[] args) throws MPIException {
        // Initialize MPJ Express
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank(); // Get the rank (ID) of the current process
        int size = MPI.COMM_WORLD.Size(); // Get the total number of processes

        double a = 12.0; // Lower bound of the interval
        double b = 18.0; // Upper bound of the interval
        int N;

        if (rank == 0) { // Code for Master process (rank 0)
            N = 100; // Size of the main array
            double[] fullArray = new double[N];

            // Populate the main array A with random double numbers for demonstration
            for (int i = 0; i < N; i++) {
                fullArray[i] = Math.random() * 30.0; // Numbers from 0 to 30
            }
            System.out.println("Master: Initializing full array:");
            System.out.println(Arrays.toString(fullArray));

            // Determine the fragment size for each worker.
            // To handle remainders, the last worker might receive a slightly larger fragment.
            int chunkSize = N / size;
            int[] sendCounts = new int[size]; // Number of elements to send to each process
            int[] displacements = new int[size]; // Displacement for each process

            for (int i = 0; i < size; i++) {
                sendCounts[i] = chunkSize;
                if (i == size - 1) { // The last process receives the remainder
                    sendCounts[i] += N % size;
                }
                displacements[i] = (i > 0) ? (displacements[i - 1] + sendCounts[i - 1]) : 0;
            }

            // Send N to all processes
            int[] nBuffer = new int[]{N};
            MPI.COMM_WORLD.Bcast(nBuffer, 0, 1, MPI.INT, 0);

            // Buffer to receive results from workers.
            // This can be more complex if workers return ArrayLists of varying sizes.
            // For simplicity, we will collect found elements as a single list on the master.

            // 1. Distribute array A fragments using MPI_Scatterv
            // MPI_Scatterv (sendbuf, sendcounts, displs, sendtype, recvbuf, recvcount, recvtype, root, comm)
            double[] recvBuffer = new double[sendCounts[0]]; // Buffer for the master, as it also "receives" its fragment

            MPI.COMM_WORLD.Scatterv(fullArray, 0, sendCounts, displacements, MPI.DOUBLE, // Send parameters
                    recvBuffer, 0, sendCounts[0], MPI.DOUBLE,          // Receive parameters for the current process
                    0);                                                // Rank of the root process (master)

            // Master also processes its fragment
            List<Double> masterFound = searchInterval(recvBuffer, a, b);
            System.out.println("Master (rank " + rank + "): Found numbers in its fragment " + masterFound);


            // 2. Collect results from workers
            // Since each worker might find a different number of elements,
            // we cannot use MPI_Gatherv without prior information about sizes.
            // It's better to use separate Send/Recv or MPI_Allgather with a prior Send of sizes.
            // For this example, each worker will send its list, and the master will receive.

            List<Double> allFoundNumbers = new ArrayList<>(masterFound); // Add own found numbers

            for (int i = 1; i < size; i++) { // Receive results from each worker, except self (rank 0)
                // First, receive the size of the list to be sent
                int[] countBuffer = new int[1];
                MPI.COMM_WORLD.Recv(countBuffer, 0, 1, MPI.INT, i, 0); // Expect size

                if (countBuffer[0] > 0) {
                    double[] workerResults = new double[countBuffer[0]];
                    MPI.COMM_WORLD.Recv(workerResults, 0, countBuffer[0], MPI.DOUBLE, i, 1); // Expect the list itself
                    for (double val : workerResults) {
                        allFoundNumbers.add(val);
                    }
                }
            }

            System.out.println("\nMaster: All numbers found in interval (" + a + ", " + b + "):");
            System.out.println(allFoundNumbers);

        } else { // Code for Worker processes (rank > 0)
            // Receive N from the master
            int[] nBuffer = new int[1];
            MPI.COMM_WORLD.Bcast(nBuffer, 0, 1, MPI.INT, 0);
            N = nBuffer[0]; // Now N is known to the worker

            // Determine the size of the buffer for receiving the fragment
            // MPI_Scatterv will automatically determine recvcount based on the master's sendCounts.
            double[] localArray = new double[chunkSizeForWorker(rank, size, N)]; // Buffer size for receiving the fragment

            MPI.COMM_WORLD.Scatterv(null, 0, null, null, MPI.DOUBLE, // Send parameters (null for workers)
                    localArray, 0, localArray.length, MPI.DOUBLE,    // Receive parameters for the current process
                    0);                                              // Rank of the root process (master)

            System.out.println("Worker (rank " + rank + "): Received fragment: " + Arrays.toString(localArray));

            // 3. Search for numbers within the interval (a,b)
            List<Double> foundNumbers = searchInterval(localArray, a, b);
            System.out.println("Worker (rank " + rank + "): Found numbers: " + foundNumbers);

            // 4. Send found values to the master process
            // First, send the size of the list
            int[] countToSend = new int[]{foundNumbers.size()};
            MPI.COMM_WORLD.Send(countToSend, 0, 1, MPI.INT, 0, 0); // Tag 0 for size

            if (foundNumbers.size() > 0) {
                double[] resultsArray = foundNumbers.stream().mapToDouble(Double::doubleValue).toArray();
                MPI.COMM_WORLD.Send(resultsArray, 0, resultsArray.length, MPI.DOUBLE, 0, 1); // Tag 1 for data
            }
        }

        // Finalize MPJ Express
        MPI.Finalize();
    }

    // Helper function to search for numbers within the interval
    private static List<Double> searchInterval(double[] array, double a, double b) {
        List<Double> found = new ArrayList<>();
        for (double val : array) {
            if (val > a && val < b) { // Strict interval (a,b)
                found.add(val);
            }
        }
        return found;
    }

    // Helper function to determine the fragment size for a worker
    // This must be consistent with the master's logic for sendCounts
    private static int chunkSizeForWorker(int rank, int size, int N) {
        int chunkSize = N / size;
        if (rank == size - 1) {
            chunkSize += N % size;
        }
        return chunkSize;
    }
}