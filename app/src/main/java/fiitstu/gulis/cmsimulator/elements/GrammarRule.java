package fiitstu.gulis.cmsimulator.elements;

import java.io.Serializable;

/**
 * Object used for storing the grammar rules. The left side and right side of the rules are stored separately.
 */
public class GrammarRule implements Serializable {

    private long id;
    private String grammarLeft;
    private String grammarRight;

    public GrammarRule() {
        grammarLeft = null;
        grammarRight = null;
    }

    public GrammarRule(String grammarLeft, String grammarRight){
        this.grammarLeft = grammarLeft;
        this.grammarRight = grammarRight;
    }

    public GrammarRule(long id, String grammarLeft, String grammarRight){
        this.id = id;
        this.grammarLeft = grammarLeft;
        this.grammarRight = grammarRight;
    }

    public String getGrammarLeft() {
        return grammarLeft;
    }

    public String getGrammarRight() {
        return grammarRight;
    }

    public void setGrammarRight(String grammarRight) {
        this.grammarRight = grammarRight;
    }

    public void setGrammarLeft(String left) {
        this.grammarLeft = left;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
