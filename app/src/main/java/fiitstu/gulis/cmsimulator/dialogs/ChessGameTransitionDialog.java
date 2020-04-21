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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.activities.ChessGameActivity;
import fiitstu.gulis.cmsimulator.adapters.configuration.ChessGameStackSymbolAdapter;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.elements.PdaTransition;
import fiitstu.gulis.cmsimulator.elements.State;
import fiitstu.gulis.cmsimulator.elements.Symbol;
import fiitstu.gulis.cmsimulator.elements.Transition;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("ValidFragment")
public class ChessGameTransitionDialog extends DialogFragment {
    private static final String TAG = "ChessGameTransitionDial";

    public static final String DIRECTION_KEY = "DIRECTION";
    public static final String POP_KEY = "POP_KEY";
    public static final String PUSH_KEY = "PUSH_KEY";

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
            togglebutton_down;

    private ImageButton
            selected_button = null,
            selected_push_button = null,
            selected_pop_button = null;

    private ImageButton
            imagebutton_add_push_symbol,
            imagebutton_add_pop_symbol;

    private LinearLayout linearlayout_pushdown_config;

    private State
            fromState,
            toState;

    private Transition transition;
    private List<Transition> transitionList;
    private List<Transition> fromTransitionList;

    private RecyclerView recyclerview_pop;
    private RecyclerView recyclerview_push;

    private List<Symbol> popSymbols;
    private List<Symbol> pushSymbol;

    public interface TransitionChangeListener {
        void OnChange(Bundle output_bundle, List<Symbol> popSymbols, List<Symbol> pushSymbol);
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
    public ChessGameTransitionDialog(Transition transition, List<Transition> fromTransitionList, List<Transition> transitions, State fromState, State toState, DIALOG_TYPE dialog_type, AUTOMATA_TYPE automata_type) {
        this.dialog_type = dialog_type;
        this.transition = transition;
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

        if (automata_type == AUTOMATA_TYPE.PUSHDOWN) {
            linearlayout_pushdown_config = view.findViewById(R.id.linearlayou_pushdown_config);
            linearlayout_pushdown_config.setVisibility(View.VISIBLE);

            recyclerview_pop = view.findViewById(R.id.recyclerview_pop);
            recyclerview_push = view.findViewById(R.id.recyclerview_push);

            DataSource dataSource = DataSource.getInstance();
            dataSource.open();
            List<Symbol> stackAlphabet = dataSource.getStackAlphabetFullExtract();

            final Symbol[] initStack = {null};
            for (Symbol s : dataSource.getStackAlphabetFullExtract()) {
                if (s.isStackBotom()) {
                    initStack[0] = s;
                    break;
                }
            }

            if (transition != null) {
                popSymbols = ((PdaTransition) transition).getPopSymbolList();
                pushSymbol = ((PdaTransition) transition).getPushSymbolList();
            } else {
                popSymbols = new ArrayList<>();
                popSymbols.add(initStack[0]);

                pushSymbol = new ArrayList<>();
                pushSymbol.add(initStack[0]);
            }


            final ChessGameStackSymbolAdapter chessGameStackSymbolPopAdapter = new ChessGameStackSymbolAdapter(popSymbols, stackAlphabet, getContext());
            final ChessGameStackSymbolAdapter chessGameStackSymbolPushAdapter = new ChessGameStackSymbolAdapter(pushSymbol, stackAlphabet, getContext());

            imagebutton_add_push_symbol = view.findViewById(R.id.imagebutton_add_push_symbol);
            imagebutton_add_push_symbol.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chessGameStackSymbolPushAdapter.addSymbol(initStack[0]);
                }
            });

            imagebutton_add_pop_symbol = view.findViewById(R.id.imagebutton_add_pop_symbol);
            imagebutton_add_pop_symbol.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chessGameStackSymbolPopAdapter.addSymbol(initStack[0]);
                }
            });

            LinearLayoutManager linearLayoutManagerPop = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true);
            LinearLayoutManager linearLayoutManagerPush = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true);

            recyclerview_pop.setAdapter(chessGameStackSymbolPopAdapter);
            recyclerview_push.setAdapter(chessGameStackSymbolPushAdapter);

            recyclerview_push.setLayoutManager(linearLayoutManagerPush);
            recyclerview_pop.setLayoutManager(linearLayoutManagerPop);

            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.item_show_animation);
            recyclerview_pop.setAnimation(animation);
            recyclerview_push.setAnimation(animation);
        }

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

            if (automata_type == AUTOMATA_TYPE.PUSHDOWN) {
                PdaTransition t = (PdaTransition) transition;
                List<Symbol> popSymbol = t.getPopSymbolList();
                List<Symbol> pushSymbol = t.getPushSymbolList();

                String popString = popSymbol.get(0).getValue();
                String pushString = pushSymbol.get(0).getValue();
            }
        }

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
        switch (dialog_type) {

            case NEW:
                alertBuilder.setTitle(R.string.new_transition);
                break;
            case EDIT:
                alertBuilder.setTitle(R.string.edit_transition);
                break;
        }
        if (automata_type == AUTOMATA_TYPE.FINITE) {
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
                    transitionChangeListener.OnChange(output_bundle, popSymbols, pushSymbol);
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
}
