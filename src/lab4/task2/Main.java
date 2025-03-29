package lab4.task2;

import java.util.concurrent.*;
import java.util.*;

class Matrix {
    private final double[][] data;
    private final int rows, cols;

    public Matrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.data = new double[rows][cols];
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public double[][] getData() { return data; }

    public boolean isEqual(Matrix other) {
        if (this.rows != other.rows || this.cols != other.cols) return false;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (Math.abs(this.data[i][j] - other.data[i][j]) > 1e-6) return false;
            }
        }
        return true;
    }

    public static Matrix[][] splitIntoBlocks(Matrix matrix, int blockSize) {
        int numBlocks = matrix.rows / blockSize;
        Matrix[][] blocks = new Matrix[numBlocks][numBlocks];
        for (int i = 0; i < numBlocks; i++) {
            for (int j = 0; j < numBlocks; j++) {
                blocks[i][j] = new Matrix(blockSize, blockSize);
                for (int k = 0; k < blockSize; k++) {
                    System.arraycopy(matrix.data[i * blockSize + k], j * blockSize, blocks[i][j].data[k], 0, blockSize);
                }
            }
        }
        return blocks;
    }
}

class FoxForkJoin extends RecursiveTask<Matrix[][]> {
    private final Matrix[][] blocksA, blocksB, blocksC;
    private final int blockSize;
    private final int stage;

    public FoxForkJoin(Matrix[][] blocksA, Matrix[][] blocksB, Matrix[][] blocksC, int blockSize, int stage) {
        this.blocksA = blocksA;
        this.blocksB = blocksB;
        this.blocksC = blocksC;
        this.blockSize = blockSize;
        this.stage = stage;
    }

    @Override
    protected Matrix[][] compute() {
        int numBlocks = blocksA.length;
        List<FoxForkJoinBlock> tasks = new ArrayList<>();
        for (int i = 0; i < numBlocks; i++) {
            for (int j = 0; j < numBlocks; j++) {
                int k = (i + stage) % numBlocks;
                tasks.add(new FoxForkJoinBlock(blocksA[i][k], blocksB[k][j], blocksC[i][j], blockSize));
            }
        }
        invokeAll(tasks);
        return blocksC;
    }
}

class FoxForkJoinBlock extends RecursiveAction {
    private final Matrix blockA, blockB, blockC;
    private final int blockSize;

    public FoxForkJoinBlock(Matrix blockA, Matrix blockB, Matrix blockC, int blockSize) {
        this.blockA = blockA;
        this.blockB = blockB;
        this.blockC = blockC;
        this.blockSize = blockSize;
    }

    @Override
    protected void compute() {
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                for (int k = 0; k < blockSize; k++) {
                    blockC.getData()[i][j] += blockA.getData()[i][k] * blockB.getData()[k][j];
                }
            }
        }
    }
}

class FoxExecutorService {
    public static Matrix multiplyFox(Matrix m1, Matrix m2, int blockSize, int numThreads) {
        int n = m1.getRows();
        Matrix[][] blocksM1 = Matrix.splitIntoBlocks(m1, blockSize);
        Matrix[][] blocksM2 = Matrix.splitIntoBlocks(m2, blockSize);
        Matrix[][] blocksC = new Matrix[n / blockSize][n / blockSize];
        for (int i = 0; i < blocksC.length; i++)
            for (int j = 0; j < blocksC.length; j++)
                blocksC[i][j] = new Matrix(blockSize, blockSize);

        ForkJoinPool pool = new ForkJoinPool(numThreads);
        for (int stage = 0; stage < n / blockSize; stage++){
            pool.invoke(new FoxForkJoin(blocksM1, blocksM2, blocksC, blockSize, stage));
        }
        pool.shutdown();
        return combineBlocks(blocksC, n, blockSize);
    }

    private static Matrix combineBlocks(Matrix[][] blocks, int n, int blockSize) {
        Matrix result = new Matrix(n, n);
        double[][] resultData = result.getData();
        for (int i = 0; i < n / blockSize; i++) {
            for (int j = 0; j < n / blockSize; j++) {
                double[][] blockData = blocks[i][j].getData();
                for (int k = 0; k < blockSize; k++) {
                    System.arraycopy(blockData[k], 0, resultData[i * blockSize + k], j * blockSize, blockSize);
                }
            }
        }
        return result;
    }
}

public class Main {
    public static void main(String[] args) {
        int[] sizes = {500, 800, 1000, 2000};
        int[] threadCounts = {4, 16, 64};
        int blockSize = 50;

        for (int size : sizes) {
            for (int numThreads : threadCounts) {
                Matrix m1 = new Matrix(size, size);
                Matrix m2 = new Matrix(size, size);
                fillRandom(m1);
                fillRandom(m2);
                Matrix seq = multiplySequential(m1, m2);

                // ExecutorService approach
                ExecutorService executor = Executors.newFixedThreadPool(numThreads);
                long start = System.nanoTime();
                Matrix result1 = FoxExecutorService.multiplyFox(m1, m2, blockSize, numThreads);
                long end = System.nanoTime();
                executor.shutdown();
                double executorTime = (end - start) / 1e6;
                System.out.printf("Size: %d, Threads: %d, ExecutorService Time: %.2f ms\n", size, numThreads, (end - start) / 1e6);

                if(result1.isEqual(seq)) {
                    System.out.println("Equal");
                }else{
                    System.out.println("Not Equal");
                }

                // ForkJoin approach
                ForkJoinPool forkJoinPool = new ForkJoinPool(numThreads);
                start = System.nanoTime();
                Matrix result2 = FoxExecutorService.multiplyFox(m1, m2, blockSize, numThreads);
                end = System.nanoTime();
                forkJoinPool.shutdown();
                System.out.printf("Size: %d, Threads: %d, ForkJoin Time: %.2f ms\n", size, numThreads, (end - start) / 1e6);
                double forkJoinTime = (end - start) / 1e6;
                if(result2.isEqual(seq)) {
                    System.out.println("Equal");
                }else{
                    System.out.println("Not Equal");
                }

                // Speedup calculation
                double speedup = ((executorTime - forkJoinTime) / executorTime) * 100;
                System.out.printf("ForkJoin is %.2f%% faster than ExecutorService\n", speedup);
            }
        }
    }

    private static void fillRandom(Matrix matrix) {
        for (int i = 0; i < matrix.getRows(); i++) {
            for (int j = 0; j < matrix.getCols(); j++) {
                matrix.getData()[i][j] = Math.random();
            }
        }
    }

    private static Matrix multiplySequential(Matrix m1, Matrix m2) {
        int size = m1.getRows();
        Matrix result = new Matrix(size, size);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    result.getData()[i][j] += m1.getData()[i][k] * m2.getData()[k][j];
                }
            }
        }
        return result;
    }
}