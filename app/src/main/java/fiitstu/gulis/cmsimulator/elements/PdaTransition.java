package fiitstu.gulis.cmsimulator.elements;

import java.util.List;

/**
 * A push-down automaton transition
 *
 * Created by Martin on 7. 3. 2017.
 */
public class PdaTransition extends Transition {

    private List<Symbol> popSymbolList;
    private List<Symbol> pushSymbolList;

    public PdaTransition(long id, State fromState, Symbol readSymbol, State toState, List<Symbol> popSymbolList, List<Symbol> pushSymbolList) {
        super(id, fromState, readSymbol, toState);
        this.popSymbolList = popSymbolList;
        this.pushSymbolList = pushSymbolList;
    }

    public List<Symbol> getPopSymbolList() {
        return popSymbolList;
    }

    public void setPopSymbolList(List<Symbol> popSymbolList) {
        this.popSymbolList = popSymbolList;
    }

    public List<Symbol> getPushSymbolList() {
        return pushSymbolList;
    }

    public void setPushSymbolList(List<Symbol> pushSymbolList) {
        this.pushSymbolList = pushSymbolList;
    }

    @Override
    public String getDesc() {
        StringBuilder descString = new StringBuilder();
        descString.append("δ(").append(getFromState().getValue()).append(", ").append(getReadSymbol().getValue()).append(", ");
        if (!popSymbolList.isEmpty()) {
            for (int i = popSymbolList.size() - 1; i >= 0; i--) {
                descString.append(popSymbolList.get(i).getValue());
            }
        } else {
            descString.append("ε");
        }
        descString.append(") = (").append(getToState().getValue()).append(", ");
        if (!pushSymbolList.isEmpty()) {
            for (int i = pushSymbolList.size() - 1; i >= 0; i--) {
                descString.append(pushSymbolList.get(i).getValue());
            }
        } else {
            descString.append("ε");
        }
        descString.append(")");
        return descString.toString();
    }

    @Override
    public String getDiagramDesc() {
        StringBuilder descString = new StringBuilder();
        descString.append(getReadSymbol().getValue()).append(", ");
        if (!popSymbolList.isEmpty()) {
            for (int i = popSymbolList.size() - 1; i >= 0; i--) {
                descString.append(popSymbolList.get(i).getValue());
            }
        } else {
            descString.append("ε");
        }
        descString.append("; ");
        if (!pushSymbolList.isEmpty()) {
            for (int i = pushSymbolList.size() - 1; i >= 0; i--) {
                descString.append(pushSymbolList.get(i).getValue());
            }
        } else {
            descString.append("ε");
        }
        return descString.toString();
    }

}
