package fiitstu.gulis.cmsimulator.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import fiitstu.gulis.cmsimulator.database.FileHandler;
import fiitstu.gulis.cmsimulator.dialogs.GuideFragment;
import org.xmlpull.v1.XmlPullParserException;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.adapters.tasks.TaskResultListAdapter;
import fiitstu.gulis.cmsimulator.database.FileFormatException;
import fiitstu.gulis.cmsimulator.elements.TaskResult;
import fiitstu.gulis.cmsimulator.network.TaskServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.BindException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * An activity for controlling a task that is currently being assigned to others and
 * for viewing submitted results.
 *
 * Expected Intent arguments (extras) (KEY (TYPE) - MEANING):
 * TASK_DOCUMENT (String) - an XML document containing the task, the test inputs and the automaton
 * TIME (int) - the time, in minutes, reserved to solving the task, 0 if unlimited
 * TITLE (String) - the displayed title of the activity, typically the title of the task
 *
 * Created by Jakub Sedlář on 15.01.2018.
 */
public class LaunchedTaskActivity extends FragmentActivity {

    //log tag
    private static final String TAG = FindTasksActivity.class.getName();

    //bundle arguments
    public static final String TASK_DOCUMENT = "TASK_DOCUMENT";
    public static final String TIME = "TIME";
    public static final String TITLE = "TITLE";

    private String title;

    private TaskResultListAdapter resultListAdapter;

    private TaskServer taskServer;
    private Timer timer;

    private long secondsElapsed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launched_task);
        Log.v(TAG, "onCreate initialization started");

        Bundle inputBundle = getIntent().getExtras();
        String taskDoc = inputBundle.getString(TASK_DOCUMENT);
        final int time = inputBundle.getInt(TIME);

        title = inputBundle.getString(TITLE);
        TextView titleTextView = findViewById(R.id.textView_launched_task_title);
        titleTextView.setText(title);

        resultListAdapter = new TaskResultListAdapter();

        RecyclerView recyclerView = findViewById(R.id.recyclerView_launched_task_results);
        recyclerView.setAdapter(resultListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //back
        ImageButton backButton = findViewById(R.id.imageButton_launched_task_back);
        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //menu
        final ImageButton menuButton = findViewById(R.id.imageButton_launched_task_menu);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(LaunchedTaskActivity.this, menuButton);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_launched_task_save:
                                if (Build.VERSION.SDK_INT > 15
                                        && ContextCompat.checkSelfPermission(LaunchedTaskActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(LaunchedTaskActivity.this,
                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            MainActivity.REQUEST_WRITE_STORAGE);
                                } else {
                                    saveResults();
                                }
                                return true;
                            case R.id.menu_launched_task_help:
                                FragmentManager fm = getSupportFragmentManager();
                                GuideFragment guideFragment = GuideFragment.newInstance(GuideFragment.CREATING_TASKS);
                                guideFragment.show(fm, "HELP_DIALOG");
                                return true;
                        }
                        return false;
                    }
                });
                popup.inflate(R.menu.menu_launched_task);
                popup.show();
            }
        });

        try {
            taskServer = new TaskServer(taskDoc, new TaskServer.ResultConsumer() {
                @Override
                public void receive(final String result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                resultListAdapter.addItem(TaskResult.fromXML(result));
                            } catch (XmlPullParserException | IOException | FileFormatException e) {
                                Log.e(TAG, "Error occurred while parsing a result", e);
                            }
                        }
                    });
                }
            });
            taskServer.start();
        } catch (BindException e) {
            Log.e(TAG, "Failed to bind socket", e);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getApplicationContext().getResources().getString(R.string.error));
            builder.setMessage(getApplicationContext().getResources().getString(R.string.bind_socket_error));
            builder.setNegativeButton(android.R.string.ok, null);
            AlertDialog alert = builder.create();
            alert.show();
        } catch (IOException e) {
            Log.e(TAG, "Failed to open socket", e);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getApplicationContext().getResources().getString(R.string.error));
            builder.setMessage(getApplicationContext().getResources().getString(R.string.unknown_socket_error));
            builder.setNegativeButton(android.R.string.ok, null);
            AlertDialog alert = builder.create();
            alert.show();
        }

        final Button stopButton = findViewById(R.id.button_launched_task_stop);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LaunchedTaskActivity.this);
                builder.setTitle(getResources().getString(R.string.stop_task));
                builder.setInverseBackgroundForced(true);
                builder.setMessage(getResources().getString(R.string.stop_task_confirm));
                builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        taskServer.close();
                        stopButton.setEnabled(false);
                        timer.cancel();
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.no), null);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        final TextView timerTextView = findViewById(R.id.textView_launched_task_timer);

        secondsElapsed = 0;

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long seconds = secondsElapsed % 60;
                final long minutes = secondsElapsed / 60;
                final String secondsStr = seconds >= 10 ? String.valueOf(seconds) : "0" + seconds;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timerTextView.setText(minutes + ":" + secondsStr + (time == 0 ? "" : "/" + time + ":00"));
                    }
                });
                secondsElapsed++;
            }
        }, 0, 1000);

        Log.i(TAG, "onCreate initialized");
    }

    @Override
    public void onBackPressed() {
        Log.v(TAG, "onBackPressed method started");
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        taskServer.close();
        timer.cancel();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MainActivity.REQUEST_WRITE_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveResults();
                }
            }
        }
    }

    private void saveResults() {
        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        String fileName = title + " results " + dateFormat.format(calendar.getTime()) + ".txt";
        final File directory = new File(FileHandler.PATH);
        directory.mkdirs();
        File outputFile = new File(directory, fileName);
        try {
            outputFile.createNewFile();

            FileWriter writer = new FileWriter(outputFile);
            writer.write(resultListAdapter.serialize());
            writer.close();

            Toast.makeText(LaunchedTaskActivity.this, FileHandler.PATH + "/" + fileName +
                    " " + getResources().getString(R.string.save_succ), Toast.LENGTH_SHORT).show();
            Log.i(TAG, "file " + fileName + " saved");
        }
        catch (IOException e) {
            Log.e(TAG, "File was not saved", e);
            Toast.makeText(LaunchedTaskActivity.this,
                    getResources().getString(R.string.file_not_saved), Toast.LENGTH_SHORT).show();
        }
    }
}
