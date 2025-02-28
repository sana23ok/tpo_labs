package lab2.task4;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SymbolSync {
    private int currentPermission = 0;
    private int printedSymbols = 0;
    private boolean stop = false;
    private final int totalLines;
    private final int symbolsPerLine;

    public SymbolSync(int totalLines, int symbolsPerLine) {
        this.totalLines = totalLines;
        this.symbolsPerLine = symbolsPerLine;
    }

    public synchronized void printSymbol(Symbol symbol, int controlValue) {
        while (getPermission() != controlValue) {
            try {
                wait();
            } catch (InterruptedException e) {
                Logger.getLogger(SymbolSync.class.getName()).log(Level.SEVERE, null, e);
            }
        }

        System.out.print(symbol.getSymbol());
        printedSymbols++;

        if (printedSymbols % symbolsPerLine == 0) {
            System.out.println();
        }

        if (printedSymbols >= totalLines * symbolsPerLine) {
            stop = true;
        }

        currentPermission++;
        notifyAll();
    }

    private int getPermission() {
        return currentPermission % 3;
    }

    public boolean shouldStop() {
        return stop;
    }
}
