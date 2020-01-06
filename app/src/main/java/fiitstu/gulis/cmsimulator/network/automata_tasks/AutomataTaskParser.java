package fiitstu.gulis.cmsimulator.network.automata_tasks;

import android.util.JsonReader;
import fiitstu.gulis.cmsimulator.elements.Task;
import fiitstu.gulis.cmsimulator.models.tasks.automata_tasks.*;
import fiitstu.gulis.cmsimulator.models.tasks.automata_type;
import fiitstu.gulis.cmsimulator.models.tasks.deterministic;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class AutomataTaskParser {

    private static final String TASK_NAME_KEY = "task_name";
    private static final String TASK_DESCRIPTION_KEY = "task_description";
    private static final String TIME_KEY = "time";
    private static final String PUBLIC_INPUT_KEY = "public_input";
    private static final String ASSIGNER_ID_KEY = "assigner_id";
    private static final String AUTOMATA_TYPE_KEY = "automata_type";
    private static final String TASK_ID_KEY = "task_id";
    private static final String FILE_NAME_KEY = "file_name";
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

    public Task getTaskFromJson(JSONObject object) throws JSONException {
        Task parsedTask;
        String task_name, task_description;
        int assigner_id, task_id;
        boolean public_input;
        Task.TASK_STATUS status = Task.TASK_STATUS.NEW;
        Time available_time;

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
        }
        else
        {
            remainingTime = Time.valueOf(sTime);
        }

        task_name = object.getString(TASK_NAME_KEY);
        task_description = object.getString(TASK_DESCRIPTION_KEY);
        available_time = Time.valueOf(sTime);
        assigner_id = object.getInt(ASSIGNER_ID_KEY);
        task_id = object.getInt(TASK_ID_KEY);
        public_input = object.getBoolean(PUBLIC_INPUT_KEY);

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

        String automataType = object.getString(AUTOMATA_TYPE_KEY);

        switch (automataType) {
            case "finite_automata":
                return new FiniteAutomataTask(
                        task_name,
                        task_description,
                        available_time,
                        remainingTime,
                        Integer.toString(assigner_id),
                        task_id,
                        public_input,
                        status
                );
            case "pushdown_automata":
                return new PushdownAutomataTask(
                        task_name,
                        task_description,
                        available_time,
                        remainingTime,
                        Integer.toString(assigner_id),
                        task_id,
                        public_input,
                        status
                );
            case "linear_bounded_automata":
                return new LinearBoundedAutomataTask(
                        task_name,
                        task_description,
                        available_time,
                        remainingTime,
                        Integer.toString(assigner_id),
                        task_id,
                        public_input,
                        status
                );
            case "turing_machine":
                return new TuringMachineTask(
                        task_name,
                        task_description,
                        available_time,
                        remainingTime,
                        Integer.toString(assigner_id),
                        task_id,
                        public_input,
                        status
                );
        }

        return null;
    }

    public List<Task> getTasksFromJsonArray(String in) throws JSONException {
        List<Task> listOfTasks = new ArrayList<>();
        if (in == null || in.isEmpty())
            return listOfTasks;
        JSONArray array = new JSONArray(in);
        final int arrayLength = array.length();

        for (int i = 0; i < arrayLength; i++) {
            JSONObject currentObject = array.getJSONObject(i);
            Task parsedTask = getTaskFromJson(currentObject);
            listOfTasks.add(parsedTask);
        }

        return listOfTasks;
    }
}
