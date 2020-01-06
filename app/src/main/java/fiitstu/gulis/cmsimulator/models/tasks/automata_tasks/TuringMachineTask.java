package fiitstu.gulis.cmsimulator.models.tasks.automata_tasks;

import fiitstu.gulis.cmsimulator.elements.Task;
import fiitstu.gulis.cmsimulator.models.tasks.deterministic;
import fiitstu.gulis.cmsimulator.models.tasks.task_solved_state;
import fiitstu.gulis.cmsimulator.models.users.User;

import java.sql.Time;

public class TuringMachineTask extends Task {
    public TuringMachineTask(String title, String text, Time available_time, String assigner, int task_id, boolean public_inputs, Task.TASK_STATUS status) {
        super(title, text, available_time, assigner, task_id);
        this.setPublicInputs(public_inputs);
        this.setStatus(status);
    }

    public TuringMachineTask() {
    }
}
