package lab2.task1;

// Базовий клас Bank
abstract class Bank {
    public static final int NTEST = 10000;
    protected final int[] accounts;
    protected long ntransacts = 0;

    public Bank(int n, int initialBalance) {
        accounts = new int[n];
        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = initialBalance;
        }
        ntransacts = 0;
    }

    public abstract void transfer(int from, int to, int amount);

    protected void test() {
        int sum = 0;
        for (int account : accounts)
            sum += account;
        String threadName = Thread.currentThread().getName();
        System.out.println(threadName + "\tTransactions: " + ntransacts + "\tSum: " + sum);
    }

    public int size() {
        return accounts.length;
    }
}
