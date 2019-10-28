package fiitstu.gulis.cmsimulator.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.R;

public class TasksGameDialog extends DialogFragment {

    //log tag
    private static final String TAG = ExampleTaskDialog.class.getName();

    public interface TasksGameDialogListener {
        void tasksGameDialogClick(String assetName);
    }

    public static TasksGameDialog newInstance() {
        return new TasksGameDialog();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setTitle(R.string.select_task);
        return inflater.inflate(R.layout.dialog_tasks_game, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button exampleButton1 = view.findViewById(R.id.button_tasks_game_preview);
        exampleButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TasksGameDialogListener) getActivity())
                        .tasksGameDialogClick(getContext().getString(R.string.task_example_asset1));
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.8),
                    (int) (getResources().getDisplayMetrics().heightPixels * 0.8));
        }
    }
}
