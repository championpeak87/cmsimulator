package fiitstu.gulis.cmsimulator.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fiitstu.gulis.cmsimulator.elements.State;
import fiitstu.gulis.cmsimulator.elements.Symbol;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.activities.MainActivity;
import fiitstu.gulis.cmsimulator.elements.Transition;

/**
 * A dialog for viewing the machine's formal specification
 *
 * Created by Martin on 16. 4. 2017.
 */
public class FormalSpecDialog extends DialogFragment {

    //log tag
    private static final String TAG = FormalSpecDialog.class.getName();

    //bundle values
    public static final String MACHINE_TYPE = "MACHINE_TYPE";
    public static final String STATE = "STATE";
    public static final String INPUT_ALPHABET = "INPUT_ALPHABET";
    public static final String TAPE_ALPHABET = "TAPE_ALPHABET";
    public static final String TRANSITION = "TRANSITION";
    public static final String INITIAL_STATE = "INITIAL_STATE";
    public static final String INITIAL_STACK_SYMBOL = "INITIAL_STACK_SYMBOL";
    public static final String FINAL_STATE = "FINAL_STATE";

    public static FormalSpecDialog newInstance(int machineType,
                                               List<Symbol> inputAlphabetList, List<Symbol> stackAlphabetList,
                                               List<State> stateList, List<Transition> transitionList) {
        FormalSpecDialog frag = new FormalSpecDialog();
        Bundle args = new Bundle();
        args.putInt(MACHINE_TYPE, machineType);

        String stateString = "{ ";
        String initialStateString = "";
        String finalStateString = "{ ";
        boolean checkFinal = false;
        if (stateList != null && stateList.size() > 0) {
            for (int i = 0; i < stateList.size(); i++) {
                stateString = stateString.concat(stateList.get(i).getValue() + ", ");
                if (stateList.get(i).isFinalState()) {
                    finalStateString = finalStateString.concat(stateList.get(i).getValue() + ", ");
                    checkFinal = true;
                }
                if (stateList.get(i).isInitialState()) {
                    initialStateString = stateList.get(i).getValue();
                }
            }
            stateString = stateString.substring(0, stateString.length() - 2).concat(" }");
        } else {
            stateString = stateString.concat(" }");
        }
        if (checkFinal) {
            finalStateString = finalStateString.substring(0, finalStateString.length() - 2).concat(" }");
        } else {
            finalStateString = finalStateString.concat(" }");
        }
        args.putString(STATE, stateString);
        args.putString(INITIAL_STATE, initialStateString);
        args.putString(FINAL_STATE, finalStateString);

        if (machineType == MainActivity.FINITE_STATE_AUTOMATON || machineType == MainActivity.PUSHDOWN_AUTOMATON) {
            String inputAlphabetString = "{ ";
            if (inputAlphabetList != null && inputAlphabetList.size() > 1) {
                for (int i = 1; i < inputAlphabetList.size(); i++) {
                    inputAlphabetString = inputAlphabetString.concat(inputAlphabetList.get(i).getValue() + ", ");
                }
                inputAlphabetString = inputAlphabetString.substring(0, inputAlphabetString.length() - 2).concat(" }");
            } else {
                inputAlphabetString = inputAlphabetString.concat(" }");
            }
            args.putString(INPUT_ALPHABET, inputAlphabetString);
        } else {
            String inputAlphabetString = "{ ";
            String tapeAlphabetString = "{ ";
            if (inputAlphabetList != null && inputAlphabetList.size() > 0) {
                tapeAlphabetString = tapeAlphabetString.concat(inputAlphabetList.get(0).getValue() + ", ");
                if (inputAlphabetList.size() > 1) {
                    for (int i = 1; i < inputAlphabetList.size(); i++) {
                        inputAlphabetString = inputAlphabetString.concat(inputAlphabetList.get(i).getValue() + ", ");
                        tapeAlphabetString = tapeAlphabetString.concat(inputAlphabetList.get(i).getValue() + ", ");
                    }
                    inputAlphabetString = inputAlphabetString.substring(0, inputAlphabetString.length() - 2).concat(" }");
                } else {
                    inputAlphabetString = inputAlphabetString.concat(" }");
                }
                tapeAlphabetString = tapeAlphabetString.substring(0, tapeAlphabetString.length() - 2).concat(" }");
            } else {
                inputAlphabetString = inputAlphabetString.concat(" }");
                tapeAlphabetString = tapeAlphabetString.concat(" }");
            }
            args.putString(INPUT_ALPHABET, inputAlphabetString);
            args.putString(TAPE_ALPHABET, tapeAlphabetString);
        }

        if (machineType == MainActivity.PUSHDOWN_AUTOMATON) {
            String tapeAlphabetString = "{ ";
            if (stackAlphabetList != null && stackAlphabetList.size() > 0) {
                for (int i = 0; i < stackAlphabetList.size(); i++) {
                    tapeAlphabetString = tapeAlphabetString.concat(stackAlphabetList.get(i).getValue() + ", ");
                }
                tapeAlphabetString = tapeAlphabetString.substring(0, tapeAlphabetString.length() - 2).concat(" }");
            } else {
                tapeAlphabetString = tapeAlphabetString.concat(" }");
            }
            args.putString(TAPE_ALPHABET, tapeAlphabetString);

            String initialStackSymbolString;
            if (stackAlphabetList != null && stackAlphabetList.get(0) != null) {
                initialStackSymbolString = stackAlphabetList.get(0).getValue();
            } else {
                initialStackSymbolString = "";
            }
            args.putString(INITIAL_STACK_SYMBOL, initialStackSymbolString);
        }

        ArrayList<String> transitionStringList = new ArrayList<>();
        if (transitionList != null) {
            for (Transition transition : transitionList) {
                transitionStringList.add(transition.getDesc());
            }
        }
        args.putStringArrayList(TRANSITION, transitionStringList);

        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_formal_spec, null);
        int machineType = getArguments().getInt(MACHINE_TYPE);

        String stateString = getArguments().getString(STATE, "");
        TextView stateTextView = view.findViewById(R.id.textView_formal_spec_state);
        stateTextView.setText(getResources().getString(R.string.spec_state) + " " + stateString);

        String inputAlphabetString = getArguments().getString(INPUT_ALPHABET, "");
        TextView inputAlphabetTextView = view.findViewById(R.id.textView_formal_spec_input_alphabet);
        inputAlphabetTextView.setText(getResources().getString(R.string.spec_input_alphabet) + " " + inputAlphabetString);

        TextView tapeAlphabetTextView = view.findViewById(R.id.textView_formal_spec_tape_alphabet);
        if (machineType == MainActivity.PUSHDOWN_AUTOMATON
                || machineType == MainActivity.LINEAR_BOUNDED_AUTOMATON || machineType == MainActivity.TURING_MACHINE) {
            String tapeAlphabetString = getArguments().getString(TAPE_ALPHABET, "");
            tapeAlphabetTextView.setText(getResources().getString(R.string.spec_tape_alphabet) + " " + tapeAlphabetString);
        } else {
            tapeAlphabetTextView.setVisibility(View.GONE);
        }

        String initialStateString = getArguments().getString(INITIAL_STATE, "");
        TextView initialStateTextView = view.findViewById(R.id.textView_formal_spec_initial_state);
        initialStateTextView.setText(getResources().getString(R.string.spec_initial_state) + " " + initialStateString);

        TextView initialStackSymbolTextView = view.findViewById(R.id.textView_formal_spec_initial_stack_symbol);
        if (machineType == MainActivity.PUSHDOWN_AUTOMATON) {
            String initialStackSymbolString = getArguments().getString(INITIAL_STACK_SYMBOL, "");
            initialStackSymbolTextView.setText(getResources().getString(R.string.spec_initial_stack_symbol) + " " + initialStackSymbolString);
        } else {
            initialStackSymbolTextView.setVisibility(View.GONE);
        }

        String finalStateString = getArguments().getString(FINAL_STATE, "");
        TextView finalStateTextView = view.findViewById(R.id.textView_formal_spec_final_state);
        finalStateTextView.setText(getResources().getString(R.string.spec_final_state) + " " + finalStateString);

        final ArrayList<String> transitionStringList = getArguments().getStringArrayList(TRANSITION);

        if (transitionStringList != null) {
            LinearLayout layout = view.findViewById(R.id.linearLayout_formal_spec_items);
            for (String transitionString: transitionStringList) {
                TextView transitionTextView = new TextView(new ContextThemeWrapper(getContext(), R.style.formalSpecTextStyle), null, R.style.formalSpecTextStyle);
                transitionTextView.setText(transitionString);
                layout.addView(transitionTextView);
            }
        }

        TextView title = view.findViewById(R.id.textView_formal_spec_title);
        switch (machineType) {
            case MainActivity.FINITE_STATE_AUTOMATON:
                title.setText(getResources().getString(R.string.spec_fsa));
                break;
            case MainActivity.PUSHDOWN_AUTOMATON:
                title.setText(getResources().getString(R.string.spec_pda));
                break;
            case MainActivity.LINEAR_BOUNDED_AUTOMATON:
                title.setText(getResources().getString(R.string.spec_lba));
                break;
            case MainActivity.TURING_MACHINE:
                title.setText(getResources().getString(R.string.spec_tm));
                break;
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(getResources().getString(R.string.formal_spec));
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setInverseBackgroundForced(true);
        alertDialogBuilder.setNeutralButton(getResources().getString(R.string.ok), null);

        return alertDialogBuilder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
