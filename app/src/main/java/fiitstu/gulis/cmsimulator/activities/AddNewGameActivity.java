package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Pair;
import android.view.*;
import android.widget.*;
import com.aditya.filebrowser.fileoperations.Operations;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.database.FileFormatException;
import fiitstu.gulis.cmsimulator.database.FileHandler;
import fiitstu.gulis.cmsimulator.dialogs.ExitAddNewGameDialog;
import fiitstu.gulis.cmsimulator.exceptions.NotImplementedException;
import fiitstu.gulis.cmsimulator.models.ChessGame;
import fiitstu.gulis.cmsimulator.models.tasks.automata_type;
import fiitstu.gulis.cmsimulator.network.ServerController;
import fiitstu.gulis.cmsimulator.network.ServerResponseController;
import fiitstu.gulis.cmsimulator.network.UrlManager;
import fiitstu.gulis.cmsimulator.views.ChessView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AddNewGameActivity extends FragmentActivity {
    private static final String TAG = "AddNewGameActivity";

    private static final int MAX_ALLOWED_STATES = 20;
    private static final int MIN_ALLOWED_STATES = 1;

    // UI ELEMENTS
    LinearLayout linearlayout_task_description;
    EditText edittext_task_name;
    EditText edittext_task_description;
    ChessView chessview_task;
    Button button_zoom_in;
    Button button_zoom_out;
    RadioButton radiobutton_finite_automata;
    RadioButton radiobutton_pushdown_automata;
    NumberPicker numberpicker_max_state_count;

    private float[] touchCoordinates = {0, 0};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_game);

        setActionBar();
        setUIElements();
        setEvents();
    }

    private automata_type getSelectedAutomataType() {
        if (radiobutton_finite_automata.isChecked())
            return automata_type.FINITE_AUTOMATA;
        else
            return automata_type.PUSHDOWN_AUTOMATA;
    }

    private void setUIElements() {
        linearlayout_task_description = findViewById(R.id.linearlayout_task_description);
        edittext_task_name = findViewById(R.id.edittext_task_name);
        edittext_task_description = findViewById(R.id.edittext_task_description);
        chessview_task = findViewById(R.id.chessview_task);
        button_zoom_in = findViewById(R.id.button_zoom_in);
        button_zoom_out = findViewById(R.id.button_zoom_out);
        radiobutton_finite_automata = findViewById(R.id.radiobutton_finite_automata);
        radiobutton_pushdown_automata = findViewById(R.id.radiobutton_pushdown_automata);
        numberpicker_max_state_count = findViewById(R.id.numberpicker_max_state_count);

        numberpicker_max_state_count.setMinValue(MIN_ALLOWED_STATES);
        numberpicker_max_state_count.setMaxValue(MAX_ALLOWED_STATES);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.menu_chessview_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final Pair<Integer, Integer> rectCoord = chessview_task.getRectCoordForTouchCoord(touchCoordinates[0], touchCoordinates[1]);
        switch (item.getItemId()) {
            case R.id.menu_set_as_start:
                try {
                    chessview_task.setStartField(rectCoord);
                } catch (ChessView.OutOfChessFieldException e) {
                    Toast.makeText(AddNewGameActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.menu_set_as_finish:
                try {
                    chessview_task.setFinishField(rectCoord);
                } catch (ChessView.OutOfChessFieldException e) {
                    Toast.makeText(AddNewGameActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void setEvents() {
        registerForContextMenu(chessview_task);

        chessview_task.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final float x = event.getX();
                final float y = event.getY();
                touchCoordinates[0] = x;
                touchCoordinates[1] = y;

                return false;
            }
        });

        chessview_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Pair<Integer, Integer> rectCoord = chessview_task.getRectCoordForTouchCoord(touchCoordinates[0], touchCoordinates[1]);
                try {
                    if (chessview_task.isFieldInPath(rectCoord))
                        chessview_task.removeFieldFromPath(rectCoord);
                    else
                        chessview_task.addFieldToPath(rectCoord);
                } catch (ChessView.OutOfChessFieldException e) {
                    Toast.makeText(AddNewGameActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        button_zoom_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int chessFieldWidth = chessview_task.getChessFieldWidth();
                final int chessFieldHeight = chessview_task.getChessFieldHeight();

                final Pair<Integer, Integer> targetSize = new Pair<>(chessFieldWidth - 1, chessFieldHeight - 1);
                try {
                    chessview_task.setChessFieldWidth(targetSize.first);
                    chessview_task.setChessFieldHeight(targetSize.second);
                } catch (ChessView.OutOfChessFieldBoundariesException e) {
                    Toast.makeText(AddNewGameActivity.this, R.string.game_field_size_exceeded, Toast.LENGTH_LONG).show();
                }
            }
        });

        button_zoom_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int chessFieldWidth = chessview_task.getChessFieldWidth();
                final int chessFieldHeight = chessview_task.getChessFieldHeight();

                final Pair<Integer, Integer> targetSize = new Pair<>(chessFieldWidth + 1, chessFieldHeight + 1);

                try {
                    chessview_task.setChessFieldWidth(targetSize.first);
                    chessview_task.setChessFieldHeight(targetSize.second);
                } catch (ChessView.OutOfChessFieldBoundariesException e) {
                    Toast.makeText(AddNewGameActivity.this, R.string.game_field_size_exceeded, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.menu_add_new_game, menu);

        return true;
    }

    private void setActionBar() {
        ActionBar actionBar = this.getActionBar();
        actionBar.setTitle(R.string.add_new_game);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_upload_task:
                // COMPLETED: IMPLEMENT GAME UPLOADING
                if (canUpload()) {
                    final String gameName = edittext_task_name.getText().toString().trim();
                    final String gameDescription = edittext_task_description.getText().toString().trim();
                    final Pair<Integer, Integer> startField = chessview_task.getStartField();
                    final Pair<Integer, Integer> finishField = chessview_task.getFinishField();
                    final Pair<Integer, Integer> fieldSize = chessview_task.getFieldSize();
                    final List<Pair<Integer, Integer>> pathList = chessview_task.getPath();
                    final int max_allowed_states = numberpicker_max_state_count.getValue();
                    final automata_type automata_type = radiobutton_finite_automata.isChecked() ? fiitstu.gulis.cmsimulator.models.tasks.automata_type.FINITE_AUTOMATA : radiobutton_pushdown_automata.isChecked() ? fiitstu.gulis.cmsimulator.models.tasks.automata_type.PUSHDOWN_AUTOMATA : fiitstu.gulis.cmsimulator.models.tasks.automata_type.UNKNOWN;

                    final ChessGame chessGame = new ChessGame(
                            startField,
                            finishField,
                            pathList,
                            fieldSize,
                            max_allowed_states,
                            automata_type
                    );
                    FileHandler fileHandler = new FileHandler(FileHandler.Format.CMSC);

                    try {
                        fileHandler.setData(chessGame);
                        fileHandler.writeFile("chessGame");
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (TransformerException e) {
                        e.printStackTrace();
                    }

                    final File file = new File(FileHandler.PATH + "/" + "chessGame" + "." + FileHandler.Format.CMSC.toString().toLowerCase());

                    class UploadGameAsync extends AsyncTask<Integer, Void, String> {
                        @Override
                        protected String doInBackground(Integer... integers) {
                            UrlManager urlManager = new UrlManager();
                            URL url = urlManager.getUploadGameURL(Integer.toString(integers[0]));
                            ServerController serverController = new ServerController();
                            String output = null;

                            output = serverController.doPostRequest(url, file);

                            return output;
                        }

                        @Override
                        protected void onPostExecute(String s) {
                            if (s == null || s.isEmpty()) {
                                Toast.makeText(AddNewGameActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                            } else {
                                if (s.equals("OK")) {
                                    Toast.makeText(AddNewGameActivity.this, R.string.game_upload_successful, Toast.LENGTH_SHORT).show();
                                } else
                                    Toast.makeText(AddNewGameActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();

                                AddNewGameActivity.this.finish();
                            }
                        }
                    }

                    class AddGameToDatabaseAsync extends AsyncTask<Void, Void, String> {
                        @Override
                        protected String doInBackground(Void... voids) {
                            UrlManager urlManager = new UrlManager();
                            automata_type selectedAutomata = getSelectedAutomataType();
                            URL url = urlManager.addGameToDatabaseURL(gameName, gameDescription, TaskLoginActivity.loggedUser.getUser_id(), selectedAutomata);
                            ServerController serverController = new ServerController();
                            String output = null;

                            try {
                                output = serverController.getResponseFromServer(url);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                return output;
                            }
                        }

                        @Override
                        protected void onPostExecute(String s) {
                            if (s == null || s.isEmpty()) {
                                Toast.makeText(AddNewGameActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                            } else {
                                try {
                                    JSONObject object = new JSONObject(s);
                                    int task_id = object.getInt("task_id");

                                    new UploadGameAsync().execute(task_id);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    }

                    new AddGameToDatabaseAsync().execute();
                } else Toast.makeText(this, R.string.task_not_set, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_settings:
                Intent settingsIntent = new Intent(this, OptionsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.menu_help:
                // TODO: SET HELP
                try {
                    throw new NotImplementedException(this);
                } catch (NotImplementedException e) {
                    e.printStackTrace();
                }
                return true;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        boolean modified = checkIfGameModified();
        if (modified) {
            final FragmentManager fragmentManager = this.getSupportFragmentManager();
            ExitAddNewGameDialog exitAddNewGameDialog = new ExitAddNewGameDialog();
            exitAddNewGameDialog.show(fragmentManager, TAG);
        } else super.onBackPressed();
    }

    private boolean checkIfGameModified() {
        boolean taskNameModified = checkIfNameSet();
        boolean taskDescriptionModified = checkIfDescriptionSet();
        boolean hasGameSet = checkIfGameSet();
        boolean automataSet = checkIfAutomataSet();

        return taskNameModified || taskDescriptionModified || hasGameSet || automataSet;
    }

    private boolean checkIfAutomataSet() {
        return (radiobutton_finite_automata.isChecked() || radiobutton_pushdown_automata.isChecked());
    }

    private boolean checkIfGameSet() {
        boolean hasStartField;
        boolean hasFinishField;
        boolean hasPath;

        hasStartField = chessview_task.getStartField().first != -1 || chessview_task.getStartField().second != -1;
        hasFinishField = chessview_task.getFinishField().first != -1 || chessview_task.getFinishField().second != -1;
        hasPath = chessview_task.getPath().size() > 0;

        return hasStartField || hasFinishField || hasPath;
    }

    private boolean checkIfNameSet() {
        return !edittext_task_name.getText().toString().isEmpty();
    }

    private boolean checkIfDescriptionSet() {
        return !edittext_task_description.getText().toString().isEmpty();
    }

    private boolean canUpload() {
        boolean nameSet = checkIfNameSet();
        boolean descriptionSet = checkIfDescriptionSet();
        boolean gameSet = checkIfGameSet();
        boolean automataSet = checkIfAutomataSet();

        return nameSet && descriptionSet && gameSet && automataSet;
    }
}
