package practice;

/*
Напишіть фрагмент коду клієнт-серверного застосування, в якому здійснюється передача масиву
текстових даних від клієнта до сервера з використанням з'єднання через сокети.
Масив даних містить 1000000 записів про покупки в магазині (назва товару).
На сервері дані агрегуються таким чином, що для кожної унікальної назви товару зберігається
кількість покупок цього товару. Назва товару з найбільшою кількістю покупок повертається клієнту.
Обробку даних на сервері реалізуйте з використанням паралельних обчислень.
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.io.*;
import java.net.*;
import java.util.Random;



class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("Сервер запущено...");

        try (Socket socket = serverSocket.accept();
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            System.out.println("Клієнт підключився");

            // Отримуємо масив назв товарів
            String[] purchases = (String[]) in.readObject();

            // Агрегуємо дані паралельно
            Map<String, Long> productCounts = Arrays.stream(purchases)
                    .parallel()
                    .collect(Collectors.groupingByConcurrent(p -> p, Collectors.counting()));

            // Знаходимо товар з найбільшою кількістю
            String topProduct = productCounts.entrySet()
                    .stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("No data");

            // Повертаємо результат клієнту
            out.writeObject(topProduct);
            System.out.println("Найпопулярніший товар: " + topProduct);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}



class Client {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String[] productSamples = {"Milk", "Bread", "Eggs", "Cheese", "Apple", "Banana", "Water", "Juice"};

        // Генеруємо 1 000 000 випадкових назв товарів
        String[] purchases = new String[1_000_000];
        Random random = new Random();
        for (int i = 0; i < purchases.length; i++) {
            purchases[i] = productSamples[random.nextInt(productSamples.length)];
        }

        try (Socket socket = new Socket("localhost", 5000);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Надсилаємо дані на сервер
            out.writeObject(purchases);

            // Отримуємо результат
            String topProduct = (String) in.readObject();
            System.out.println("Найпопулярніший товар: " + topProduct);
        }
    }
}
