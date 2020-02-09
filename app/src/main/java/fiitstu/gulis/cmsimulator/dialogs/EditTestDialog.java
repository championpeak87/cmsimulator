package fiitstu.gulis.cmsimulator.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.activities.BulkTestActivity;
import fiitstu.gulis.cmsimulator.activities.MainActivity;
import fiitstu.gulis.cmsimulator.adapters.simulation.DefaultTapeListAdapter;
import fiitstu.gulis.cmsimulator.adapters.simulation.MachineTapeSpinnerAdapter;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.elements.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The dialog for creating and editing test scenarios for bulk testing
 * <p>
 * Created by Jakub Sedlář on 15.10.2017.
 */
public class EditTestDialog extends DialogFragment {

    public interface EditTestDialogListener {
        void onSaveTestClick(List<Symbol> input, List<Symbol> output, boolean isNew);
        void onSaveRegexTestClick(List<Symbol> input, List<Symbol> output, boolean isNew);
    }

    //log tag
    private static final String TAG = EditTestDialog.class.getName();

    //bundle arguments
    private static final String OFFER_OUTPUT = "OFFER_OUTPUT";
    private static final String IS_NEW = "IS_NEW";
    private static final String EDIT_TEST = "EDIT_TEST";

    //dialog content
    private CheckBox setOutputWordCheckBox;
    private TextView outputWordTextView;

    private RecyclerView inputTapeRecyclerView;
    private DefaultTapeListAdapter inputTapeAdapter;
    private RecyclerView outputTapeRecyclerView;
    private DefaultTapeListAdapter outputTapeAdapter;

    private TestScenario editTest;
    private boolean isNew;

    /**
     * Creates a new instance of the dialog with the given arguments set
     *
     * @param offerOutput if true, user will be allowed to set the expected output word
     * @param editTest    the scenario to be edited
     * @param isNew       true if new scenario is being added (affects the title of the dialog)
     * @return the created new instance
     */
    public static EditTestDialog newInstance(boolean offerOutput, TestScenario editTest, boolean isNew) {
        EditTestDialog frag = new EditTestDialog();
        Bundle args = new Bundle();
        args.putBoolean(OFFER_OUTPUT, offerOutput);
        args.putBoolean(IS_NEW, isNew);
        args.putSerializable(EDIT_TEST, editTest);
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DataSource.getInstance().open();

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_new_test, null);

        outputWordTextView = view.findViewById(R.id.textView_popup_new_test_output_word);
        setOutputWordCheckBox = view.findViewById(R.id.checkBox_popup_new_test_set_output_word);

        List<Symbol> inputAlphabet = DataSource.getInstance().getInputAlphabetFullExtract();
        Symbol.removeSpecialSymbols(inputAlphabet, Symbol.LEFT_BOUND | Symbol.RIGHT_BOUND);

        MachineTapeSpinnerAdapter machineTapeSpinnerAdapter = new MachineTapeSpinnerAdapter(getContext(), inputAlphabet);
        DisplayMetrics metrics = getContext().getApplicationContext().getResources().getDisplayMetrics();
        int dimension = (int) (Math.max(metrics.heightPixels, metrics.widthPixels) * 0.07);

        inputTapeRecyclerView = view.findViewById(R.id.recyclerView_popup_edit_test_input_tape);
        inputTapeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        inputTapeAdapter = new DefaultTapeListAdapter(getContext(), new MachineColorsGenerator(getContext(), DataSource.getInstance()), dimension, machineTapeSpinnerAdapter);
        inputTapeRecyclerView.setAdapter(inputTapeAdapter);
        inputTapeAdapter.setItemClickCallback(new TapeCallback(inputTapeAdapter, inputTapeRecyclerView));

        outputTapeRecyclerView = view.findViewById(R.id.recyclerView_popup_edit_test_output_tape);
        outputTapeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        outputTapeAdapter = new DefaultTapeListAdapter(getContext(), new MachineColorsGenerator(getContext(), DataSource.getInstance()), dimension, machineTapeSpinnerAdapter);
        outputTapeRecyclerView.setAdapter(outputTapeAdapter);
        outputTapeAdapter.setItemClickCallback(new TapeCallback(outputTapeAdapter, outputTapeRecyclerView));

        editTest = (TestScenario) getArguments().getSerializable(EDIT_TEST);

        if (getArguments().getBoolean(OFFER_OUTPUT)) {
            setOutputWordCheckBox.setVisibility(View.VISIBLE);
            if (editTest.getOutputWord() != null) {
                setOutputWordCheckBox.setChecked(true);
                outputWordTextView.setVisibility(View.VISIBLE);
                outputTapeRecyclerView.setVisibility(View.VISIBLE);
            }
        }

        setOutputWordCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    outputWordTextView.setVisibility(View.VISIBLE);
                    outputTapeRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    outputWordTextView.setVisibility(View.GONE);
                    outputTapeRecyclerView.setVisibility(View.GONE);
                }
            }
        });

        List<TapeElement> tapeElements = new ArrayList<>();
        for (int i = 0; i < editTest.getInputWord().size(); i++) {
            tapeElements.add(new TapeElement(editTest.getInputWord().get(i), i));
        }
        inputTapeAdapter.setItems(tapeElements);
        if (editTest.getOutputWord() != null) {
            tapeElements = new ArrayList<>();
            for (int i = 0; i < editTest.getOutputWord().size(); i++) {
                tapeElements.add(new TapeElement(editTest.getOutputWord().get(i), i));
            }
            outputTapeAdapter.setItems(tapeElements);
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        isNew = getArguments().getBoolean(IS_NEW);
        alertDialogBuilder.setTitle(isNew
                ? R.string.new_test_popup_title
                : R.string.edit_test_popup_title);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setInverseBackgroundForced(true);
        alertDialogBuilder.setPositiveButton(R.string.save, null);
        alertDialogBuilder.setNegativeButton(R.string.discard, null);

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
                    ((EditTestDialogListener) getActivity()).onSaveTestClick(Symbol.extractFromTape(inputTapeAdapter.getItems()),
                            setOutputWordCheckBox.isChecked() ? Symbol.extractFromTape(outputTapeAdapter.getItems()) : null,
                            isNew);

                    RecyclerView recycler = getActivity().findViewById(R.id.recyclerView_bulk_test_scenarios);
                    recycler.setVisibility(View.VISIBLE);

                    LinearLayout emptyLayout = getActivity().findViewById(R.id.linearLayout_empty_tests);
                    emptyLayout.setVisibility(View.GONE);

                }
            });
        }
    }

    class TapeCallback implements DefaultTapeListAdapter.ItemClickCallback {
        private DefaultTapeListAdapter tapeListAdapter;
        private RecyclerView recyclerView;

        public TapeCallback(DefaultTapeListAdapter tapeListAdapter, RecyclerView recyclerView) {
            this.tapeListAdapter = tapeListAdapter;
            this.recyclerView = recyclerView;
        }

        @Override
        public void onLeftButtonClick() {
            TapeElement tapeElement = new TapeElement(DataSource.getInstance().getInputAlphabetFullExtract().get(0),
                    tapeListAdapter.getItems().isEmpty() ? 0 : tapeListAdapter.getItems().get(0).getOrder() - 1);

            tapeListAdapter.addLeftItem(tapeElement);

            Log.i(TAG, "newTapeElement '" + tapeElement.getSymbol().getValue() + "' created");
        }

        @Override
        public void onRightButtonClick() {
            TapeElement tapeElement = new TapeElement(DataSource.getInstance().getInputAlphabetFullExtract().get(0),
                    tapeListAdapter.getItems().isEmpty() ? 0 : tapeListAdapter.getItems().get(tapeListAdapter.getItems().size() - 1).getOrder() + 1);

            tapeListAdapter.addRightItem(tapeElement);

            //scroll right
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    // Call smooth scroll
                    inputTapeRecyclerView.smoothScrollToPosition(tapeListAdapter.getItemCount() - 1);
                }
            });
            Log.i(TAG, "newTapeElement '" + tapeElement.getSymbol().getValue() + "' created");
        }

        @Override
        public void onSpinnerItemSelected(int position, Symbol symbol) {
            tapeListAdapter.getItems().get(position).setSymbol(symbol);
            Log.d(TAG, "Spinner item clicked: " + position + " " + symbol.getValue());
        }

        @Override
        public void onSpinnerLongClick(int position) {
            final TapeElement tapeElement = tapeListAdapter.getItems().get(position);

            CharSequence[] contextSource = new CharSequence[]{getResources().getString(R.string.remove)};

            new AlertDialog.Builder(getActivity())
                    .setItems(contextSource, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //a very useless switch, but ignoring "which" feels more wrong than having this switch
                            switch (which) {
                                case 0:
                                    tapeListAdapter.removeItem(tapeElement);
                                    break;
                            }
                        }
                    })
                    .show();
        }
    }
}
