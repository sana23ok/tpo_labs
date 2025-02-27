package lab2.task1;

class BankSynchronized implements Bank {
    private final int[] accounts;
    private long ntransacts = 0;
    public static final int NTEST = 10000;

    public BankSynchronized(int n, int initialBalance) {
        accounts = new int[n];
        for (int i = 0; i < accounts.length; i++)
            accounts[i] = initialBalance;
    }

    public synchronized void transfer(int from, int to, int amount) {
        if (accounts[from] < amount) return;
        accounts[from] -= amount;
        accounts[to] += amount;
        ntransacts++;
        if (ntransacts % NTEST == 0) test();
    }

    private void test() {
        int sum = 0;
        for (int a : accounts) sum += a;
        System.out.println("Transactions: " + ntransacts + " Sum: " + sum);
    }

    public int size() { return accounts.length; }
}