package fiitstu.gulis.cmsimulator.elements;

import java.io.Serializable;

/**
 * Metadata about a task
 *
 * Created by Jakub Sedlář on 05.01.2018.
 */
public class Task implements Serializable {
    private String title;
    private String text;
    private int minutes;
    private long started;
    private String assigner;
    private boolean publicInputs;
    private int maxSteps;
    private int task_id;
    private TASK_STATUS status;

    private int resultVersion;

    public Task() {
        publicInputs = true;
        resultVersion = TaskResult.CURRENT_VERSION;
    }

    public enum TASK_STATUS {
        IN_PROGRESS("in_progress"),
        CORRECT("correct"),
        WRONG("wrong"),
        NEW("new"),
        TOO_LATE("too_late");

        private String query;

        TASK_STATUS(String query) {
            this.query = query;
        }


        @Override
        public String toString() {
            return query;
        }
    }

    public Task(String title, String text, int minutes, String assigner, int task_id) {
        this.title = title;
        this.text = text;
        this.minutes = minutes;
        this.assigner = assigner;
        this.task_id = task_id;
        this.status = TASK_STATUS.NEW;
    }

    public Task(String title, String text, int minutes, boolean publicInputs, int maxSteps, int resultVersion) {
        this.title = title;
        this.text = text;
        this.minutes = minutes;
        this.publicInputs = publicInputs;
        this.maxSteps = maxSteps;
        this.resultVersion = resultVersion;
        this.status = TASK_STATUS.NEW;
    }

    public Task(String title, String text, int minutes, String assigner) {
        this.title = title;
        this.text = text;
        this.minutes = minutes;
        this.assigner = assigner;
        this.status = TASK_STATUS.NEW;
    }

    public TASK_STATUS getStatus() {
        return status;
    }

    public void setStatus(TASK_STATUS status) {
        this.status = status;
    }

    public boolean isPublicInputs() {
        return publicInputs;
    }

    public int getTask_id() {
        return task_id;
    }

    public void setTask_id(int task_id) {
        this.task_id = task_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    /**
     * Returns the time, in minutes, to solve the task. 0 means there is no time limit
     * @return the time, in minutes, to solve the task, or 0 if there is no time limit
     */
    public int getMinutes() {
        return minutes;
    }

    /**
     * Sets the time, in minutes, to solve the task. 0 means there is no time limit
     * @param minutes the time, in minutes, to solve the task, or 0 to set no time limit
     */
    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public String getAssigner() {
        return assigner;
    }

    public void setAssigner(String assigner) {
        this.assigner = assigner;
    }

    /**
     * Returns the time when the task was started
     * @return the time when the task was started
     */
    public long getStarted() {
        return started;
    }

    /**
     * Sets the time when the task was started
     * @param started the time when the task was started
     */
    public void setStarted(long started) {
        this.started = started;
    }

    /**
     * Returns true if the inputs are public, false otherwise
     * @return true if the inputs are public, false otherwise
     */
    public boolean getPublicInputs() {
        return publicInputs;
    }

    /**
     * Set whether or not inputs should be public
     * @param publicInputs true if inputs should be public, false otherwise
     */
    public void setPublicInputs(boolean publicInputs) {
        this.publicInputs = publicInputs;
    }

    /**
     * Returns the maximum number of steps the machine should be simulated for while solving the task
     * @return the maximum number of steps the machine should be simulated for while solving the task
     */
    public int getMaxSteps() {
        return maxSteps;
    }

    /**
     * Sets the maximum number of steps the machine should be simulated for while solving the task
     * @param maxSteps the maximum number of steps the machine should be simulated for while solving the task
     */
    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }

    public int getResultVersion() {
        return resultVersion;
    }

    public void setResultVersion(int resultVersion) {
        this.resultVersion = resultVersion;
    }
}
