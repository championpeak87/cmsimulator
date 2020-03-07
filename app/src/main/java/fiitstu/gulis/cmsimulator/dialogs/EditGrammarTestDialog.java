package fiitstu.gulis.cmsimulator.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.activities.GrammarTestingActivity;
import fiitstu.gulis.cmsimulator.adapters.grammar.TestsAdapter;
import fiitstu.gulis.cmsimulator.database.DataSource;

@SuppressLint("ValidFragment")
public class EditGrammarTestDialog extends DialogFragment {
    private static final String TAG = "EditGrammarTestDialog";

    private String inputWord;
    private int position;
    private TestsAdapter adapter;

    @SuppressLint("ValidFragment")
    public EditGrammarTestDialog(String inputWord, int position, TestsAdapter adapter) {
        this.inputWord = inputWord;
        this.position = position;
        this.adapter = adapter;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_new_grammar_test, null);
        EditText inputWordEditText = view.findViewById(R.id.edittext_input_word);
        inputWordEditText.setText(inputWord);

        builder.setView(view)
                .setTitle(R.string.edit_test_popup_title)
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
                final EditText inputWordEditText = dialog.findViewById(R.id.edittext_input_word);
                final String inputWord = inputWordEditText.getText().toString();

                if (inputWord.length() <= 0) {
                    String error = getActivity().getString(R.string.wrong_input_word);
                    inputWordEditText.setError(error);
                } else {
                    DataSource dataSource = DataSource.getInstance();
                    dataSource.open();
                    dataSource.updateGrammarTest(EditGrammarTestDialog.this.inputWord, inputWord);
                    dataSource.close();

                    dialog.dismiss();

                    adapter.updateTest(inputWord, position);
                }

            }
        });

    }
}
