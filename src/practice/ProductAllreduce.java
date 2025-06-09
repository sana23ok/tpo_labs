package practice;

import mpi.*;

/*
Напишіть виклик методу MPJ Express, який виконує обчислення добутку всіх елементів масивів,
що зберігаються в процесах за адресою &arr та записує його в усі процеси у змінну result.
 */
//MPJ_HOME=C:\mpj
//-jar C:/mpj\lib\starter.jar -np 4 -dev multicore lab7.CollectiveMpi

public class ProductAllreduce {
    public static void main(String[] args) throws MPIException {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        // Припустимо, що кожен процес має свій масив 'arr'
        // Для простоти, кожен процес матиме масив з одним елементом,
        // який представляє частину загального добутку.
        // У реальному сценарії 'arr' може бути локальним результатом обчислень.
        double[] localArr = new double[1];
        localArr[0] = (double) (rank + 2); // Приклад: процес 0 має 2.0, процес 1 має 3.0, ...

        System.out.println("Process " + rank + ": local value = " + localArr[0]);

        // Буфер для збереження кінцевого результату добутку
        double[] result = new double[1];

        // Виклик MPI_Allreduce для обчислення добутку
        // MPI_Allreduce(sendbuf, sendoffset, recvbuf, recvoffset, count, datatype, op, comm)
        MPI.COMM_WORLD.Allreduce(localArr, 0, // sendbuf, sendoffset (відправляємо свій локальний добуток)
                result, 0,  // recvbuf, recvoffset (отримуємо загальний добуток)
                1,          // count (кількість елементів для операції - тут 1, бо масив з одним значенням)
                MPI.DOUBLE, // datatype (тип даних)
                MPI.PROD);  // op (операція - добуток)

        System.out.println("Process " + rank + ": final product (result) = " + result[0]);

        MPI.Finalize();
    }
}
