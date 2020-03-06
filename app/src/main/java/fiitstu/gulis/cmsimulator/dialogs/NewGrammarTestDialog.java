package fiitstu.gulis.cmsimulator.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.activities.GrammarTestingActivity;
import fiitstu.gulis.cmsimulator.activities.UsersManagmentActivity;
import fiitstu.gulis.cmsimulator.database.DataSource;

public class NewGrammarTestDialog extends DialogFragment {
    private static final String TAG = "NewGrammarTestDialog";

    private OnAddedTestListener onAddedTestListener = null;

    public interface OnAddedTestListener {
        void OnAdd(String inputWord);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.onAddedTestListener = (GrammarTestingActivity)context;
    }

    public void setOnAddedTestListener(OnAddedTestListener onAddedTestListener) {
        this.onAddedTestListener = onAddedTestListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_new_grammar_test, null);

        builder.setView(view)
                .setTitle(R.string.new_test)
                .setNeutralButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, null);

        AlertDialog dialog = builder.create();
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        final AlertDialog dialog = (AlertDialog) this.getDialog();
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: HANDLE POSITIVE BUTTON CLICK
                final EditText inputWordEditText = dialog.findViewById(R.id.edittext_input_word);
                final String inputWord = inputWordEditText.getText().toString();

                if (inputWord.length() <= 0) {
                    String error = getActivity().getString(R.string.wrong_input_word);
                    inputWordEditText.setError(error);
                } else {
                    DataSource dataSource = DataSource.getInstance();
                    dataSource.open();
                    dataSource.addGrammarTest(inputWord);
                    dataSource.close();

                    dialog.dismiss();

                    if (onAddedTestListener != null)
                        onAddedTestListener.OnAdd(inputWord);
                }

            }
        });

    }
}
