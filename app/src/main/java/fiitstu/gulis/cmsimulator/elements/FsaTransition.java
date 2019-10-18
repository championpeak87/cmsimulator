package fiitstu.gulis.cmsimulator.elements;

/**
 * Finite state automaton transition
 *
 * Created by Martin on 7. 3. 2017.
 */
public class FsaTransition extends Transition {

    public FsaTransition(long id, State fromState, Symbol readSymbol, State toState) {
        super(id, fromState, readSymbol, toState);
    }

    @Override
    public String getDesc() {
        return ("δ(" + getFromState().getValue() + ", " + getReadSymbol().getValue() + ") = (" + getToState().getValue() + ")");
    }

    @Override
    public String getDiagramDesc() {
        return (getReadSymbol().getValue());
    }

}
