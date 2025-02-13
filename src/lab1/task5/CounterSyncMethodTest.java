package lab1.task5;

// CounterSyncMethodTest.java
public class CounterSyncMethodTest {
    public static void main(String[] args) throws InterruptedException {
        CounterSyncMethod counter = new CounterSyncMethod();

        Runnable incrementTask = () -> {
            for (int i = 0; i < 100000; i++) {
                counter.increment();
            }
        };

        Runnable decrementTask = () -> {
            for (int i = 0; i < 100000; i++) {
                counter.decrement();
            }
        };

        Thread incrementThread = new Thread(incrementTask);
        Thread decrementThread = new Thread(decrementTask);

        incrementThread.start();
        decrementThread.start();

        incrementThread.join();
        decrementThread.join();

        System.out.println("Final count (sync method): " + counter.getCount());
    }
}

