package lab2.task2;

import java.util.Random;

public class Producer implements Runnable {
    private final Drop sharedDrop;
    private final int totalMessages;

    public Producer(Drop sharedDrop, int totalMessages) {
        this.sharedDrop = sharedDrop;
        this.totalMessages = totalMessages;
    }

    @Override
    public void run() {
        Random rng = new Random();

        // Generate and pass using Drop
        for (int i = 1; i <= totalMessages; i++) {
            String message = String.valueOf(i);
            sharedDrop.put(message);
            try {
                Thread.sleep(rng.nextInt(10));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        sharedDrop.put("DONE");
    }
}
