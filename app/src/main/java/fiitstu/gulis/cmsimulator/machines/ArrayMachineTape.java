package fiitstu.gulis.cmsimulator.machines;

import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.elements.Symbol;
import fiitstu.gulis.cmsimulator.elements.TapeElement;

import java.util.ArrayList;
import java.util.List;

/**
 * A machine tape that is a simple wrapper around an array.
 *
 * Created by Jakub Sedlář on 30.12.2017.
 */
public class ArrayMachineTape implements  MachineTape {
    private List<TapeElement> elements;
    private int currentPosition;
    private int initialPosition;
    private final Symbol emptySymbol;

    public ArrayMachineTape(Symbol emptySymbol, List<Symbol> tape) {
        this(emptySymbol, tape, tape.isEmpty() ? -1 : 0);
    }

    public ArrayMachineTape(Symbol emptySymbol, List<Symbol> tape, int initialPosition) {
        this.emptySymbol = emptySymbol;
        this.initialPosition = initialPosition;
        this.currentPosition = initialPosition;

        this.elements = new ArrayList<>();
        for (int i = 0; i < tape.size(); i++) {
            this.elements.add(new TapeElement(tape.get(i), i));
        }
    }

    public ArrayMachineTape(ArrayMachineTape arrayMachineTape) {
        this.emptySymbol = arrayMachineTape.emptySymbol;
        this.initialPosition = arrayMachineTape.initialPosition;
        this.currentPosition = arrayMachineTape.currentPosition;

        this.elements = new ArrayList<>();
        for (TapeElement element: arrayMachineTape.elements) {
            this.elements.add(new TapeElement(element));
        }
    }

    /**
     * Does absolutely nothing
     * @param machineStep the MachineStep associated with the tape (not used by this class)
     */
    @Override
    public void setMachineStep(MachineStep machineStep) {

    }

    @Override
    public void addToLeft(Symbol symbol) {
        int order = elements.size() > 0 ? elements.get(0).getOrder() - 1 : 0;
        elements.add(0, new TapeElement(symbol, order));
    }

    @Override
    public void addToRight(Symbol symbol) {
        int order = elements.get(elements.size() - 1).getOrder() + 1;
        elements.add(new TapeElement(symbol, order));
    }

    @Override
    public void setSymbol(int position, Symbol symbol) {
        elements.get(position).setSymbol(symbol);
    }

    @Override
    public void setBreakpoint(int position, boolean breakpoint) {
        elements.get(position).setBreakpoint(breakpoint);
    }

    @Override
    public void clearBreakpoints() {
        for (TapeElement tapeElement: elements) {
            tapeElement.setBreakpoint(false);
        }
    }

    @Override
    public void removeElement(int index) {
        elements.remove(index);
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public TapeElement getCurrentElement() {
        if (currentPosition == -1) {
            return null;
        }
        else {
            return elements.get(currentPosition);
        }
    }

    @Override
    public int getCurrentPosition() {
        return currentPosition;
    }

    @Override
    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    @Override
    public void moveRight() {
        if (currentPosition != -1) {
            if (currentPosition == elements.size() - 1) {
                addToRight(emptySymbol);
            }
            currentPosition++;
        }
    }

    @Override
    public void moveLeft() {
        if (currentPosition != -1) {
            if (currentPosition == 0) {
                addToLeft(emptySymbol);
            } else {
                currentPosition--;
            }
        }
    }

    @Override
    public boolean matches(List<Symbol> symbolList) {
        Symbol emptySymbol = DataSource.getInstance().getInputSymbolWithProperties(Symbol.EMPTY);
        int myFirstNonempty = 0;
        while (myFirstNonempty < elements.size() && elements.get(myFirstNonempty).getSymbol().getId() == emptySymbol.getId()) {
            myFirstNonempty++;
        }
        int argFirstNonempty = 0;
        while (argFirstNonempty < symbolList.size() && symbolList.get(argFirstNonempty).getId() == emptySymbol.getId()) {
            argFirstNonempty++;
        }

        //resolve the case when either tape consists entirely out of empty symbols
        if (argFirstNonempty == symbolList.size()) {
            if (myFirstNonempty == elements.size()) {
                return true; //both are empty, thus equivalent
            }
            else {
                return false; //tape is not empty, but argument is
            }
        }
        else if (myFirstNonempty == elements.size()) {
            return false; //tape is empty, but argument is not
        }

        int myLastNonempty = elements.size() - 1;
        while (elements.get(myLastNonempty).getSymbol().getId() == emptySymbol.getId()) {
            myLastNonempty--;
        }
        int argLastNonempty = symbolList.size() - 1;
        while (symbolList.get(argLastNonempty).getId() == emptySymbol.getId()) {
            argLastNonempty--;
        }

        if (argLastNonempty - argFirstNonempty != myLastNonempty - myFirstNonempty) {
            return false; //different lengths
        }

        for (int i = 0; i <= myLastNonempty - myFirstNonempty; i++) {
            if (!symbolList.get(argFirstNonempty + i).getValue().equals(elements.get(myFirstNonempty + i).getSymbol().getValue())) {
                return false;
            }
        }

        return true;
    }

    public int getInitialPosition() {
        return initialPosition;
    }

    public void setInitialPosition(int initialPosition) {
        this.initialPosition = initialPosition;
    }

    @Override
    public MachineTape copy() {
        return new ArrayMachineTape(this);
    }
}
