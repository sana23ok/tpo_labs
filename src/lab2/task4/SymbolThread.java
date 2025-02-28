package lab2.task4;

public class SymbolThread implements Runnable {
    private final SymbolSync symbolSync;
    private final Symbol symbol;
    private final int controlValue;

    public SymbolThread(Symbol symbol, SymbolSync symbolSync, int controlValue) {
        this.symbol = symbol;
        this.symbolSync = symbolSync;
        this.controlValue = controlValue;
    }

    @Override
    public void run() {
        while (!symbolSync.shouldStop()) {
            symbolSync.printSymbol(symbol, controlValue);
        }
    }
}
