package fiitstu.gulis.cmsimulator.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.models.tasks.grammar_tasks.GrammarTask;

@SuppressLint("ValidFragment")
public class GrammarTaskDialog extends DialogFragment {
    private static final String TAG = "GrammarTaskDialog";

    // UI ELEMENTS
    private TextView taskName_TextView;
    private TextView taskDescription_TextView;
    private TextView remainingTime_TextView;
    private LinearLayout remainingTime_LinearLayout;

    public enum DialogStyle {
        NEW_TASK_ENTERING,
        OLD_TASK_ENTERING,
        SOLVED_TASK_ENTERING,
        PREVIEW_TASK_ENTERING
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
                .setTitle(R.string.start_task)
                .setView(view)
                .setNeutralButton(android.R.string.cancel, null);

        switch (style)
        {
            case NEW_TASK_ENTERING:
                builder.setPositiveButton(R.string.start_task, null);
                break;
            case OLD_TASK_ENTERING:
                builder.setPositiveButton(R.string.continue_task, null);
                break;
            case SOLVED_TASK_ENTERING:
                break;
            case PREVIEW_TASK_ENTERING:
                builder.setPositiveButton(R.string.show_task, null);
                break;
        }

        setUIElements(view);

        return builder.create();
    }

    private void setUIElements(View view)
    {
        taskName_TextView = view.findViewById(R.id.taskName_TextView);
        taskDescription_TextView = view.findViewById(R.id.taskDescription_TextView);
        remainingTime_TextView = view.findViewById(R.id.remainingTime_TextView);
        remainingTime_LinearLayout = view.findViewById(R.id.linearLayout_dialog_task_remaining_time);
    }

}
