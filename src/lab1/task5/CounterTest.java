package lab1.task5;

// CounterTest.java
public class CounterTest {
    public static void main(String[] args) throws InterruptedException {
        Counter counter = new Counter();

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

        System.out.println("Final count (without sync): " + counter.getCount());
    }
}

