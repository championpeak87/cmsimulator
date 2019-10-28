package fiitstu.gulis.cmsimulator.activities;

import android.Manifest;
import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.database.FileFormatException;
import fiitstu.gulis.cmsimulator.database.FileHandler;
import fiitstu.gulis.cmsimulator.dialogs.ExampleTaskDialog;
import fiitstu.gulis.cmsimulator.dialogs.GuideFragment;
import fiitstu.gulis.cmsimulator.dialogs.TasksGameDialog;
import fiitstu.gulis.cmsimulator.elements.Task;
import fiitstu.gulis.cmsimulator.dialogs.FileSelector;

import java.io.IOException;

/**
 * A main-ish menu for task-related activities.
 * <p>
 * Created by Jakub Sedlář on 05.01.2018.
 */
public class TasksActivity extends FragmentActivity implements ExampleTaskDialog.ExampleTaskDialogListener, TasksGameDialog.TasksGameDialogListener {

    //log tag
    private static final String TAG = TasksActivity.class.getName();

    private static final String EXAMPLE_DIALOG = "EXAMPLE_DIALOG";
    private static final String GAME_DIALOG = "GAME_DIALOG";
    private static final String TASK_CONFIGURATION = "TASK_CONFIGURATION";
    public static final String GAME_EXAMPLE_NUMBER = "GAME_EXAMPLE_NUMBER";

    public static final int GAME_EXAMPLE_PREVIEW = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);
        Log.v(TAG, "onCreate initialization started");

        //menu
        ActionBar actionBar = this.getActionBar();
        actionBar.setTitle(R.string.tasks);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Button findTasksButton = findViewById(R.id.button_tasks_find);
        findTasksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextActivityIntent = new Intent(TasksActivity.this, FindTasksActivity.class);
                startActivity(nextActivityIntent);
            }
        });

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
        });

        Button game = findViewById(R.id.button_tasks_game);
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
        menuInflater.inflate(R.menu.menu_tasks, menu);

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
        }

        return false;
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

                    Intent nextActivityIntent = new Intent(TasksActivity.this, EditTaskActivity.class);
                    nextActivityIntent.putExtras(outputBundle);
                    startActivity(nextActivityIntent);
                    Log.i(TAG, "editTask activity intent executed");
                } catch (FileFormatException | IOException e) {
                    Log.e(TAG, "Could not read file", e);
                    Toast.makeText(TasksActivity.this, getResources().getString(R.string.file_not_loaded), Toast.LENGTH_SHORT).show();
                }
            }
        });
        fileSelector.setExceptionListener(new FileSelector.ExceptionListener() {
            @Override
            public void onException(Exception e) {
                Toast.makeText(TasksActivity.this, getResources().getString(R.string.file_not_loaded), Toast.LENGTH_SHORT).show();
            }
        });
        fileSelector.selectFile(TasksActivity.this);
    }

    @Override
    public void tasksGameDialogClick(String assetName) {
        Toast.makeText(getApplicationContext(), "TOTO JE TOAST", Toast.LENGTH_LONG).show();

        Bundle bundle = new Bundle();
        bundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.FINITE_STATE_AUTOMATON);
        bundle.putInt(TASK_CONFIGURATION, MainActivity.GAME_MACHINE);
        bundle.putInt(GAME_EXAMPLE_NUMBER, GAME_EXAMPLE_PREVIEW);

        Intent automata = new Intent(TasksActivity.this, ConfigurationActivity.class);
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

            Intent nextActivityIntent = new Intent(TasksActivity.this, EditTaskActivity.class);
            nextActivityIntent.putExtras(outputBundle);
            startActivity(nextActivityIntent);
            Log.i(TAG, "editTask activity intent executed");
        } catch (FileFormatException | IOException e) {
            Log.e(TAG, "Could not read file", e);
            Toast.makeText(TasksActivity.this, getResources().getString(R.string.file_not_loaded), Toast.LENGTH_SHORT).show();
        }

        ExampleTaskDialog dialog = (ExampleTaskDialog) getSupportFragmentManager()
                .findFragmentByTag(EXAMPLE_DIALOG);
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
