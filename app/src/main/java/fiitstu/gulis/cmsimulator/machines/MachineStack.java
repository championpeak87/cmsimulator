package fiitstu.gulis.cmsimulator.machines;

import fiitstu.gulis.cmsimulator.elements.Symbol;

import java.util.List;

/**
 * An interface for a stack of a push-down automaton.
 *
 * Created by Jakub Sedlář on 03.01.2018.
 */
public interface MachineStack {
    void pushItems(List<Symbol> items);
    void popItems(int count);

    Symbol getItem(int index);
    int getItemCount();

    MachineStack copy();
}
