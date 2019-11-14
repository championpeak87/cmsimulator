package fiitstu.gulis.cmsimulator.models.tasks.automata_tasks;

import fiitstu.gulis.cmsimulator.models.tasks.deterministic;
import fiitstu.gulis.cmsimulator.models.tasks.task_solved_state;
import fiitstu.gulis.cmsimulator.models.users.User;

public class AutomataTask {
    private String task_name;
    private String task_description;
    private task_solved_state solved;
    private int minutes;
    private long started;
    private User assigner;
    private boolean publicInputs;
    private int maxSteps;
    private int task_id;
    private String file_name;
    private deterministic determinism;

    public AutomataTask(String task_name, String task_description, int task_id, String file_name, deterministic determinism) {
        this.task_name = task_name;
        this.task_description = task_description;
        this.solved = task_solved_state.NEW;
        this.task_id = task_id;
        this.file_name = file_name;
        this.determinism = determinism;
        this.minutes = -1;
        this.started = -1;
        this.assigner = null;
        this.publicInputs = true;
        this.maxSteps = 99;
    }

    public AutomataTask(String task_name, String task_description, task_solved_state solved, int minutes, long started, User assigner, boolean publicInputs, int maxSteps, int task_id, String file_name, deterministic determinism) {
        this.task_name = task_name;
        this.task_description = task_description;
        this.solved = solved;
        this.minutes = minutes;
        this.started = started;
        this.assigner = assigner;
        this.publicInputs = publicInputs;
        this.maxSteps = maxSteps;
        this.task_id = task_id;
        this.file_name = file_name;
        this.determinism = determinism;
    }

    public deterministic getDeterminism() {
        return determinism;
    }

    public void setDeterminism(deterministic determinism) {
        this.determinism = determinism;
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

    public task_solved_state getSolved() {
        return solved;
    }

    public void setSolved(task_solved_state solved) {
        this.solved = solved;
    }

    public int getTask_id() {
        return task_id;
    }

    public void setTask_id(int task_id) {
        this.task_id = task_id;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }
}
