package fiitstu.gulis.cmsimulator.models;

import android.util.Pair;
import fiitstu.gulis.cmsimulator.elements.State;
import fiitstu.gulis.cmsimulator.elements.Symbol;
import fiitstu.gulis.cmsimulator.elements.Transition;

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
    private static List<Symbol> listOfSymbols;

    private List<Symbol> getMovementSymbolList()
    {
        List<Symbol> movementList = new ArrayList<>();
        movementList.add(new Symbol(Symbol.MOVEMENT_UP_ID, Symbol.MOVEMENT_UP));
        movementList.add(new Symbol(Symbol.MOVEMENT_DOWN_ID, Symbol.MOVEMENT_DOWN));
        movementList.add(new Symbol(Symbol.MOVEMENT_LEFT_ID, Symbol.MOVEMENT_LEFT));
        movementList.add(new Symbol(Symbol.MOVEMENT_RIGHT_ID, Symbol.MOVEMENT_RIGHT));

        return movementList;
    }

    public ChessGame(Pair<Integer, Integer> startField, Pair<Integer, Integer> finishField, List<Pair<Integer, Integer>> pathFields, Pair<Integer, Integer> fieldSize, int maxStateCount) {
        this.startField = startField;
        this.finishField = finishField;
        this.pathFields = pathFields;
        this.fieldSize = fieldSize;
        this.maxStateCount = maxStateCount;
        listOfStates = new ArrayList<>();
        listOfTransitions = new ArrayList<>();
        listOfSymbols = getMovementSymbolList();
    }

    public ChessGame(Pair<Integer, Integer> startField, Pair<Integer, Integer> finishField, List<Pair<Integer, Integer>> pathFields, Pair<Integer, Integer> fieldSize, int maxStateCount, List<State> listOfStates, List<Transition> listOfTransitions) {
        this.startField = startField;
        this.finishField = finishField;
        this.pathFields = pathFields;
        this.fieldSize = fieldSize;
        this.maxStateCount = maxStateCount;
        this.listOfStates = listOfStates;
        this.listOfTransitions = listOfTransitions;
        listOfSymbols = getMovementSymbolList();
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

    public static List<Symbol> getListOfSymbols() {
        return listOfSymbols;
    }
}
