package fiitstu.gulis.cmsimulator.models.tasks.automata_tasks;

import fiitstu.gulis.cmsimulator.elements.Task;
import fiitstu.gulis.cmsimulator.models.tasks.deterministic;
import fiitstu.gulis.cmsimulator.models.tasks.task_solved_state;
import fiitstu.gulis.cmsimulator.models.users.User;

public class FiniteAutomataTask extends Task {
    public FiniteAutomataTask(String title, String text, int minutes, String assigner) {
        super(title, text, minutes, assigner);
    }

    public FiniteAutomataTask(String title, String text, int minutes, String assigner, int task_id) {
        super(title, text, minutes, assigner, task_id);
    }

    public FiniteAutomataTask(String title, String text, int minutes, String assigner, int task_id, boolean public_inputs, Task.TASK_STATUS status) {
        super(title, text, minutes, assigner, task_id);
        this.setPublicInputs(public_inputs);
        this.setStatus(status);
        this.setMaxSteps(100);
    }

    public FiniteAutomataTask() {
    }
}
