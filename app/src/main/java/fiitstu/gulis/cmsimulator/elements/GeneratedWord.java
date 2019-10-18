package fiitstu.gulis.cmsimulator.elements;

import java.io.Serializable;

/**
 * Object used in simulation, the current word and the rule for its generation are stored in this object.
 */
public class GeneratedWord implements Serializable {

    private String word;
    private GrammarRule usedRule;

    public GeneratedWord(String word, GrammarRule usedRule){
        this.word = word;
        this.usedRule = usedRule;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public GrammarRule getUsedRule() {
        return usedRule;
    }

    public void setUsedRule(GrammarRule usedRule) {
        this.usedRule = usedRule;
    }


}
