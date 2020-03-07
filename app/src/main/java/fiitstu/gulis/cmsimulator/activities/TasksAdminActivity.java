package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

import java.io.IOException;
import java.net.URL;

import static fiitstu.gulis.cmsimulator.activities.TaskLoginActivity.loggedUser;

/**
 * A main-ish menu for task-related activities.
 * <p>
 * Created by Jakub Sedlář on 05.01.2018.
 */
public class TasksAdminActivity extends FragmentActivity implements ExampleTaskDialog.ExampleTaskDialogListener, TasksGameDialog.TasksGameDialogListener {

    //log tag
    private static final String TAG = TasksAdminActivity.class.getName();

    private static final String EXAMPLE_DIALOG = "EXAMPLE_DIALOG";
    private static final String GAME_DIALOG = "GAME_DIALOG";
    public static final String TASK_CONFIGURATION = "TASK_CONFIGURATION";
    public static final String GAME_EXAMPLE_NUMBER = "GAME_EXAMPLE_NUMBER";

    public static final int GAME_EXAMPLE_PREVIEW = 0;

    public static User loggedUser = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_admin);
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

        TextView fullnameTextView = findViewById(R.id.textview_tasks_fullname);
        fullnameTextView.setText(loggedUser.getLast_name() + ", " + loggedUser.getFirst_name());

        TextView usernameTextView = findViewById(R.id.textview_tasks_username);
        usernameTextView.setText(loggedUser.getUsername());

        //menu
        ActionBar actionBar = this.getActionBar();
        actionBar.setTitle(R.string.tasks);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Button example = findViewById(R.id.button_tasks_find_automata);
        example.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loggedUser == null) {
                    Toast.makeText(TasksAdminActivity.this, R.string.logged_out, Toast.LENGTH_LONG).show();
                    TasksAdminActivity.this.finish();
                } else {
                    Intent nextActivityIntent = new Intent(TasksAdminActivity.this, EditTaskActivity.class);
                    nextActivityIntent.putExtra("LOGGED_USER_ID", loggedUser.getUser_id());
                    startActivity(nextActivityIntent);
                }
            }
        });

        Button findAutomataTasks = findViewById(R.id.button_tasks_create_automata);
        findAutomataTasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loggedUser == null)
                {
                    Toast.makeText(TasksAdminActivity.this, R.string.logged_out, Toast.LENGTH_LONG).show();
                    TasksAdminActivity.this.finish();
                }
                else {
                    Intent intent = new Intent(TasksAdminActivity.this, BrowseAutomataTasksActivity.class);
                    intent.putExtra("USER_ID", loggedUser.getUser_id());
                    intent.putExtra("AUTHKEY", loggedUser.getAuth_key());
                    startActivity(intent);
                }
            }
        });

        Button viewResultsButton = findViewById(R.id.button_tasks_results_automata);
        viewResultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextActivityIntent = new Intent(TasksAdminActivity.this, UsersManagmentActivity.class);
                nextActivityIntent.putExtra("VIEW_AUTOMATA_RESULTS", true);
                startActivity(nextActivityIntent);
            }
        });

        Button createNewTaskGrammar = findViewById(R.id.button_tasks_create_grammar);
        createNewTaskGrammar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent nextActivityIntent = new Intent(TasksAdminActivity.this, NewGrammarTaskActivity.class);
                startActivity(nextActivityIntent);
            }
        });

        Button findTasksButton = findViewById(R.id.button_tasks_find_grammar);
        findTasksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextActivityIntent = new Intent(TasksAdminActivity.this, BrowseGrammarTasksActivity.class);
                startActivity(nextActivityIntent);
            }
        });/*

        Button newTaskButton = findViewById(R.id.button_tasks_new);
        newTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextActivityIntent = new Intent(TasksActivity.this, EditTaskActivity.class);
                nextActivityIntent.putExtras(new Bundle());
                startActivity(nextActivityIntent);
            }
        });

        Button loadTaskButton = findViewById(R.id.button_tasks_load);
        loadTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT > 15
                        && ContextCompat.checkSelfPermission(TasksActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(TasksActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MainActivity.REQUEST_READ_STORAGE);
                } else {
                    loadTask();
                }
            }
        });

        Button exampleTaskButton = findViewById(R.id.button_tasks_examples);
        exampleTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                ExampleTaskDialog exampleTaskDialog = ExampleTaskDialog.newInstance();
                exampleTaskDialog.show(fm, EXAMPLE_DIALOG);

            }
        });*/

        Button game = findViewById(R.id.button_tasks_games_automatas);
        game.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                TasksGameDialog dialog = TasksGameDialog.newInstance();
                dialog.show(fm, GAME_DIALOG);
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

                    Intent nextActivityIntent = new Intent(TasksAdminActivity.this, EditTaskActivity.class);
                    nextActivityIntent.putExtras(outputBundle);
                    startActivity(nextActivityIntent);
                    Log.i(TAG, "editTask activity intent executed");
                } catch (FileFormatException | IOException e) {
                    Log.e(TAG, "Could not read file", e);
                    Toast.makeText(TasksAdminActivity.this, getResources().getString(R.string.file_not_loaded), Toast.LENGTH_SHORT).show();
                }
            }
        });
        fileSelector.setExceptionListener(new FileSelector.ExceptionListener() {
            @Override
            public void onException(Exception e) {
                Toast.makeText(TasksAdminActivity.this, getResources().getString(R.string.file_not_loaded), Toast.LENGTH_SHORT).show();
            }
        });
        fileSelector.selectFile(TasksAdminActivity.this);
    }

    @Override
    public void tasksGameDialogClick(String assetName) {
        Toast.makeText(getApplicationContext(), "TOTO JE TOAST", Toast.LENGTH_LONG).show();

        Bundle bundle = new Bundle();
        bundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.FINITE_STATE_AUTOMATON);
        bundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.NEW_MACHINE);
        bundle.putInt(TASK_CONFIGURATION, MainActivity.GAME_MACHINE);
        bundle.putInt(GAME_EXAMPLE_NUMBER, GAME_EXAMPLE_PREVIEW);

        Intent automata = new Intent(TasksAdminActivity.this, SimulationActivity.class);
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

            Intent nextActivityIntent = new Intent(TasksAdminActivity.this, EditTaskActivity.class);
            nextActivityIntent.putExtras(outputBundle);
            startActivity(nextActivityIntent);
            Log.i(TAG, "editTask activity intent executed");
        } catch (FileFormatException | IOException e) {
            Log.e(TAG, "Could not read file", e);
            Toast.makeText(TasksAdminActivity.this, getResources().getString(R.string.file_not_loaded), Toast.LENGTH_SHORT).show();
        }

        ExampleTaskDialog dialog = (ExampleTaskDialog) getSupportFragmentManager()
                .findFragmentByTag(EXAMPLE_DIALOG);
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
