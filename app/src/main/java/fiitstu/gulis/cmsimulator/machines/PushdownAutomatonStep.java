package fiitstu.gulis.cmsimulator.machines;

import android.support.v4.util.LongSparseArray;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import fiitstu.gulis.cmsimulator.adapters.simulation.DefaultTapeListAdapter;
import fiitstu.gulis.cmsimulator.adapters.simulation.MachineListAdapter;
import fiitstu.gulis.cmsimulator.adapters.simulation.StackListAdapter;
import fiitstu.gulis.cmsimulator.adapters.simulation.StackTapeSpinnerAdapter;
import fiitstu.gulis.cmsimulator.adapters.simulation.StackTapeListAdapter;
import fiitstu.gulis.cmsimulator.elements.PdaTransition;
import fiitstu.gulis.cmsimulator.elements.State;
import fiitstu.gulis.cmsimulator.elements.Symbol;
import fiitstu.gulis.cmsimulator.elements.Transition;

/**
 * A class that represent the configuration of a push-down automaton
 *
 * @see MachineStep
 *
 * Created by Martin on 7. 3. 2017.
 */
public class PushdownAutomatonStep extends MachineStep {

    //log tag
    private static final String TAG = PushdownAutomatonStep.class.getName();

    private StackListObserver stackListObserver; //to notify elements
    private MachineStack stack; //actual stack tape

    private PushdownAutomatonStep(PushdownAutomatonStep prevMachineStep) {
        super(prevMachineStep);
        this.stackListObserver = prevMachineStep.stackListObserver;
        this.stack = prevMachineStep.stack.copy();
    }

    public PushdownAutomatonStep(MachineListAdapter machineListAdapter,
                                 int tapeDimension, Symbol emptySymbol,
                                 LongSparseArray<List<Transition>> transitionMap,
                                 State initialState, DefaultTapeListAdapter defaultTapeAdapter,
                                 StackListAdapter stackListAdapter, StackTapeSpinnerAdapter stackTapeSpinnerAdapter, List<Symbol> stack,
                                 int maxSteps) {
        super(machineListAdapter, emptySymbol, transitionMap, initialState, defaultTapeAdapter, maxSteps);
        this.stackListObserver = stackListAdapter;
        this.stack = new StackTapeListAdapter(defaultTapeAdapter.getContext(), new ArrayList<>(stack), tapeDimension, stackTapeSpinnerAdapter);
    }

    public PushdownAutomatonStep(Symbol emptySymbol,
                                    List<Symbol> tape,
                                    Symbol baseStackSymbol,
                                    LongSparseArray<List<Transition>> transitionMap,
                                    State initialState, int maxSteps) {
        super(emptySymbol, tape, transitionMap, initialState, maxSteps);
        this.stackListObserver = new StackListObserver() {
            @Override
            public void notifyDataSetChanged() {
                //do nothing
            }
        };
        this.stack = new ArrayMachineStack();
        this.stack.pushItems(Collections.singletonList(baseStackSymbol));
        if (tape.isEmpty()) {
            getTape().addToLeft(emptySymbol);
            getTape().setCurrentPosition(0);
        }
        findUsableTransitions();
    }

    //method to check the top of the stack
    private boolean checkStack(List<Symbol> popSymbolList) {
        if (stack.getItemCount() < popSymbolList.size()) {
            return false;
        }
        for (int i = 0; i < popSymbolList.size(); i++) {
            if (stack.getItem(stack.getItemCount() - popSymbolList.size() + i).getId()
                    != popSymbolList.get(i).getId()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getMachineStatus() {
        //check if final state is reached and the end of tape
        if (getCurrentState() != null && getCurrentState().isFinalState()
                && getTape().getCurrentElement() != null
                && getTape().getCurrentElement().getSymbol().getId() == getEmptySymbol().getId()) {
            return MachineStep.DONE;
        } else {
            if (getUsableTransitionList() != null && getUsableTransitionList().isEmpty()) {
                return MachineStep.STUCK;
            } else {
                return MachineStep.PROGRESS;
            }
        }
    }

    @Override
    public void findUsableTransitions() {
        Log.v(TAG, "findUsableTransitions method started");
        List<Transition> usableTransitions = new ArrayList<>();
        if (getCurrentState() != null
                && getTape().getCurrentElement() != null) {
            List<Transition> transitionList = getTransitionMap().get(getCurrentState().getId());
            if (transitionList != null) {
                for (Transition transition : transitionList) {
                    //check if empty symbol (epsilon transition) or the same symbol
                    if ((transition.getReadSymbol().getId() == getEmptySymbol().getId()
                            || transition.getReadSymbol().getId() == getTape().getCurrentElement().getSymbol().getId())
                            && checkStack(((PdaTransition) transition).getPopSymbolList())) {
                        usableTransitions.add(transition);
                    }
                }
            }
        }
        setUsableTransitionList(usableTransitions);
        Log.v(TAG, "usableTransitions set");
    }

    @Override
    public void simulateStepF(int depth) {
        Log.v(TAG, "simulateStepF method started");

        //iterate through every input machine (list changes, so iterate through copy)
        for (MachineStep machineStep : new ArrayList<>(getMachineList().getItems())) {
            PushdownAutomatonStep pdaStep = (PushdownAutomatonStep) machineStep;
            //check if right depth and not stuck
            if (pdaStep.getDepth() == depth && pdaStep.getUsableTransitionList() != null && !pdaStep.getUsableTransitionList().isEmpty()) {

                //increase depth
                pdaStep.setDepth(pdaStep.getDepth() + 1);

                //first handle the new machines, the last one will inherit the machine
                List<Transition> usableTransitionList = pdaStep.getUsableTransitionList();
                for (int i = usableTransitionList.size() - 1; i >= 0; i--) {
                    Transition transition = usableTransitionList.get(i);
                    PushdownAutomatonStep actualMachineStep;
                    if (usableTransitionList.indexOf(transition) == 0) {
                        actualMachineStep = pdaStep;
                    } else {
                        actualMachineStep = new PushdownAutomatonStep(pdaStep);
                    }

                    //save used transition into list (because of backtracking)
                    actualMachineStep.getLastTransitionList().add(transition);

                    //change state
                    actualMachineStep.setCurrentState(transition.getToState());

                    //pop symbol from stack
                    actualMachineStep.getStack()
                            .popItems(((PdaTransition) transition).getPopSymbolList().size());

                    //push symbols into stack
                    actualMachineStep.getStack()
                            .pushItems(((PdaTransition) transition).getPushSymbolList());

                    //move right (if not epsilon transition)
                    if (transition.getReadSymbol().getId() != getEmptySymbol().getId()) {
                        actualMachineStep.getTape().moveRight();
                    }

                    //calculate new usable transitions
                    actualMachineStep.findUsableTransitions();

                    //add new machine to the machine list, position after existing one
                    if (usableTransitionList.indexOf(transition) != 0) {
                        actualMachineStep.getMachineList().addItem(
                                pdaStep.getMachineList().getItems().indexOf(pdaStep) + 1, actualMachineStep);
                        actualMachineStep.stackListObserver.notifyDataSetChanged();
                    }

                    //add and remove colors from simulationFlow
                    actualMachineStep.getMachineList().recordTransition(
                            pdaStep, actualMachineStep, transition, actualMachineStep.getUsableTransitionList());
                }
            } else {
                Log.v(TAG, "usableTransitions is empty (machineStep is stuck)");
            }
        }

        Log.i(TAG, "simulateStepF done");
    }

    @Override
    public void simulateFull() {
        Log.v(TAG, "simulateFull method started");

        int stuckCounter = 0;
        int stepCounter = 0;
        boolean breakpoint = false;

        //repeat until every machine is stuck or max steps
        while (stuckCounter != getMachineList().getItems().size() && stepCounter < maxSteps && !breakpoint) {
            stuckCounter = 0;
            //iterate through every input machine (list changes, so iterate through copy)
            for (MachineStep machineStep : new ArrayList<>(getMachineList().getItems())) {
                PushdownAutomatonStep pdaStep = (PushdownAutomatonStep) machineStep;
                //check if not stuck
                if (pdaStep.getUsableTransitionList() != null && !pdaStep.getUsableTransitionList().isEmpty()) {

                    //increase depth
                    pdaStep.setDepth(pdaStep.getDepth() + 1);

                    //first handle the new machines, the last one will inherit the machine
                    List<Transition> usableTransitionList = pdaStep.getUsableTransitionList();
                    for (int i = usableTransitionList.size() - 1; i >= 0; i--) {
                        Transition transition = usableTransitionList.get(i);
                        PushdownAutomatonStep actualMachineStep;
                        if (usableTransitionList.indexOf(transition) == 0) {
                            actualMachineStep = pdaStep;
                        } else {
                            actualMachineStep = new PushdownAutomatonStep(pdaStep);
                        }

                        //save used transition into list (because of backtracking)
                        actualMachineStep.getLastTransitionList().add(transition);

                        //change state
                        actualMachineStep.setCurrentState(transition.getToState());

                        //pop symbol from stack
                        actualMachineStep.getStack()
                                .popItems(((PdaTransition) transition).getPopSymbolList().size());

                        //push symbols into stack
                        actualMachineStep.getStack()
                                .pushItems(((PdaTransition) transition).getPushSymbolList());

                        //move right (if not epsilon transition)
                        if (transition.getReadSymbol().getId() != getEmptySymbol().getId()) {
                            actualMachineStep.getTape().moveRight();
                        }

                        //calculate new usable transitions
                        actualMachineStep.findUsableTransitions();

                        //machine will be stuck at breakpoint
                        if (actualMachineStep.getTape().getCurrentElement().isBreakpoint()) {
                            breakpoint = true;
                        }

                        //add new machine to the machine list, position after existing one
                        if (usableTransitionList.indexOf(transition) != 0) {
                            actualMachineStep.getMachineList().addItem(
                                    pdaStep.getMachineList().getItems().indexOf(pdaStep) + 1, actualMachineStep);
                            (actualMachineStep).stackListObserver.notifyDataSetChanged();
                        }

                        //add and remove colors from simulationFlow
                        actualMachineStep.getMachineList().recordTransition(
                                pdaStep, actualMachineStep, transition, actualMachineStep.getUsableTransitionList());
                    }
                } else {
                    stuckCounter++;
                    Log.w(TAG, "usableTransitions is empty (machineStep is stuck)");
                }
            }
            stepCounter++;
        }

        Log.i(TAG, "simulateFull done");
    }

    @Override
    public void simulateStepB(int depth) {
        Log.v(TAG, "simulateStepB method started");

        //iterate through every input machine (list changes, so iterate through copy)
        for (MachineStep machineStep : new ArrayList<>(getMachineList().getItems())) {
            PushdownAutomatonStep pdaStep = (PushdownAutomatonStep) machineStep;
            //check if right depth
            if (pdaStep.getDepth() == depth) {

                //decrease depth
                pdaStep.setDepth(pdaStep.getDepth() - 1);

                //remove colors from simulationFlow
                pdaStep.getMachineList().undoTransition(pdaStep);

                //initial simulation step is reached, remove the pdaStep
                if (pdaStep.getLastTransitionList().size() <= 1) {
                    int position = pdaStep.getMachineList().getItems().indexOf(pdaStep);
                    pdaStep.getMachineList().removeItem(pdaStep);
                    pdaStep.stackListObserver.notifyDataSetChanged();
                } else {
                    //receive and remove last used transition from the list
                    PdaTransition lastTransition = (PdaTransition) pdaStep.getLastTransitionList().get(pdaStep.getLastTransitionList().size() - 1);
                    //can contain the same transition multiple times, so remove according to the position
                    pdaStep.getLastTransitionList().remove(pdaStep.getLastTransitionList().size() - 1);

                    //change state
                    pdaStep.setCurrentState(lastTransition.getFromState());

                    //push symbols into stack (reverse)
                    pdaStep.getStack()
                            .popItems(lastTransition.getPushSymbolList().size());

                    //pop symbol from stack (reverse)
                    pdaStep.getStack()
                            .pushItems(lastTransition.getPopSymbolList());

                    //move left (if not epsilon transition)
                    if (lastTransition.getReadSymbol().getId() != getEmptySymbol().getId()) {
                        pdaStep.getTape().moveLeft();
                    }

                    //calculate new usable transitions
                    pdaStep.findUsableTransitions();
                }
            }
        }

        Log.i(TAG, "simulateStepB done");
    }

    public MachineStack getStack() {
        return stack;
    }
}
