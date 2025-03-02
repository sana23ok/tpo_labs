package lab2.task4;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SymbolSync {
    private int printedSymbols = 0;
    private boolean stop = false;
    private final int totalLines;
    private final int symbolsPerLine;
    private int currentIndex = 0; // Індекс для правильного порядку символів
    private final char[] pattern = {'|', '\\', '/'}; // Фіксований шаблон

    public SymbolSync(int totalLines, int symbolsPerLine) {
        this.totalLines = totalLines;
        this.symbolsPerLine = symbolsPerLine;
    }

    public synchronized void printSymbol(Symbol symbol, int controlValue) {
        while (!stop && pattern[currentIndex % 3] != symbol.getSymbol()) {
            try {
                wait();
            } catch (InterruptedException e) {
                Logger.getLogger(SymbolSync.class.getName()).log(Level.SEVERE, null, e);
            }
        }

        if (stop) return; // Додана перевірка, щоб потоки не чекали після завершення

        System.out.print(symbol.getSymbol());
        printedSymbols++;
        currentIndex++;

        if (printedSymbols % symbolsPerLine == 0) {
            System.out.println();
            currentIndex = 0; // Почати новий рядок з '|'
        }

        if (printedSymbols >= totalLines * symbolsPerLine) {
            stop = true; // Фіксуємо завершення
            notifyAll(); // Пробуджуємо всі потоки, щоб вони не залишилися заблокованими
            return;
        }

        notifyAll();
    }

    public boolean shouldStop() {
        return stop;
    }
}
