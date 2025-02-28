package lab2.task2;

import java.util.Random;

public class Producer implements Runnable {
    private final Drop drop;
    private final int size;

    public Producer(Drop drop, int size) {
        this.drop = drop;
        this.size = size;
    }

    @Override
    public void run() {
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            drop.put(i);
//            System.out.println("Produced: " + i+1);
            try {
                Thread.sleep(random.nextInt(10));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        drop.put(-1);
    }
}
