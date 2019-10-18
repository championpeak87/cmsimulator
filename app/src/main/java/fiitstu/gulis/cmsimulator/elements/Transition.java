package fiitstu.gulis.cmsimulator.elements;

import android.support.v4.util.LongSparseArray;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin on 7. 3. 2017.
 */
public abstract class Transition implements Serializable {

    //log tag
    private static final String TAG = Transition.class.getName();

    private long id;
    private State fromState;
    private Symbol readSymbol;
    private State toState;

    public static LongSparseArray<List<Transition>> createTransitionMap(List<Transition> transitionList) {
        Log.v(TAG, "createTransitionMap method started");
        LongSparseArray<List<Transition>> transitionMap = new LongSparseArray<>();

        for (Transition transition : transitionList) {
            List<Transition> actualTransitions = transitionMap.get(transition.getFromState().getId());
            if (actualTransitions != null) {
                actualTransitions.add(transition);
                transitionMap.put(transition.getFromState().getId(), actualTransitions);
                Log.d(TAG, transition.getDesc() + " added to existing transitionList");
            } else {
                //transition for this state was not added yet
                actualTransitions = new ArrayList<>();
                actualTransitions.add(transition);
                transitionMap.put(transition.getFromState().getId(), actualTransitions);
                Log.d(TAG, transition.getDesc() + " added to new transitionList");
            }
        }
        return transitionMap;
    }

    public Transition(long id, State fromState, Symbol readSymbol, State toState) {
        this.id = id;
        this.fromState = fromState;
        this.readSymbol = readSymbol;
        this.toState = toState;
    }

    public long getId() {
        return id;
    }

    public State getFromState() {
        return fromState;
    }

    public void setFromState(State fromState) {
        this.fromState = fromState;
    }

    public Symbol getReadSymbol() {
        return readSymbol;
    }

    public void setReadSymbol(Symbol readSymbol) {
        this.readSymbol = readSymbol;
    }

    public State getToState() {
        return toState;
    }

    public void setToState(State toState) {
        this.toState = toState;
    }

    public abstract String getDesc();

    public abstract String getDiagramDesc();
}
