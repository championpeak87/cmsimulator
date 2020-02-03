package fiitstu.gulis.cmsimulator.models.tasks.automata_tasks;

import fiitstu.gulis.cmsimulator.elements.Task;
import fiitstu.gulis.cmsimulator.models.tasks.deterministic;
import fiitstu.gulis.cmsimulator.models.tasks.task_solved_state;
import fiitstu.gulis.cmsimulator.models.users.User;

import java.sql.Time;
import java.util.Date;

public class FiniteAutomataTask extends Task {
    public FiniteAutomataTask(String title, String text, Time available_time, String assigner, int task_id, boolean public_inputs, Task.TASK_STATUS status) {
        super(title, text, available_time, assigner, task_id);
        this.setPublicInputs(public_inputs);
        this.setStatus(status);
    }

    public FiniteAutomataTask(String title, String text, Time available_time, Time remaining_time, String assigner, int task_id, boolean public_inputs, Task.TASK_STATUS status, Date submissionDate) {
        super(title, text, available_time, remaining_time, assigner, task_id);
        this.setPublicInputs(public_inputs);
        this.setStatus(status);
        this.setSubmission_date(submissionDate);
    }

    public FiniteAutomataTask() {
    }
}
