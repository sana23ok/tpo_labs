package lab2.task4;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        int symbolsPerLine = 50;
        int numLines = 10;

        // Тест синхронного способу
        testSync(symbolsPerLine, numLines);

        // Тест асинхронного способу
        testAsync(symbolsPerLine, numLines);
    }

    private static void testSync(int symbolsPerLine, int numLines) throws InterruptedException {
        System.out.println("Testing Synchronous Method");

        SymbolSync sync = new SymbolSync(numLines, symbolsPerLine);

        Symbol pipeSymbol = new Symbol('|');
        Symbol backslashSymbol = new Symbol('\\');
        Symbol slashSymbol = new Symbol('/');

        Thread thread1 = new Thread(new SymbolThread(pipeSymbol, sync, 0));
        Thread thread2 = new Thread(new SymbolThread(backslashSymbol, sync, 1));
        Thread thread3 = new Thread(new SymbolThread(slashSymbol, sync, 2));

        thread1.start();
        thread2.start();
        thread3.start();

        thread1.join();
        thread2.join();
        thread3.join();
    }

    private static void testAsync(int symbolsPerLine, int numLines) throws InterruptedException {
        System.out.println("\nTesting Asynchronous Method");

        SymbolAsync async = new SymbolAsync(numLines, symbolsPerLine);

        Symbol pipeSymbol = new Symbol('|');
        Symbol backslashSymbol = new Symbol('\\');
        Symbol slashSymbol = new Symbol('/');

        Thread thread1 = new Thread(() -> {
            while (!async.shouldStop()) {
                async.printSymbol(pipeSymbol);
            }
        });

        Thread thread2 = new Thread(() -> {
            while (!async.shouldStop()) {
                async.printSymbol(backslashSymbol);
            }
        });

        Thread thread3 = new Thread(() -> {
            while (!async.shouldStop()) {
                async.printSymbol(slashSymbol);
            }
        });

        thread1.start();
        thread2.start();
        thread3.start();

        thread1.join();
        thread2.join();
        thread3.join();
    }
}
