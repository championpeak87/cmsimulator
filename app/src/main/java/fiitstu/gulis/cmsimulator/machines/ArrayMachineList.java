package fiitstu.gulis.cmsimulator.machines;

import fiitstu.gulis.cmsimulator.elements.Transition;

import java.util.ArrayList;
import java.util.List;

/**
 * A MachineList that is a simple wrapper around an array. It is intended for
 * silently simulating a machine in the background and all event-recording methods
 * do not do anything.
 *
 * Created by Jakub Sedlář on 03.01.2018.
 */
public class ArrayMachineList implements MachineList {

    private List<MachineStep> items = new ArrayList<>();

    @Override
    public List<MachineStep> getItems() {
        return items;
    }

    @Override
    public void addItem(MachineStep item) {
        items.add(item);
    }

    @Override
    public void addItem(int position, MachineStep item) {
        items.add(position, item);
    }

    @Override
    public void removeItem(MachineStep item) {
        items.remove(item);
    }

    /**
     * Does nothing
     * @param oldMachineStep ignored
     * @param machineStep ignored
     * @param lastTransition ignored
     * @param newTransitionList ignored
     */
    @Override
    public void recordTransition(MachineStep oldMachineStep, MachineStep machineStep, Transition lastTransition, List<Transition> newTransitionList) {

    }

    /**
     * Does nothing
     * @param machineStep ignored
     */
    @Override
    public void undoTransition(MachineStep machineStep) {

    }
}
