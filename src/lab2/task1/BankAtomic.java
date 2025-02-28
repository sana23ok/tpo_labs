package lab2.task1;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;

class BankAtomic extends Bank {
    private final AtomicIntegerArray atomicAccounts;
    private final AtomicLong ntransacts = new AtomicLong(0);

    public BankAtomic(int n, int initialBalance) {
        super(n, initialBalance);
        atomicAccounts = new AtomicIntegerArray(n);
        for (int i = 0; i < n; i++) {
            atomicAccounts.set(i, initialBalance);
        }
    }

    @Override
    public void transfer(int from, int to, int amount) {
        while (true) {
            // Отримуємо поточний баланс для відправника
            int fromBalance = atomicAccounts.get(from);
            if (fromBalance < amount) return; // Якщо недостатньо коштів, зупиняємо

            // Пробуємо зробити операцію за допомогою атомарних операцій
            if (atomicAccounts.compareAndSet(from, fromBalance, fromBalance - amount)) {
                // Якщо операція успішна, додаємо гроші на рахунок отримувача
                atomicAccounts.addAndGet(to, amount);
                break; // Трансфер успішно завершений
            }
        }

        // Перевірка кожну NTEST транзакцію
        if (ntransacts.incrementAndGet() % NTEST == 0) {
            test();
        }
    }

    @Override
    public void test() {
        int sum = 0;
        for (int i = 0; i < atomicAccounts.length(); i++) {
            sum += atomicAccounts.get(i);
        }
        System.out.println("Transactions: " + ntransacts.get() + " Sum: " + sum);
    }
}
