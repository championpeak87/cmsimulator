package fiitstu.gulis.cmsimulator.machines;

import fiitstu.gulis.cmsimulator.elements.Transition;

import java.util.List;

/**
 * An interface for maintaining all simultaneous configurations of a nondeterministic machine
 * and listening for certain events that occur during a simulation.
 *
 * Created by Jakub Sedlář on 03.01.2018.
 */
public interface MachineList {
    List<MachineStep> getItems();
    void addItem(MachineStep item);
    void addItem(int position, MachineStep item);
    void removeItem(MachineStep item);

    void recordTransition(MachineStep oldMachineStep, MachineStep machineStep, Transition lastTransition, List<Transition> newTransitionList);
    void undoTransition(MachineStep machineStep);
}
