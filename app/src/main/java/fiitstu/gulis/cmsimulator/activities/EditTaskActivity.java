package fiitstu.gulis.cmsimulator.activities;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
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
import android.widget.*;

import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.database.FileHandler;
import fiitstu.gulis.cmsimulator.dialogs.NewMachineDialog;
import fiitstu.gulis.cmsimulator.dialogs.SaveMachineDialog;
import fiitstu.gulis.cmsimulator.elements.Task;
import fiitstu.gulis.cmsimulator.dialogs.GuideFragment;
import fiitstu.gulis.cmsimulator.elements.TaskResult;
import fiitstu.gulis.cmsimulator.network.ServerController;
import fiitstu.gulis.cmsimulator.network.UrlManager;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.URL;

/**
 * Activity for editing tasks.
 * <p>
 * Expected Intent arguments (extras) (KEY (TYPE) - MEANING):
 * TASK (Serializable - Task) - the task being edited
 * <p>
 * Created by Jakub Sedlář on 12.01.2018.
 */
public class EditTaskActivity extends FragmentActivity implements SaveMachineDialog.SaveDialogListener,
        NewMachineDialog.NewMachineDialogListener {
    //log tag
    private static final String TAG = EditTaskActivity.class.getName();

    //bundle arguments
    public static final String TASK = "TASK";

    private EditText titleEditText;
    private EditText textEditText;
    private EditText minutesEditText;
    private CheckBox timeLimitCheckbox;
    private CheckBox publishInputsCheckbox;

    private Task task;
    private int machineType;

    private int logged_user_id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);
        Log.v(TAG, "onCreate initialization started");

        // menu
        ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.edit_task);

        logged_user_id = this.getIntent().getIntExtra("LOGGED_USER_ID", 0);

        final Bundle inputBundle = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;

        machineType = inputBundle.getInt(MainActivity.MACHINE_TYPE, MainActivity.UNDEFINED);
        task = (Task) inputBundle.getSerializable(TASK);
        if (task == null) {
            task = new Task();
        }

        titleEditText = findViewById(R.id.editText_task_title);
        textEditText = findViewById(R.id.editText_task_text);
        timeLimitCheckbox = findViewById(R.id.checkBox_edit_task_time_limit);
        minutesEditText = findViewById(R.id.editText_edit_task_minutes);
        publishInputsCheckbox = findViewById(R.id.checkBox_edit_task_show_tests);

        publishInputsCheckbox.setChecked(task.getPublicInputs());

        if (task.getTitle() != null) {
            titleEditText.setText(task.getTitle());
        }

        if (task.getText() != null) {
            textEditText.setText(task.getText());
        }

        if (task.getMinutes() != 0) {
            timeLimitCheckbox.setChecked(true);
            minutesEditText.setEnabled(true);
            minutesEditText.setText(String.valueOf(task.getMinutes()));
        }

        if (timeLimitCheckbox.isChecked()) {
            minutesEditText.setEnabled(true);
        }

        timeLimitCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timeLimitCheckbox.isChecked())
                    minutesEditText.setEnabled(true);
                else
                    minutesEditText.setEnabled(false);
            }
        });


        Button setAutomatonButton = findViewById(R.id.button_edit_task_set_automaton);
        setAutomatonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (machineType == MainActivity.UNDEFINED) {
                    FragmentManager fm = getSupportFragmentManager();
                    NewMachineDialog newMachineDialog = NewMachineDialog.newInstance();
                    newMachineDialog.show(fm, "MACHINE_CHOICE_DIALOG");
                } else {
                    editAutomaton(false);
                }
            }
        });

        Log.i(TAG, "onCreate initialized");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.menu_edit_task, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_edit_task_save_task:
                if (Build.VERSION.SDK_INT > 15
                        && ContextCompat.checkSelfPermission(EditTaskActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(EditTaskActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MainActivity.REQUEST_WRITE_STORAGE);
                } else {
                    FragmentManager fm = getSupportFragmentManager();
                    SaveMachineDialog saveMachineDialog = SaveMachineDialog.newInstance(task.getTitle(), FileHandler.Format.CMST, false);
                    saveMachineDialog.show(fm, "SAVE_DIALOG");
                }
                return true;
            case R.id.menu_edit_task_settings:
                Intent nextActivityIntent = new Intent(EditTaskActivity.this, OptionsActivity.class);
                startActivity(nextActivityIntent);
                Log.i(TAG, "options activity intent executed");
                return true;
            case R.id.menu_edit_task_help:
                FragmentManager fm = getSupportFragmentManager();
                GuideFragment guideFragment = GuideFragment.newInstance(GuideFragment.CREATING_TASKS);
                guideFragment.show(fm, "HELP_DIALOG");
                return true;
            case R.id.menu_edit_task_upload_task:
                // IN_PROGRESS: Implement task uploading
                if (machineType == MainActivity.UNDEFINED) {
                    Toast.makeText(EditTaskActivity.this, R.string.automaton_not_created, Toast.LENGTH_SHORT).show();
                } else if (titleEditText.getText().toString().isEmpty()) {
                    Toast.makeText(EditTaskActivity.this, R.string.no_task_title, Toast.LENGTH_SHORT).show();
                } else {
                    Bundle outputBundle = new Bundle();
                    FileHandler fileHandler = new FileHandler(FileHandler.Format.CMST);
                    DataSource dataSource = DataSource.getInstance();
                    dataSource.open();
                    try {
                        fileHandler.setData(dataSource, machineType);
                        updateTask();
                        task.setResultVersion(TaskResult.CURRENT_VERSION);
                        fileHandler.writeTask(task);

                        String taskDoc = fileHandler.writeToString();

                        final File file = new File(this.getFilesDir(), task.getTitle() + ".cmst");
                        final String[] filename = new String[1];
                        FileOutputStream outputStream;

                        outputStream = openFileOutput(task.getTitle() + ".cmst", Context.MODE_PRIVATE);
                        outputStream.write(taskDoc.getBytes());
                        outputStream.close();

                        class uploadTaskAsync extends AsyncTask<File, Void, File> {
                            @Override
                            protected File doInBackground(File... files) {
                                UrlManager urlManager = new UrlManager();
                                ServerController serverController = new ServerController();
                                URL uploadTaskUrl = urlManager.getPublishAutomataTaskURL(filename[0]);
                                serverController.doPostRequest(uploadTaskUrl, files[0]);

                                return files[0];
                            }

                            @Override
                            protected void onPostExecute(File f) {
                                f.delete();
                            }
                        }

                        class addTaskToDatabaseAsync extends AsyncTask<Task, Void, String> {
                            @Override
                            protected String doInBackground(Task... tasks) {
                                UrlManager urlManager = new UrlManager();
                                URL url = urlManager.getPushAutomataTaskToTable(tasks[0], logged_user_id, machineType);

                                ServerController serverController = new ServerController();
                                try {
                                    return serverController.getResponseFromServer(url);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                return null;
                            }

                            @Override
                            protected void onPostExecute(String s) {
                                //Toast.makeText(EditTaskActivity.this, s, Toast.LENGTH_SHORT).show();
                                try {
                                    JSONObject object = new JSONObject(s);
                                    filename[0] = Integer.toString(object.getInt("task_id"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                new uploadTaskAsync().execute(file);
                                uploadFinished();
                            }
                        }


                        new addTaskToDatabaseAsync().execute(task);

                    } catch (ParserConfigurationException | TransformerException e) {
                        Log.e(TAG, "Error happened when serializing task", e);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        dataSource.close();
                    }
                }
                return true;
        }

        return false;
    }

    private void uploadFinished()
    {
        finish();
        Toast.makeText(this, R.string.upload_task_complete, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MainActivity.MACHINE_TYPE, machineType);
        outState.putSerializable("TASK", task);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        Log.v(TAG, "onBackPressed method started");
        new AlertDialog.Builder(this)
                .setTitle(R.string.warning)
                .setMessage(R.string.exit_confirmation)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DataSource.getInstance().open();
                        ;
                        DataSource.getInstance().globalDrop();
                        DataSource.getInstance().close();
                        finish();
                        EditTaskActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public void saveDialogClick(String filename, FileHandler.Format format, boolean exit) {
        try {
            FileHandler fileHandler = new FileHandler(FileHandler.Format.CMST);
            if (machineType != MainActivity.UNDEFINED) {
                DataSource dataSource = DataSource.getInstance();
                dataSource.open();
                fileHandler.setData(dataSource, machineType);
                dataSource.close();
            }
            updateTask();
            fileHandler.writeTask(task);
            fileHandler.writeFile(filename);

            Toast.makeText(this, FileHandler.PATH + "/" + filename + ".cmst " + getResources().getString(R.string.save_succ), Toast.LENGTH_SHORT).show();
            SaveMachineDialog saveMachineDialog = (SaveMachineDialog) getSupportFragmentManager()
                    .findFragmentByTag("SAVE_DIALOG");
            if (saveMachineDialog != null) {
                saveMachineDialog.dismiss();
            }
        } catch (Exception e) {
            Log.e(TAG, "File was not saved", e);
            Toast.makeText(this, getResources().getString(R.string.file_not_saved), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void newMachineDialogClick(Bundle outputBundle) {
        machineType = outputBundle.getInt(MainActivity.MACHINE_TYPE);
        editAutomaton(true);
        NewMachineDialog newMachineDialog = (NewMachineDialog) getSupportFragmentManager()
                .findFragmentByTag("MACHINE_CHOICE_DIALOG");
        if (newMachineDialog != null) {
            newMachineDialog.dismiss();
        }
    }

    /**
     * Updates  edited Task object to reflect the changes made by the user via the GUI
     */
    private void updateTask() {
        task.setTitle(titleEditText.getText().toString());
        task.setText(textEditText.getText().toString());
        task.setPublicInputs(publishInputsCheckbox.isChecked());
        if (timeLimitCheckbox.isChecked()) {
            if (minutesEditText.getText().toString().isEmpty()) {
                task.setMinutes(0);
            } else {
                task.setMinutes(Integer.parseInt(minutesEditText.getText().toString()));
            }
        } else {
            task.setMinutes(0);
        }
        DataSource dataSource = DataSource.getInstance();
        dataSource.open();
        task.setMaxSteps(dataSource.getMaxSteps());
        dataSource.close();
    }

    private void editAutomaton(boolean isNew) {
        updateTask();
        Bundle outputBundle = new Bundle();
        outputBundle.putInt(MainActivity.MACHINE_TYPE, machineType);
        if (isNew) {
            outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.NEW_TASK);
        } else {
            outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EDIT_TASK);
        }
        outputBundle.putSerializable("TASK", task);
        outputBundle.putInt("USER_ID", logged_user_id);
        Intent nextActivityIntent = new Intent(EditTaskActivity.this, SimulationActivity.class);
        nextActivityIntent.putExtras(outputBundle);
        startActivity(nextActivityIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MainActivity.REQUEST_WRITE_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    FragmentManager fm = getSupportFragmentManager();
                    SaveMachineDialog saveMachineDialog = SaveMachineDialog.newInstance(task.getTitle(), FileHandler.Format.CMST, false);
                    saveMachineDialog.show(fm, "SAVE_DIALOG");
                }
            }
        }
    }

    private class MenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {

            }
            return false;
        }
    }
}
