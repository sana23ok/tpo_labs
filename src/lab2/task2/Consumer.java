package lab2.task2;

import java.util.Random;

public class Consumer implements Runnable {
    private Drop drop;
    private int mgsRecivedCount = 0;

    public Consumer(Drop drop) {
        this.drop = drop;
    }

    public void run() {
        Random random = new Random();
        for (String m = drop.take(); !m.equals("DONE"); m = drop.take()) {
            System.out.format("MESSAGE RECEIVED: %s%n", m);
            mgsRecivedCount++;

            try {
                Thread.sleep(random.nextInt(10));
            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Total messages received: " + mgsRecivedCount);
    }
}