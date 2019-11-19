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

    public Task getTaskFromJson(JSONObject object) throws JSONException {
        Task parsedTask;
        String task_name, task_description;
        int time, assigner_id, task_id;
        boolean public_input;

        task_name = object.getString(TASK_NAME_KEY);
        task_description = object.getString(TASK_DESCRIPTION_KEY);
        time = object.getInt(TIME_KEY);
        assigner_id = object.getInt(ASSIGNER_ID_KEY);
        task_id = object.getInt(TASK_ID_KEY);
        public_input = object.getBoolean(PUBLIC_INPUT_KEY);

        String automataType = object.getString(AUTOMATA_TYPE_KEY);

        switch (automataType) {
            case "finite_automata":
                return new FiniteAutomataTask(
                        task_name,
                        task_description,
                        time,
                        Integer.toString(assigner_id)
                );
            case "pushdown_automata":
                return new PushdownAutomataTask(
                        task_name,
                        task_description,
                        time,
                        Integer.toString(assigner_id)
                );
            case "linear_bounded_automata":
                return new LinearBoundedAutomataTask(
                        task_name,
                        task_description,
                        time,
                        Integer.toString(assigner_id)
                );
            case "turing_machine":
                return new TuringMachineTask(
                        task_name,
                        task_description,
                        time,
                        Integer.toString(assigner_id)
                );
        }

        return null;
    }

    public List<Task> getTasksFromJsonArray(String in) throws JSONException {
        List<Task> listOfTasks = new ArrayList<>();
        if ( in == null || in.isEmpty() )
            return listOfTasks;
        JSONArray array = new JSONArray(in);
        final int arrayLength = array.length();

        for (int i = 0; i < arrayLength; i++)
        {
            JSONObject currentObject = array.getJSONObject(i);
            Task parsedTask = getTaskFromJson(currentObject);
            listOfTasks.add(parsedTask);
        }

        return listOfTasks;
    }
}
