package fiitstu.gulis.cmsimulator.elements;

/**
 * An element on the tape
 *
 * Created by Martin on 7. 3. 2017.
 */
public class TapeElement {

    private long id;
    private Symbol symbol;
    private int order;
    private boolean breakpoint;

    public TapeElement(long id, Symbol symbol, int order, boolean breakpoint) {
        this.id = id;
        this.symbol = symbol;
        this.order = order;
        this.breakpoint = breakpoint;
    }

    public TapeElement(Symbol symbol, int order) {
        this.symbol = symbol;
        this.order = order;
        this.breakpoint = false;
    }

    public TapeElement(TapeElement tapeElement) {
        this.symbol = tapeElement.symbol;
        this.order = tapeElement.order;
        this.breakpoint = tapeElement.breakpoint;
    }

    public long getId() {
        return id;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isBreakpoint() {
        return breakpoint;
    }

    public void setBreakpoint(boolean breakpoint) {
        this.breakpoint = breakpoint;
    }
}
