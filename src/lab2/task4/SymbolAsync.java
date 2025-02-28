package lab2.task4;

public class SymbolAsync {
    private int printedSymbols = 0;
    private boolean stop = false;
    private final int totalLines;
    private final int symbolsPerLine;

    public SymbolAsync(int totalLines, int symbolsPerLine) {
        this.totalLines = totalLines;
        this.symbolsPerLine = symbolsPerLine;
    }

    public void printSymbol(Symbol symbol) {
        System.out.print(symbol.getSymbol());
        printedSymbols++;

        if (printedSymbols % symbolsPerLine == 0) {
            System.out.println();
        }

        if (printedSymbols >= totalLines * symbolsPerLine) {
            stop = true;
        }
    }

    public boolean shouldStop() {
        return stop;
    }
}

