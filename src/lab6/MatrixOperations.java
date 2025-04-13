package lab6;

import java.util.Arrays;

public class MatrixOperations {

    public static void initializeMatrices(double[][] matrixA, double[][] matrixB) {
        int value = 1;
        for (double[] row : matrixA)
            Arrays.fill(row, value++);
        for (double[] row : matrixB)
            Arrays.fill(row, value++);
    }

    public static void performMatrixMultiplication(double[][] matrixA, double[][] matrixB, double[][] resultMatrix) {
        for (int i = 0; i < matrixA.length; i++) {
            for (int j = 0; j < matrixB[0].length; j++) {
                for (int k = 0; k < matrixB.length; k++) {
                    resultMatrix[i][j] += matrixA[i][k] * matrixB[k][j];
                }
            }
        }
    }

    public static void validateResults(double[][] matrixA, double[][] matrixB, double[][] resultMatrix) {
        double[][] expectedMatrix = new double[matrixA.length][matrixB[0].length];
        performMatrixMultiplication(matrixA, matrixB, expectedMatrix);

        for (int i = 0; i < matrixA.length; i++) {
            if (!Arrays.equals(resultMatrix[i], expectedMatrix[i])) {
                throw new RuntimeException("Matrix multiplication result is incorrect!");
            }
        }
        System.out.println("Matrix multiplication result is correct!");
    }
}
