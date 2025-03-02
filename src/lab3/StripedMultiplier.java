package lab3;

import java.util.ArrayList;
import java.util.concurrent.*;

public class StripedMultiplier {

    public static Matrix multiplyStriped(Matrix matrix1, Matrix matrix2, int numOfThreads) {
        if (matrix1.getCols() != matrix2.getRows()) {
            throw new IllegalArgumentException("Matrices cannot be multiplied - invalid dimensions");
        }

        Matrix result = new Matrix(matrix1.getRows(), matrix2.getCols());
        Matrix transMatrix2 = matrix2.transpose();

        ExecutorService executor = Executors.newFixedThreadPool(numOfThreads);
        ArrayList<Future<Double>> futures = new ArrayList<>();

        for (int i = 0; i < matrix1.getRows(); i++) {
            for (int j = 0; j < matrix2.getCols(); j++) {
                double[] row = matrix1.extractRow(i);
                double[] col = transMatrix2.extractRow(j); // getRow(j) changed to extractRow(j)
                futures.add(executor.submit(() -> dotProduct(row, col)));
            }
        }
        executor.shutdown();

        try {
            for (int i = 0; i < result.getRows(); i++) {
                for (int j = 0; j < result.getCols(); j++) {
                    result.getData()[i][j] = futures.get(i * result.getCols() + j).get();
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    private static Double dotProduct(double[] row, double[] column) {
        double result = 0;
        for (int k = 0; k < row.length; k++) {
            result += row[k] * column[k];
        }
        return result;
    }
}
