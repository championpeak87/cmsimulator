package fiitstu.gulis.cmsimulator.elements;

import java.io.Serializable;

/**
 * A machine state
 *
 * Created by Martin on 7. 3. 2017.
 */
public class State implements Serializable {

    private long id;
    private String value;
    private int positionX;
    private int positionY;
    private boolean initialState;
    private boolean finalState;

    public State(long id, String value, int positionX, int positionY, boolean initialState, boolean finalState) {
        this.id = id;
        this.value = value;
        this.positionX = positionX;
        this.positionY = positionY;
        this.initialState = initialState;
        this.finalState = finalState;
    }

    public long getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getPositionX() {
        return positionX;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    public boolean isInitialState() {
        return initialState;
    }

    public void setInitialState(boolean initialState) {
        this.initialState = initialState;
    }

    public boolean isFinalState() {
        return finalState;
    }

    public void setFinalState(boolean finalState) {
        this.finalState = finalState;
    }
}
