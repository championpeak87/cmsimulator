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

    private int resultVersion;

    public Task() {
        publicInputs = true;
        resultVersion = TaskResult.CURRENT_VERSION;
    }

    public Task(String title, String text, int minutes, boolean publicInputs, int maxSteps, int resultVersion) {
        this.title = title;
        this.text = text;
        this.minutes = minutes;
        this.publicInputs = publicInputs;
        this.maxSteps = maxSteps;
        this.resultVersion = resultVersion;
    }

    public Task(String title, String text, int minutes, String assigner) {
        this.title = title;
        this.text = text;
        this.minutes = minutes;
        this.assigner = assigner;
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
