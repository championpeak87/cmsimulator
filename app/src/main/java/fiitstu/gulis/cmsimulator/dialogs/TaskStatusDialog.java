package fiitstu.gulis.cmsimulator.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.activities.MainActivity;
import fiitstu.gulis.cmsimulator.elements.Task;
import fiitstu.gulis.cmsimulator.models.tasks.automata_type;

import java.util.ArrayList;

/**
 * A dialog for selecting tye of machine to be created
 * <p>
 * Created by Martin on 15. 4. 2017.
 */
public class TaskStatusDialog extends DialogFragment {
    private Spinner status_spinner;
    private ArrayList<String> task_states;
    private ArrayAdapter<String> adapter;
    private Task.TASK_STATUS selectedStatus = Task.TASK_STATUS.IN_PROGRESS;
    private Context mContext;

    private OnClickListener listener;

    public interface OnClickListener {
        void onClick(Bundle outputBundle);
    }

    public void initStates() {
        ArrayList<String> list = new ArrayList<>();
        for (Task.TASK_STATUS status :
                Task.TASK_STATUS.values()) {
            list.add(status.getLocalised_name(mContext));
        }

        task_states = list;
    }

    public void setOnClickListener(OnClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder((getActivity()));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_flag_task, null);

        builder.setView(view)
                .setTitle(R.string.change_task_status)
                .setNeutralButton(R.string.cancel, null)
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle bundle = getOutputBundle();
                        listener.onClick(bundle);
                    }
                });

        status_spinner = view.findViewById(R.id.spinner_task_status);
        initStates();
        String[] itemsArray = new String[task_states.size()];
        itemsArray = task_states.toArray(itemsArray);
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, itemsArray);
        status_spinner.setAdapter(adapter);
        return builder.create();
    }

    private Bundle getOutputBundle() {
        Bundle outputBundle = new Bundle();
        switch (status_spinner.getSelectedItemPosition()) {
            case 0:
                selectedStatus = Task.TASK_STATUS.IN_PROGRESS;
                break;
            case 1:
                selectedStatus = Task.TASK_STATUS.CORRECT;
                break;
            case 2:
                selectedStatus = Task.TASK_STATUS.WRONG;
                break;
            case 3:
                selectedStatus = Task.TASK_STATUS.NEW;
                break;
            case 4:
                selectedStatus = Task.TASK_STATUS.TOO_LATE;
                break;
        }
        outputBundle.putSerializable("SELECTED_FLAG", selectedStatus);

        return outputBundle;
    }

    public void setContext(Context context)
    {
        this.mContext = context;
    }

}
