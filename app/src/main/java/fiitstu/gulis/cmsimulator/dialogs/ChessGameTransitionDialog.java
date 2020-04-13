package fiitstu.gulis.cmsimulator.dialogs;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.activities.ChessGameActivity;
import fiitstu.gulis.cmsimulator.elements.State;
import fiitstu.gulis.cmsimulator.elements.Symbol;
import fiitstu.gulis.cmsimulator.elements.Transition;

import java.util.List;

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

    private ImageButton
            togglebutton_up,
            togglebutton_left,
            togglebutton_right,
            togglebutton_down,

    togglebutton_push_up,
            togglebutton_push_left,
            togglebutton_push_right,
            togglebutton_push_down,

    togglebutton_pop_up,
            togglebutton_pop_left,
            togglebutton_pop_right,
            togglebutton_pop_down;

    private ImageButton
            selected_button = null,
            selected_push_button = null,
            selected_pop_button = null;

    private LinearLayout linearlayout_pushdown_config;

    private State
            fromState,
            toState;

    private Transition transition;
    private List<Transition> transitionList;
    private List<Transition> fromTransitionList;

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
    public ChessGameTransitionDialog(List<Transition> fromTransitionList, List<Transition> transitions, State fromState, State toState, DIALOG_TYPE dialog_type, AUTOMATA_TYPE automata_type) {
        this.dialog_type = dialog_type;
        this.fromTransitionList = fromTransitionList;
        this.transitionList = transitions;
        this.fromState = fromState;
        this.toState = toState;
        this.automata_type = automata_type;
    }

    @SuppressLint("ValidFragment")
    public ChessGameTransitionDialog(Transition transition, List<Transition> transitions, State fromState, State toState, DIALOG_TYPE dialog_type, AUTOMATA_TYPE automata_type) {
        this.dialog_type = dialog_type;
        this.transition = transition;
        this.transitionList = transitions;
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

        edittext_start_state = view.findViewById(R.id.edittext_start_state);
        if (fromState != null)
            edittext_start_state.setText(fromState.getValue());
        edittext_finish_state = view.findViewById(R.id.edittext_finish_state);
        if (toState != null)
            edittext_finish_state.setText(toState.getValue());

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

        if (dialog_type == DIALOG_TYPE.EDIT) {
            Symbol symbol = transition.getReadSymbol();
            String symbolValue = symbol.getValue();

            switch (symbolValue) {
                case Symbol.MOVEMENT_UP:
                    setDirectionButton(togglebutton_up);
                    break;
                case Symbol.MOVEMENT_DOWN:
                    setDirectionButton(togglebutton_down);
                    break;
                case Symbol.MOVEMENT_RIGHT:
                    setDirectionButton(togglebutton_right);
                    break;
                case Symbol.MOVEMENT_LEFT:
                    setDirectionButton(togglebutton_left);
                    break;
            }
        }

        if (automata_type == AUTOMATA_TYPE.PUSHDOWN) {
            linearlayout_pushdown_config = view.findViewById(R.id.linearlayou_pushdown_config);
            linearlayout_pushdown_config.setVisibility(View.VISIBLE);

            togglebutton_pop_up = view.findViewById(R.id.togglebutton_pop_up);
            togglebutton_pop_down = view.findViewById(R.id.togglebutton_pop_down);
            togglebutton_pop_left = view.findViewById(R.id.togglebutton_pop_left);
            togglebutton_pop_right = view.findViewById(R.id.togglebutton_pop_right);

            togglebutton_push_up = view.findViewById(R.id.togglebutton_push_up);
            togglebutton_push_down = view.findViewById(R.id.togglebutton_push_down);
            togglebutton_push_left = view.findViewById(R.id.togglebutton_push_left);
            togglebutton_push_right = view.findViewById(R.id.togglebutton_push_right);

            togglebutton_pop_up.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setDirectionPopButton((ImageButton) v);
                }
            });

            togglebutton_pop_down.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setDirectionPopButton((ImageButton) v);
                }
            });

            togglebutton_pop_left.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setDirectionPopButton((ImageButton) v);
                }
            });

            togglebutton_pop_right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setDirectionPopButton((ImageButton) v);
                }
            });


            togglebutton_push_up.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setDirectionPushButton((ImageButton) v);
                }
            });

            togglebutton_push_down.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setDirectionPushButton((ImageButton) v);
                }
            });

            togglebutton_push_left.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setDirectionPushButton((ImageButton) v);
                }
            });

            togglebutton_push_right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setDirectionPushButton((ImageButton) v);
                }
            });
        }

        for (Transition t : transitionList) {
            Symbol s = t.getReadSymbol();
            String value = s.getValue();


            if (t.equals(transition))
                continue;

            switch (value) {
                case Symbol.MOVEMENT_UP:
                    togglebutton_up.setEnabled(false);
                    break;
                case Symbol.MOVEMENT_DOWN:
                    togglebutton_down.setEnabled(false);
                    break;
                case Symbol.MOVEMENT_RIGHT:
                    togglebutton_right.setEnabled(false);
                    break;
                case Symbol.MOVEMENT_LEFT:
                    togglebutton_left.setEnabled(false);
                    break;
            }
        }

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
        switch (dialog_type) {

            case NEW:
                alertBuilder.setTitle(R.string.new_transition);
                for (Transition t : fromTransitionList) {
                    Symbol s = t.getReadSymbol();
                    String value = s.getValue();


                    if (t.equals(transition))
                        continue;

                    switch (value) {
                        case Symbol.MOVEMENT_UP:
                            togglebutton_up.setEnabled(false);
                            break;
                        case Symbol.MOVEMENT_DOWN:
                            togglebutton_down.setEnabled(false);
                            break;
                        case Symbol.MOVEMENT_RIGHT:
                            togglebutton_right.setEnabled(false);
                            break;
                        case Symbol.MOVEMENT_LEFT:
                            togglebutton_left.setEnabled(false);
                            break;
                    }
                }
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
        final Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle output_bundle = new Bundle();
                if (selected_button != null) {
                    if (selected_button.equals(togglebutton_up))
                        output_bundle.putString(DIRECTION_KEY, Symbol.MOVEMENT_UP);
                    else if (selected_button.equals(togglebutton_down))
                        output_bundle.putString(DIRECTION_KEY, Symbol.MOVEMENT_DOWN);
                    else if (selected_button.equals(togglebutton_left))
                        output_bundle.putString(DIRECTION_KEY, Symbol.MOVEMENT_LEFT);
                    else if (selected_button.equals(togglebutton_right))
                        output_bundle.putString(DIRECTION_KEY, Symbol.MOVEMENT_RIGHT);
                }


                if (selected_button != null && transitionChangeListener != null) {
                    transitionChangeListener.OnChange(output_bundle);
                    dismiss();
                } else {
                    ValueAnimator valueAnimator = new ValueAnimator();
                    valueAnimator.setIntValues(Color.RED, getContext().getColor(R.color.bootstrap_gray));
                    valueAnimator.setEvaluator(new ArgbEvaluator());
                    valueAnimator.setDuration(2000);
                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            if (togglebutton_up.isEnabled())
                                togglebutton_up.getBackground().setColorFilter((int) animation.getAnimatedValue(), PorterDuff.Mode.MULTIPLY);
                            if (togglebutton_down.isEnabled())
                                togglebutton_down.getBackground().setColorFilter((int) animation.getAnimatedValue(), PorterDuff.Mode.MULTIPLY);
                            if (togglebutton_left.isEnabled())
                                togglebutton_left.getBackground().setColorFilter((int) animation.getAnimatedValue(), PorterDuff.Mode.MULTIPLY);
                            if (togglebutton_right.isEnabled())
                                togglebutton_right.getBackground().setColorFilter((int) animation.getAnimatedValue(), PorterDuff.Mode.MULTIPLY);
                        }
                    });

                    valueAnimator.start();
                }

            }
        });
    }

    private void setDirectionButton(ImageButton selected_button) {
        if (this.selected_button != null)
            this.selected_button.getBackground().clearColorFilter();
        this.selected_button = selected_button;
        selected_button.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.toggle_color), PorterDuff.Mode.MULTIPLY);
    }

    private void setDirectionPushButton(ImageButton selected_button) {
        if (this.selected_push_button != null) {
            if (this.selected_push_button.equals(selected_button)) {
                this.selected_push_button.getBackground().clearColorFilter();
                this.selected_push_button = null;
                return;
            } else {
                this.selected_push_button.getBackground().clearColorFilter();
                this.selected_push_button = selected_button;
            }
        }
        selected_button.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.toggle_color), PorterDuff.Mode.MULTIPLY);
        selected_push_button = selected_button;
    }

    private void setDirectionPopButton(ImageButton selected_button) {
        if (this.selected_pop_button != null) {
            if (this.selected_pop_button.equals(selected_button)) {
                this.selected_pop_button.getBackground().clearColorFilter();
                this.selected_pop_button = null;
                return;
            } else {
                this.selected_pop_button.getBackground().clearColorFilter();
                this.selected_pop_button = selected_button;
            }
        }
        selected_button.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.toggle_color), PorterDuff.Mode.MULTIPLY);
        selected_pop_button = selected_button;
    }
}
