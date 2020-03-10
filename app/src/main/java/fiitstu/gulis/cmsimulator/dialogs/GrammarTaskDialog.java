package fiitstu.gulis.cmsimulator.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.models.tasks.grammar_tasks.GrammarTask;

import java.sql.Time;

@SuppressLint("ValidFragment")
public class GrammarTaskDialog extends DialogFragment {
    private static final String TAG = "GrammarTaskDialog";
    private OnTaskStartListener onTaskStartListener = null;

    // UI ELEMENTS
    private TextView taskName_TextView;
    private TextView taskDescription_TextView;
    private TextView remainingTime_TextView;
    private LinearLayout remainingTime_LinearLayout;

    public interface OnTaskStartListener {
        void onStart();
    }

    public enum DialogStyle {
        NEW_TASK_ENTERING,
        OLD_TASK_ENTERING,
        SOLVED_TASK_ENTERING,
        PREVIEW_TASK_ENTERING
    }

    public void setOnTaskStartListener(OnTaskStartListener onTaskStartListener) {
        this.onTaskStartListener = onTaskStartListener;
    }

    // INTENT VALUES
    private GrammarTask enteringTask;
    private DialogStyle style;

    @SuppressLint("ValidFragment")
    public GrammarTaskDialog(GrammarTask enteringTask, DialogStyle style) {
        this.enteringTask = enteringTask;
        this.style = style;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View view = layoutInflater.inflate(R.layout.dialog_start_grammar_task, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setNeutralButton(android.R.string.cancel, null);

        switch (style) {
            case NEW_TASK_ENTERING:
                builder.setPositiveButton(R.string.start_task, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (onTaskStartListener != null)
                            onTaskStartListener.onStart();
                    }
                });
                builder.setTitle(R.string.start_task);
                break;
            case OLD_TASK_ENTERING:
                builder.setPositiveButton(R.string.continue_task, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (onTaskStartListener != null)
                            onTaskStartListener.onStart();
                    }
                });
                builder.setTitle(R.string.continue_task);
                break;
            case SOLVED_TASK_ENTERING:
                builder.setTitle(R.string.task_details);
                break;
            case PREVIEW_TASK_ENTERING:
                builder.setPositiveButton(R.string.show_task, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (onTaskStartListener != null)
                            onTaskStartListener.onStart();
                    }
                });
                builder.setTitle(R.string.preview_task);
                break;
        }

        setUIElements(view);
        setData();

        return builder.create();
    }

    private void setUIElements(View view) {
        taskName_TextView = view.findViewById(R.id.taskName_TextView);
        taskDescription_TextView = view.findViewById(R.id.taskDescription_TextView);
        remainingTime_TextView = view.findViewById(R.id.remainingTime_TextView);
        remainingTime_LinearLayout = view.findViewById(R.id.linearLayout_dialog_task_remaining_time);
    }

    private void setData() {
        taskName_TextView.setText(enteringTask.getTitle());
        taskDescription_TextView.setText(enteringTask.getText());
        if (enteringTask.getAvailable_time().after(Time.valueOf("00:00:00"))) {
            remainingTime_LinearLayout.setVisibility(View.VISIBLE);
            remainingTime_TextView.setText(enteringTask.getRemaining_time().toString());
        } else {
            remainingTime_LinearLayout.setVisibility(View.GONE);
        }
    }

}
