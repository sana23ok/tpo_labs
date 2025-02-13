package lab1.task5;

// CounterSyncBlock.java
public class CounterSyncBlock {
    private int count = 0;

    public void increment() {
        synchronized (this) {
            count++;
        }
    }

    public void decrement() {
        synchronized (this) {
            count--;
        }
    }

    public int getCount() {
        return count;
    }
}
