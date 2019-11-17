package fiitstu.gulis.cmsimulator.models.tasks.automata_tasks;

import fiitstu.gulis.cmsimulator.elements.Task;
import fiitstu.gulis.cmsimulator.models.tasks.deterministic;
import fiitstu.gulis.cmsimulator.models.tasks.task_solved_state;
import fiitstu.gulis.cmsimulator.models.users.User;

public class AutomataExampleTask {
    private String task_name;
    private String task_description;
    private deterministic determinism;
    private Task task_type;

    public AutomataExampleTask(String task_name, String task_description, deterministic determinism, Task task_type) {
        this.task_name = task_name;
        this.task_description = task_description;
        this.determinism = determinism;
        this.task_type = task_type;
    }

    public String getTask_name() {
        return task_name;
    }

    public void setTask_name(String task_name) {
        this.task_name = task_name;
    }

    public String getTask_description() {
        return task_description;
    }

    public void setTask_description(String task_description) {
        this.task_description = task_description;
    }

    public deterministic getDeterminism() {
        return determinism;
    }

    public void setDeterminism(deterministic determinism) {
        this.determinism = determinism;
    }

    public Task getTask_type() {
        return task_type;
    }

    public void setTask_type(Task task_type) {
        this.task_type = task_type;
    }
}
