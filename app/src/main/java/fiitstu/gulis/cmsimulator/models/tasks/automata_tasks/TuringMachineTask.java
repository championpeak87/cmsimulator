package fiitstu.gulis.cmsimulator.models.tasks.automata_tasks;

import fiitstu.gulis.cmsimulator.elements.Task;
import fiitstu.gulis.cmsimulator.models.tasks.deterministic;
import fiitstu.gulis.cmsimulator.models.tasks.task_solved_state;
import fiitstu.gulis.cmsimulator.models.users.User;

public class TuringMachineTask extends Task {
    public TuringMachineTask(String title, String text, int minutes, String assigner) {
        super(title, text, minutes, assigner);
    }

    public TuringMachineTask(String title, String text, int minutes, String assigner, int task_id) {
        super(title, text, minutes, assigner, task_id);
    }

    public TuringMachineTask(String title, String text, int minutes, String assigner, int task_id, boolean public_inputs) {
        super(title, text, minutes, assigner, task_id);
        this.setPublicInputs(public_inputs);
    }

    public TuringMachineTask() {
    }
}
