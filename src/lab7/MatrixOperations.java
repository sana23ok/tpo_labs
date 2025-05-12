package lab7;

import java.util.Arrays;
import java.util.Random;

public class MatrixOperations {

    public static double[][] multiplySequential(double[][] a, double[][] b) {
        int rowsA = a.length;
        int colsA = a[0].length;
        int rowsB = b.length;
        int colsB = b[0].length;

        if (colsA != rowsB) {
            throw new IllegalArgumentException("Number of columns in the first matrix must equal the number of rows in the second matrix.");
        }

        double[][] result = new double[rowsA][colsB];
        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                for (int k = 0; k < colsA; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return result;
    }

    public static double[][] createRandomMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        Random random = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextDouble();
            }
        }
        return matrix;
    }

    public static boolean equals(double[][] a, double[][] b) {
        if (a.length != b.length || (a.length > 0 && a[0].length != b[0].length)) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (!Arrays.equals(a[i], b[i])) {
                return false;
            }
        }
        return true;
    }

    public static double[] flatten(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[] flat = new double[rows * cols];
        int index = 0;
        for (double[] row : matrix) {
            for (double val : row) {
                flat[index++] = val;
            }
        }
        return flat;
    }

    public static double[][] fromFlattened(double[] flat, int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = flat[i * cols + j];
            }
        }
        return matrix;
    }

    public static double[][] getSubmatrix(double[][] matrix, int startRow, int endRow, int startCol, int endCol) {
        int rows = endRow - startRow;
        int cols = endCol - startCol;
        double[][] subMatrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(matrix[startRow + i], startCol, subMatrix[i], 0, cols);
        }
        return subMatrix;
    }
}