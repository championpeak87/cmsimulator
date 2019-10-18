package fiitstu.gulis.cmsimulator.elements;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import fiitstu.gulis.cmsimulator.activities.MainActivity;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.machines.*;

import java.io.Serializable;
import java.util.List;

/**
 * A class that represents a single test scenario in a bulk test
 *
 * Created by Jakub Sedlář on 15.10.2017.
 */
public class TestScenario implements Serializable{

    /**
     * The unique identifier of the object instance
     */
    private long id;

    /**
     * The input word for the test
     */
    @NonNull
    private List<Symbol> inputWord;

    /**
     * The expected output word on the tape (for LBA and TM, if set), or null
     */
    @Nullable
    private List<Symbol> outputWord;

    /**
     * Constructs a new TestScenario
     * @param id The unique identifier of the object instance
     * @param inputWord The input word for the test
     * @param outputWord The expected output word on the tape (for LBA and TM), or null
     */
    public TestScenario(long id, @NonNull List<Symbol> inputWord, @Nullable List<Symbol> outputWord) {
        this.id = id;
        this.inputWord = inputWord;
        this.outputWord = outputWord;
    }

    /**
     * Constructs a new TestScenario
     * @param inputWord The input word for the test
     * @param outputWord The expected output word on the tape (for LBA and TM), or null
     */
    public TestScenario(@NonNull List<Symbol> inputWord, @Nullable List<Symbol> outputWord) {
        this.inputWord = inputWord;
        this.outputWord = outputWord;
    }

    public long getId() {
        return id;
    }

    public void setInputWord(@NonNull List<Symbol> inputWord) {
        this.inputWord = inputWord;
    }

    @NonNull
    public List<Symbol> getInputWord() {
        return inputWord;
    }

    /**
     * Sets the output word. Set to null to indicate no particular output is expected.
     * @param outputWord the output word, or null if no particular output is expected.
     */
    public void setOutputWord(@Nullable List<Symbol> outputWord) {
        this.outputWord = outputWord;
    }

    /**
     * Returns the output word, or null if no particular output is expected.
     * @return the output word, or null if no particular output is expected.
     */
    @Nullable
    public List<Symbol> getOutputWord() {
        return outputWord;
    }

    public void persist(boolean negative, DataSource dataSource) {
        id = dataSource.addOrUpdateTest(this, negative);
    }

    /**
     * Prepares a machine to simulate this test scenario.
     * @param machineType the type of the machine
     * @param dataSource the database with the machine data (must be open)
     * @param maxSteps the maximum number of simulated steps
     * @return the prepared machine
     */
    public MachineStep prepareMachine(int machineType, DataSource dataSource, int maxSteps) {
        List<Symbol> alphabet = dataSource.getInputAlphabetFullExtract();
        List<State> states = dataSource.getStateFullExtract();
        State initialState = null;
        for (State state: states) {
            if (state.isInitialState()) {
                initialState = state;
                break;
            }
        }

        switch (machineType) {
            case MainActivity.FINITE_STATE_AUTOMATON:
                return new FiniteStateAutomatonStep(dataSource.getInputSymbolWithProperties(Symbol.EMPTY),
                        getInputWord(),
                        Transition.createTransitionMap(dataSource.getFsaTransitionFullExtract(alphabet, states)),
                        initialState, maxSteps);
            case MainActivity.PUSHDOWN_AUTOMATON:
                List<Symbol> stackAlphabet = dataSource.getStackAlphabetFullExtract();
                return new PushdownAutomatonStep(dataSource.getInputSymbolWithProperties(Symbol.EMPTY),
                        getInputWord(),
                        stackAlphabet.get(0),
                        Transition.createTransitionMap(dataSource.getPdaTransitionFullExtract(alphabet, stackAlphabet, states)),
                        initialState, maxSteps);
            case MainActivity.LINEAR_BOUNDED_AUTOMATON:
                return new LinearBoundedAutomatonStep(dataSource.getInputSymbolWithProperties(Symbol.EMPTY),
                        dataSource.getInputSymbolWithProperties(Symbol.LEFT_BOUND),
                        dataSource.getInputSymbolWithProperties(Symbol.RIGHT_BOUND),
                        getInputWord(),
                        Transition.createTransitionMap(dataSource.getTmTransitionFullExtract(alphabet, states)),
                        initialState, maxSteps);
            case MainActivity.TURING_MACHINE:
                return new TuringMachineStep(dataSource.getInputSymbolWithProperties(Symbol.EMPTY),
                        getInputWord(),
                        Transition.createTransitionMap(dataSource.getTmTransitionFullExtract(alphabet, states)),
                        initialState, maxSteps);
            default:
                return null;
        }
    }

    /**
     * Prepares a machine to simulate this test scenario.
     * @param machineType the type of the machine
     * @param dataSource the database with the machine data (must be open)
     * @return the prepared machine
     */
    public MachineStep prepareMachine(int machineType, DataSource dataSource) {
        return prepareMachine(machineType, dataSource, dataSource.getMaxSteps());
    }
}
