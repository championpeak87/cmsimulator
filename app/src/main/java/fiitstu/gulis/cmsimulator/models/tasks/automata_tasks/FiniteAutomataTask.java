package fiitstu.gulis.cmsimulator.models.tasks.automata_tasks;

import fiitstu.gulis.cmsimulator.elements.Task;
import fiitstu.gulis.cmsimulator.models.tasks.deterministic;
import fiitstu.gulis.cmsimulator.models.tasks.task_solved_state;
import fiitstu.gulis.cmsimulator.models.users.User;

public class FiniteAutomataTask extends Task {
    public FiniteAutomataTask(String title, String text, int minutes, String assigner) {
        super(title, text, minutes, assigner);
    }

    public FiniteAutomataTask() {
    }
}
