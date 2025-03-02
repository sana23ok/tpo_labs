package lab2.task1;

// 1. Асинхронний банк
class BankAsync extends Bank {
    public BankAsync(int n, int initialBalance) {
        super(n, initialBalance);
    }

    @Override
    public void transfer(int from, int to, int amount) {
        if (accounts[from] < amount) return;
        accounts[from] -= amount;
        accounts[to] += amount;
        ntransacts++;
        if (ntransacts % NTEST == 0) test();
    }
}

