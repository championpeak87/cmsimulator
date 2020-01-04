package fiitstu.gulis.cmsimulator.dialogs;

import android.annotation.SuppressLint;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.activities.MainActivity;
import fiitstu.gulis.cmsimulator.models.tasks.automata_type;

/**
 * A dialog for selecting tye of machine to be created
 * <p>
 * Created by Martin on 15. 4. 2017.
 */
public class SubmitTaskDialog extends DialogFragment {
    private LinearLayout message;
    private EditText positive_tests;
    private EditText negative_tests;

    private int positiveCount;
    private int positiveSuccessful;
    private int negativeCount;
    private int negativeSuccessful;

    private SubmitTaskDialogListener listener;

    public interface SubmitTaskDialogListener {
        void submitTaskDialogClick();
    }

    @SuppressLint("ValidFragment")
    public SubmitTaskDialog(int positiveCount, int positiveSuccessful, int negativeCount, int negativeSuccessful) {
        this.positiveCount = positiveCount;
        this.positiveSuccessful = positiveSuccessful;
        this.negativeCount = negativeCount;
        this.negativeSuccessful = negativeSuccessful;
    }

    public void setOnClickListener(SubmitTaskDialogListener listener) {
        this.listener = listener;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder((getActivity()));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_submit_task, null);

        message = view.findViewById(R.id.linearlayout_submit_warning_message);
        positive_tests = view.findViewById(R.id.edittext_positive_test_results);
        negative_tests = view.findViewById(R.id.edittext_negative_test_results);

        if (positiveSuccessful == positiveCount && negativeSuccessful == negativeCount) {
            message.setVisibility(View.GONE);
        }

        if (positiveSuccessful == positiveCount) {
            positive_tests.setTextColor(getActivity().getColor(R.color.md_green_400));
        } else {
            positive_tests.setTextColor(getActivity().getColor(R.color.md_red_500));
        }

        if (negativeSuccessful == negativeCount) {
            negative_tests.setTextColor(getActivity().getColor(R.color.md_green_400));
        } else {
            negative_tests.setTextColor(getActivity().getColor(R.color.md_red_500));
        }

        final String positive_message = Integer.toString(positiveSuccessful) + "/" + Integer.toString(positiveCount);
        final String negative_message = Integer.toString(negativeSuccessful) + "/" + Integer.toString(negativeCount);

        builder.setView(view)
                .setTitle(R.string.submit_task)
                .setNeutralButton(R.string.cancel, null)
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.submitTaskDialogClick();
                    }
                });

        positive_tests.setText(positive_message, TextView.BufferType.EDITABLE);
        negative_tests.setText(negative_message, TextView.BufferType.EDITABLE);

        return builder.create();
    }

}
