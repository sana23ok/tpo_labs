package lab3;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        // Створення двох матриць 4x4
        Matrix matrix1 = new Matrix(4, 4);
        Matrix matrix2 = new Matrix(4, 4);

        // Заповнення матриць випадковими значеннями
        matrix1.fillRandom();
        matrix2.fillRandom();

        // Виведення вихідних матриць
        System.out.println("Matrix 1:");
        matrix1.display();
        System.out.println("\nMatrix 2:");
        matrix2.display();

        // Додавання двох матриць
        Matrix sumMatrix = Matrix.add(matrix1, matrix2);
        System.out.println("\nSum of Matrix 1 and Matrix 2:");
        sumMatrix.display();

        // Множення двох матриць
        Matrix productMatrix = Matrix.multiply(matrix1, matrix2);
        System.out.println("\nProduct of Matrix 1 and Matrix 2:");
        productMatrix.display();

        // Транспонування першої матриці
        Matrix transposedMatrix = matrix1.transpose();
        System.out.println("\nTransposed Matrix 1:");
        transposedMatrix.display();

        // Витягування рядка та стовпця
        double[] row = matrix1.extractRow(1);
        double[] column = matrix1.extractColumn(1);
        System.out.println("\nExtracted Row 1: " + Arrays.toString(row));
        System.out.println("Extracted Column 1: " + Arrays.toString(column));

        if (matrix1.getRows() % 2 == 0 && matrix1.getCols() % 2 == 0) {
            Matrix[][] blocks = Matrix.splitIntoBlocks(matrix1, 2);
            System.out.println("\nFirst Block of Matrix 1:");
            blocks[0][0].display();
        } else {
            System.out.println("\nMatrix 1 cannot be evenly split into 2x2 blocks.");
        }

        System.out.println("-------------------------------------------");
        // Створення двох матриць (наприклад, 3x2 та 2x4)
        Matrix m1 = new Matrix(3, 2);  // Матриця 3x2
        m1.fillRandom();  // Заповнення матриці випадковими значеннями

        Matrix m2 = new Matrix(2, 4);  // Матриця 2x4
        m2.fillRandom();  // Заповнення матриці випадковими значеннями

        // Виведення матриць для наочності
        System.out.println("Матриця 1 (3x2):");
        m1.display();

        System.out.println("\nМатриця 2 (2x4):");
        m2.display();

        // Виклик методу multiplyStriped для множення матриць
        int numThreads = 4;  // Кількість потоків
        Matrix product = StripedMultiplier.multiplyStriped(m1, m2, numThreads);

        // Виведення результату множення
        System.out.println("\nРезультат множення (3x4):");
        product.display();


        System.out.println("-----------------------------------------------------");
        int size = 4; // Розмір матриць (має бути кратний blockSize)
        int blockSize = 2; // Розмір блоку (має бути дільником size)
        int numTh = 2; // Кількість потоків

        // Створюємо дві матриці
        Matrix A = new Matrix(size, size);
        Matrix B = new Matrix(size, size);

        // Заповнюємо випадковими числами
        A.fillRandom();
        B.fillRandom();

        // Виводимо початкові матриці
        System.out.println("Matrix A:");
        A.display();
        System.out.println("\nMatrix B:");
        B.display();

        // Множимо алгоритмом Фокса
        Result result = FoxMultiplier.multiplyFox(A, B, blockSize, numTh);

        // Виводимо результат
        System.out.println("\nResult Matrix:");
        result.display();

    }
}

