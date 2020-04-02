package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.database.FileFormatException;
import fiitstu.gulis.cmsimulator.database.FileHandler;
import fiitstu.gulis.cmsimulator.dialogs.*;
import fiitstu.gulis.cmsimulator.elements.Task;
import fiitstu.gulis.cmsimulator.models.users.Admin;
import fiitstu.gulis.cmsimulator.models.users.Lector;
import fiitstu.gulis.cmsimulator.models.users.Student;
import fiitstu.gulis.cmsimulator.models.users.User;
import fiitstu.gulis.cmsimulator.network.ServerController;
import fiitstu.gulis.cmsimulator.network.UrlManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

/**
 * A main-ish menu for task-related activities.
 * <p>
 * Created by Jakub Sedlář on 05.01.2018.
 */
public class TasksStudentActivity extends FragmentActivity implements ExampleTaskDialog.ExampleTaskDialogListener, TasksGameDialog.TasksGameDialogListener {

    //log tag
    private static final String TAG = TasksStudentActivity.class.getName();

    private static final String EXAMPLE_DIALOG = "EXAMPLE_DIALOG";
    private static final String GAME_DIALOG = "GAME_DIALOG";
    public static final String TASK_CONFIGURATION = "TASK_CONFIGURATION";
    public static final String GAME_EXAMPLE_NUMBER = "GAME_EXAMPLE_NUMBER";

    public static final int GAME_EXAMPLE_PREVIEW = 0;

    private EditText edittext_automatas_new, edittext_automatas_in_progress, edittext_automatas_correct, edittext_automatas_wrong, edittext_automatas_too_late;
    private EditText edittext_grammars_new, edittext_grammars_in_progress, edittext_grammars_correct, edittext_grammars_wrong, edittext_grammars_too_late;
    private int automata_tasks_count = 0, grammar_tasks_count = 0;

    public static User loggedUser = null;

    private void setData() {
        new FetchAutomataTasksCountAsync().execute();
        new FetchGrammarTasksCountAsync().execute();
    }

    class FetchUserResultsAsync extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... integers) {
            ServerController serverController = new ServerController();
            UrlManager urlManager = new UrlManager();
            URL url = urlManager.getUserResultsOverviewURL(integers[0]);
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
                Toast.makeText(TasksStudentActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONArray array = new JSONArray(s);
                    final int arraySize = array.length();

                    int automatas_correct = 0, automatas_new = 0, automatas_in_progress = 0, automatas_wrong = 0, automatas_too_late = 0;
                    for (int i = 0; i < arraySize; i++) {
                        JSONObject object = array.getJSONObject(i);
                        if (object.has("found"))
                            break;
                        String currentStatus = object.getString("task_status");

                        switch (currentStatus) {
                            case "new":
                                automatas_new = object.getInt("count");
                                break;
                            case "in_progress":
                                automatas_in_progress = object.getInt("count");
                                break;
                            case "correct":
                                automatas_correct = object.getInt("count");
                                break;
                            case "wrong":
                                automatas_wrong = object.getInt("count");
                                break;
                            case "too_late":
                                automatas_too_late = object.getInt("count");
                                break;
                            default:
                                break;
                        }
                    }
                    edittext_automatas_new.setText(Integer.toString(automatas_new) + " / " + Integer.toString(automata_tasks_count));
                    edittext_automatas_in_progress.setText(Integer.toString(automatas_in_progress) + " / " + Integer.toString(automata_tasks_count));
                    edittext_automatas_correct.setText(Integer.toString(automatas_correct) + " / " + Integer.toString(automata_tasks_count));
                    edittext_automatas_wrong.setText(Integer.toString(automatas_wrong) + " / " + Integer.toString(automata_tasks_count));
                    edittext_automatas_too_late.setText(Integer.toString(automatas_too_late) + " / " + Integer.toString(automata_tasks_count));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(TasksStudentActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    class FetchGrammarResultsAsync extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... integers) {
            ServerController serverController = new ServerController();
            UrlManager urlManager = new UrlManager();
            URL url = urlManager.getGrammarResultsOverviewURL(integers[0]);
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
                Toast.makeText(TasksStudentActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONArray array = new JSONArray(s);
                    final int arraySize = array.length();

                    int grammars_correct = 0, grammars_new = 0, grammars_in_progress = 0, grammars_wrong = 0, grammars_too_late = 0;
                    for (int i = 0; i < arraySize; i++) {
                        JSONObject object = array.getJSONObject(i);
                        if (object.has("found"))
                            break;
                        String currentStatus = object.getString("task_status");

                        switch (currentStatus) {
                            case "new":
                                grammars_new = object.getInt("count");
                                break;
                            case "in_progress":
                                grammars_in_progress = object.getInt("count");
                                break;
                            case "correct":
                                grammars_correct = object.getInt("count");
                                break;
                            case "wrong":
                                grammars_wrong = object.getInt("count");
                                break;
                            case "too_late":
                                grammars_too_late = object.getInt("count");
                                break;
                            default:
                                break;
                        }
                    }
                    edittext_grammars_new.setText(Integer.toString(grammars_new) + " / " + Integer.toString(grammar_tasks_count));
                    edittext_grammars_in_progress.setText(Integer.toString(grammars_in_progress) + " / " + Integer.toString(grammar_tasks_count));
                    edittext_grammars_correct.setText(Integer.toString(grammars_correct) + " / " + Integer.toString(grammar_tasks_count));
                    edittext_grammars_wrong.setText(Integer.toString(grammars_wrong) + " / " + Integer.toString(grammar_tasks_count));
                    edittext_grammars_too_late.setText(Integer.toString(grammars_too_late) + " / " + Integer.toString(grammar_tasks_count));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(TasksStudentActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    class FetchAutomataTasksCountAsync extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            ServerController serverController = new ServerController();
            UrlManager urlManager = new UrlManager();
            URL url = urlManager.getAutomataTasksCountURL();
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
                Toast.makeText(TasksStudentActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONArray array = new JSONArray(s);
                    JSONObject object = array.getJSONObject(0);
                    automata_tasks_count = object.getInt("count");
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(TasksStudentActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                }
                new FetchUserResultsAsync().execute(TaskLoginActivity.loggedUser.getUser_id());
            }
        }
    }

    class FetchGrammarTasksCountAsync extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            ServerController serverController = new ServerController();
            UrlManager urlManager = new UrlManager();
            URL url = urlManager.getGrammarTasksCountURL();
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
                Toast.makeText(TasksStudentActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONArray array = new JSONArray(s);
                    JSONObject object = array.getJSONObject(0);
                    grammar_tasks_count = object.getInt("count");
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(TasksStudentActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                }
            }
            new FetchGrammarResultsAsync().execute(TaskLoginActivity.loggedUser.getUser_id());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_student);
        Log.v(TAG, "onCreate initialization started");

        Bundle bundle;
        bundle = getIntent().getExtras();

        String username = bundle.getString(User.USERNAME_KEY),
                first_name = bundle.getString(User.FIRST_NAME_KEY),
                last_name = bundle.getString(User.LAST_NAME_KEY),
                authkey = bundle.getString(User.AUTHKEY_KEY),
                user_type = bundle.getString(User.USER_TYPE_KEY);

        int user_id = bundle.getInt(User.USER_ID_KEY);

        final String Lector_tag = Lector.class.getName();
        final String Student_tag = Student.class.getName();
        final String Admin_tag = Admin.class.getName();

        if (user_type.equals(Lector_tag))
            loggedUser = new Lector(username, first_name, last_name, user_id, authkey);
        else if (user_type.equals(Student_tag))
            loggedUser = new Admin(username, first_name, last_name, user_id, authkey);
        else if (user_type.equals(Admin_tag))
            loggedUser = new Student(username, first_name, last_name, user_id, authkey);

        edittext_automatas_new = findViewById(R.id.edittext_automatas_new);
        edittext_automatas_in_progress = findViewById(R.id.edittext_automatas_in_progress);
        edittext_automatas_correct = findViewById(R.id.edittext_automatas_correct);
        edittext_automatas_wrong = findViewById(R.id.edittext_automatas_wrong);
        edittext_automatas_too_late = findViewById(R.id.edittext_automatas_too_late);

        edittext_grammars_new = findViewById(R.id.edittext_grammars_new);
        edittext_grammars_in_progress = findViewById(R.id.edittext_grammars_in_progress);
        edittext_grammars_correct = findViewById(R.id.edittext_grammars_correct);
        edittext_grammars_wrong = findViewById(R.id.edittext_grammars_wrong);
        edittext_grammars_too_late = findViewById(R.id.edittext_grammars_too_late);

        setData();

        TextView fullnameTextView = findViewById(R.id.textview_tasks_fullname);
        fullnameTextView.setText(loggedUser.getLast_name() + ", " + loggedUser.getFirst_name());

        TextView usernameTextView = findViewById(R.id.textview_tasks_username);
        usernameTextView.setText(loggedUser.getUsername());

        //menu
        ActionBar actionBar = this.getActionBar();
        actionBar.setTitle(R.string.tasks);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Button findAutomataTasks = findViewById(R.id.button_tasks_find_automata);
        findAutomataTasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loggedUser == null) {
                    Toast.makeText(TasksStudentActivity.this, R.string.logged_out, Toast.LENGTH_LONG).show();
                    TasksStudentActivity.this.finish();
                } else {
                    Intent intent = new Intent(TasksStudentActivity.this, BrowseAutomataTasksActivity.class);
                    intent.putExtra("USER_ID", loggedUser.getUser_id());
                    intent.putExtra("AUTHKEY", loggedUser.getAuth_key());
                    startActivity(intent);
                }
            }
        });

        Button findGrammarButton = findViewById(R.id.button_tasks_find_grammar);
        findGrammarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextActivityIntent = new Intent(TasksStudentActivity.this, BrowseGrammarTasksActivity.class);
                startActivity(nextActivityIntent);
            }
        });

        Log.i(TAG, "onCreate initialized");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.menu_tasks_admin, menu);

        return true;
    }

    public void signOut(View view) {
        Intent signInActivity = new Intent(this, TaskLoginActivity.class);
        startActivity(signInActivity);

        loggedUser = null;

        Context context = this.getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(
                TaskLoginActivity.SETTINGS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(TaskLoginActivity.AUTOLOGIN_SETTING, false);
        editor.putString(TaskLoginActivity.AUTOLOGIN_USERNAME, "");
        editor.putString(TaskLoginActivity.AUTOLOGIN_AUTHKEY, "");
        editor.commit();

        finish();
    }

    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {

        menu.getItem(1).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_tasks_help:
                FragmentManager fm = getSupportFragmentManager();
                GuideFragment guideFragment = GuideFragment.newInstance(GuideFragment.TASKS);
                guideFragment.show(fm, "HELP_DIALOG");
                return true;
            case R.id.menu_tasks_change_password:
                changePassword(null);
                return true;
            case R.id.menu_tasks_sign_out:
                signOut(null);
                return true;
            case R.id.menu_edit_task_upload_task:
                // TODO: Implement task uploading
                Toast.makeText(this, "THIS FEATURE HAS NOT BEEN IMPLEMENTED", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_tasks_manage_users:
                Intent usersActivity = new Intent(this, UsersManagmentActivity.class);
                usersActivity.putExtra("LOGGED_USER_AUTHKEY", loggedUser.getAuth_key());
                usersActivity.putExtra("LOGGED_USER_ID", loggedUser.getUser_id());


                startActivity(usersActivity);
        }

        return false;
    }

    public void findAutomataTasks(View view) {

    }

    @Override
    public void onBackPressed() {
        Log.v(TAG, "onBackPressed method started");
        finish();
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MainActivity.REQUEST_READ_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadTask();
                }
            }
        }
    }

    public void changePassword(View view) {
        ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog();
        changePasswordDialog.show(this.getSupportFragmentManager(), "Change_password");
    }

    private void loadTask() {
        FileSelector fileSelector = new FileSelector();
        fileSelector.setFileSelectedListener(new FileSelector.FileSelectedListener() {
            @Override
            public void onFileSelected(String filePath, FileHandler.Format format) {
                FileHandler fileHandler = new FileHandler(FileHandler.Format.CMST);
                DataSource dataSource = DataSource.getInstance();
                try {
                    fileHandler.loadFile(filePath);
                    Bundle outputBundle = new Bundle();
                    dataSource.open();
                    fileHandler.getData(dataSource);
                    dataSource.close();
                    int machineType = fileHandler.getMachineType();
                    outputBundle.putInt(MainActivity.MACHINE_TYPE, machineType);
                    Task task = fileHandler.getTask();

                    outputBundle.putSerializable(EditTaskActivity.TASK, task);
                    Log.v(TAG, "outputBundle initialized");

                    Intent nextActivityIntent = new Intent(TasksStudentActivity.this, EditTaskActivity.class);
                    nextActivityIntent.putExtras(outputBundle);
                    startActivity(nextActivityIntent);
                    Log.i(TAG, "editTask activity intent executed");
                } catch (FileFormatException | IOException e) {
                    Log.e(TAG, "Could not read file", e);
                    Toast.makeText(TasksStudentActivity.this, getResources().getString(R.string.file_not_loaded), Toast.LENGTH_SHORT).show();
                }
            }
        });
        fileSelector.setExceptionListener(new FileSelector.ExceptionListener() {
            @Override
            public void onException(Exception e) {
                Toast.makeText(TasksStudentActivity.this, getResources().getString(R.string.file_not_loaded), Toast.LENGTH_SHORT).show();
            }
        });
        fileSelector.selectFile(TasksStudentActivity.this);
    }

    @Override
    public void tasksGameDialogClick(String assetName) {
        Toast.makeText(getApplicationContext(), "TOTO JE TOAST", Toast.LENGTH_LONG).show();

        Bundle bundle = new Bundle();
        bundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.FINITE_STATE_AUTOMATON);
        bundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.NEW_MACHINE);
        bundle.putInt(TASK_CONFIGURATION, MainActivity.GAME_MACHINE);
        bundle.putInt(GAME_EXAMPLE_NUMBER, GAME_EXAMPLE_PREVIEW);

        Intent automata = new Intent(TasksStudentActivity.this, SimulationActivity.class);
        automata.putExtras(bundle);
        startActivity(automata);
        Log.i(TAG, "game started");
    }

    @Override
    public void exampleTaskDialogClick(String assetName) {
        FileHandler fileHandler = new FileHandler(FileHandler.Format.CMST);
        DataSource dataSource = DataSource.getInstance();
        try {
            fileHandler.loadAsset(getAssets(), assetName);
            Bundle outputBundle = new Bundle();
            dataSource.open();
            fileHandler.getData(dataSource);
            dataSource.close();
            int machineType = fileHandler.getMachineType();
            outputBundle.putInt(MainActivity.MACHINE_TYPE, machineType);
            Task task = fileHandler.getTask();

            outputBundle.putSerializable(EditTaskActivity.TASK, task);
            Log.v(TAG, "outputBundle initialized");

            Intent nextActivityIntent = new Intent(TasksStudentActivity.this, EditTaskActivity.class);
            nextActivityIntent.putExtras(outputBundle);
            startActivity(nextActivityIntent);
            Log.i(TAG, "editTask activity intent executed");
        } catch (FileFormatException | IOException e) {
            Log.e(TAG, "Could not read file", e);
            Toast.makeText(TasksStudentActivity.this, getResources().getString(R.string.file_not_loaded), Toast.LENGTH_SHORT).show();
        }

        ExampleTaskDialog dialog = (ExampleTaskDialog) getSupportFragmentManager()
                .findFragmentByTag(EXAMPLE_DIALOG);
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
