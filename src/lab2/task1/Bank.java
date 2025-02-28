package lab2.task1;

abstract class Bank {
    protected final int[] accounts;
    protected long ntransacts = 0;
    public static final int NTEST = 10000;

    public Bank(int n, int initialBalance) {
        accounts = new int[n];
        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = initialBalance;
        }
    }

    public void test() {
        int sum = 0;
        for (int a : accounts) sum += a;
        System.out.println("Transactions: " + ntransacts + "\tSum: " + sum);
    }

    public int size() {
        return accounts.length;
    }

    public abstract void transfer(int from, int to, int amount);
}
