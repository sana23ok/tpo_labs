package lab1.task5;

public class CounterSyncMethod extends Counter {

    @Override
    public synchronized void increment() {
        count++;
    }

    @Override
    public synchronized void decrement() {
        count--;
    }
}

