package lab2.task1;

class TransferThread extends Thread {
    private final Bank bank;
    private final int fromAccount;
    private final int maxAmount;
    private static final int REPS = 1000;

    public TransferThread(Bank bank, int from, int max) {
        this.bank = bank;
        this.fromAccount = from;
        this.maxAmount = max;
    }

    @Override
    public void run() {
        for (int j = 0; j < 100; j++) {
            for (int i = 0; i < REPS; i++) {
                int toAccount = (int) (bank.size() * Math.random());
                int amount = (int) (maxAmount * Math.random() / REPS);
                bank.transfer(fromAccount, toAccount, amount);
            }
        }
    }
}
