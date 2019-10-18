package fiitstu.gulis.cmsimulator.elements;

public class GrammarType {

    private String grammar_type;
    private String non_terminals;
    private String terminals;
    private String definition;

    public GrammarType(String grammar_type, String non_terminals, String terminals){
        this.grammar_type = grammar_type;
        this.non_terminals = non_terminals;
        this.terminals = terminals;
        this.definition = "N, T, P, S";
    }


    public String getGrammar_type() {
        return grammar_type;
    }

    public String getDefinition() {
        return definition;
    }

    public String getNon_terminals() {
        return non_terminals;
    }

    public String getTerminals() {
        return terminals;
    }

}
