package fiitstu.gulis.cmsimulator.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.activities.MainActivity;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.elements.RegexTest;
import fiitstu.gulis.cmsimulator.elements.Symbol;
import fiitstu.gulis.cmsimulator.models.tasks.automata_type;

import java.util.List;

/**
 * A dialog for selecting tye of machine to be created
 * <p>
 * Created by Martin on 15. 4. 2017.
 */
public class NewRegexTestDialog extends DialogFragment {
    private EditText regexTestInput;
    private TextInputLayout regexInputLayout;

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

        regexInputLayout.setError("NIEKDE JE CHYBA");

        builder.setTitle("PRIDAJ NOVY TEST");
        builder.setNeutralButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<Symbol> list = DataSource.getInstance().getInputAlphabetFullExtract();

                //create maps from list
                LongSparseArray<Symbol> inputAlphabetMap = new LongSparseArray<>();
                for (Symbol symbol : list) {
                    if (symbol.getValue() == "*")
                        continue;
                    inputAlphabetMap.put(symbol.getId(), symbol);
                }

                RegexTest regexTest = RegexTest.getInstance();
                if (!regexTest.containsWrongSymbols(regexTestInput.getText().toString()))
                {
                    regexTest.getListOfParsedStrings(regexTestInput.getText().toString());
                }

                String regex_input = regexTestInput.getText().toString();
                List<Symbol> new_test = Symbol.deserializeList(regex_input, inputAlphabetMap);
            }
        });

        return builder.create();
    }
}
