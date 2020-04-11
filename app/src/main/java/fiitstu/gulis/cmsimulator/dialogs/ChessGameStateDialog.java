package fiitstu.gulis.cmsimulator.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import fiitstu.gulis.cmsimulator.R;

@SuppressLint("ValidFragment")
public class ChessGameStateDialog extends DialogFragment {
    private static final String TAG = "ChessGameStateDialog";

    public static final String STATE_NAME_KEY = "STATE_NAME";
    public static final String INITIAL_STATE_KEY = "INITIAL_STATE_KEY";
    public static final String FINAL_STATE_KEY = "FINAL_STATE_KEY";

    public interface StateChangeListener {
        void onChange(Bundle output_bundle);
    }

    private DIALOG_STATE dialog_state;

    private String state_name;
    private boolean initalState;
    private boolean finalState;

    private EditText edittext_state_name;
    private CheckBox checkBox_initial_state;
    private CheckBox checkBox_final_state;

    private StateChangeListener stateChangeListener = null;

    private enum DIALOG_STATE {
        ADD,
        EDIT
    }

    public ChessGameStateDialog() {
        this.dialog_state = DIALOG_STATE.ADD;
    }

    public ChessGameStateDialog(String state_name, boolean initalState, boolean finalState) {
        this.dialog_state = DIALOG_STATE.EDIT;
        this.state_name = state_name;
        this.initalState = initalState;
        this.finalState = finalState;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_chess_game_state, null, false);

        edittext_state_name = view.findViewById(R.id.edittext_state_name);
        checkBox_initial_state = view.findViewById(R.id.checkbox_initial_state);
        checkBox_final_state = view.findViewById(R.id.checkbox_final_state);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
        switch (dialog_state) {
            case ADD:
                alertBuilder.setTitle(R.string.add_state);
                break;
            case EDIT:
                alertBuilder.setTitle(R.string.edit_state);

                edittext_state_name.setText(state_name);
                checkBox_initial_state.setChecked(initalState);
                checkBox_final_state.setChecked(finalState);
                break;
        }
        alertBuilder.setView(view);
        alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle outputBundle = new Bundle();

                final String stateName = edittext_state_name.getText().toString().trim();
                final boolean isInitial = checkBox_initial_state.isChecked();
                final boolean isFinal = checkBox_final_state.isChecked();

                outputBundle.putString(STATE_NAME_KEY, stateName);
                outputBundle.putBoolean(INITIAL_STATE_KEY, isInitial);
                outputBundle.putBoolean(FINAL_STATE_KEY, isFinal);
                if (stateChangeListener != null)
                    stateChangeListener.onChange(outputBundle);
            }
        });
        alertBuilder.setNeutralButton(android.R.string.cancel, null);

        AlertDialog dialog = alertBuilder.create();
        return dialog;

    }

    public void setStateChangeListener(StateChangeListener stateChangeListener) {
        this.stateChangeListener = stateChangeListener;
    }
}
