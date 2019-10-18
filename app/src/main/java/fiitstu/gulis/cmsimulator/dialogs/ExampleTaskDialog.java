package fiitstu.gulis.cmsimulator.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import fiitstu.gulis.cmsimulator.R;

/**
 * Dialog for selecting example task
 *
 * Created by Jakub Sedlář on 03.04.2018.
 */
public class ExampleTaskDialog extends DialogFragment {

    //log tag
    private static final String TAG = ExampleTaskDialog.class.getName();

    public interface ExampleTaskDialogListener {
        void exampleTaskDialogClick(String assetName);
    }

    public static ExampleTaskDialog newInstance() {
        return new ExampleTaskDialog();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return inflater.inflate(R.layout.dialog_tasks_example, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button exampleButton1 = view.findViewById(R.id.button_popup_tasks_example1);
        exampleButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ExampleTaskDialogListener) getActivity())
                        .exampleTaskDialogClick(getContext().getString(R.string.task_example_asset1));
            }
        });
        Button exampleButton2 = view.findViewById(R.id.button_popup_tasks_example2);
        exampleButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ExampleTaskDialogListener) getActivity())
                        .exampleTaskDialogClick(getContext().getString(R.string.task_example_asset2));
            }
        });
        Button exampleButton3 = view.findViewById(R.id.button_popup_tasks_example3);
        exampleButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ExampleTaskDialogListener) getActivity())
                        .exampleTaskDialogClick(getContext().getString(R.string.task_example_asset3));
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
