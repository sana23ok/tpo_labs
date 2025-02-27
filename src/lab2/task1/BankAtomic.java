package lab2.task1;

import java.util.concurrent.atomic.AtomicIntegerArray;

class BankAtomic implements Bank {
    private final AtomicIntegerArray accounts;
    private long ntransacts = 0;
    public static final int NTEST = 10000;

    public BankAtomic(int n, int initialBalance) {
        accounts = new AtomicIntegerArray(n);
        for (int i = 0; i < n; i++)
            accounts.set(i, initialBalance);
    }

    public void transfer(int from, int to, int amount) {
        accounts.addAndGet(from, -amount);
        accounts.addAndGet(to, amount);
        ntransacts++;
        if (ntransacts % NTEST == 0) test();
    }

    private void test() {
        int sum = 0;
        for (int i = 0; i < accounts.length(); i++)
            sum += accounts.get(i);
        System.out.println("Transactions: " + ntransacts + " Sum: " + sum);
    }

    public int size() { return accounts.length(); }
}
