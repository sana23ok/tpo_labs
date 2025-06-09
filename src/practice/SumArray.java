package practice;

import mpi.MPI;
/*
Напишіть реалізацію модифікованого каскадного алгоритму сумування елементів масиву з використанням
колективних методів MPJ Express
 */

public class SumArray{

    public static void main(String[] args) throws Exception {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        int arraySize = 100000; // Збільшимо розмір для кращої демонстрації паралелізму
        int[] globalArray = null;
        int[] localArray;
        long localSum = 0;
        long finalSum = 0; // Змінено на finalSum для коректної назви

        if (rank == 0) {
            globalArray = new int[arraySize];
            for (int i = 0; i < arraySize; i++) {
                globalArray[i] = i + 1;
            }
            System.out.println("Root process: Generated array of size " + arraySize);
        }

        // Перевірка на достатню кількість процесів для схеми
        // Наприклад, для 16 елементів потрібно 16 процесів для початкових обчислень.
        // Але ми розбиваємо масив на `size` частин, тож це нормально.
        if (size == 0 || (arraySize % size != 0 && rank == 0)) {
            System.err.println("Warning: Array size not perfectly divisible by number of processes. Some processes might get slightly more or less elements.");
        }

        // Розподіл елементів. Scatter автоматично обробляє, якщо arraySize % size != 0,
        // розподіляючи залишок першим процесам.
        // Проте для простоти ми будемо використовувати elementsPerProcess,
        // і припускатимемо, що arraySize ділиться на size для ідеального розподілу.
        int elementsPerProcess = arraySize / size;
        int remainingElements = arraySize % size;

        int actualLocalElements = elementsPerProcess;
        if (rank < remainingElements) {
            actualLocalElements++; // Ці процеси отримують на один елемент більше
        }

        localArray = new int[actualLocalElements];

        // Використовуємо Scatterv для більш гнучкого розподілу, якщо arraySize % size != 0
        // або залишаємо Scatter, якщо впевнені, що arraySize % size == 0.
        // Для простоти, щоб не ускладнювати сильно, продовжимо з Scatter, але матимемо на увазі його обмеження
        // якщо arraySize не ділиться на size без залишку.
        // Для точної реалізації Scatterv, нам потрібні count та displacements масиви.
        // Залишимо поточний Scatter і додамо перевірку на arraySize % size != 0.
        // Якщо arraySize % size != 0, це може призвести до помилки, тому краще його уникнути
        // або перейти на Scatterv. Для відповідності "схемі" (що зазвичай працює з 2^k елементами),
        // краще мати arraySize кратним size.

        // Якщо arraySize не ділиться на size без залишку, Scatter може не працювати належним чином.
        // Тому для демонстрації каскаду краще припустити, що arraySize % size == 0
        // або використовувати Scatterv. Для цього прикладу, ми будемо вважати, що arraySize % size == 0.
        // Якщо ви хочете обробити arraySize % size != 0 коректно, вам знадобиться MPI.Scatterv.

        // Для спрощення і відповідності візуальній схемі (яка зазвичай припускає 2^k елементів
        // і 2^k процесів, або n елементів і n/log2n процесів), будемо вважати
        // що arraySize ділиться на size.
        // Якщо arraySize % size != 0, то це призведе до некоректного розподілу з Scatter.
        if (rank == 0 && arraySize % size != 0) {
            System.err.println("ERROR: For this simplified cascade implementation with MPI.Scatter, arraySize must be divisible by size. Please adjust arraySize or size.");
            MPI.Finalize();
            return; // Завершуємо роботу
        }

        MPI.COMM_WORLD.Scatter(globalArray, 0, elementsPerProcess, MPI.INT,
                localArray, 0, elementsPerProcess, MPI.INT,
                0);

        for (int i = 0; i < elementsPerProcess; i++) {
            localSum += localArray[i];
        }

        // System.out.println("Process " + rank + ": Local sum = " + localSum);

        // --- Модифікована каскадна схема сумування ---
        // Кожен процес має свою localSum.
        // Ми імітуємо дерево редукції.
        long currentSum = localSum; // Сума, яка буде передаватися або отримуватися на кожному кроці
        long[] receivedSum = new long[1];

        // Кількість кроків для редукції - log2(size)
        // Для прикладу, якщо size = 8:
        // Крок 0: p0+p1, p2+p3, p4+p5, p6+p7
        // Крок 1: (p0+p1)+(p2+p3), (p4+p5)+(p6+p7)
        // Крок 2: ((p0+p1)+(p2+p3))+((p4+p5)+(p6+p7))

        // На кожному кроці `step` ми об'єднуємося з процесом, який знаходиться на відстані `2^step`.
        for (int step = 0; step < Math.log(size) / Math.log(2); step++) {
            int partner = rank ^ (1 << step); // Обчислення рангу партнера за допомогою XOR
            // (1 << step) це 2^step

            // Якщо rank < partner, то поточний процес відправляє свою суму партнеру
            // і перестає бути активним на цьому кроці.
            // Якщо rank > partner, то поточний процес отримує суму від партнера
            // і додає її до своєї.
            if (rank < partner) { // Процес з меншим рангом надсилає
                if (rank + (1 << step) < size) { // Перевірка, щоб не вийти за межі розміру
                    // System.out.println("Process " + rank + " sending " + currentSum + " to " + partner);
                    MPI.COMM_WORLD.Send(new long[]{currentSum}, 0, 1, MPI.LONG, partner, 0);
                    currentSum = 0; // Цей процес "завершує" свою активність на цьому кроці
                    // або його сума вже врахована.
                    // Ми можемо просто вийти з циклу для неактивних процесів
                    // або дозволити їм продовжувати, але їхня currentSum буде 0.
                    // Краще, щоб тільки процеси, які отримують, продовжували.
                    break; // Вийти з циклу для цього процесу, він більше не бере участі в редукції.
                }
            } else { // Процес з більшим рангом отримує
                if (partner >= 0) { // Перевірка, щоб партнер був коректним
                    // System.out.println("Process " + rank + " receiving from " + partner);
                    MPI.COMM_WORLD.Recv(receivedSum, 0, 1, MPI.LONG, partner, 0);
                    currentSum += receivedSum[0]; // Додаємо отриману суму
                }
            }
        }

        // Після всіх кроків, тільки процес з рангом 0 матиме повну суму
        finalSum = currentSum;

        if (rank == 0) {
            System.out.println("Root process: Total sum of array (cascade) = " + finalSum);
            long expectedSum = (long) arraySize * (arraySize + 1) / 2;
            System.out.println("Expected sum = " + expectedSum);
            if (finalSum == expectedSum) {
                System.out.println("Result is correct!");
            } else {
                System.out.println("Result is incorrect! Final sum: " + finalSum + ", Expected: " + expectedSum);
            }
        }

        MPI.Finalize();
    }
}