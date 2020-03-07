package fiitstu.gulis.cmsimulator.network.grammar_tasks;

import fiitstu.gulis.cmsimulator.elements.Task;
import fiitstu.gulis.cmsimulator.models.tasks.grammar_tasks.GrammarTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GrammarTasksParser {
    private static final String TAG = "GrammarTasksParser";

    // SINGLETON
    private static GrammarTasksParser instance = null;

    private static final String TASK_NAME_KEY = "task_name";
    private static final String TASK_DESCRIPTION_KEY = "task_description";
    private static final String TIME_KEY = "time";
    private static final String PUBLIC_INPUT_KEY = "public_input";
    private static final String ASSIGNER_ID_KEY = "assigner_id";
    private static final String TASK_ID_KEY = "task_id";
    private static final String TASK_STATUS_KEY = "task_status";
    private static final String REMAINING_TIME_KEY = "remaining_time";

    // TIME KEYS
    private static final String HOURS_KEY = "hours";
    private static final String MINUTES_KEY = "minutes";
    private static final String SECONDS_KEY = "seconds";

    // STATUS KEYS
    private static final String IN_PROGRESS_KEY = Task.TASK_STATUS.IN_PROGRESS.toString();
    private static final String WRONG_KEY = Task.TASK_STATUS.WRONG.toString();
    private static final String CORRECT_KEY = Task.TASK_STATUS.CORRECT.toString();
    private static final String NEW_KEY = Task.TASK_STATUS.NEW.toString();

    private static final String SUBMITTED_KEY = "submitted";
    private static final String SUBMISSION_TIME = "submission_date";

    private GrammarTasksParser() {
    }

    public static GrammarTasksParser getInstance() {
        if (instance == null)
            instance = new GrammarTasksParser();
        return instance;
    }

    public GrammarTask getTaskFromJSON(JSONObject object) throws JSONException {
        GrammarTask parsedTask;
        String task_name, task_description;
        int assigner_id, task_id;
        boolean public_input;
        Task.TASK_STATUS status = Task.TASK_STATUS.NEW;
        Time available_time;
        Date submissionDate = null;

        final JSONObject timeObject = object.getJSONObject(TIME_KEY);
        final int hours, minutes, seconds;

        if (timeObject.has(HOURS_KEY)) {
            hours = timeObject.getInt(HOURS_KEY);
        } else hours = 0;
        if (timeObject.has(MINUTES_KEY)) {
            minutes = timeObject.getInt(MINUTES_KEY);
        } else minutes = 0;
        if (timeObject.has(SECONDS_KEY)) {
            seconds = timeObject.getInt(SECONDS_KEY);
        } else seconds = 0;

        final String sTime = String.format("%02d:%02d:%02d",
                hours,
                minutes,
                seconds);

        final int remainingHours, remainingMinutes, remainingSeconds;

        final Time remainingTime;
        if (!object.isNull(REMAINING_TIME_KEY)) {
            final JSONObject remainingTimeObject = object.getJSONObject(REMAINING_TIME_KEY);
            if (remainingTimeObject.has(HOURS_KEY)) {
                remainingHours = remainingTimeObject.getInt(HOURS_KEY);
            } else remainingHours = 0;
            if (remainingTimeObject.has(MINUTES_KEY)) {
                remainingMinutes = remainingTimeObject.getInt(MINUTES_KEY);
            } else remainingMinutes = 0;
            if (remainingTimeObject.has(SECONDS_KEY)) {
                remainingSeconds = remainingTimeObject.getInt(SECONDS_KEY);
            } else remainingSeconds = 0;

            final String sRemainingTime = String.format("%02d:%02d:%02d", remainingHours, remainingMinutes, remainingSeconds);
            remainingTime = Time.valueOf(sRemainingTime);
        } else {
            remainingTime = Time.valueOf(sTime);
        }

        task_name = object.getString(TASK_NAME_KEY);
        task_description = object.getString(TASK_DESCRIPTION_KEY);
        available_time = Time.valueOf(sTime);
        assigner_id = object.getInt(ASSIGNER_ID_KEY);
        task_id = object.getInt(TASK_ID_KEY);
        public_input = object.getBoolean(PUBLIC_INPUT_KEY);

        if (!object.isNull(SUBMITTED_KEY) && object.getBoolean(SUBMITTED_KEY)) {
            String submissionDateString = object.getString(SUBMISSION_TIME);
            submissionDateString = submissionDateString.replace('T', ' ');
            try {
                submissionDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(submissionDateString);
                submissionDate.setHours(submissionDate.getHours() + 1);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (object.isNull(TASK_STATUS_KEY))
            status = Task.TASK_STATUS.NEW;
        else
            switch (object.getString(TASK_STATUS_KEY)) {
                case "in_progress":
                    status = Task.TASK_STATUS.IN_PROGRESS;
                    break;
                case "new":
                    status = Task.TASK_STATUS.NEW;
                    break;
                case "wrong":
                    status = Task.TASK_STATUS.WRONG;
                    break;
                case "correct":
                    status = Task.TASK_STATUS.CORRECT;
                    break;
                case "too_late":
                    status = Task.TASK_STATUS.TOO_LATE;
                    break;
            }

        return new GrammarTask(
                task_name,
                task_description,
                available_time,
                remainingTime,
                public_input,
                task_id,
                status,
                submissionDate);
    }

    public List<GrammarTask> getTasksFromJSONArray(String input) throws JSONException {
        List<GrammarTask> listOfTasks = new ArrayList<>();
        if (input == null || input.isEmpty())
            return listOfTasks;
        JSONArray array = new JSONArray(input);
        final int arrayLength = array.length();

        for (int i = 0; i < arrayLength; i++)
        {
            JSONObject currentObject =array.getJSONObject(i);
            GrammarTask parsedTask = getTaskFromJSON(currentObject);
            listOfTasks.add(parsedTask);
        }

        return listOfTasks;
    }
}
