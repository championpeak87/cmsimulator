package fiitstu.gulis.cmsimulator.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

import fiitstu.gulis.cmsimulator.adapters.configuration.ConfigurationSpinnerAdapter;
import fiitstu.gulis.cmsimulator.adapters.configuration.ConfigurationStackSpinnerAdapter;
import fiitstu.gulis.cmsimulator.elements.PdaTransition;
import fiitstu.gulis.cmsimulator.elements.State;
import fiitstu.gulis.cmsimulator.elements.Symbol;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.activities.ConfigurationActivity;
import fiitstu.gulis.cmsimulator.adapters.configuration.ConfigurationStackListAdapter;
import fiitstu.gulis.cmsimulator.elements.TmTransition;

/**
 * A dialog for adding and editing symbols, states and transitions in the ConfigurationActivity.
 *
 * Warning: this class and everything related to it is a stuff of nightmares.
 *
 * Created by Martin on 16. 4. 2017.
 */

public class ConfigurationDialog extends DialogFragment {

    //log tag
    private static final String TAG = ConfigurationDialog.class.getName();

    //bundle values
    public static final String ELEMENT_TYPE = "ELEMENT_TYPE";
    public static final String ELEMENT_ACTION = "ELEMENT_ACTION";
    public static final String FROM_STATE_ID = "FROM_STATE_ID";
    public static final String TO_STATE_ID = "TO_STATE_ID";
    public static final String INDEX = "INDEX";

    private int elementType;

    //dialog content
    private EditText valueEditText;
    private CheckBox initialStateCheckBox;
    private CheckBox finalStateCheckBox;
    private Spinner fromStateSpinner;
    private ConfigurationSpinnerAdapter symbolSpinnerAdapter;
    private ConfigurationSpinnerAdapter filteredSymbolSpinnerAdapter;
    private Spinner readSymbolSpinner;
    private ConfigurationStackListAdapter popSymbolAdapter;
    private Spinner toStateSpinner;
    private ConfigurationStackListAdapter pushSymbolAdapter;
    private Spinner writeSymbolSpinner;
    private RadioButton radioButtonLeft;
    private RadioButton radioButtonRight;

    private View newElementView;

    public interface ConfigurationDialogListener {
        void inputSymbolConfigurationDialogClick(String value);

        void stackSymbolConfigurationDialogClick(String value);

        void stateConfigurationDialogClick(String value, boolean initialState, boolean finalState);

        void fsaTransitionConfigurationDialogClick(State fromState, Symbol readSymbol, State toState);

        void pdaTransitionConfigurationDialogClick(State fromState, Symbol readSymbol, State toState, List<Symbol> popSymbolList, List<Symbol> pushSymbolList);

        void tmTransitionConfigurationDialogClick(State fromState, Symbol readSymbol, State toState, Symbol writeSymbol, TmTransition.Direction direction);
    }

    public static ConfigurationDialog newInstance(int elementType, int elementAction) {
        ConfigurationDialog frag = new ConfigurationDialog();
        Bundle args = new Bundle();
        args.putInt(ELEMENT_TYPE, elementType);
        args.putInt(ELEMENT_ACTION, elementAction);
        frag.setArguments(args);
        return frag;
    }

    public static ConfigurationDialog newInstance(int elementType, int elementAction, int index) {
        ConfigurationDialog frag = new ConfigurationDialog();
        Bundle args = new Bundle();
        args.putInt(ELEMENT_TYPE, elementType);
        args.putInt(ELEMENT_ACTION, elementAction);
        args.putInt(INDEX, index);
        frag.setArguments(args);
        return frag;
    }

    public static ConfigurationDialog newInstance(int elementType, int elementAction, long fromStateId, long toStateId) {
        ConfigurationDialog frag = new ConfigurationDialog();
        Bundle args = new Bundle();
        args.putInt(ELEMENT_TYPE, elementType);
        args.putInt(ELEMENT_ACTION, elementAction);
        args.putLong(FROM_STATE_ID, fromStateId);
        args.putLong(TO_STATE_ID, toStateId);
        frag.setArguments(args);
        return frag;
    }

    public void addAndSelectState(State state) {
        Spinner spinner = (Spinner) newElementView;
        ConfigurationSpinnerAdapter spinnerAdapter = (ConfigurationSpinnerAdapter)spinner.getAdapter();
        List items = spinnerAdapter.getItems();
        if (state != items.get(items.size() - 1)) {
            items.add(items.size() - 1, state);
            spinnerAdapter.notifyDataSetChanged();
            spinner.setSelection(items.size() - 2);
        }
    }

    public void addAndSelectInputSymbol(Symbol symbol) {
        Spinner spinner = (Spinner) newElementView;
        List items = symbolSpinnerAdapter.getItems();
        items.add(items.size() - 1, symbol);
        symbolSpinnerAdapter.notifyDataSetChanged();
        items = filteredSymbolSpinnerAdapter.getItems();
        items.add(items.size() - 1, symbol);
        filteredSymbolSpinnerAdapter.notifyDataSetChanged();
        spinner.setSelection(items.size() - 2);
    }

    public void addAndSelectStackSymbol(Symbol symbol) {
        RecyclerView recyclerView = (RecyclerView) newElementView;
        ConfigurationStackListAdapter stackAdapter = (ConfigurationStackListAdapter) recyclerView.getAdapter();
        List<Symbol> items = stackAdapter.getStackAlphabetSpinnerAdapter().getItems();
        items.add(items.size() - 1, symbol);
        stackAdapter.selectSymbol(getArguments().getInt(INDEX), symbol);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_configuration, null);
        elementType = getArguments().getInt(ELEMENT_TYPE);
        int elementAction = getArguments().getInt(ELEMENT_ACTION);
        long fromStateId = getArguments().getLong(FROM_STATE_ID, -1);
        long toStateId = getArguments().getLong(TO_STATE_ID, -1);

        ////popupWindow content initialization
        LinearLayout valueLinearLayout = view.findViewById(R.id.linearLayout_popup_configuration_value);
        TextView valueTextView = view.findViewById(R.id.textView_popup_configuration_value);
        valueEditText = view.findViewById(R.id.editText_popup_configuration_value);

        LinearLayout initialStateLinearLayout = view.findViewById(R.id.linearLayout_popup_configuration_state_initial);
        initialStateCheckBox = view.findViewById(R.id.checkBox_popup_configuration_state_initial);

        LinearLayout finalStateLinearLayout = view.findViewById(R.id.linearLayout_popup_configuration_state_final);
        finalStateCheckBox = view.findViewById(R.id.checkBox_popup_configuration_state_final);

        ConfigurationSpinnerAdapter stateSpinnerAdapter = new ConfigurationSpinnerAdapter(getContext(),
                new ArrayList<>(((ConfigurationActivity) getActivity()).getStateList()), ConfigurationActivity.STATE);

        final LinearLayout fromStateLinearLayout = view.findViewById(R.id.linearLayout_popup_configuration_transition_from_state);
        fromStateSpinner = view.findViewById(R.id.spinner_popup_configuration_transition_from_state);
        fromStateSpinner.setAdapter(stateSpinnerAdapter);


        List<Symbol> symbolList = new ArrayList<>(((ConfigurationActivity) getActivity()).getInputAlphabetList());
        symbolSpinnerAdapter = new ConfigurationSpinnerAdapter(getContext(),
                new ArrayList<>(symbolList), ConfigurationActivity.INPUT_SYMBOL);
        Symbol.removeSpecialSymbols(symbolList, Symbol.LEFT_BOUND | Symbol.RIGHT_BOUND);
        filteredSymbolSpinnerAdapter = new ConfigurationSpinnerAdapter(getContext(),
                symbolList, ConfigurationActivity.INPUT_SYMBOL);

        LinearLayout readSymbolLinearLayout = view.findViewById(R.id.linearLayout_popup_configuration_transition_read_symbol);
        readSymbolSpinner = view.findViewById(R.id.spinner_popup_configuration_transition_read_symbol);
        readSymbolSpinner.setAdapter(symbolSpinnerAdapter);

        LinearLayout popSymbolLinearLayout = view.findViewById(R.id.linearLayout_popup_configuration_transition_pop_symbol);
        final RecyclerView popSymbolRecyclerView = view.findViewById(R.id.recyclerView_popup_configuration_transition_pop_symbol);
        popSymbolRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, true));

        LinearLayout toStateLinearLayout = view.findViewById(R.id.linearLayout_popup_configuration_transition_to_state);
        toStateSpinner = view.findViewById(R.id.spinner_popup_configuration_transition_to_state);
        toStateSpinner.setAdapter(stateSpinnerAdapter);

        LinearLayout pushSymbolLinearLayout = view.findViewById(R.id.linearLayout_popup_configuration_transition_push_symbol);
        final RecyclerView pushSymbolRecyclerView = view.findViewById(R.id.recyclerView_popup_configuration_transition_push_symbol);
        pushSymbolRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, true));

        LinearLayout writeSymbolLinearLayout = view.findViewById(R.id.linearLayout_popup_configuration_transition_write_symbol);
        writeSymbolSpinner = view.findViewById(R.id.spinner_popup_configuration_transition_write_symbol);
        writeSymbolSpinner.setAdapter(symbolSpinnerAdapter);

        ConfigurationStackSpinnerAdapter stackSpinnerAdapter = new ConfigurationStackSpinnerAdapter(getContext(), new ArrayList<>(((ConfigurationActivity) getActivity()).getStackAlphabetList()));

        popSymbolAdapter = new ConfigurationStackListAdapter(getActivity(),
                (((ConfigurationActivity) getActivity()).getInputAlphabetList().get(0)),
                stackSpinnerAdapter);
        popSymbolAdapter.setAddItemListener(new ConfigurationStackListAdapter.AddItemListener() {
            @Override
            public void onAddItem(int index) {
                newElementView = popSymbolRecyclerView;
                ((ConfigurationActivity) getActivity()).showStackSupportConfigurationDialog(index);
            }
        });
        popSymbolRecyclerView.setAdapter(popSymbolAdapter);

        pushSymbolAdapter = new ConfigurationStackListAdapter(getActivity(),
                (((ConfigurationActivity) getActivity()).getInputAlphabetList().get(0)),
                stackSpinnerAdapter);
        pushSymbolAdapter.setAddItemListener(new ConfigurationStackListAdapter.AddItemListener() {
            @Override
            public void onAddItem(int index) {
                newElementView = pushSymbolRecyclerView;
                ((ConfigurationActivity) getActivity()).showStackSupportConfigurationDialog(index);
            }
        });
        pushSymbolRecyclerView.setAdapter(pushSymbolAdapter);

        LinearLayout directionLinearLayout = view.findViewById(R.id.linearLayout_popup_configuration_transition_direction);
        radioButtonLeft = view.findViewById(R.id.radioButton_popup_configuration_transition_direction_left);
        radioButtonRight = view.findViewById(R.id.radioButton_popup_configuration_transition_direction_right);

        readSymbolSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == symbolSpinnerAdapter.getCount() - 1) {
                    readSymbolSpinner.setSelection(0);
                    newElementView = readSymbolSpinner;
                    ((ConfigurationActivity) getActivity()).showSupportConfigurationDialog(ConfigurationActivity.INPUT_SYMBOL);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        fromStateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == fromStateSpinner.getCount() - 1) {
                    fromStateSpinner.setSelection(0);
                    newElementView = fromStateSpinner;
                    ((ConfigurationActivity) getActivity()).showSupportConfigurationDialog(ConfigurationActivity.STATE);                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        toStateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == toStateSpinner.getCount() - 1) {
                    toStateSpinner.setSelection(0);
                    newElementView = toStateSpinner;
                    ((ConfigurationActivity) getActivity()).showSupportConfigurationDialog(ConfigurationActivity.STATE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        switch (elementType) {
            case ConfigurationActivity.INPUT_SYMBOL:
                valueLinearLayout.setVisibility(View.VISIBLE);
                initialStateLinearLayout.setVisibility(View.GONE);
                finalStateLinearLayout.setVisibility(View.GONE);
                fromStateLinearLayout.setVisibility(View.GONE);
                readSymbolLinearLayout.setVisibility(View.GONE);
                popSymbolLinearLayout.setVisibility(View.GONE);
                toStateLinearLayout.setVisibility(View.GONE);
                pushSymbolLinearLayout.setVisibility(View.GONE);
                writeSymbolLinearLayout.setVisibility(View.GONE);
                directionLinearLayout.setVisibility(View.GONE);
                switch (elementAction) {
                    case ConfigurationActivity.NEW:
                        alertDialogBuilder.setTitle(getResources().getString(R.string.new_symbol));
                        valueTextView.setText(R.string.symbol);
                        valueEditText.setText("");
                        break;
                    case ConfigurationActivity.UPDATE:
                        alertDialogBuilder.setTitle(getResources().getString(R.string.edit_symbol));
                        valueTextView.setText(R.string.symbol);
                        valueEditText.setText(((ConfigurationActivity) getActivity()).getInputSymbolEdit().getValue());
                        break;
                }
                break;
            case ConfigurationActivity.STACK_SYMBOL:
                valueLinearLayout.setVisibility(View.VISIBLE);
                initialStateLinearLayout.setVisibility(View.GONE);
                finalStateLinearLayout.setVisibility(View.GONE);
                fromStateLinearLayout.setVisibility(View.GONE);
                readSymbolLinearLayout.setVisibility(View.GONE);
                popSymbolLinearLayout.setVisibility(View.GONE);
                toStateLinearLayout.setVisibility(View.GONE);
                pushSymbolLinearLayout.setVisibility(View.GONE);
                writeSymbolLinearLayout.setVisibility(View.GONE);
                directionLinearLayout.setVisibility(View.GONE);
                switch (elementAction) {
                    case ConfigurationActivity.NEW:
                        alertDialogBuilder.setTitle(getResources().getString(R.string.new_symbol));
                        valueTextView.setText(R.string.symbol);
                        valueEditText.setText("");
                        break;
                    case ConfigurationActivity.UPDATE:
                        alertDialogBuilder.setTitle(getResources().getString(R.string.edit_symbol));
                        valueTextView.setText(R.string.symbol);
                        valueEditText.setText(((ConfigurationActivity) getActivity()).getStackSymbolEdit().getValue());
                        break;
                }
                break;
            case ConfigurationActivity.STATE:
                valueLinearLayout.setVisibility(View.VISIBLE);
                initialStateLinearLayout.setVisibility(View.VISIBLE);
                finalStateLinearLayout.setVisibility(View.VISIBLE);
                fromStateLinearLayout.setVisibility(View.GONE);
                readSymbolLinearLayout.setVisibility(View.GONE);
                popSymbolLinearLayout.setVisibility(View.GONE);
                toStateLinearLayout.setVisibility(View.GONE);
                pushSymbolLinearLayout.setVisibility(View.GONE);
                writeSymbolLinearLayout.setVisibility(View.GONE);
                directionLinearLayout.setVisibility(View.GONE);
                switch (elementAction) {
                    case ConfigurationActivity.NEW:
                        alertDialogBuilder.setTitle(getResources().getString(R.string.new_state));
                        valueTextView.setText(R.string.state);
                        valueEditText.setText("");
                        initialStateCheckBox.setChecked(false);
                        finalStateCheckBox.setChecked(false);
                        break;
                    case ConfigurationActivity.UPDATE:
                        alertDialogBuilder.setTitle(getResources().getString(R.string.edit_state));
                        valueTextView.setText(R.string.state);
                        valueEditText.setText(((ConfigurationActivity) getActivity()).getStateEdit().getValue());
                        initialStateCheckBox.setChecked(((ConfigurationActivity) getActivity()).getStateEdit().isInitialState());
                        finalStateCheckBox.setChecked(((ConfigurationActivity) getActivity()).getStateEdit().isFinalState());
                        break;
                }
                break;
            case ConfigurationActivity.FSA_TRANSITION:
                valueLinearLayout.setVisibility(View.GONE);
                initialStateLinearLayout.setVisibility(View.GONE);
                finalStateLinearLayout.setVisibility(View.GONE);
                fromStateLinearLayout.setVisibility(View.VISIBLE);
                readSymbolLinearLayout.setVisibility(View.VISIBLE);
                popSymbolLinearLayout.setVisibility(View.GONE);
                toStateLinearLayout.setVisibility(View.VISIBLE);
                pushSymbolLinearLayout.setVisibility(View.GONE);
                writeSymbolLinearLayout.setVisibility(View.GONE);
                directionLinearLayout.setVisibility(View.GONE);
                switch (elementAction) {
                    case ConfigurationActivity.NEW:
                        alertDialogBuilder.setTitle(getResources().getString(R.string.new_transition));
                        fromStateSpinner.setSelection(0);
                        readSymbolSpinner.setSelection(0);
                        toStateSpinner.setSelection(0);
                        break;
                    case ConfigurationActivity.UPDATE:
                        alertDialogBuilder.setTitle(getResources().getString(R.string.edit_transition));
                        fromStateSpinner.setSelection(
                                getStateSpinnerIndex(fromStateSpinner, ((ConfigurationActivity) getActivity()).getTransitionEdit().getFromState().getId()));
                        readSymbolSpinner.setSelection(
                                getSymbolSpinnerIndex(readSymbolSpinner, ((ConfigurationActivity) getActivity()).getTransitionEdit().getReadSymbol().getId()));
                        toStateSpinner.setSelection(
                                getStateSpinnerIndex(toStateSpinner, ((ConfigurationActivity) getActivity()).getTransitionEdit().getToState().getId()));
                        break;
                }
                break;
            case ConfigurationActivity.PDA_TRANSITION:
                valueLinearLayout.setVisibility(View.GONE);
                initialStateLinearLayout.setVisibility(View.GONE);
                finalStateLinearLayout.setVisibility(View.GONE);
                fromStateLinearLayout.setVisibility(View.VISIBLE);
                readSymbolLinearLayout.setVisibility(View.VISIBLE);
                popSymbolLinearLayout.setVisibility(View.VISIBLE);
                toStateLinearLayout.setVisibility(View.VISIBLE);
                pushSymbolLinearLayout.setVisibility(View.VISIBLE);
                writeSymbolLinearLayout.setVisibility(View.GONE);
                directionLinearLayout.setVisibility(View.GONE);
                switch (elementAction) {
                    case ConfigurationActivity.NEW:
                        alertDialogBuilder.setTitle(getResources().getString(R.string.new_transition));
                        fromStateSpinner.setSelection(0);
                        readSymbolSpinner.setSelection(0);
                        popSymbolAdapter.removeAll();
                        toStateSpinner.setSelection(0);
                        pushSymbolAdapter.removeAll();
                        break;
                    case ConfigurationActivity.UPDATE:
                        alertDialogBuilder.setTitle(getResources().getString(R.string.edit_transition));
                        fromStateSpinner.setSelection(
                                getStateSpinnerIndex(fromStateSpinner, ((ConfigurationActivity) getActivity()).getTransitionEdit().getFromState().getId()));
                        readSymbolSpinner.setSelection(
                                getSymbolSpinnerIndex(readSymbolSpinner, ((ConfigurationActivity) getActivity()).getTransitionEdit().getReadSymbol().getId()));
                        popSymbolAdapter.setItemList(((PdaTransition) ((ConfigurationActivity) getActivity()).getTransitionEdit()).getPopSymbolList());
                        toStateSpinner.setSelection(
                                getStateSpinnerIndex(toStateSpinner, ((ConfigurationActivity) getActivity()).getTransitionEdit().getToState().getId()));
                        pushSymbolAdapter.setItemList(((PdaTransition) ((ConfigurationActivity) getActivity()).getTransitionEdit()).getPushSymbolList());
                        break;
                }
                break;
            case ConfigurationActivity.TM_TRANSITION:
                valueLinearLayout.setVisibility(View.GONE);
                initialStateLinearLayout.setVisibility(View.GONE);
                finalStateLinearLayout.setVisibility(View.GONE);
                fromStateLinearLayout.setVisibility(View.VISIBLE);
                readSymbolLinearLayout.setVisibility(View.VISIBLE);
                popSymbolLinearLayout.setVisibility(View.GONE);
                toStateLinearLayout.setVisibility(View.VISIBLE);
                pushSymbolLinearLayout.setVisibility(View.GONE);
                writeSymbolLinearLayout.setVisibility(View.VISIBLE);
                directionLinearLayout.setVisibility(View.VISIBLE);

                final TmTransition tmTransition = (TmTransition) ((ConfigurationActivity) getActivity()).getTransitionEdit();

                readSymbolSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position == symbolSpinnerAdapter.getCount() - 1) {
                            newElementView = readSymbolSpinner;
                            ((ConfigurationActivity) getActivity()).showSupportConfigurationDialog(ConfigurationActivity.INPUT_SYMBOL);
                        }
                        else {
                            Symbol readSymbol = (Symbol) readSymbolSpinner.getItemAtPosition(position);
                            if (readSymbol.isRightBound()) {
                                writeSymbolSpinner.setAdapter(symbolSpinnerAdapter);
                                writeSymbolSpinner.setSelection(position);
                                writeSymbolSpinner.setEnabled(false);
                                radioButtonLeft.setChecked(true);
                                radioButtonRight.setChecked(false);
                                radioButtonRight.setEnabled(false);
                            } else if (readSymbol.isLeftBound()) {
                                writeSymbolSpinner.setAdapter(symbolSpinnerAdapter);
                                writeSymbolSpinner.setSelection(position);
                                writeSymbolSpinner.setEnabled(false);
                                radioButtonRight.setChecked(true);
                                radioButtonLeft.setChecked(false);
                                radioButtonLeft.setEnabled(false);
                            } else {
                                writeSymbolSpinner.setAdapter(filteredSymbolSpinnerAdapter);
                                writeSymbolSpinner.setEnabled(true);
                                radioButtonRight.setEnabled(true);
                                radioButtonLeft.setEnabled(true);
                                if (tmTransition != null) {
                                    writeSymbolSpinner.setSelection(
                                            getSymbolSpinnerIndex(writeSymbolSpinner, tmTransition.getWriteSymbol().getId()));
                                }
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

                writeSymbolSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position == writeSymbolSpinner.getAdapter().getCount() - 1) {
                            writeSymbolSpinner.setSelection(0);
                            newElementView = writeSymbolSpinner;
                            ((ConfigurationActivity) getActivity()).showSupportConfigurationDialog(ConfigurationActivity.INPUT_SYMBOL);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

                switch (elementAction) {
                    case ConfigurationActivity.NEW:
                        alertDialogBuilder.setTitle(getResources().getString(R.string.new_transition));
                        fromStateSpinner.setSelection(0);
                        readSymbolSpinner.setSelection(0);
                        toStateSpinner.setSelection(0);
                        writeSymbolSpinner.setSelection(0);
                        if (tmTransition != null
                            && !(tmTransition.getReadSymbol().isRightBound() || tmTransition.getReadSymbol().isLeftBound())) {
                            radioButtonLeft.setChecked(false);
                            radioButtonRight.setChecked(true);
                        }
                        break;
                    case ConfigurationActivity.UPDATE:
                        alertDialogBuilder.setTitle(getResources().getString(R.string.edit_transition));

                        fromStateSpinner.setSelection(
                                getStateSpinnerIndex(fromStateSpinner, tmTransition.getFromState().getId()));
                        readSymbolSpinner.setSelection(
                                getSymbolSpinnerIndex(readSymbolSpinner, tmTransition.getReadSymbol().getId()));
                        toStateSpinner.setSelection(
                                getStateSpinnerIndex(toStateSpinner, tmTransition.getToState().getId()));
                        writeSymbolSpinner.setSelection(
                                getSymbolSpinnerIndex(writeSymbolSpinner, tmTransition.getWriteSymbol().getId()));
                        radioButtonLeft.setChecked(tmTransition.getDirection() == TmTransition.Direction.LEFT);
                        radioButtonRight.setChecked(tmTransition.getDirection() == TmTransition.Direction.RIGHT);
                        break;
                }
                break;
        }
        if (fromStateId != -1) {
            fromStateSpinner.setSelection(getStateSpinnerIndex(fromStateSpinner, fromStateId));
        }
        if (toStateId != -1) {
            toStateSpinner.setSelection(getStateSpinnerIndex(toStateSpinner, toStateId));
        }

        alertDialogBuilder.setView(view);
        alertDialogBuilder.setInverseBackgroundForced(true);
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.save), null);
        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.discard), null);

        return alertDialogBuilder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        final AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (elementType) {
                        case ConfigurationActivity.INPUT_SYMBOL:
                            ((ConfigurationDialogListener) getActivity()).inputSymbolConfigurationDialogClick(
                                    valueEditText.getText().toString());
                            break;
                        case ConfigurationActivity.STACK_SYMBOL:
                            ((ConfigurationDialogListener) getActivity()).stackSymbolConfigurationDialogClick(
                                    valueEditText.getText().toString());
                            break;
                        case ConfigurationActivity.STATE:
                            ((ConfigurationDialogListener) getActivity()).stateConfigurationDialogClick(
                                    valueEditText.getText().toString(),
                                    initialStateCheckBox.isChecked(),
                                    finalStateCheckBox.isChecked());
                            break;
                        case ConfigurationActivity.FSA_TRANSITION:
                            ((ConfigurationDialogListener) getActivity()).fsaTransitionConfigurationDialogClick(
                                    (State) fromStateSpinner.getSelectedItem(),
                                    (Symbol) readSymbolSpinner.getSelectedItem(),
                                    (State) toStateSpinner.getSelectedItem());
                            break;
                        case ConfigurationActivity.PDA_TRANSITION:
                            List<Symbol> popSymbolList = new ArrayList<>(popSymbolAdapter.getItems());
                            popSymbolList.remove(popSymbolList.size() - 1);
                            List<Symbol> pushSymbolList = new ArrayList<>(pushSymbolAdapter.getItems());
                            pushSymbolList.remove(pushSymbolList.size() - 1);
                            ((ConfigurationDialogListener) getActivity()).pdaTransitionConfigurationDialogClick(
                                    (State) fromStateSpinner.getSelectedItem(),
                                    (Symbol) readSymbolSpinner.getSelectedItem(),
                                    (State) toStateSpinner.getSelectedItem(),
                                    popSymbolList,
                                    pushSymbolList);
                            break;
                        case ConfigurationActivity.TM_TRANSITION:
                            ((ConfigurationDialogListener) getActivity()).tmTransitionConfigurationDialogClick(
                                    (State) fromStateSpinner.getSelectedItem(),
                                    (Symbol) readSymbolSpinner.getSelectedItem(),
                                    (State) toStateSpinner.getSelectedItem(),
                                    (Symbol) writeSymbolSpinner.getSelectedItem(),
                                    radioButtonLeft.isChecked() ? TmTransition.Direction.LEFT : TmTransition.Direction.RIGHT);
                            break;
                    }
                }
            });
        }
    }

    //method to get index of Symbol spinner content
    private int getSymbolSpinnerIndex(Spinner spinner, long id) {
        int index = -1;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (((Symbol) spinner.getItemAtPosition(i)).getId() == id) {
                index = i;
                break;
            }
        }
        return index;
    }

    //method to get index of Symbol spinner content
    private int getStateSpinnerIndex(Spinner spinner, long id) {
        int index = -1;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (((State) spinner.getItemAtPosition(i)).getId() == id) {
                index = i;
                break;
            }
        }
        return index;
    }
}
