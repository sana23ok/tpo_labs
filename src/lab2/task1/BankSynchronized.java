package lab2.task1;

class BankSynchronized extends Bank {

    public BankSynchronized(int n, int initialBalance) {
        super(n, initialBalance);
    }

    @Override
    public synchronized void transfer(int from, int to, int amount) {
        if (accounts[from] < amount) return;
        accounts[from] -= amount;
        accounts[to] += amount;
        ntransacts++;
        if (ntransacts % NTEST == 0) test();
    }
}
