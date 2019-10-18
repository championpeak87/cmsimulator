package fiitstu.gulis.cmsimulator.elements;

/**
 * Created by Martin on 7. 3. 2017.
 */
public class TmTransition extends Transition {

    public enum Direction {
        LEFT, RIGHT;

        public static Direction fromString(String str) {
            switch(str) {
                case "L":
                    return LEFT;
                case "R":
                    return RIGHT;
                default:
                    return null;
            }
        }

        public String toString() {
            switch (this) {
                case LEFT:
                    return "L";
                case RIGHT:
                    return "R";
                default:
                    return null;

            }
        }
    }

    private Symbol writeSymbol;
    private  Direction direction;

    public TmTransition(long id, State fromState, Symbol readSymbol, State toState, Symbol writeSymbol, Direction direction) {
        super(id, fromState, readSymbol, toState);
        this.writeSymbol = writeSymbol;
        this.direction = direction;
    }

    public Symbol getWriteSymbol() {
        return writeSymbol;
    }

    public void setWriteSymbol(Symbol writeSymbol) {
        this.writeSymbol = writeSymbol;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Override
    public String getDesc() {
        return ("δ(" + getFromState().getValue() + ", " + getReadSymbol().getValue() +
                ") = (" + getToState().getValue() + ", " + writeSymbol.getValue() + ", <" + direction.toString() + ">)");
    }

    @Override
    public String getDiagramDesc() {
        return (getReadSymbol().getValue() + "; " + writeSymbol.getValue() + ", <" + direction.toString() + ">");
    }

}
