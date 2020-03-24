package fiitstu.gulis.cmsimulator.dialogs;

import android.annotation.SuppressLint;
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
import fiitstu.gulis.cmsimulator.adapters.grammar.TestsAdapter;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.elements.RegexTest;
import fiitstu.gulis.cmsimulator.elements.Symbol;
import fiitstu.gulis.cmsimulator.models.tasks.automata_type;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("ValidFragment")
public class NewRegexTestDialog extends DialogFragment {
    private EditText regexTestInput;
    private TextInputLayout regexInputLayout;
    private RegexTest.TestVerification testVerification;
    private TestsAdapter adapter;

    public NewRegexTestDialog() {
        this.testVerification = RegexTest.TestVerification.AUTOMATA;
    }

    @SuppressLint("ValidFragment")
    public NewRegexTestDialog(RegexTest.TestVerification testVerification) {
        this.testVerification = testVerification;
    }

    public void setAdapter(TestsAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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

        builder.setTitle(R.string.new_test);
        builder.setNeutralButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, null);

        AlertDialog dialog = builder.create();
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        AlertDialog dialog = (AlertDialog) this.getDialog();
        Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegexTest regexTest = RegexTest.getInstance();
                List<String> regex_parsed_input_tests = new ArrayList<>();
                switch (testVerification) {
                    case GRAMMAR:
                        regex_parsed_input_tests = regexTest.getListOfParsedStrings(regexTestInput.getText().toString());
                        DataSource dataSource = DataSource.getInstance();
                        dataSource.open();
                        for (String test : regex_parsed_input_tests) {
                            dataSource.addGrammarTest(test);
                            adapter.addNewTest(test);
                        }

                        NewRegexTestDialog.this.dismiss();
                        break;
                    case AUTOMATA:
                        List<Symbol> list = DataSource.getInstance().getInputAlphabetFullExtract();

                        //create maps from list
                        LongSparseArray<Symbol> inputAlphabetMap = new LongSparseArray<>();
                        for (Symbol symbol : list) {
                            inputAlphabetMap.put(symbol.getId(), symbol);
                        }

                        if (!regexTest.containsWrongSymbols(regexTestInput.getText().toString())) {
                            regex_parsed_input_tests = regexTest.getListOfParsedStrings(regexTestInput.getText().toString());
                        } else {
                            regexInputLayout.setError(getActivity().getString(R.string.regex_contains_wrong_symbols));
                            return;
                        }

                        List<List<Symbol>> listOfTest = new ArrayList<>();
                        for (String stringTest : regex_parsed_input_tests) {
                            List<Symbol> new_symbol_list = Symbol.stringIntoSymbolList(stringTest, inputAlphabetMap);
                            listOfTest.add(new_symbol_list);
                        }

                        EditTestDialog.EditTestDialogListener testManagementActivity = ((EditTestDialog.EditTestDialogListener) getActivity());
                        for (List<Symbol> test : listOfTest) {
                            testManagementActivity.onSaveRegexTestClick(test, null, true);
                        }

                        NewRegexTestDialog.this.dismiss();
                        break;
                }

            }
        });
    }
}