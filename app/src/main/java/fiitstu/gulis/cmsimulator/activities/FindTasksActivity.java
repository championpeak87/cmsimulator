package fiitstu.gulis.cmsimulator.activities;


import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.os.Bundle;

import android.support.annotation.VisibleForTesting;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.adapters.tasks.TaskListAdapter;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.database.FileFormatException;
import fiitstu.gulis.cmsimulator.database.FileHandler;
import fiitstu.gulis.cmsimulator.dialogs.GuideFragment;
import fiitstu.gulis.cmsimulator.dialogs.TaskDialog;
import fiitstu.gulis.cmsimulator.elements.Task;
import fiitstu.gulis.cmsimulator.network.TaskFinder;

import javax.xml.parsers.ParserConfigurationException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;

/**
 * An activity for finding and displaying tasks that are being assigned by other people in the network.
 *
 * Created by Jakub Sedlář on 05.01.2018.
 */
public class FindTasksActivity extends FragmentActivity implements TaskDialog.TaskDialogListener {

    //log tag
    private static final String TAG = FindTasksActivity.class.getName();

    private static final String TASK_DIALOG = "TASK_DIALOG";

    //maximum number of milliseconds a task will be kept without hearing from the sender
    private static final int MAX_LIFETIME = 10000;

    private TaskListAdapter taskListAdapter;

    private TaskFinder taskFinder;

    //maps tasks to their XML files (which also contain the related automaton) and remaining lifetimes
    private final Map<Task, TaskData> taskDataMap = new HashMap<>();

    //periodically deletes tasks that have not been refreshed for longer than the maximum lifetime
    private Timer reaper;

    private EditText nameEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_tasks);
        Log.v(TAG, "onCreate initialization started");
        DataSource dataSource = DataSource.getInstance();
        dataSource.open();

        //menu
        ActionBar actionBar = this.getActionBar();
        actionBar.setTitle(R.string.find_tasks);
        actionBar.setDisplayHomeAsUpEnabled(true);

        taskListAdapter = new TaskListAdapter();
        taskListAdapter.setItemClickListener(new TaskListAdapter.TaskClickListener() {
            @Override
            public void onClick(Task task) {
                int machineType;
                FileHandler fileHandler = new FileHandler(FileHandler.Format.CMST);
                try {
                    TaskData taskData;
                    synchronized (taskDataMap) {
                        taskData = taskDataMap.get(task);
                    }
                    if (taskData == null) { //the task disappeared just after the user tapped the button
                        return;
                    }
                    fileHandler.loadFromString(taskData.getDocument());
                    machineType = fileHandler.getMachineType();
                } catch (ParserConfigurationException e) {
                    Log.e(TAG, "Parser configuration error", e);
                    return;
                } catch (FileFormatException e) {
                    Log.e(TAG, "Invalid file format", e);
                    return;
                }

                TaskDialog taskDialog = TaskDialog.newInstance(task, TaskDialog.ENTERING, machineType);
                taskDialog.show(getSupportFragmentManager(), TASK_DIALOG);
                Log.v(TAG, "Task \"" + task.getTitle() + "\" button click noted");
            }
        });
        RecyclerView recyclerView = findViewById(R.id.recyclerView_find_tasks_tasks);
        recyclerView.setAdapter(taskListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        reaper = new Timer();

        nameEditText = findViewById(R.id.editText_find_tasks_name);
        nameEditText.setText(dataSource.getUserName());

        final Button refreshButton = findViewById(R.id.button_find_tasks_refresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //disable the button for a while to discourage spamming it (tasks may need a while to appear)
                refreshButton.setEnabled(false);
                taskFinder.close();
                taskListAdapter.clear();
                synchronized (taskDataMap) {
                    taskDataMap.clear();
                }
                initTaskFinder();
                new CountDownTimer(2000, 2000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }

                    @Override
                    public void onFinish() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refreshButton.setEnabled(true);
                            }
                        });
                    }
                }.start();
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
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_tasks_help:
                FragmentManager fm = getSupportFragmentManager();
                GuideFragment guideFragment = GuideFragment.newInstance(GuideFragment.SOLVING_TASKS);
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
    protected void onPause() {
        taskListAdapter.clear();
        synchronized (taskDataMap) {
            taskDataMap.clear();
        }
        taskFinder.close();
        reaper.cancel();
        DataSource.getInstance().updateUserName(nameEditText.getText().toString());
        DataSource.getInstance().close();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataSource.getInstance().open();
        initTaskFinder();
        reaper.schedule(new TimerTask() {
            @Override
            public void run() {
                List<Task> removedTasks = new ArrayList<>();
                for (Map.Entry<Task, TaskData> task: taskDataMap.entrySet()) {
                    task.getValue().setLifetime(task.getValue().getLifetime() - 1000);
                    if (task.getValue().getLifetime() <= 0) {
                        final Task t = task.getKey();
                        removedTasks.add(t);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                taskListAdapter.removeItem(t);
                            }
                        });
                    }
                }

                synchronized (taskDataMap) {
                    for (Task task : removedTasks) {
                        taskDataMap.remove(task);
                    }
                }
            }
        }, 1000, 1000);
    }

    @Override
    public void onTaskDialogClick(Task task, int machineType, int dialogMode) {
        TaskDialog taskDialog = (TaskDialog) getSupportFragmentManager().findFragmentByTag(TASK_DIALOG);
        if (nameEditText.getText().toString().isEmpty()) {
            Toast.makeText(this, R.string.no_name, Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle outputBundle = new Bundle();
        outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.SOLVE_TASK);
        outputBundle.putInt(MainActivity.MACHINE_TYPE, machineType);
        task.setStarted(SystemClock.elapsedRealtime());
        outputBundle.putSerializable(MainActivity.TASK, task);

        FileHandler fileHandler = new FileHandler(FileHandler.Format.CMST);
        try {
            TaskData taskData;
            synchronized (taskDataMap) {
                taskData = taskDataMap.get(task);
            }
            if (taskData == null) { //task disappeared while user was reading the dialog
                Toast.makeText(this, R.string.task_no_longer_available, Toast.LENGTH_LONG).show();
                return;
            }
            fileHandler.loadFromString(taskData.getDocument());
            fileHandler.getData(DataSource.getInstance());
            DataSource.getInstance().updateUserName(nameEditText.getText().toString());
        } catch (ParserConfigurationException e) {
            Log.e(TAG, "Parser configuration error", e);
            return;
        } catch (FileFormatException e) {
            Log.e(TAG, "Invalid file format", e);
            return;
        }

        taskDialog.dismiss();
        Intent nextActivityIntent = new Intent(this, SimulationActivity.class);
        nextActivityIntent.putExtras(outputBundle);
        startActivity(nextActivityIntent);
    }

    //initializes and starts the TaskFinder
    private void initTaskFinder() {
        try {
            taskFinder = new TaskFinder(new TaskConsumer());

            taskFinder.start();
        } catch (BindException e) {
            Log.e(TAG, "Failed to bind socket", e);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getApplicationContext().getResources().getString(R.string.error));
            builder.setMessage(getApplicationContext().getResources().getString(R.string.bind_socket_error));
            builder.setNegativeButton("OK", null);
            AlertDialog alert = builder.create();
            alert.show();
        } catch (SocketException e) {
            Log.e(TAG, "Failed to open socket", e);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getApplicationContext().getResources().getString(R.string.error));
            builder.setMessage(getApplicationContext().getResources().getString(R.string.unknown_socket_error));
            builder.setNegativeButton("OK", null);
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    /**
     * A small class for grouping different data related to the same client
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public static class TaskData {
        private String document;
        private int lifetime;

        public TaskData(String document) {
            this.document = document;
            this.lifetime = MAX_LIFETIME;
        }

        public String getDocument() {
            return document;
        }

        public void setDocument(String document) {
            this.document = document;
        }

        public int getLifetime() {
            return lifetime;
        }

        public void setLifetime(int lifetime) {
            this.lifetime = lifetime;
        }
    }

    /**
     * An implementation of TaskConsumer
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public class TaskConsumer implements TaskFinder.TaskConsumer {
        @Override
        public void receive(final String taskDoc, final InetAddress assigner) {
            FileHandler fileHandler = new FileHandler(FileHandler.Format.CMST);
            try {
                fileHandler.loadFromString(taskDoc);
                Task t = fileHandler.getTask();
                t.setAssigner(assigner.getHostAddress());
                Log.i(TAG, "Received task \"" + t.getTitle() + "\" from " + assigner.getHostAddress());

                synchronized (taskDataMap) {
                    taskDataMap.put(t, new TaskData(taskDoc));
                }
                final Task finalTask = t;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        taskListAdapter.addItem(finalTask);
                    }
                });
            } catch (ParserConfigurationException | FileFormatException e) {
                Log.e(TAG, "Could not parse the received task", e);
            }
        }

        @Override
        public void refresh(InetAddress assigner) {
            boolean found = false;
            for (Map.Entry<Task, TaskData> task: taskDataMap.entrySet()) {
                if (task.getKey().getAssigner().equals(assigner.getHostAddress())) {
                    task.getValue().setLifetime(MAX_LIFETIME);
                    found = true;
                    break;
                }
            }

            if (!found) {
                //the server thinks we have this task, but we don't;
                //change the request ID to force it to re-send it the next time we ask
                taskFinder.changeID();
            }
        }
    }
}
