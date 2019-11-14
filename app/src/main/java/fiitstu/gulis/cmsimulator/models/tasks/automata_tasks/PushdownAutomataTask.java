package fiitstu.gulis.cmsimulator.models.tasks.automata_tasks;

import fiitstu.gulis.cmsimulator.models.tasks.deterministic;
import fiitstu.gulis.cmsimulator.models.tasks.task_solved_state;

public class PushdownAutomataTask extends AutomataTask {
    public PushdownAutomataTask(String task_name, String task_description, int task_id, String file_name, deterministic determinism) {
        super(task_name, task_description, task_id, file_name, determinism);
    }
}
