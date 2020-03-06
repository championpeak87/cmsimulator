package fiitstu.gulis.cmsimulator.models.tasks.grammar_tasks;

import fiitstu.gulis.cmsimulator.elements.Task;

import java.sql.Date;
import java.sql.Time;

public class GrammarTask {
    private String title;
    private String text;
    private Time available_time;
    private Time remaining_time;
    private boolean publicInputs;
    private int task_id;
    private Task.TASK_STATUS status;
    private Date submission_date;
    private boolean submitted;

    public GrammarTask(String title, String text, Time available_time, boolean publicInputs) {
        this.title = title;
        this.text = text;
        this.available_time = available_time;
        this.publicInputs = publicInputs;
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

    public Time getAvailable_time() {
        return available_time;
    }

    public void setAvailable_time(Time available_time) {
        this.available_time = available_time;
    }

    public Time getRemaining_time() {
        return remaining_time;
    }

    public void setRemaining_time(Time remaining_time) {
        this.remaining_time = remaining_time;
    }

    public boolean isPublicInputs() {
        return publicInputs;
    }

    public void setPublicInputs(boolean publicInputs) {
        this.publicInputs = publicInputs;
    }

    public int getTask_id() {
        return task_id;
    }

    public void setTask_id(int task_id) {
        this.task_id = task_id;
    }

    public Task.TASK_STATUS getStatus() {
        return status;
    }

    public void setStatus(Task.TASK_STATUS status) {
        this.status = status;
    }

    public Date getSubmission_date() {
        return submission_date;
    }

    public void setSubmission_date(Date submission_date) {
        this.submission_date = submission_date;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }
}
