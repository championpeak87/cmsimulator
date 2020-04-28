package fiitstu.gulis.cmsimulator.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import fiitstu.gulis.cmsimulator.R;

import fiitstu.gulis.cmsimulator.activities.*;
import fiitstu.gulis.cmsimulator.adapters.tasks.AutomataTaskAdapter;
import fiitstu.gulis.cmsimulator.database.DataSource;

import fiitstu.gulis.cmsimulator.database.FileHandler;
import fiitstu.gulis.cmsimulator.elements.Task;
import fiitstu.gulis.cmsimulator.models.tasks.automata_tasks.FiniteAutomataTask;
import fiitstu.gulis.cmsimulator.models.tasks.automata_tasks.LinearBoundedAutomataTask;
import fiitstu.gulis.cmsimulator.models.tasks.automata_tasks.PushdownAutomataTask;
import fiitstu.gulis.cmsimulator.models.tasks.automata_tasks.TuringMachineTask;
import fiitstu.gulis.cmsimulator.network.ServerController;
import fiitstu.gulis.cmsimulator.network.UrlManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A dialog that displays details about a task and offers various options
 * what to do with it, depending on the mode
 * <p>
 * Created by Jakub Sedlář on 09.01.2018.
 */
public class TaskDialog extends DialogFragment {

    public interface TaskDialogListener {
        void onTaskDialogClick(Task task, int machineType, int dialogMode);
    }

    //log tag
    private static final String TAG = TaskDialog.class.getName();

    //offers options to start solving the task
    public static final int ENTERING = 0;
    //offers options to turn the task in
    public static final int SOLVING = 1;
    //offers options to return to the EditTaskActivity
    public static final int EDITING = 2;
    //offers options to return to main menu
    public static final int SOLVED = 3;
    //offers options to preview solution by other user
    public static final int PREVIEW = 4;

    private static final String MODE = "MODE";
    private static final String TASK = "TASK";

    private static CharSequence statusText;

    private Task task;
    private int mode;
    private int machineType;

    private boolean timeOut;

    private TextView timeRemainingTextView;
    private ProgressBar progressBar;
    private TextView doneTextView;
    private LinearLayout timeLayout;

    private Timer timer;
    private AutomataTaskAdapter adapter;

    public void setAdapter(AutomataTaskAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * Ugly global variable.
     *
     * @param statusText the displayed status text
     */
    public static void setStatusText(CharSequence statusText) {
        TaskDialog.statusText = statusText;
    }

    /**
     * Creates a new instance of the TaskDialog with the given arguments set
     *
     * @param task        the relevant task
     * @param mode        affects the offered options, {@link #ENTERING}, {@link #SOLVING}, {@link #EDITING}, or {@link #SOLVED}
     * @param machineType the type of the machine (one of MainActivity's static members)
     * @return the created instance
     */
    public static TaskDialog newInstance(Task task, int mode, int machineType) {
        TaskDialog frag = new TaskDialog();
        Bundle args = new Bundle();
        args.putInt(MODE, mode);
        args.putInt(MainActivity.MACHINE_TYPE, machineType);
        args.putSerializable(TASK, task);
        frag.setArguments(args);
        return frag;
    }

    /**
     * Disables all buttons, makes the dialog uncancellable and shows a progress bar.
     *
     * @see #unfreeze() unfreeze
     */
    public void freeze() {
        final AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            progressBar.setVisibility(View.VISIBLE);
            d.setCancelable(false);
            d.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
            d.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(false);
        }
    }

    /**
     * Hides progress bar, makes the dialog cancellable, enables all buttons that should be enabled
     *
     * @see #freeze() freeze
     */
    public void unfreeze() {
        final AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            progressBar.setVisibility(View.GONE);
            d.setCancelable(true);
            d.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(true);
            if (!timeOut) {
                d.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
            }
        }
    }

    public void markAsStarted(final Task.TASK_STATUS status, final int task_id, final int user_id) {
        class ChangeTaskFlagAsync extends AsyncTask<Void, Void, String> {
            @Override
            protected String doInBackground(Void... voids) {
                UrlManager urlManager = new UrlManager();
                ServerController serverController = new ServerController();
                URL changeFlagURL = urlManager.getChangeFlagUrl(status, task_id, user_id);

                String output = null;

                try {
                    output = serverController.getResponseFromServer(changeFlagURL);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    return output;
                }
            }

            @Override
            protected void onPostExecute(String s) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if (!jsonObject.getBoolean("updated"))
                        Log.e("SERVER", "ERROR CHANGING TASK FLAG!");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        Task.TASK_STATUS currentStatus = task.getStatus();
        if (currentStatus == Task.TASK_STATUS.WRONG || currentStatus == Task.TASK_STATUS.CORRECT)
            return;
        else {
            new ChangeTaskFlagAsync().execute();
            task.setStatus(Task.TASK_STATUS.IN_PROGRESS);
        }
    }


    /**
     * Changes the dialog mode to SOLVED and the positive button text to "back to menu"
     */
    public void makeSolved(CharSequence statusText) {
        final AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            d.getButton(Dialog.BUTTON_POSITIVE).setText(R.string.back_to_menu);
            doneTextView.setText(statusText);
            doneTextView.setVisibility(View.VISIBLE);
            timeLayout.setVisibility(View.GONE);
            mode = SOLVED;
            setStatusText(statusText);
        }
    }

    private boolean isTimeSet(Time time) {
        if (time.getHours() == 0 && time.getMinutes() == 0 && time.getSeconds() == 0)
            return false;
        else return true;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        DataSource.getInstance().open();

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_task, null);

        task = (Task) args.getSerializable(TASK);

        mode = args.getInt(MODE);
        machineType = args.getInt(MainActivity.MACHINE_TYPE);

        TextView taskTextView = view.findViewById(R.id.textView_dialog_task_text);

        TextView timeLabel = view.findViewById(R.id.textView_dialog_task_time_label);

        progressBar = view.findViewById(R.id.progressBar_dialog_task);
        doneTextView = view.findViewById(R.id.textView_dialog_task_done);
        ((TextView) view.findViewById(R.id.textView_dialog_task_name)).setText(task.getTitle());
        timeRemainingTextView = view.findViewById(R.id.textView_dialog_task_remaining_time);
        timeLayout = view.findViewById(R.id.linearLayout_dialog_task_remaining_time);

        Time time = task.getAvailable_time();
        if (!isTimeSet(time)) {
            timeLayout.setVisibility(View.GONE);
        }

        taskTextView.setText(task.getText());

        if (mode == SOLVING && isTimeSet(time)) {
            timeLabel.setText(R.string.remaining_time);

        } else if (mode == ENTERING || mode == EDITING || mode == PREVIEW) {
            timeLabel.setText(R.string.available_time);
            timeRemainingTextView.setText(task.getRemaining_time().toString());
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(R.string.start_task);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setInverseBackgroundForced(true);
        if (mode == SOLVING) {
            alertDialogBuilder.setPositiveButton(getResources().getString(R.string.turn_in), null);
        } else if (mode == ENTERING) {
            Time remainingTime = task.getRemaining_time();
            Time outOfTime = Time.valueOf("00:00:00");
            Task.TASK_STATUS status = task.getStatus();
            if (status != Task.TASK_STATUS.CORRECT && status != Task.TASK_STATUS.WRONG) {
                if (remainingTime.compareTo(outOfTime) > 0 || task.getAvailable_time().compareTo(Time.valueOf("00:00:00")) == 0)
                    alertDialogBuilder.setPositiveButton(getResources().getString(R.string.solve), null);
            }
        } else if (mode == EDITING) {
            alertDialogBuilder.setPositiveButton(R.string.back_to_task_edit, null);
        } else if (mode == PREVIEW) {
            Time remainingTime = task.getRemaining_time();
            alertDialogBuilder.setPositiveButton(R.string.show_task, null);
        }
        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), null);

        return alertDialogBuilder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            final Time time = task.getAvailable_time();
            if (mode == SOLVING && isTimeSet(time)
                    && (SystemClock.elapsedRealtime() - task.getStarted()) >= time.getMinutes() * 60 * 1000) {
                ((AlertDialog) getDialog()).getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), SimulationActivity.class);

                    Bundle outputBundle = new Bundle();
                    outputBundle.putBoolean(MainActivity.DEFAULT_FORMAT, true);
                    outputBundle.putString(MainActivity.FILE_NAME, getActivity().getApplicationInfo().dataDir + "/automataTask.cmst");
                    if (TaskLoginActivity.loggedUser == null)
                        outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.LOAD_MACHINE);
                    else {
                        DataSource dataSource = DataSource.getInstance();
                        dataSource.open();
                        dataSource.globalDrop();
                        dataSource.close();
                        if (mode == PREVIEW) {
                            outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.PREVIEW_TASK);
                        } else {
                            outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.SOLVE_TASK);
                        }
                    }
                    outputBundle.putInt(MainActivity.MACHINE_TYPE, machineType);
                    outputBundle.putSerializable(MainActivity.TASK, task);

                    final Task.TASK_STATUS currentStatus = task.getStatus();
                    if (TaskLoginActivity.loggedUser != null) {
                        if (currentStatus == Task.TASK_STATUS.NEW && mode != PREVIEW) {
                            markAsStarted(Task.TASK_STATUS.IN_PROGRESS, TaskLoginActivity.loggedUser.getUser_id(), task.getTask_id());
                            int position = adapter.getListOfTasks().indexOf(task);
                            adapter.notifyItemChanged(position);
                        }
                    }

                    intent.putExtras(outputBundle);

                    startActivity(intent);
                    TaskDialog.super.dismiss();
                }
            });

            if (statusText != null) {
                makeSolved(statusText);
            }
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (timer != null) {
            timer.cancel();
        }
        super.onDismiss(dialog);
    }

    /**
     * Changes the contents of the displayed timer
     *
     * @param seconds the displayed time, in seconds
     */
    private void setRemainingSeconds(int seconds) {
        int minutes = seconds / 60;
        seconds %= 60;

        timeRemainingTextView.setText(minutes + "zmin " + seconds + "s");

        if (minutes == 0 && seconds == 0 && mode == SOLVING) {
            timeOut = true;
            final AlertDialog d = (AlertDialog) getDialog();
            if (d != null) {
                d.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
            }
        }
    }
}
