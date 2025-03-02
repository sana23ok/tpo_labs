package lab3;


public class Matrix {
    private double[][] data;
    private int rows;
    private int cols;

    public Matrix(int rows, int cols) {
        this.data = new double[rows][cols];
        this.rows = rows;
        this.cols = cols;
    }

    public Matrix(double[][] data) {
        this.data = data;
        this.rows = data.length;
        this.cols = data[0].length;
    }

    public void fillRandom() {
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                this.data[i][j] = Math.random();
            }
        }
    }

    public void display() {
        for (double[] row : this.data) {
            for (double value : row) {
                System.out.printf("%10.4f", value);
            }
            System.out.println();
        }
    }

    public Matrix transpose() {
        Matrix transposed = new Matrix(this.cols, this.rows);
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                transposed.data[j][i] = this.data[i][j];
            }
        }
        return transposed;
    }

    public static Matrix add(Matrix m1, Matrix m2) {
        Matrix sum = new Matrix(m1.rows, m1.cols);
        for (int i = 0; i < sum.rows; i++) {
            for (int j = 0; j < sum.cols; j++) {
                sum.data[i][j] = m1.data[i][j] + m2.data[i][j];
            }
        }
        return sum;
    }

    // Множення двох матриць
    public static Matrix multiply(Matrix m1, Matrix m2) {
        Matrix product = new Matrix(m1.rows, m2.cols);
        for (int i = 0; i < product.rows; i++) {
            for (int j = 0; j < product.cols; j++) {
                for (int k = 0; k < m1.cols; k++) {
                    product.data[i][j] += m1.data[i][k] * m2.data[k][j];
                }
            }
        }
        return product;
    }

    // Витягування рядка матриці
    public double[] extractRow(int rowIndex) {
        return this.data[rowIndex];
    }

    // Витягування стовпця матриці
    public double[] extractColumn(int colIndex) {
        double[] column = new double[this.rows];
        for (int i = 0; i < this.rows; i++) {
            column[i] = this.data[i][colIndex];
        }
        return column;
    }

    // Розбиття матриці на блоки заданого розміру
    public static Matrix[][] splitIntoBlocks(Matrix matrix, int blockSize) {
        int blocksPerRow = matrix.rows / blockSize;
        Matrix[][] blocks = new Matrix[blocksPerRow][blocksPerRow];
        for (int i = 0; i < blocksPerRow; i++) {
            for (int j = 0; j < blocksPerRow; j++) {
                blocks[i][j] = new Matrix(blockSize, blockSize);
                for (int k = 0; k < blockSize; k++) {
                    for (int l = 0; l < blockSize; l++) {
                        blocks[i][j].data[k][l] = matrix.data[i * blockSize + k][j * blockSize + l];
                    }
                }
            }
        }
        return blocks;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public double[][] getData() {
        return data;
    }

}
