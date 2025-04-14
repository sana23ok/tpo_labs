package lab6;

import java.util.Arrays;

public class MatrixOperations {

    public static void fill(double[][] A, double[][] B) {
        int value = 1;
        for (double[] row : A)
            Arrays.fill(row, value++);
        for (double[] row : B)
            Arrays.fill(row, value++);
    }

    public static void multiply(double[][] A, double[][] B, double[][] res) {
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < B[0].length; j++) {
                for (int k = 0; k < B.length; k++) {
                    res[i][j] += A[i][k] * B[k][j];
                }
            }
        }
    }

    public static void validateResults(double[][] A, double[][] B, double[][] res) {
        double[][] expectedMatrix = new double[A.length][B[0].length];
        multiply(A, B, expectedMatrix);

        for (int i = 0; i < A.length; i++) {
            if (!Arrays.equals(res[i], expectedMatrix[i])) {
                throw new RuntimeException("Matrix multiplication result is incorrect!");
            }
        }
        System.out.println("Matrix multiplication result is correct!");
    }
}
