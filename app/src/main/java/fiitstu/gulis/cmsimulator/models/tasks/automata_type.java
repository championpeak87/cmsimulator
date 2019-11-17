package fiitstu.gulis.cmsimulator.models.tasks;

import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.app.CMSimulator;

public enum automata_type {
    FINITE_AUTOMATA(CMSimulator.getContext().getString(R.string.finite_state_automaton), "finite_automata"),
    PUSHDOWN_AUTOMATA(CMSimulator.getContext().getString(R.string.pushdown_automaton), "pushdown_automata"),
    LINEAR_BOUNDED_AUTOMATA(CMSimulator.getContext().getString(R.string.linear_bounded_automaton), "linear_bounded_automata"),
    TURING_MACHINE(CMSimulator.getContext().getString(R.string.turing_machine), "turing_machine"),
    UNKNOWN(CMSimulator.getContext().getString(R.string.unknown_machine), "unknown");

    private String automata_name;
    private String api_key;

    automata_type(String name, String api_key) {
        this.automata_name = name;
        this.api_key = api_key;
    }


    @Override
    public String toString() {
        return this.automata_name;
    }

    public String getApiKey()
    {
        return this.api_key;
    }

}
