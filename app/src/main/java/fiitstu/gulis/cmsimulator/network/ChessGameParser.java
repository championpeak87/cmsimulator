package fiitstu.gulis.cmsimulator.network;

import fiitstu.gulis.cmsimulator.elements.Task;
import fiitstu.gulis.cmsimulator.models.ChessGame;
import fiitstu.gulis.cmsimulator.models.ChessGameModel;
import fiitstu.gulis.cmsimulator.models.tasks.automata_type;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChessGameParser {
    private static final String TASK_ID_KEY = "task_id";
    private static final String ASSIGNER_ID_KEY = "assigner_id";
    private static final String TASK_NAME_KEY = "task_name";
    private static final String TASK_DESCRIPTION_KEY = "task_description";
    private static final String AUTOMATA_TYPE_KEY = "automata_type";
    private static final String TASK_STATUS = "task_status";

    public static ChessGameModel getGameFromJSONObject(JSONObject object) {
        int task_id;
        int assigner_id;
        String task_name;
        String task_description;
        automata_type automata_type;
        Task.TASK_STATUS status = Task.TASK_STATUS.NEW;

        ChessGameModel chessGame = null;
        try {
            task_id = object.getInt(TASK_ID_KEY);
            assigner_id = object.getInt(ASSIGNER_ID_KEY);
            task_name = object.getString(TASK_NAME_KEY);
            task_description = object.getString(TASK_DESCRIPTION_KEY);
            automata_type = object.getString(AUTOMATA_TYPE_KEY).equals(fiitstu.gulis.cmsimulator.models.tasks.automata_type.FINITE_AUTOMATA.getApiKey()) ? fiitstu.gulis.cmsimulator.models.tasks.automata_type.FINITE_AUTOMATA : fiitstu.gulis.cmsimulator.models.tasks.automata_type.PUSHDOWN_AUTOMATA;
            if (object.has(TASK_STATUS))
            {
                String st = object.getString(TASK_STATUS);
                switch (st)
                {
                    case "in_progress":
                        status = Task.TASK_STATUS.IN_PROGRESS;
                        break;
                    case "correct":
                        status = Task.TASK_STATUS.CORRECT;
                        break;
                    case "wrong":
                        status = Task.TASK_STATUS.WRONG;
                        break;

                }
            }
            else status = Task.TASK_STATUS.NEW;

            chessGame = new ChessGameModel(task_id, assigner_id, task_name, task_description, automata_type, status);
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            return chessGame;
        }
    }

    public static List<ChessGameModel> getListOfChessGamesFromJSONArray(JSONArray array) {
        final int arraySize = array.length();
        List<ChessGameModel> chessGameModelList = new ArrayList<>();
        for (int i = 0; i < arraySize; i++) {
            try {
                JSONObject currentObject = array.getJSONObject(i);
                ChessGameModel currentModel = getGameFromJSONObject(currentObject);
                chessGameModelList.add(currentModel);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return chessGameModelList;
    }
}
