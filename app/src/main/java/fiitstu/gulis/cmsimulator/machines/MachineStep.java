package fiitstu.gulis.cmsimulator.machines;

import android.support.v4.util.LongSparseArray;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import fiitstu.gulis.cmsimulator.adapters.simulation.DefaultTapeListAdapter;
import fiitstu.gulis.cmsimulator.adapters.simulation.MachineListAdapter;
import fiitstu.gulis.cmsimulator.adapters.simulation.MachineTapeListAdapter;
import fiitstu.gulis.cmsimulator.elements.*;

/**
 * A class that represent a complete configuration of a machine and provides methods for stepping forwards
 * and backwards. It also keeps a list of other MachineSteps together with which they represent the entire
 * configuration of a nondeterministic machine. This list is shared by all machines in it and calling the
 * stepping methods on any of them should behave equivalently.
 *
 * Created by Martin on 7. 3. 2017.
 */
public abstract class MachineStep {

    //log tag
    private static final String TAG = MachineStep.class.getName();

    //machine status
    public static final int PROGRESS = 0;
    public static final int DONE = 1;
    public static final int STUCK = 2;

    //for adding content and deriving new machines
    private MachineList machineList;
    //hashMap for all transitions (usable transitions from specific state), every simulation step have the whole map for the successor
    private LongSparseArray<List<Transition>> transitionMap;

    //variables for specific machine
    private MachineColor color;
    //depth in the tree
    private int depth;

    private final Symbol emptySymbol;

    protected final int maxSteps;

    //defines actual step of simulation
    private State currentState;
    private List<Transition> lastTransitionList; //list of executed transitions
    private MachineTape tape; //actual tape and position
    private List<Transition> usableTransitionList; //list of next usable transitions

    /**
     * Creates a new MachineStep based on an existing one. This is not a full
     * copy constructor, some attributes that are very specific to a step (e.g. list of transitions)
     * are not copied
     * @param prevMachineStep the original MachineStep
     */
    protected MachineStep(MachineStep prevMachineStep) {
        Log.v(TAG, "new machine constructor called");
        this.machineList = prevMachineStep.getMachineList();
        this.emptySymbol = prevMachineStep.emptySymbol;
        this.transitionMap = prevMachineStep.getTransitionMap();
        this.currentState = prevMachineStep.getCurrentState();
        this.lastTransitionList = new ArrayList<>();
        this.depth = prevMachineStep.getDepth();
        this.tape = prevMachineStep.tape.copy();
        this.tape.setMachineStep(this);
        this.maxSteps = prevMachineStep.maxSteps;

        Log.i(TAG, "new machine initialized");
    }

    /**
     * Creates a MachineStep bound to a GUI
     * @param machineListAdapter adapter that keeps track of all MachineSteps in the nondeterministic machine
     * @param emptySymbol the empty symbol
     * @param transitionMap a map that maps State ID's to transitions possible from each state
     * @param initialState the initial state
     * @param defaultTapeAdapter the adapter that contains the initial tape
     * @param maxSteps the maximum number of steps to be simulated by {@link #simulateFull()}
     */
    public MachineStep(MachineListAdapter machineListAdapter,
                       Symbol emptySymbol,
                       LongSparseArray<List<Transition>> transitionMap,
                       State initialState, DefaultTapeListAdapter defaultTapeAdapter,
                       int maxSteps) {
        Log.v(TAG, "first machine constructor called");

        this.machineList = machineListAdapter;
        this.emptySymbol = emptySymbol;
        this.transitionMap = transitionMap;
        this.currentState = initialState;
        this.lastTransitionList = new ArrayList<>();
        this.maxSteps = maxSteps;
        depth = 0;
        copySetTapeList(defaultTapeAdapter);
        Log.i(TAG, "first machine initialized");
    }

    /**
     * Creates a MachineStep not bound to the GUI
     * @param emptySymbol the empty symbol
     * @param tape the initial contents of the tape
     * @param transitionMap a map that maps State ID's to transitions possible from each state
     * @param initialState the initial state
     * @param maxSteps the maximum number of steps to be simulated by {@link #simulateFull()}
     */
    public MachineStep(Symbol emptySymbol,
                       List<Symbol> tape,
                       LongSparseArray<List<Transition>> transitionMap,
                       State initialState,
                       int maxSteps) {
        Log.v(TAG, "non-graphical machine constructor called");

        this.machineList = new ArrayMachineList();
        this.machineList.addItem(this);
        this.tape = new ArrayMachineTape(emptySymbol, tape);
        this.emptySymbol = emptySymbol;
        this.transitionMap = transitionMap;
        this.currentState = initialState;
        this.lastTransitionList = new ArrayList<>();
        this.maxSteps = maxSteps;
        depth = 0;
        Log.i(TAG, "non-graphical machine initialized");
    }

    public Symbol getEmptySymbol() {
        return emptySymbol;
    }

    //copies contents fo the default tape to the machine's own tape
    private void copySetTapeList(DefaultTapeListAdapter defaultTapeAdapter) {
        Log.v(TAG, "copySetTapeList method started");

        List<TapeElement> newTapeList = new ArrayList<>();
        //add the left most tapeElement
        TapeElement newTapeElement = new TapeElement(emptySymbol,
                defaultTapeAdapter.getItems().isEmpty() ? 0 : defaultTapeAdapter.getItems().get(0).getOrder() - 1);
        newTapeList.add(newTapeElement);
        for (TapeElement tapeElement : defaultTapeAdapter.getItems()) {
            newTapeElement = new TapeElement(tapeElement.getSymbol(), tapeElement.getOrder());
            newTapeElement.setBreakpoint(tapeElement.isBreakpoint());
            newTapeList.add(newTapeElement);
        }
        //add the right most tapeElement
        newTapeElement = new TapeElement(emptySymbol,
                defaultTapeAdapter.getItems().isEmpty() ? 0 : defaultTapeAdapter.getItems().get(defaultTapeAdapter.getItems().size() - 1).getOrder() + 1);
        newTapeList.add(newTapeElement);

        tape = new MachineTapeListAdapter(defaultTapeAdapter.getContext(), newTapeList, defaultTapeAdapter.getMachineTapeSpinnerAdapter(), this);
        int index = defaultTapeAdapter.getItems().indexOf(defaultTapeAdapter.getInitialTapeElement());
        tape.setCurrentPosition(index);

        Log.i(TAG, "tapeList copied");
    }

    /**
     * Same as calling {@link #getMachineStatus()} for every machine in the {@link #machineList}.
     * Returns {@link #DONE} if at least one of them is done, {@link #STUCK} if all of them are stuck,
     * else {@link #PROGRESS}
     * @return {@link #DONE} if at least one machine is done, {@link #STUCK} if all of them are stuck,
     * {@link #PROGRESS} otherwise
     */
    public int getNondeterministicMachineStatus() {
        int stuckMachines = 0;
        for (MachineStep machineStep: getMachineList().getItems()) {
            int status = machineStep.getMachineStatus();
            if (status == MachineStep.DONE) {
                return MachineStep.DONE; //if one branch accepts, whole machine accepts
            }
            else if (status == MachineStep.STUCK) {
                stuckMachines++;
            }
        }

        //if all branches are stuck, whole machine is stuck
        if (stuckMachines == getMachineList().getItems().size()) {
            return MachineStep.STUCK;
        }

        return MachineStep.PROGRESS;
    }

    /**
     * Returns the machine's current status, i.e. {@link #DONE}, {@link #STUCK}, or {@link #PROGRESS}
     * @return the machine's current status, i.e. {@link #DONE}, {@link #STUCK}, or {@link #PROGRESS}
     */
    public abstract int getMachineStatus();

    public abstract void findUsableTransitions();

    public abstract void simulateStepF(int depth);

    public abstract void simulateFull();

    public abstract void simulateStepB(int depth);

    MachineList getMachineList() {
        return machineList;
    }

    LongSparseArray<List<Transition>> getTransitionMap() {
        return transitionMap;
    }

    public MachineColor getColor() {
        return color;
    }

    public void setColor(MachineColor color) {
        this.color = color;
    }

    public State getCurrentState() {
        return currentState;
    }

    void setCurrentState(State currentState) {
        this.currentState = currentState;
    }

    public MachineTape getTape() {
        return tape;
    }

    public List<Transition> getLastTransitionList() {
        return lastTransitionList;
    }

    public List<Transition> getUsableTransitionList() {
        return usableTransitionList;
    }

    void setUsableTransitionList(List<Transition> usableTransitionList) {
        this.usableTransitionList = usableTransitionList;
    }

    public int getDepth() {
        return depth;
    }

    void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * Calls {@link MachineTape#matches(List)} for every branch of the nondeterministic machine,
     * returns true if the tape of at least one of them matches the argument
     * @param symbolList the list of symbols that should match the tape
     * @return true if the tape of at least one branch matches the argument, false otherwise
     */
    public boolean matchTapeNondeterministic(List<Symbol> symbolList) {
        boolean result = false;
        for (MachineStep machineStep: getMachineList().getItems()) {
            result |= machineStep.getTape().matches(symbolList);
        }

        return result;
    }

    /**
     * Returns true if any branch of the nondeterministic machine is at a breakpoint,
     * false otherwise
     *
     * @return true if any branch of the nondeterministic machine is at a breakpoint,
     * false otherwise
     */
    public boolean isNondeterministicBreakpoint() {
        for (MachineStep machineStep: getMachineList().getItems()) {
            if (machineStep.getTape().getCurrentElement().isBreakpoint()) {
                return true;
            }
        }

        return false;
    }
}
