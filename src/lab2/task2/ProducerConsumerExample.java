package lab2.task2;

public class ProducerConsumerExample {
    public static void main(String[] args) {
        int arraySize = 100;
        Drop drop = new Drop(arraySize);

        Thread producerThread = new Thread(new Producer(drop, arraySize));
        Thread consumerThread = new Thread(new Consumer(drop));

        producerThread.start();
        consumerThread.start();

        try {
            producerThread.join();
            consumerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Producer-Consumer process completed.");
    }
}

