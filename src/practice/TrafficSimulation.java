package practice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class TrafficSimulation {
    private static final int CARS_COUNT = 100;
    private static final int TOTAL_CARS_TO_PASS = 200;
    private static final AtomicInteger carsPassed = new AtomicInteger(0);
    private static volatile String trafficLightColor = "red";
    private static final Object lock = new Object();

    static class Car implements Runnable {
        private final int id;

        public Car(int id) {
            this.id = id;
        }

        public void go() throws InterruptedException {
            System.out.println("Car " + id + " passed on green light. Total: " + (carsPassed.get()+1));
            Thread.sleep(2);
            carsPassed.incrementAndGet();
        }

        @Override
        public void run() {
            try {
                while (carsPassed.get() < TOTAL_CARS_TO_PASS) {
                    synchronized (lock) {
                        while (!trafficLightColor.equals("green")) {
                            lock.wait();
                        }
                        if (carsPassed.get() < TOTAL_CARS_TO_PASS) {
                            go();
                        }
                    }
                    Thread.sleep(400);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class TrafficLight implements Runnable {
        @Override
        public void run() {
            try {
                while (carsPassed.get() < TOTAL_CARS_TO_PASS) {
                    synchronized (lock) {
                        trafficLightColor = "green";
                        System.out.println("Traffic Light: green");
                        lock.notifyAll();
                    }
                    TimeUnit.MILLISECONDS.sleep(70);

                    trafficLightColor = "yellow";
                    System.out.println("Traffic Light: yellow");
                    TimeUnit.MILLISECONDS.sleep(10);

                    trafficLightColor = "red";
                    System.out.println("Traffic Light: red");
                    TimeUnit.MILLISECONDS.sleep(40);

                    trafficLightColor = "yellow";
                    System.out.println("Traffic Light: yellow");
                    TimeUnit.MILLISECONDS.sleep(10);
                }
                System.out.println("Traffic Light finished its work.");
                synchronized (lock) {
                    lock.notifyAll();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ExecutorService carThreadPool = Executors.newFixedThreadPool(CARS_COUNT);
        Thread trafficLightThread = new Thread(new TrafficLight());

        System.out.println("Traffic simulation started.");
        trafficLightThread.start();

        for (int i = 1; i <= CARS_COUNT; i++) {
            carThreadPool.submit(new Car(i));
        }

        while (carsPassed.get() < TOTAL_CARS_TO_PASS) {
            TimeUnit.MILLISECONDS.sleep(100);
        }

        System.out.println("All " + TOTAL_CARS_TO_PASS + " cars have passed.");

        carThreadPool.shutdown();
        carThreadPool.awaitTermination(5, TimeUnit.SECONDS);


        System.out.println("Traffic simulation finished.");
    }
}
