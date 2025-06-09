package practice;

import mpi.MPI;
import java.util.Arrays;
//MPJ_HOME=C:\mpj
//-jar C:/mpj\lib\starter.jar -np 4 -dev multicore lab7.CollectiveMpi
public class ParallelMatrixRowAvg {

    public static void main(String[] args) throws Exception {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        int n = 4; // Розмір матриць (n x n)
        double[][] A = null;
        double[][] B = null;

        // Кількість рядків на процес
        int rowsPerProcess = n / size;

        // Локальні блоки для кожного процесу
        double[] localA = new double[rowsPerProcess * n];
        double[] localB = new double[rowsPerProcess * n];
        double[] localC = new double[rowsPerProcess];

        if (rank == 0) {
            // Ініціалізація матриць A і B
            A = new double[n][n];
            B = new double[n][n];

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    A[i][j] = i + j + 1;
                    B[i][j] = (i + 1) * (j + 1);
                }
            }
            System.out.println("Matrix A:");
            printMatrix(A);
            System.out.println("Matrix B:");
            printMatrix(B);
        }

        // Плоскі масиви для передачі
        double[] flatA = new double[n * n];
        double[] flatB = new double[n * n];

        if (rank == 0) {
            // Розплющення
            for (int i = 0; i < n; i++) {
                System.arraycopy(A[i], 0, flatA, i * n, n);
                System.arraycopy(B[i], 0, flatB, i * n, n);
            }
        }

        // Розсилка частин матриць
        MPI.COMM_WORLD.Scatter(flatA, 0, rowsPerProcess * n, MPI.DOUBLE, localA, 0, rowsPerProcess * n, MPI.DOUBLE, 0);
        MPI.COMM_WORLD.Scatter(flatB, 0, rowsPerProcess * n, MPI.DOUBLE, localB, 0, rowsPerProcess * n, MPI.DOUBLE, 0);

        // Обчислення локальних елементів C
        for (int i = 0; i < rowsPerProcess; i++) {
            double sumA = 0.0, sumB = 0.0;
            for (int j = 0; j < n; j++) {
                sumA += localA[i * n + j];
                sumB += localB[i * n + j];
            }
            localC[i] = (sumA / n) * (sumB / n);
        }

        // Збір результатів
        double[] C = new double[n];
        MPI.COMM_WORLD.Gather(localC, 0, rowsPerProcess, MPI.DOUBLE, C, 0, rowsPerProcess, MPI.DOUBLE, 0);

        // Вивід результату
        if (rank == 0) {
            System.out.println("Result array C:");
            System.out.println(Arrays.toString(C));
        }

        MPI.Finalize();
    }

    private static void printMatrix(double[][] matrix) {
        for (double[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }
}

