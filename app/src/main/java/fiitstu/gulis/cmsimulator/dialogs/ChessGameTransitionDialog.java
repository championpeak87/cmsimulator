package fiitstu.gulis.cmsimulator.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.activities.ChessGameActivity;
import fiitstu.gulis.cmsimulator.activities.ConfigurationActivity;
import fiitstu.gulis.cmsimulator.adapters.configuration.ConfigurationSpinnerAdapter;
import fiitstu.gulis.cmsimulator.diagram.DiagramView;
import fiitstu.gulis.cmsimulator.elements.State;
import fiitstu.gulis.cmsimulator.elements.Symbol;

import java.util.ArrayList;

@SuppressLint("ValidFragment")
public class ChessGameTransitionDialog extends DialogFragment {
    private static final String TAG = "ChessGameTransitionDial";

    public static final String DIRECTION_KEY = "DIRECTION";

    private AUTOMATA_TYPE automata_type;
    private DIALOG_TYPE dialog_type;

    private TransitionChangeListener transitionChangeListener;

    private EditText
            edittext_start_state,
            edittext_finish_state;

    private ToggleButton
            togglebutton_up,
            togglebutton_left,
            togglebutton_right,
            togglebutton_down;

    private State
            fromState,
            toState;

    public interface TransitionChangeListener {
        void OnChange(Bundle output_bundle);
    }

    public enum AUTOMATA_TYPE {
        FINITE,
        PUSHDOWN
    }

    public enum DIALOG_TYPE {
        NEW,
        EDIT
    }

    @SuppressLint("ValidFragment")
    public ChessGameTransitionDialog(State fromState, State toState, DIALOG_TYPE dialog_type, AUTOMATA_TYPE automata_type) {
        this.dialog_type = dialog_type;
        this.fromState = fromState;
        this.toState = toState;
        this.automata_type = automata_type;
    }

    public void setTransitionChangeListener(TransitionChangeListener transitionChangeListener) {
        this.transitionChangeListener = transitionChangeListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_chess_game_transition, null, false);

        edittext_start_state = view.findViewById(R.id.edittext_state_name);
        edittext_finish_state = view.findViewById(R.id.edittext_finish_state);

        togglebutton_up = view.findViewById(R.id.togglebutton_up);
        togglebutton_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDirectionButton(togglebutton_up);
            }
        });

        togglebutton_down = view.findViewById(R.id.togglebutton_down);
        togglebutton_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDirectionButton(togglebutton_down);
            }
        });

        togglebutton_left = view.findViewById(R.id.togglebutton_left);
        togglebutton_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDirectionButton(togglebutton_left);
            }
        });

        togglebutton_right = view.findViewById(R.id.togglebutton_right);
        togglebutton_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDirectionButton(togglebutton_right);
            }
        });

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
        switch (dialog_type) {

            case NEW:
                alertBuilder.setTitle(R.string.new_transition);
                break;
            case EDIT:
                alertBuilder.setTitle(R.string.edit_transition);
                break;
        }
        alertBuilder.setView(view);
        alertBuilder.setNeutralButton(android.R.string.cancel, null);
        alertBuilder.setPositiveButton(android.R.string.yes, null);

        AlertDialog dialog = alertBuilder.create();
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();

        AlertDialog dialog = (AlertDialog) getDialog();
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle output_bundle = new Bundle();
                if (togglebutton_up.isChecked())
                    output_bundle.putString(DIRECTION_KEY, Symbol.MOVEMENT_UP);
                else if (togglebutton_down.isChecked())
                    output_bundle.putString(DIRECTION_KEY, Symbol.MOVEMENT_DOWN);
                else if (togglebutton_left.isChecked())
                    output_bundle.putString(DIRECTION_KEY, Symbol.MOVEMENT_LEFT);
                else if (togglebutton_right.isChecked())
                    output_bundle.putString(DIRECTION_KEY, Symbol.MOVEMENT_RIGHT);

                if (transitionChangeListener != null)
                    transitionChangeListener.OnChange(output_bundle);

                if (output_bundle.size() > 0)
                    dismiss();
            }
        });
    }

    private void setDirectionButton(ToggleButton selected_button) {
        togglebutton_up.setChecked(selected_button == togglebutton_up);
        togglebutton_down.setChecked(selected_button == togglebutton_down);
        togglebutton_left.setChecked(selected_button == togglebutton_left);
        togglebutton_right.setChecked(selected_button == togglebutton_right);
    }
}
