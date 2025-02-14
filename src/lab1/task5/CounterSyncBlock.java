package lab1.task5;

public class CounterSyncBlock extends Counter {
    @Override
    public void increment() {
        synchronized (this) {
            count++;
        }
    }

    @Override
    public void decrement() {
        synchronized (this) {
            count--;
        }
    }
}
