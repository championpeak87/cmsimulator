package fiitstu.gulis.cmsimulator.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.activities.MainActivity;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.elements.RegexTest;
import fiitstu.gulis.cmsimulator.elements.Symbol;
import fiitstu.gulis.cmsimulator.models.tasks.automata_type;

import java.util.ArrayList;
import java.util.List;

/**
 * A dialog for selecting tye of machine to be created
 * <p>
 * Created by Martin on 15. 4. 2017.
 */
public class NewRegexTestDialog extends DialogFragment {
    private EditText regexTestInput;
    private TextInputLayout regexInputLayout;
    private CheckBox IOCheckBox;
    private TextInputLayout regexOutputLayout;
    private EditText regexTestOutput;
    private TextView outputWordLabel;

    private boolean hasInputOutputTest = false;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public void setHasInputOutputTest(boolean value) {
        this.hasInputOutputTest = value;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder((getActivity()));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_new_regex_test, null);
        builder.setView(view);

        regexTestInput = view.findViewById(R.id.edittext_regex_test);
        regexInputLayout = view.findViewById(R.id.textinputlayout_regex_test);
        IOCheckBox = view.findViewById(R.id.checkbox_output_word);
        regexTestOutput = view.findViewById(R.id.edittext_regex_test_output);
        regexOutputLayout = view.findViewById(R.id.textinputlayout_regex_test_output);
        outputWordLabel = view.findViewById(R.id.textview_output_word_label);

        if (hasInputOutputTest) {
            IOCheckBox.setVisibility(View.VISIBLE);
            IOCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (IOCheckBox.isChecked()) {
                        regexOutputLayout.setVisibility(View.VISIBLE);
                        outputWordLabel.setVisibility(View.VISIBLE);
                    } else {
                        regexOutputLayout.setVisibility(View.GONE);
                        outputWordLabel.setVisibility(View.GONE);
                    }
                }
            });
            regexOutputLayout.setVisibility(IOCheckBox.isChecked() ? View.VISIBLE : View.GONE);
            outputWordLabel.setVisibility(IOCheckBox.isChecked() ? View.VISIBLE : View.GONE);
        } else {
            view.findViewById(R.id.textview_output_word_label).setVisibility(View.GONE);
            IOCheckBox.setVisibility(View.GONE);
            regexOutputLayout.setVisibility(View.GONE);
        }

        builder.setTitle(R.string.new_test);
        builder.setNeutralButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<Symbol> list = DataSource.getInstance().getInputAlphabetFullExtract();

                //create maps from list
                LongSparseArray<Symbol> inputAlphabetMap = new LongSparseArray<>();
                for (Symbol symbol : list) {
                    inputAlphabetMap.put(symbol.getId(), symbol);
                }

                RegexTest regexTest = RegexTest.getInstance();
                List<String> regex_parsed_tests = new ArrayList<>();
                if (!regexTest.containsWrongSymbols(regexTestInput.getText().toString())) {
                    regex_parsed_tests = regexTest.getListOfParsedStrings(regexTestInput.getText().toString());
                } else {
                    regexInputLayout.setError(getActivity().getString(R.string.regex_contains_wrong_symbols));
                }

                List<List<Symbol>> listOfTest = new ArrayList<>();
                for (String stringTest : regex_parsed_tests) {
                    List<Symbol> new_symbol_list = Symbol.stringIntoSymbolList(stringTest, inputAlphabetMap);
                    listOfTest.add(new_symbol_list);
                }

                EditTestDialog.EditTestDialogListener testManagementActivity = ((EditTestDialog.EditTestDialogListener) getActivity());
                for (List<Symbol> test : listOfTest) {
                    testManagementActivity.onSaveRegexTestClick(test, null, true);
                }

                /* TODO: Recyclerview shows empty tests, they are hidden behind the empty tests screen! Implement proper show!

                 */
            }
        });

        return builder.create();
    }
}