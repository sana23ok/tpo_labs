package lab3;

public class Main {
    public static void main(String[] args) {
        // Визначаємо розміри матриць і кількість потоків для тестування
        int[] sizes = {500, 800, 1000, 2000};
        int[] threadCounts = {4, 16, 64}; // Кількість потоків для паралельного множення

        // Перебираємо різні розміри матриць і кількість потоків
        for (int size : sizes) {
            for (int threads : threadCounts) {
                System.out.println("Тестуємо матрицю розміром: " + size + "x" + size + " з " + threads + " потоками");

                // Створюємо випадкові матриці для тесту
                Matrix m1 = new Matrix(size, size);
                Matrix m2 = new Matrix(size, size);
                m1.fillRandom();
                m2.fillRandom();

                // Стандартне множення (без паралелізму)
                long startTime = System.currentTimeMillis();
                Matrix standardResult = Matrix.multiply(m1, m2);
                long endTime = System.currentTimeMillis();
                System.out.println("Час стандартного множення: " + (endTime - startTime) + " мс");

                // Полосове множення (StripedMultiplier) з використанням потоків
                startTime = System.currentTimeMillis();
                Result stripedResult = StripedMultiplier.multiplyStriped(m1, m2, threads);
                endTime = System.currentTimeMillis();
                System.out.println("Час стрічкового множення: " + (endTime - startTime) + " мс");

                // Множення за алгоритмом Фокса
                int blockSize = size / 4;  // Обираємо розмір блоку для алгоритму Фокса
                startTime = System.currentTimeMillis();
                Result foxResult = FoxMultiplier.multiplyFox(m1, m2, blockSize, threads);
                endTime = System.currentTimeMillis();
                System.out.println("Час множення за алгоритмом Фокса: " + (endTime - startTime) + " мс");

                System.out.println("-----------------------------------------------------");
            }
        }
    }
}

