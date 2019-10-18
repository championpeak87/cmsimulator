package fiitstu.gulis.cmsimulator.machines;

import fiitstu.gulis.cmsimulator.elements.Symbol;
import fiitstu.gulis.cmsimulator.elements.TapeElement;

import java.util.List;

/**
 * An interface for a machine's tape
 *
 * Created by Jakub Sedlář on 30.12.2017.
 */
public interface MachineTape {
    void setMachineStep(MachineStep machineStep);

    /**
     * Adds a new tape element with the given symbol to the left end of the tape.
     * This does not change the head's position relative to the start of the tape, so it
     * will be pointing to the element left of the one it was previously pointing to.
     *
     * @param symbol the symbol to be written on the new tape element
     */
    void addToLeft(Symbol symbol);

    /**
     * Adds a new tape element with the given symbol to the right end of the tape.
     *
     * @param symbol the symbol to be written on the new tape element
     */
    void addToRight(Symbol symbol);

    /**
     * Sets the symbol on the tape element at the given position
     * @param position the position of the tape element to be changed
     * @param symbol the symbol to be set
     */
    void setSymbol(int position, Symbol symbol);

    /**
     * Sets or clears a breakpoint on the tape element at the given position.
     * @param position the position of the tape element to be changed
     * @param breakpoint true to set the breakpoint, false to remove the breakpoint
     */
    void setBreakpoint(int position, boolean breakpoint);

    /**
     * Removes all breakpoints from the tape
     */
    void clearBreakpoints();

    /**
     * Removes the element at the given index. This does not change the head's
     * position so if it was pointing to the removed element or any element to the right of it,
     * it will now point to the next element right, potentially going out of bouds.
     *
     * @param index the index of the element to be removed
     */
    void removeElement(int index);

    /**
     * Returns the number of elements on the tape
     * @return the number of elements on the tape
     */
    int size();

    /**
     * Returns the tape element the tape's head is currently pointing to
     * @return the tape element the tape's head is currently pointing to
     */
    TapeElement getCurrentElement();

    /**
     * Returns the index of the tape element the tape's head is currently pointing to
     * @return the index of the tape element the tape's head is currently pointing to
     */
    int getCurrentPosition();

    /**
     * Sets the tape's head to the given index
     * @param currentPosition the index of the tape element the tape's head should point to
     */
    void setCurrentPosition(int currentPosition);

    /**
     * Moves the head one position to the right, appending the tape with empty symbol if necessary
     */
    void moveRight();

    /**
     * Moves the head one position to the left, prepending the tape with empty symbol if necessary
     */
    void moveLeft();

    /**
     * Compares the contents of the tape to the list of symbols, ignoring continuous sequence of empty
     * symbols at the start and end of both the tape and the given list. Returns true if the non-ignored
     * portion of the list matches the contents of the non-ignored portion fo the tape, false otherwise.
     * @param symbolList the list of symbols to be compared to the contents of the tape
     * @return true if the contents of the tape match the contents of the list of symbols, ignoring continuous
     * sequences of empty symbols at the start and end of either; false otherwise.
     */
    boolean matches(List<Symbol> symbolList);

    /**
     * Creates a copy of the MachineTape.
     * Intentionally not clone(), because that interface is, sadly, fubar
     * @return a copy of the MachineTape object
     */
    MachineTape copy();
}
