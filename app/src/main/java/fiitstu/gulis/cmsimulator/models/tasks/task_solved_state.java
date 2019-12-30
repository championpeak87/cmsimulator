package fiitstu.gulis.cmsimulator.models.tasks;

public enum task_solved_state {
    NEW (1),
    WRONG (2),
    CORRECT (3),
    IN_PROGRESS (4);

    private final int id;

    task_solved_state(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
