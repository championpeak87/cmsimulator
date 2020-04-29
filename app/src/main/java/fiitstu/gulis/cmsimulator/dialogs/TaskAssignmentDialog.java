package fiitstu.gulis.cmsimulator.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.elements.Task;

public class TaskAssignmentDialog extends DialogFragment {
    private Task task = null;

    private String task_name = null;
    private String task_text = null;

    public static TaskAssignmentDialog newInstance(Task task) {
        TaskAssignmentDialog taskAssignmentDialog = new TaskAssignmentDialog();
        taskAssignmentDialog.task = task;

        return taskAssignmentDialog;
    }

    public static TaskAssignmentDialog newInstance(String task_name, String task_text) {
        TaskAssignmentDialog taskAssignmentDialog = new TaskAssignmentDialog();
        taskAssignmentDialog.task_name = task_name;
        taskAssignmentDialog.task_text = task_text;

        return taskAssignmentDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View view = layoutInflater.inflate(R.layout.dialog_task, null, false);

        TextView taskName = view.findViewById(R.id.textView_dialog_task_name);
        TextView taskText = view.findViewById(R.id.textView_dialog_task_text);
        LinearLayout time = view.findViewById(R.id.linearLayout_dialog_task_remaining_time);

        time.setVisibility(View.GONE);
        if (task != null) {
            taskName.setText(task.getTitle());
            taskText.setText(task.getText());
        } else {
            taskName.setText(task_name);
            taskText.setText(task_text);
        }

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.task_details)
                .setView(view)
                .setPositiveButton(android.R.string.yes, null)
                .create();

        return dialog;
    }
}
