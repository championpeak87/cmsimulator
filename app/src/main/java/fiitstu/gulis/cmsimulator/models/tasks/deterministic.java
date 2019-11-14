package fiitstu.gulis.cmsimulator.models.tasks;

import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.app.CMSimulator;

public enum deterministic {
    DETERMINISTIC (CMSimulator.getContext().getString(R.string.deterministic)),
    NONDETERMINISTIC (CMSimulator.getContext().getString(R.string.nondeterministic)),
    UNKNOWN ("");

    private String determinism;

    deterministic(String determinism) {
        this.determinism = determinism;
    }

    public String toString() {
        return determinism;
    }
}
