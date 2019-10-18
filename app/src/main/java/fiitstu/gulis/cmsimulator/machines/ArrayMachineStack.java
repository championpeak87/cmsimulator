package fiitstu.gulis.cmsimulator.machines;

import fiitstu.gulis.cmsimulator.elements.Symbol;

import java.util.ArrayList;
import java.util.List;

/**
 * A MachineStack that is a simple wrapper around an array.
 *
 * Created by Jakub Sedlář on 03.01.2018.
 */
public class ArrayMachineStack implements MachineStack {
    List<Symbol> items;

    public ArrayMachineStack() {
        items = new ArrayList<>();
    }

    public ArrayMachineStack(ArrayMachineStack arrayMachineStack) {
        items = new ArrayList<>(arrayMachineStack.items);
    }

    @Override
    public void pushItems(List<Symbol> items) {
        this.items.addAll(items);
    }

    @Override
    public void popItems(int count) {
        for (int i = 0; i < count; i++) {
            items.remove(items.size() - 1);
        }
    }

    @Override
    public Symbol getItem(int index) {
        return items.get(index);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public ArrayMachineStack copy() {
        return new ArrayMachineStack(this);
    }
}
