package practice;
import mpi.*;

import java.util.Arrays;

/*
З використанням колективних методів обміну повідомленнями MPJ Express напишіть фрагмент коду,
який виконує розсилку фрагментів масиву А в процеси-воркери, виконує сортування переданих масивів
в процесах-воркерах та отримує в процесі-майстрі перші 5 значень відсортованих масивів або весь масив,
якщо значень в ньому менше 5
 */

//MPJ_HOME=C:\mpj
//-jar C:/mpj\lib\starter.jar -np 4 -dev multicore lab7.CollectiveMpi

public class DistributedSort {
    public static void main(String[] args) throws Exception {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        // Початковий масив лише в процесі-майстрі
        int[] A = null;
        int totalSize = 20; // Наприклад, 20 елементів

        int[] sendCounts = new int[size];
        int[] displs = new int[size];

        if (rank == 0) {
            A = new int[totalSize];
            for (int i = 0; i < totalSize; i++) {
                A[i] = (int) (Math.random() * 100); // Заповнення випадковими числами
            }
            System.out.println("Original array: " + Arrays.toString(A));

            // Обчислення розмірів для кожного процесу
            int chunkSize = totalSize / size;
            int remainder = totalSize % size;

            for (int i = 0; i < size; i++) {
                sendCounts[i] = chunkSize + (i < remainder ? 1 : 0);
            }

            // Обчислення зсувів
            displs[0] = 0;
            for (int i = 1; i < size; i++) {
                displs[i] = displs[i - 1] + sendCounts[i - 1];
            }
        }

        // Розсилка розмірів кожному процесу
        int[] myCount = new int[1];
        MPI.COMM_WORLD.Scatter(sendCounts, 0, 1, MPI.INT, myCount, 0, 1, MPI.INT, 0);

        int[] myChunk = new int[myCount[0]];

        // Розсилка фрагментів масиву A
        MPI.COMM_WORLD.Scatterv(A, 0, sendCounts, displs, MPI.INT, myChunk, 0, myChunk.length, MPI.INT, 0);

        // Кожен процес сортує свою частину
        Arrays.sort(myChunk);

        // Вибираємо перші 5 або менше елементів
        int resultLen = Math.min(5, myChunk.length);
        int[] top5 = new int[resultLen];
        System.arraycopy(myChunk, 0, top5, 0, resultLen);

        // Майстер готує буфер для прийому
        int[][] gathered = null;
        int[] recvCounts = new int[size];
        if (rank == 0) {
            gathered = new int[size][];
        }

        // Надсилаємо довжину кожного результату
        int[] resultLengths = new int[size];
        MPI.COMM_WORLD.Gather(new int[]{resultLen}, 0, 1, MPI.INT, resultLengths, 0, 1, MPI.INT, 0);

        // Використовуємо Gatherv вручну, бо MPJ не підтримує Gatherv напряму для int[]
        int[] flatTop5 = new int[resultLen];
        System.arraycopy(top5, 0, flatTop5, 0, resultLen);

        // Майстер збирає всі значення в один масив
        int[] allData = null;
        int[] recvDispls = null;

        if (rank == 0) {
            int totalRecv = 0;
            for (int len : resultLengths) {
                totalRecv += len;
            }
            allData = new int[totalRecv];
            recvDispls = new int[size];
            recvDispls[0] = 0;
            for (int i = 1; i < size; i++) {
                recvDispls[i] = recvDispls[i - 1] + resultLengths[i - 1];
            }
        }

        MPI.COMM_WORLD.Gatherv(flatTop5, 0, resultLen, MPI.INT,
                allData, 0, resultLengths, recvDispls, MPI.INT, 0);

        if (rank == 0) {
            System.out.println("Collected top 5 (or fewer) values from each process:");
            int index = 0;
            for (int i = 0; i < size; i++) {
                System.out.print("Process " + i + ": ");
                for (int j = 0; j < resultLengths[i]; j++) {
                    System.out.print(allData[index++] + " ");
                }
                System.out.println();
            }
        }

        MPI.Finalize();
    }
}

