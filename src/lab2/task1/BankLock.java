package lab2.task1;

import java.util.concurrent.locks.ReentrantLock;

// 3. Банк з ReentrantLock
class BankLock extends Bank {
    private final ReentrantLock lock = new ReentrantLock();

    public BankLock(int n, int initialBalance) {
        super(n, initialBalance);
    }

    @Override
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
}
