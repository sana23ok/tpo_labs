package lab2.task1;

class BankTest {
    public static final int NACCOUNTS = 10;
    public static final int INITIAL_BALANCE = 10000;

    public static void main(String[] args) {
        Bank[] banks = new Bank[NACCOUNTS];
//        Bank bank = new BankAsync(NACCOUNTS, INITIAL_BALANCE);
        Bank bank = new BankSynchronized(NACCOUNTS, INITIAL_BALANCE);
//        Bank bank = new BankLock(NACCOUNTS, INITIAL_BALANCE);
//        Bank bank = new BankAtomic(NACCOUNTS, INITIAL_BALANCE);
        for (int i = 0; i < NACCOUNTS; i++) {
            TransferThread t = new TransferThread(bank, i, INITIAL_BALANCE);
            t.setName("Thread-" + i);
            t.start();
        }
    }
}
