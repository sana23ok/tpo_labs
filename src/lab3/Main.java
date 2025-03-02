package lab3;

public class Main {
    public static void main(String[] args) {
        // Define the matrix sizes and thread counts to test
        int[] sizes = {500, 800, 1000, 2000}; // Matrix sizes (e.g., 500x500, 800x800, etc.)
        int[] threadCounts = {4, 16, 64}; // Number of threads for parallelization

        // Loop through different matrix sizes and thread counts to measure the performance
        for (int size : sizes) {
            for (int threads : threadCounts) {
                System.out.println("Testing Matrix Size: " + size + "x" + size + " with " + threads + " threads");

                // Create random matrices for the test
                Matrix m1 = new Matrix(size, size);
                Matrix m2 = new Matrix(size, size);
                m1.fillRandom();
                m2.fillRandom();

                // Standard Matrix Multiplication (without parallelism)
                long startTime = System.currentTimeMillis();
                Matrix standardResult = Matrix.multiply(m1, m2);
                long endTime = System.currentTimeMillis();
                System.out.println("Standard Multiplication Time: " + (endTime - startTime) + " ms");

                // Striped Matrix Multiplication (using multiple threads)
                startTime = System.currentTimeMillis();
                Matrix stripedResult = StripedMultiplier.multiplyStriped(m1, m2, threads);
                endTime = System.currentTimeMillis();
                System.out.println("Striped Multiplication Time: " + (endTime - startTime) + " ms");

                // Fox Algorithm (Block multiplication using multiple threads)
                int blockSize = size / 4;  // Choose a reasonable block size for Fox algorithm
                startTime = System.currentTimeMillis();
                Result foxResult = FoxMultiplier.multiplyFox(m1, m2, blockSize, threads);
                endTime = System.currentTimeMillis();
                System.out.println("Fox Algorithm Multiplication Time: " + (endTime - startTime) + " ms");

                System.out.println("-----------------------------------------------------");
            }
        }
    }
}
//public class Main {
//    public static void main(String[] args) {
//        System.out.println("-------------------------------------------");
//        // Створення двох матриць (наприклад, 3x2 та 2x4)
//        Matrix m1 = new Matrix(3, 2);  // Матриця 3x2
//        m1.fillRandom();  // Заповнення матриці випадковими значеннями
//
//        Matrix m2 = new Matrix(2, 4);  // Матриця 2x4
//        m2.fillRandom();  // Заповнення матриці випадковими значеннями
//
//        // Виведення матриць для наочності
//        System.out.println("Матриця 1 (3x2):");
//        m1.display();
//
//        System.out.println("\nМатриця 2 (2x4):");
//        m2.display();
//
//        // Виклик методу multiplyStriped для множення матриць
//        int numThreads = 4;  // Кількість потоків
//        Matrix product = StripedMultiplier.multiplyStriped(m1, m2, numThreads);
//
//        // Виведення результату множення
//        System.out.println("\nРезультат множення (3x4):");
//        product.display();
//
//
//        System.out.println("-----------------------------------------------------");
//        int size = 4; // Розмір матриць (має бути кратний blockSize)
//        int blockSize = 2; // Розмір блоку (має бути дільником size)
//        int numTh = 2; // Кількість потоків
//
//        // Створюємо дві матриці
//        Matrix A = new Matrix(size, size);
//        Matrix B = new Matrix(size, size);
//
//        // Заповнюємо випадковими числами
//        A.fillRandom();
//        B.fillRandom();
//
//        // Виводимо початкові матриці
//        System.out.println("Matrix A:");
//        A.display();
//        System.out.println("\nMatrix B:");
//        B.display();
//
//        // Множимо алгоритмом Фокса
//        Result result = FoxMultiplier.multiplyFox(A, B, blockSize, numTh);
//
//        // Виводимо результат
//        System.out.println("\nResult Matrix:");
//        result.display();
//
//    }
//}

