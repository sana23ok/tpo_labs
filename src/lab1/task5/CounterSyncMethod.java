package lab1.task5;

// CounterSyncMethod.java
public class CounterSyncMethod {
    private int count = 0;

    public synchronized void increment() {
        count++;
    }

    public synchronized void decrement() {
        count--;
    }

    public int getCount() {
        return count;
    }
}

