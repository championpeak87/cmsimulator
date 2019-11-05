package fiitstu.gulis.cmsimulator.models.tasks.automata_tasks;

import fiitstu.gulis.cmsimulator.models.tasks.task_solved_state;

public class PushdownAutomataTask extends AutomataTask {
    public PushdownAutomataTask(String task_name, String task_description, task_solved_state solved, int task_id, String file_name) {
        super(task_name, task_description, solved, task_id, file_name);
    }
}
