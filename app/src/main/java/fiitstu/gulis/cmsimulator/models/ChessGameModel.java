package fiitstu.gulis.cmsimulator.models;

import fiitstu.gulis.cmsimulator.models.tasks.automata_type;

public class ChessGameModel {
    private int task_id;
    private int assigner_id;
    private String task_name;
    private String task_description;
    private automata_type automata_type;

    public ChessGameModel(int task_id, int assigner_id, String task_name, String task_description, fiitstu.gulis.cmsimulator.models.tasks.automata_type automata_type) {
        this.task_id = task_id;
        this.assigner_id = assigner_id;
        this.task_name = task_name;
        this.task_description = task_description;
        this.automata_type = automata_type;
    }

    public int getTask_id() {
        return task_id;
    }

    public void setTask_id(int task_id) {
        this.task_id = task_id;
    }

    public int getAssigner_id() {
        return assigner_id;
    }

    public void setAssigner_id(int assigner_id) {
        this.assigner_id = assigner_id;
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

    public fiitstu.gulis.cmsimulator.models.tasks.automata_type getAutomata_type() {
        return automata_type;
    }

    public void setAutomata_type(fiitstu.gulis.cmsimulator.models.tasks.automata_type automata_type) {
        this.automata_type = automata_type;
    }
}
