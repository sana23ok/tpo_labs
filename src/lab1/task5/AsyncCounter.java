package lab1.task5;

public class AsyncCounter extends Counter {
    @Override
    public void increment() {
        count++;
    }

    @Override
    public void decrement() {
        count--;
    }
}
