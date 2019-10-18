package fiitstu.gulis.cmsimulator.machines;

import android.support.v4.util.LongSparseArray;
import android.support.v4.util.Pair;
import android.util.Log;

import java.util.*;

import fiitstu.gulis.cmsimulator.adapters.simulation.DefaultTapeListAdapter;
import fiitstu.gulis.cmsimulator.adapters.simulation.MachineListAdapter;
import fiitstu.gulis.cmsimulator.elements.*;

/**
 * A class that represent the configuration of a finite state automaton
 *
 * @see MachineStep
 *
 * Created by Martin on 7. 3. 2017.
 */
public class FiniteStateAutomatonStep extends MachineStep {

    //log tag
    private static final String TAG = FiniteStateAutomatonStep.class.getName();

    /**
     * A helper function for generating deterministic machines. Takes a set of the states in the old machine
     * that are represented by one state in the new machine and the list of old machine's transitions. Recursively
     * follows all epsilon transitions and adds more states into the set.
     * @param mappedStates a set of states in the original machine that the new state represents
     * @param transitions the list of all transitions in the original machine
     */
    private static void mergeEpsilon(Set<State> mappedStates, List<Transition> transitions) {
        Queue<State> queue = new LinkedList<>();
        queue.addAll(mappedStates);
        while (!queue.isEmpty()) {
            State state = queue.poll();
            for (Transition transition : transitions) {
                if (transition.getFromState().equals(state)
                        && transition.getReadSymbol().isEmpty()
                        && !mappedStates.contains(transition.getToState())) {
                    mappedStates.add(transition.getToState());
                    queue.add(transition.getToState());
                }
            }
        }
    }

    /**
     * A helper function for spatially laying out states. The height and width arguments are treated
     * mostly as hints - the generated states are not guaranteed to fit within the given bounds.
     * @param states the states to be laid out, organized in tiers
     * @param height the height of the display area
     * @param width the width fo the display area
     * @param radius the radius of state when displayed
     */
    private static void orderStates(List<List<State>> states, int height, int width, int radius) {
        final boolean xDominant = width >= height;

        int mainCoordinate = 0;
        int delta = radius * 4;

        for (List<State> tier: states) {
            int i = 1;
            for (State state: tier) {
                if (xDominant) {
                    state.setPositionX(mainCoordinate);
                    state.setPositionY(i * height / (tier.size() + 1));
                }
                else {
                    state.setPositionX(i * width / (tier.size() + 1));
                    state.setPositionY(mainCoordinate);
                }

                i++;
            }
            mainCoordinate += delta;
        }
    }

    /**
     * Takes states, transitions and input alphabet of a nondeterministic FSA, returns states and
     * transitions of an equivalent DSA with the same alphabet. The height and width arguments are treated
     * mostly as hints - the generated states are not guaranteed to fit within the given bounds.
     * @param oldStates list of the nondeterministic machine's states
     * @param oldTransitions list of the nondeterministic machines's transitions
     * @param height the height of the display are for states
     * @param width the width of the display area for states
     * @param radius the radius of state when displayed
     * @return lists of the deterministic machine's states and transitions
     */
    public static Pair<List<State>, List<Transition>> createDeterministic(List<State> oldStates,
                                                                             List<Transition> oldTransitions,
                                                                             int height, int width, int radius) {
        //result variables
        List<State> newStates = new ArrayList<>();
        List<Transition> newTransitions = new ArrayList<>();

        //maps a state of the new machine to the set of states in the original machine it represents
        Map<State, Set<State>> stateMap = new HashMap<>();

        //physical layers of the states, as displayed in the diagram
        List<List<State>> tiers = new ArrayList<>();
        //for convenience, we store the state's tier in their x coordinate

        //create initial state
        State currentState = new State(0, "q0", 0, 0, true, false);
        tiers.add(new ArrayList<State>());
        tiers.get(0).add(currentState);
        newStates.add(currentState);
        stateMap.put(newStates.get(0), new HashSet<State>());
        for (State state: oldStates) {
            if (state.isInitialState()) {
                stateMap.get(currentState).add(state);
                break;
            }
        }

        mergeEpsilon(stateMap.get(currentState), oldTransitions);

        int currentIndex = 0;
        while (currentIndex < newStates.size()) {
            currentState = newStates.get(currentIndex);
            currentIndex++;

            //if any of the states that are mapped to this state are final, it is final
            for (State state : stateMap.get(currentState)) {
                if (state.isFinalState()) {
                    currentState.setFinalState(true);
                    break;
                }
            }

            //find the transitions for the current state, for now represented by the input symbol
            //and the set of states in the old machine that the "to" state will represent
            Map<Symbol, Set<State>> futureTransitions = new HashMap<>();
            for (Transition transition : oldTransitions) {
                if (!transition.getReadSymbol().isEmpty() && stateMap.get(currentState).contains(transition.getFromState())) {
                    Set<State> toStates = futureTransitions.get(transition.getReadSymbol());
                    if (toStates == null) {
                        toStates = new HashSet<>();
                        futureTransitions.put(transition.getReadSymbol(), toStates);
                    }
                    toStates.add(transition.getToState());
                }
            }

            //parse the discovered transitions, create a new state if the set of old states is not already
            //represented by an existing one
            for (Map.Entry<Symbol, Set<State>> entry : futureTransitions.entrySet()) {
                mergeEpsilon(entry.getValue(), oldTransitions);
                State toState = null;
                for (Map.Entry<State, Set<State>> existingState : stateMap.entrySet()) {
                    if (existingState.getValue().equals(entry.getValue())) {
                        toState = existingState.getKey();
                        break;
                    }
                }

                if (toState == null) {
                    toState = new State(newStates.size(), "q" + newStates.size(), currentState.getPositionX() + 1, 0, false, false);
                    if (tiers.size() < currentState.getPositionX() + 2) {
                        tiers.add(new ArrayList<State>());
                    }
                    tiers.get(currentState.getPositionX() + 1).add(toState);
                    newStates.add(toState);
                    stateMap.put(toState, entry.getValue());
                }
                newTransitions.add(new FsaTransition(newTransitions.size(), currentState, entry.getKey(), toState));
            }
        }

        orderStates(tiers, height, width, radius);
        return new Pair<>(newStates, newTransitions);
    }

    private FiniteStateAutomatonStep(MachineStep prevMachineStep) {
        super(prevMachineStep);
    }

    public FiniteStateAutomatonStep(MachineListAdapter machineListAdapter,
                                    Symbol emptySymbol,
                                    LongSparseArray<List<Transition>> transitionMap,
                                    State initialState, DefaultTapeListAdapter defaultTapeAdapter,
                                    int maxSteps) {
        super(machineListAdapter, emptySymbol, transitionMap, initialState, defaultTapeAdapter, maxSteps);
    }

    public FiniteStateAutomatonStep(Symbol emptySymbol,
                       List<Symbol> tape,
                       LongSparseArray<List<Transition>> transitionMap,
                       State initialState, int maxSteps) {
        super(emptySymbol, tape, transitionMap, initialState, maxSteps);
        if (tape.isEmpty()) {
            getTape().addToLeft(emptySymbol);
            getTape().setCurrentPosition(0);
        }
        findUsableTransitions();
    }

    @Override
    public int getMachineStatus() {
        //check if final state is reached and the end of tape
        if (getCurrentState() != null && getCurrentState().isFinalState()
                && getTape().getCurrentElement() != null
                && getTape().getCurrentElement().getSymbol().getId() == getEmptySymbol().getId()) {
            return DONE;
        } else {
            if (getUsableTransitionList() != null && getUsableTransitionList().isEmpty()) {
                return STUCK;
            } else {
                return PROGRESS;
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
                    if (transition.getReadSymbol().getId() == getEmptySymbol().getId()
                            || transition.getReadSymbol().getId() == getTape().getCurrentElement().getSymbol().getId()) {
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
                        actualMachineStep = new FiniteStateAutomatonStep(machineStep);
                    }

                    //save used transition into list (because of backtracking)
                    actualMachineStep.getLastTransitionList().add(transition);

                    //change state
                    actualMachineStep.setCurrentState(transition.getToState());

                    //move right (if not epsilon transition)
                    if (transition.getReadSymbol().getId() != getEmptySymbol().getId()) {
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
                            actualMachineStep = new FiniteStateAutomatonStep(machineStep);
                        }

                        //save used transition into list (because of backtracking)
                        actualMachineStep.getLastTransitionList().add(transition);

                        //change state
                        actualMachineStep.setCurrentState(transition.getToState());

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
                    FsaTransition lastTransition = (FsaTransition) machineStep.getLastTransitionList().get(machineStep.getLastTransitionList().size() - 1);
                    //can contain the same transition multiple times, so remove according to the position
                    machineStep.getLastTransitionList().remove(machineStep.getLastTransitionList().size() - 1);

                    //change state
                    machineStep.setCurrentState(lastTransition.getFromState());

                    //move left (if not epsilon transition)
                    if (lastTransition.getReadSymbol().getId() != getEmptySymbol().getId()) {
                        machineStep.getTape().moveLeft();
                    }

                    //calculate new usable transitions
                    machineStep.findUsableTransitions();
                }
            }
        }

        Log.i(TAG, "simulateStepB done");
    }
}