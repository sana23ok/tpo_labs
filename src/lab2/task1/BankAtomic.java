package lab2.task1;

import java.util.concurrent.atomic.AtomicIntegerArray;

// 4. Банк з AtomicIntegerArray
class BankAtomic extends Bank {
    private final AtomicIntegerArray atomicAccounts;

    public BankAtomic(int n, int initialBalance) {
        super(n, initialBalance);
        atomicAccounts = new AtomicIntegerArray(n);
        for (int i = 0; i < accounts.length; i++) {
            atomicAccounts.set(i, initialBalance);
        }
    }

    @Override
    public void transfer(int from, int to, int amount) {
        while (true) {
            int fromBalance = atomicAccounts.get(from);
            if (fromBalance < amount) return;

            if (atomicAccounts.compareAndSet(from, fromBalance, fromBalance - amount)) {
                atomicAccounts.addAndGet(to, amount);
                break;
            }
        }
        ntransacts++;
        if (ntransacts % NTEST == 0) test();
    }
}
