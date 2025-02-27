package lab2.task1;
import java.util.concurrent.locks.ReentrantLock;

class BankLock implements Bank {
    private final int[] accounts;
    private long ntransacts = 0;
    private final ReentrantLock lock = new ReentrantLock();
    public static final int NTEST = 10000;

    public BankLock(int n, int initialBalance) {
        accounts = new int[n];
        for (int i = 0; i < accounts.length; i++)
            accounts[i] = initialBalance;
    }

    public void transfer(int from, int to, int amount) {
        lock.lock();
        try {
            if (accounts[from] < amount) return;
            accounts[from] -= amount;
            accounts[to] += amount;
            ntransacts++;
            if (ntransacts % NTEST == 0) test();
        } finally {
            lock.unlock();
        }
    }

    private void test() {
        int sum = 0;
        for (int a : accounts) sum += a;
        System.out.println("Transactions: " + ntransacts + " Sum: " + sum);
    }

    public int size() { return accounts.length; }
}
