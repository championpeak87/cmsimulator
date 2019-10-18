package fiitstu.gulis.cmsimulator.machines;

import android.support.v4.util.LongSparseArray;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import fiitstu.gulis.cmsimulator.adapters.simulation.DefaultTapeListAdapter;
import fiitstu.gulis.cmsimulator.adapters.simulation.MachineListAdapter;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.elements.*;

/**
 * A class that represent the configuration of a linear-bounded automaton
 *
 * @see MachineStep
 *
 * Created by Martin on 7. 3. 2017.
 */
public class LinearBoundedAutomatonStep extends MachineStep {

    //log tag
    private static final String TAG = LinearBoundedAutomatonStep.class.getName();

    private LinearBoundedAutomatonStep(MachineStep prevMachineStep) {
        super(prevMachineStep);
    }

    public LinearBoundedAutomatonStep(MachineListAdapter machineListAdapter,
                                      Symbol emptySymbol, Symbol leftEnd, Symbol rightEnd,
                                      LongSparseArray<List<Transition>> transitionMap,
                                      State initialState, DefaultTapeListAdapter defaultTapeAdapter,
                                      int maxSteps) {
        super(machineListAdapter, emptySymbol, transitionMap, initialState, defaultTapeAdapter, maxSteps);
        getTape().addToLeft(leftEnd);
        getTape().addToRight(rightEnd);
        getTape().moveRight();
    }

    public LinearBoundedAutomatonStep(Symbol emptySymbol, Symbol leftEnd, Symbol rightEnd,
                                    List<Symbol> tape,
                                    LongSparseArray<List<Transition>> transitionMap,
                                    State initialState, int maxSteps) {
        super(emptySymbol, tape, transitionMap, initialState, maxSteps);
        getTape().addToLeft(leftEnd);
        getTape().addToRight(rightEnd);
        getTape().moveRight();
        findUsableTransitions();
    }

    @Override
    public int getMachineStatus() {
        //check if final state is reached
        if (getCurrentState() != null && getCurrentState().isFinalState()) {
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
                    if (transition.getReadSymbol().getId() == getTape().getCurrentElement().getSymbol().getId()) {
                        if (((TmTransition) transition).getDirection() == TmTransition.Direction.LEFT) {
                            if (getTape().getCurrentPosition() != 0) {
                                usableTransitions.add(transition);
                            }
                        } else {
                            if (getTape().getCurrentPosition() != getTape().size() - 1) {
                                usableTransitions.add(transition);
                            }
                        }
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
            //check if right depth and not stuck
            if (machineStep.getDepth() == depth && machineStep.getUsableTransitionList() != null && !machineStep.getUsableTransitionList().isEmpty()) {

                //increase depth
                machineStep.setDepth(machineStep.getDepth() + 1);

                //first handle the new machines, the last one will inherit the machine
                List<Transition> usableTransitionList = machineStep.getUsableTransitionList();
                for (int i = usableTransitionList.size() - 1; i >= 0; i--) {
                    Transition transition = usableTransitionList.get(i);
                    MachineStep actualMachineStep;
                    if (usableTransitionList.indexOf(transition) == 0) {
                        actualMachineStep = machineStep;
                    } else {
                        actualMachineStep = new LinearBoundedAutomatonStep(machineStep);
                    }

                    //save used transition into list (because of backtracking)
                    actualMachineStep.getLastTransitionList().add(transition);

                    //change state
                    actualMachineStep.setCurrentState(transition.getToState());

                    //change symbol
                    actualMachineStep.getTape().getCurrentElement()
                            .setSymbol(((TmTransition) transition).getWriteSymbol());

                    //move left/right
                    if (((TmTransition) transition).getDirection() == TmTransition.Direction.LEFT) {
                        actualMachineStep.getTape().moveLeft();
                    } else {
                        actualMachineStep.getTape().moveRight();
                    }

                    //calculate new usable transitions
                    actualMachineStep.findUsableTransitions();

                    //add new machine to the machine list, position after existing one
                    if (usableTransitionList.indexOf(transition) != 0) {
                        actualMachineStep.getMachineList().addItem(
                                machineStep.getMachineList().getItems().indexOf(machineStep) + 1, actualMachineStep);
                    }

                    //add and remove colors from simulationFlow
                    actualMachineStep.getMachineList().recordTransition(
                            machineStep, actualMachineStep, transition, actualMachineStep.getUsableTransitionList());
                }
            } else {
                Log.w(TAG, "usableTransitions is empty (machineStep is stuck)");
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
                //check if not stuck
                if (machineStep.getUsableTransitionList() != null && !machineStep.getUsableTransitionList().isEmpty()) {

                    //increase depth
                    machineStep.setDepth(machineStep.getDepth() + 1);

                    //first handle the new machines, the last one will inherit the machine
                    List<Transition> usableTransitionList = machineStep.getUsableTransitionList();
                    for (int i = usableTransitionList.size() - 1; i >= 0; i--) {
                        Transition transition = usableTransitionList.get(i);
                        MachineStep actualMachineStep;
                        if (usableTransitionList.indexOf(transition) == 0) {
                            actualMachineStep = machineStep;
                        } else {
                            actualMachineStep = new LinearBoundedAutomatonStep(machineStep);
                        }

                        //save used transition into list (because of backtracking)
                        actualMachineStep.getLastTransitionList().add(transition);

                        //change state
                        actualMachineStep.setCurrentState(transition.getToState());

                        //change symbol
                        actualMachineStep.getTape().getCurrentElement()
                                .setSymbol(((TmTransition) transition).getWriteSymbol());

                        //move left/right
                        if (((TmTransition) transition).getDirection() == TmTransition.Direction.LEFT) {
                            actualMachineStep.getTape().moveLeft();
                        } else {
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
                                    machineStep.getMachineList().getItems().indexOf(machineStep) + 1, actualMachineStep);
                        }

                        //add and remove colors from simulationFlow
                        actualMachineStep.getMachineList().recordTransition(
                                machineStep, actualMachineStep, transition, actualMachineStep.getUsableTransitionList());
                    }
                } else {
                    stuckCounter++;
                    Log.v(TAG, "usableTransitions is empty (machineStep is stuck)");
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
            //check if right depth
            if (machineStep.getDepth() == depth) {

                //decrease depth
                machineStep.setDepth(machineStep.getDepth() - 1);

                //remove colors from simulationFlow
                machineStep.getMachineList().undoTransition(machineStep);

                //initial simulation step is reached, remove the machineStep
                if (machineStep.getLastTransitionList().size() <= 1) {
                    machineStep.getMachineList().removeItem(machineStep);
                } else {
                    //receive and remove last used transition from the list
                    TmTransition lastTransition = (TmTransition) machineStep.getLastTransitionList().get(machineStep.getLastTransitionList().size() - 1);
                    //can contain the same transition multiple times, so remove according to the position
                    machineStep.getLastTransitionList().remove(machineStep.getLastTransitionList().size() - 1);

                    //change state
                    machineStep.setCurrentState(lastTransition.getFromState());

                    //move left/right
                    if (lastTransition.getDirection() == TmTransition.Direction.LEFT) {
                        machineStep.getTape().moveRight();
                    } else {
                        machineStep.getTape().moveLeft();
                    }

                    //change symbol
                    machineStep.getTape().getCurrentElement()
                            .setSymbol(lastTransition.getReadSymbol());

                    //calculate new usable transitions
                    machineStep.findUsableTransitions();
                }
            }
        }

        Log.i(TAG, "simulateStepB done");
    }

    @Override
    public boolean matchTapeNondeterministic(List<Symbol> symbolList) {
        List<Symbol> list = new ArrayList<>();
        list.add(DataSource.getInstance().getInputSymbolWithProperties(Symbol.LEFT_BOUND));
        list.addAll(symbolList);
        list.add(DataSource.getInstance().getInputSymbolWithProperties(Symbol.RIGHT_BOUND));

        return super.matchTapeNondeterministic(list);
    }
}