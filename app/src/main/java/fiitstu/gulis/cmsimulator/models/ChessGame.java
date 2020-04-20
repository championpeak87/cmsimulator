package fiitstu.gulis.cmsimulator.models;

import android.util.Pair;
import fiitstu.gulis.cmsimulator.elements.State;
import fiitstu.gulis.cmsimulator.elements.Symbol;
import fiitstu.gulis.cmsimulator.elements.Transition;
import fiitstu.gulis.cmsimulator.models.tasks.automata_type;

import java.util.ArrayList;
import java.util.List;

public class ChessGame {
    private Pair<Integer, Integer> startField;
    private Pair<Integer, Integer> finishField;
    private List<Pair<Integer, Integer>> pathFields;
    private Pair<Integer, Integer> fieldSize;
    private int maxStateCount;
    private List<State> listOfStates;
    private List<Transition> listOfTransitions;
    private List<Symbol> stackAlphabet;
    private automata_type automata_type;

    private static List<Symbol> listOfSymbols;

    public static List<Symbol> getMovementSymbolList() {
        List<Symbol> movementList = new ArrayList<>();
        movementList.add(new Symbol(Symbol.MOVEMENT_UP_ID, Symbol.MOVEMENT_UP));
        movementList.add(new Symbol(Symbol.MOVEMENT_DOWN_ID, Symbol.MOVEMENT_DOWN));
        movementList.add(new Symbol(Symbol.MOVEMENT_LEFT_ID, Symbol.MOVEMENT_LEFT));
        movementList.add(new Symbol(Symbol.MOVEMENT_RIGHT_ID, Symbol.MOVEMENT_RIGHT));

        return movementList;
    }

    public ChessGame(Pair<Integer, Integer> startField, Pair<Integer, Integer> finishField, List<Pair<Integer, Integer>> pathFields, Pair<Integer, Integer> fieldSize, int maxStateCount, automata_type automata_type) {
        this.startField = startField;
        this.finishField = finishField;
        this.pathFields = pathFields;
        this.fieldSize = fieldSize;
        this.maxStateCount = maxStateCount;
        this.automata_type = automata_type;
        listOfStates = new ArrayList<>();
        listOfTransitions = new ArrayList<>();
        listOfSymbols = getMovementSymbolList();

        stackAlphabet = new ArrayList<>();
    }

    public ChessGame(Pair<Integer, Integer> startField, Pair<Integer, Integer> finishField, List<Pair<Integer, Integer>> pathFields, Pair<Integer, Integer> fieldSize, int maxStateCount, List<State> listOfStates, List<Transition> listOfTransitions, fiitstu.gulis.cmsimulator.models.tasks.automata_type automata_type) {
        this.startField = startField;
        this.finishField = finishField;
        this.pathFields = pathFields;
        this.fieldSize = fieldSize;
        this.maxStateCount = maxStateCount;
        this.listOfStates = listOfStates;
        this.listOfTransitions = listOfTransitions;
        this.automata_type = automata_type;
        listOfSymbols = getMovementSymbolList();
        stackAlphabet = new ArrayList<>();
    }

    public Pair<Integer, Integer> getStartField() {
        return startField;
    }

    public void setStartField(Pair<Integer, Integer> startField) {
        this.startField = startField;
    }

    public Pair<Integer, Integer> getFinishField() {
        return finishField;
    }

    public void setFinishField(Pair<Integer, Integer> finishField) {
        this.finishField = finishField;
    }

    public List<Pair<Integer, Integer>> getPathFields() {
        return pathFields;
    }

    public void setPathFields(List<Pair<Integer, Integer>> pathFields) {
        this.pathFields = pathFields;
    }

    public Pair<Integer, Integer> getFieldSize() {
        return fieldSize;
    }

    public void setFieldSize(Pair<Integer, Integer> fieldSize) {
        this.fieldSize = fieldSize;
    }

    public int getMaxStateCount() {
        return maxStateCount;
    }

    public void setMaxStateCount(int maxStateCount) {
        this.maxStateCount = maxStateCount;
    }

    public List<State> getListOfStates() {
        return listOfStates;
    }

    public void setListOfStates(List<State> listOfStates) {
        this.listOfStates = listOfStates;
    }

    public List<Transition> getListOfTransitions() {
        return listOfTransitions;
    }

    public void setListOfTransitions(List<Transition> listOfTransitions) {
        this.listOfTransitions = listOfTransitions;
    }

    public fiitstu.gulis.cmsimulator.models.tasks.automata_type getAutomata_type() {
        return automata_type;
    }

    public void setAutomata_type(fiitstu.gulis.cmsimulator.models.tasks.automata_type automata_type) {
        this.automata_type = automata_type;
    }

    public List<Symbol> getStackAlphabet() {
        return stackAlphabet;
    }

    public void setStackAlphabet(List<Symbol> stackAlphabet) {
        this.stackAlphabet = stackAlphabet;
    }

    public static List<Symbol> getListOfSymbols() {
        return listOfSymbols;
    }

    public static void setListOfSymbols(List<Symbol> listOfSymbols) {
        ChessGame.listOfSymbols = listOfSymbols;
    }
}
