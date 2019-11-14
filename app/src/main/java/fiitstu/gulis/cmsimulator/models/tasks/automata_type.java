package fiitstu.gulis.cmsimulator.models.tasks;

import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.app.CMSimulator;

public enum automata_type {
    FINITE_AUTOMATA(CMSimulator.getContext().getString(R.string.finite_state_automaton)),
    PUSHDOWN_AUTOMATA(CMSimulator.getContext().getString(R.string.pushdown_automaton)),
    LINEAR_BOUNDED_AUTOMATA(CMSimulator.getContext().getString(R.string.linear_bounded_automaton)),
    TURING_MACHINE(CMSimulator.getContext().getString(R.string.turing_machine)),
    UNKNOWN(CMSimulator.getContext().getString(R.string.unknown_machine));

    private String automata_name;

    automata_type(String name)
    {
        this.automata_name = name;
    }
}
