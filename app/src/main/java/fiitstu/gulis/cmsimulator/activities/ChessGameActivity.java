package fiitstu.gulis.cmsimulator.activities;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Pair;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.database.FileHandler;
import fiitstu.gulis.cmsimulator.diagram.DiagramView;
import fiitstu.gulis.cmsimulator.dialogs.ChessGameStateDialog;
import fiitstu.gulis.cmsimulator.dialogs.ChessGameTransitionDialog;
import fiitstu.gulis.cmsimulator.dialogs.ConfigurationDialog;
import fiitstu.gulis.cmsimulator.elements.FsaTransition;
import fiitstu.gulis.cmsimulator.elements.State;
import fiitstu.gulis.cmsimulator.elements.Symbol;
import fiitstu.gulis.cmsimulator.elements.Transition;
import fiitstu.gulis.cmsimulator.models.ChessGame;
import fiitstu.gulis.cmsimulator.models.tasks.automata_type;
import fiitstu.gulis.cmsimulator.views.ChessView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * The activity for editing the state diagram, the alphabet, the states and tre transitions.
 * <p>
 * Expected Intent arguments (extras) (KEY (TYPE) - MEANING):
 * MACHINE_TYPE (int) - type of the machine (one of MainActivity's static fields)
 * TASK_CONFIGURATION (int) - 0 (not a task), MainActivity.EditTask, or MainActivity.SolveTask
 * FILE_NAME (String) - the name of the currently open file (or the default filename)
 * TASK (Serializable - Task) - the task being solved (or null); TASK = null <=> TASK_CONFIGURATION = 0
 * EMPTY_INPUT_SYMBOL (long) - the ID of the empty symbol
 * START_STACK_SYMBOL (long) - the ID of the stack start symbol (only needed for push-down automaton)
 * <p>
 * Created by Martin on 7. 3. 2017.
 */
public class ChessGameActivity extends FragmentActivity implements DiagramView.ItemClickCallback {
    private static final String TAG = "ChessGameActivity";
    private static final String DEFAULT_FILE_NAME = "cmsGame.cmsc";

    private static final int DEFAULT_CHESS_FIELD_HEIGHT = 750;
    private static final int SHOW_UP_DURATION = 400;

    private boolean isFieldVisible = false;
    private automata_type automata_type;
    private long emptyInputSymbolId;
    private long startStackSymbolId;
    private int elementAction;
    private boolean initialStateExits = false;
    private ChessGame chessGame;
    private List<State> stateList = new ArrayList<>();
    private List<Transition> transitions = new ArrayList<>();

    //dialog value
    private static final String CONFIGURATION_DIALOG = "CONFIGURATION_DIALOG";
    private static final String SUPPORT_CONFIGURATION_DIALOG = "SUPPORT_CONFIGURATION_DIALOG";
    private static final String TASK_DIALOG = "TASK_DIALOG";
    private static final String HELP_DIALOG = "HELP_DIALOG";
    public static final String TASK_CONFIGURATION = "TASK_CONFIGURATION";

    //element actions
    public static final int NEW = 0;
    public static final int UPDATE = 1;

    //element types
    public static final int INPUT_SYMBOL = 0;
    public static final int STACK_SYMBOL = 1;
    public static final int STATE = 2;
    public static final int FSA_TRANSITION = 3;
    public static final int PDA_TRANSITION = 4;
    public static final int TM_TRANSITION = 5;

    private DataSource dataSource = DataSource.getInstance();
    private ImageButton lastPressedImageButton;


    // UI ELEMENTS
    private ImageButton imagebutton_drop_up;
    private ChessView chessview_field;
    private ImageButton
            imageButton_configuration_diagram_move,
            imageButton_configuration_diagram_state,
            imageButton_configuration_diagram_transition,
            imageButton_configuration_diagram_edit,
            imageButton_configuration_diagram_remove;
    private DiagramView diagramView_configuration;

    //onCreate method
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess_game_activity);

        setActionBar();
        setUIElements();
        setEvents();
        loadGame();
        setChessField();

        // SET INPUT SYMBOLS
        List<Symbol> listOfSymbols = ChessGame.getMovementSymbolList();
        dataSource.open();
        for (Symbol s : listOfSymbols) {
            dataSource.addInputSymbol(s.getValue(), 0);
        }
        Symbol emptySymbol = dataSource.addInputSymbol("ε", Symbol.EMPTY);
        emptyInputSymbolId = emptySymbol.getId();
    }

    private void setUIElements() {
        imagebutton_drop_up = findViewById(R.id.imagebutton_drop_up);
        chessview_field = findViewById(R.id.chessview_field);
        imageButton_configuration_diagram_move = findViewById(R.id.imageButton_configuration_diagram_move);
        imageButton_configuration_diagram_state = findViewById(R.id.imageButton_configuration_diagram_state);
        imageButton_configuration_diagram_transition = findViewById(R.id.imageButton_configuration_diagram_transition);
        imageButton_configuration_diagram_edit = findViewById(R.id.imageButton_configuration_diagram_edit);
        imageButton_configuration_diagram_remove = findViewById(R.id.imageButton_configuration_diagram_remove);
        diagramView_configuration = findViewById(R.id.diagramView_configuration);
    }

    private void setEvents() {
        diagramView_configuration.setItemClickCallback(this);

        imagebutton_drop_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValueAnimator valueAnimator = new ValueAnimator();
                valueAnimator.setDuration(SHOW_UP_DURATION);
                if (isFieldVisible)
                    valueAnimator.setIntValues(DEFAULT_CHESS_FIELD_HEIGHT, 0);
                else
                    valueAnimator.setIntValues(0, DEFAULT_CHESS_FIELD_HEIGHT);
                valueAnimator.setEvaluator(new IntEvaluator());
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        chessview_field.getLayoutParams().height = (int) animation.getAnimatedValue();
                        chessview_field.requestLayout();
                        if (isFieldVisible) {
                            if ((int) animation.getAnimatedValue() == 0) {
                                isFieldVisible = false;
                                imagebutton_drop_up.setImageDrawable(getResources().getDrawable(R.drawable.baseline_arrow_drop_down_24, null));
                            }
                        } else {
                            if ((int) animation.getAnimatedValue() == DEFAULT_CHESS_FIELD_HEIGHT) {
                                isFieldVisible = true;
                                imagebutton_drop_up.setImageDrawable(getResources().getDrawable(R.drawable.baseline_arrow_drop_up_24, null));
                            }
                        }
                    }
                });

                valueAnimator.start();
            }
        });

        imageButton_configuration_diagram_move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastPressedImageButton != null) {
                    lastPressedImageButton.getBackground().clearColorFilter();
                }
                lastPressedImageButton = imageButton_configuration_diagram_move;
                lastPressedImageButton.getBackground().setColorFilter(ContextCompat.getColor(ChessGameActivity.this, R.color.toggle_color), PorterDuff.Mode.MULTIPLY);
                diagramView_configuration.setAction(DiagramView.MOVE);
            }
        });

        imageButton_configuration_diagram_state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastPressedImageButton != null) {
                    lastPressedImageButton.getBackground().clearColorFilter();
                }
                lastPressedImageButton = imageButton_configuration_diagram_state;
                lastPressedImageButton.getBackground().setColorFilter(ContextCompat.getColor(ChessGameActivity.this, R.color.toggle_color), PorterDuff.Mode.MULTIPLY);
                diagramView_configuration.setAction(DiagramView.ADD_STATE);
            }
        });

        imageButton_configuration_diagram_transition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastPressedImageButton != null) {
                    lastPressedImageButton.getBackground().clearColorFilter();
                }
                lastPressedImageButton = imageButton_configuration_diagram_transition;
                lastPressedImageButton.getBackground().setColorFilter(ContextCompat.getColor(ChessGameActivity.this, R.color.toggle_color), PorterDuff.Mode.MULTIPLY);
                diagramView_configuration.setAction(DiagramView.ADD_TRANSITION);
            }
        });

        imageButton_configuration_diagram_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastPressedImageButton != null) {
                    lastPressedImageButton.getBackground().clearColorFilter();
                }
                lastPressedImageButton = imageButton_configuration_diagram_edit;
                lastPressedImageButton.getBackground().setColorFilter(ContextCompat.getColor(ChessGameActivity.this, R.color.toggle_color), PorterDuff.Mode.MULTIPLY);
                diagramView_configuration.setAction(DiagramView.EDIT);
            }
        });

        imageButton_configuration_diagram_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastPressedImageButton != null) {
                    lastPressedImageButton.getBackground().clearColorFilter();
                }
                lastPressedImageButton = imageButton_configuration_diagram_remove;
                lastPressedImageButton.getBackground().setColorFilter(ContextCompat.getColor(ChessGameActivity.this, R.color.toggle_color), PorterDuff.Mode.MULTIPLY);
                diagramView_configuration.setAction(DiagramView.REMOVE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        dataSource.open();

        diagramView_configuration.buildDiagram(false, emptyInputSymbolId, stateList, transitions);
        if (Build.VERSION.SDK_INT < 15) {
            imageButton_configuration_diagram_move.performClick();
        } else {
            imageButton_configuration_diagram_move.callOnClick();
        }
    }

    private void setActionBar() {
        ActionBar actionBar = this.getActionBar();
        actionBar.setTitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.menu_game, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_save_task:
                // TODO: SAVE GAME
                return true;
            case R.id.menu_submit_task:
                // TODO: SUBMIT_TASK
                return true;
            case R.id.menu_configuration_simulate:
                // TODO: SIMULATION
                if (!isFieldVisible)
                    imagebutton_drop_up.callOnClick();

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        runSimulationStep();
                        Pair<Integer, Integer> cField = activeField;
                        Pair<Integer, Integer> fField = finishField;
                        if (!simulationStuck)
                            handler.postDelayed(this, 500);
                        else {
                            if (cField.equals(fField))
                                if (currentState.isFinalState())
                                    Toast.makeText(ChessGameActivity.this, "SIMULACIA PREBEHLA USPESNE", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(ChessGameActivity.this, "SIMULACIA SKONCILA ALE AUTOMAT NIE JE V KONCOVOM STAVE", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(ChessGameActivity.this, "AUTOMAT SA ZASEKOL", Toast.LENGTH_SHORT).show();
                            simulationStuck = false;
                            startField = null;
                            finishField = null;
                            activeField = null;
                            currentState = null;
                        }
                    }
                }, 0);

                return true;
            case R.id.menu_settings:
                Intent settingsIntent = new Intent(this, OptionsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.menu_configuration_help:
                // TODO: HELP
                return true;
        }
    }

    private void loadGame() {
        try {
            final String filename = getApplicationInfo().dataDir + "/" + DEFAULT_FILE_NAME;
            FileHandler fileHandler = new FileHandler(FileHandler.Format.CMSC);
            fileHandler.loadFile(filename);
            chessGame = fileHandler.getChessGame();
            automata_type = chessGame.getAutomata_type();
            fileHandler.getData(dataSource);
        } catch (Exception e) {
            Log.e(TAG, "File was not loaded", e);
            Toast.makeText(this, getResources().getString(R.string.file_not_loaded), Toast.LENGTH_SHORT).show();
            dataSource.globalDrop();
            dataSource.close();
            finish();
        }
    }

    private void showConfigurationDialog(int elementType, int elementAction) {
        this.elementAction = elementAction;
        FragmentManager fm = getSupportFragmentManager();
        ConfigurationDialog configurationDialog = ConfigurationDialog.newInstance(elementType, elementAction);
        configurationDialog.show(fm, CONFIGURATION_DIALOG);
    }

    @Override
    public void onAddState() {
        Log.v(TAG, "onAddState from diagram noted");
        ChessGameStateDialog chessGameStateDialog = new ChessGameStateDialog();
        chessGameStateDialog.setStateChangeListener(new ChessGameStateDialog.StateChangeListener() {
            @Override
            public void onChange(Bundle output_bundle) {
                final String stateName = output_bundle.getString(ChessGameStateDialog.STATE_NAME_KEY);
                final boolean isInitial = output_bundle.getBoolean(ChessGameStateDialog.INITIAL_STATE_KEY);
                if (initialStateExits && isInitial) {
                    Toast.makeText(ChessGameActivity.this, R.string.error_initial_state, Toast.LENGTH_SHORT).show();
                    return;
                }
                initialStateExits = isInitial;
                final boolean isFinal = output_bundle.getBoolean(ChessGameStateDialog.FINAL_STATE_KEY);
                dataSource.open();
                final int x = diagramView_configuration.getNewStatePositionX();
                final int y = diagramView_configuration.getNewStatePositionY();
                State state = dataSource.addState(stateName, x, y, isInitial, isFinal);
                diagramView_configuration.addState(state);
                stateList.add(state);
                dataSource.close();
            }
        });

        FragmentManager fm = this.getSupportFragmentManager();
        chessGameStateDialog.show(fm, TAG);
    }

    @Override
    public void onBackPressed() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.exit)
                .setMessage(R.string.exit_game)
                .setNeutralButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ChessGameActivity.super.onBackPressed();
                    }
                }).create();

        alertDialog.show();
    }

    @Override
    public void onAddTransition(final State fromState, final State toState) {
        final List<Transition> transitionList = new ArrayList<>();
        final List<Transition> fromTransitionList = new ArrayList<>();
        for (Transition t : transitions) {
            if (t.getFromState().equals(fromState) && t.getToState().equals(toState))
                transitionList.add(t);
            if (t.getFromState().equals(fromState))
                fromTransitionList.add(t);
        }
        ChessGameTransitionDialog.AUTOMATA_TYPE type = automata_type == fiitstu.gulis.cmsimulator.models.tasks.automata_type.FINITE_AUTOMATA ? ChessGameTransitionDialog.AUTOMATA_TYPE.FINITE : ChessGameTransitionDialog.AUTOMATA_TYPE.PUSHDOWN;
        ChessGameTransitionDialog chessGameTransitionDialog = new ChessGameTransitionDialog(fromTransitionList, transitionList, fromState, toState, ChessGameTransitionDialog.DIALOG_TYPE.NEW, type);
        chessGameTransitionDialog.setTransitionChangeListener(new ChessGameTransitionDialog.TransitionChangeListener() {
            @Override
            public void OnChange(Bundle output_bundle) {
                final String direction = output_bundle.getString(ChessGameTransitionDialog.DIRECTION_KEY);
                dataSource.open();
                List<Symbol> symbols = dataSource.getInputAlphabetFullExtract();
                for (Symbol s : symbols) {
                    if (s.getValue().equals(direction)) {
                        FsaTransition transition = (FsaTransition) dataSource.addFsaTransition(fromState, s, toState, emptyInputSymbolId);
                        diagramView_configuration.addTransition(transition);
                        transitions.add(transition);
                        break;
                    }
                }

                dataSource.close();
            }
        });
        FragmentManager fm = getSupportFragmentManager();
        chessGameTransitionDialog.show(fm, TAG);
    }

    @Override
    public void onEditState(final State stateEdit) {
        Log.v(TAG, "onAddState from diagram noted");
        ChessGameStateDialog chessGameStateDialog = new ChessGameStateDialog(stateEdit);
        chessGameStateDialog.setStateChangeListener(new ChessGameStateDialog.StateChangeListener() {
            @Override
            public void onChange(Bundle output_bundle) {
                final String stateName = output_bundle.getString(ChessGameStateDialog.STATE_NAME_KEY);
                final boolean isInitial = output_bundle.getBoolean(ChessGameStateDialog.INITIAL_STATE_KEY);
                if (stateEdit.isInitialState())
                    initialStateExits = isInitial;
                final boolean isFinal = output_bundle.getBoolean(ChessGameStateDialog.FINAL_STATE_KEY);
                dataSource.open();
                dataSource.updateState(stateEdit, stateName, stateEdit.getPositionX(), stateEdit.getPositionY(), isInitial, isFinal);
                diagramView_configuration.invalidate();
                stateList.get(stateList.indexOf(stateEdit)).setValue(stateName);
                stateList.get(stateList.indexOf(stateEdit)).setInitialState(isInitial);
                stateList.get(stateList.indexOf(stateEdit)).setFinalState(isFinal);
                dataSource.close();
            }
        });

        FragmentManager fm = this.getSupportFragmentManager();
        chessGameStateDialog.show(fm, TAG);
    }

    @Override
    public void onEditTransition(final List<Transition> transitionList) {
        Log.v(TAG, "onEditTransition from diagram noted");
        ArrayAdapter<Transition> adapter = new ArrayAdapter<Transition>(this,
                android.R.layout.select_dialog_item, android.R.id.text1,
                transitionList) {

            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                // creates view
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setText(transitionList.get(position).getDesc());
                return view;
            }
        };
        new AlertDialog.Builder(this)
                .setTitle(R.string.choose_transition)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, int i) {
                        final Transition transition = transitionList.get(i);
                        final State fromState = transition.getFromState();
                        final State toState = transition.getToState();
                        ChessGameTransitionDialog chessGameTransitionDialog = new ChessGameTransitionDialog(transition, transitionList, fromState, toState, ChessGameTransitionDialog.DIALOG_TYPE.EDIT, ChessGameTransitionDialog.AUTOMATA_TYPE.FINITE);
                        chessGameTransitionDialog.setTransitionChangeListener(new ChessGameTransitionDialog.TransitionChangeListener() {
                            @Override
                            public void OnChange(Bundle output_bundle) {
                                final String direction = output_bundle.getString(ChessGameTransitionDialog.DIRECTION_KEY);
                                dataSource.open();
                                List<Symbol> symbols = dataSource.getInputAlphabetFullExtract();
                                for (Symbol s : symbols) {
                                    if (s.getValue().equals(direction)) {
                                        dataSource.updateFsaTransition((FsaTransition) transition, fromState, s, toState, emptyInputSymbolId);
                                        transitions.get(transitions.indexOf(transition)).setReadSymbol(s);
                                        diagramView_configuration.invalidate();
                                        break;
                                    }
                                }

                                dataSource.close();
                            }
                        });
                        FragmentManager fm = getSupportFragmentManager();
                        chessGameTransitionDialog.show(fm, TAG);
                    }
                })
                .setCancelable(true)
                .show();
    }

    @Override
    public void onRemoveState(final State stateRemove) {
        String message = getString(R.string.delete_state);
        message = String.format(message, stateRemove.getValue());

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage(message)
                .setNeutralButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dataSource.open();
                        if (stateRemove.isInitialState())
                            initialStateExits = false;
                        boolean hasTransitions = false;
                        for (Transition t : transitions) {
                            if (t.getToState().equals(stateRemove) || t.getFromState().equals(stateRemove)) {
                                hasTransitions = true;
                                break;
                            }
                        }
                        if (!hasTransitions) {
                            dataSource.deleteState(stateRemove);
                            stateList.remove(stateRemove);
                            diagramView_configuration.removeState(stateRemove);
                        } else
                            Toast.makeText(ChessGameActivity.this, R.string.state_has_transitions, Toast.LENGTH_SHORT).show();
                        dataSource.close();
                    }
                })
                .create();

        alertDialog.show();
    }

    @Override
    public void onRemoveTransition(final List<Transition> transitionList) {
        Log.v(TAG, "onRemoveTransition from diagram noted");
        ArrayAdapter<Transition> adapter = new ArrayAdapter<Transition>(this,
                android.R.layout.select_dialog_item, android.R.id.text1,
                transitionList) {

            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                // creates view
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view
                        .findViewById(android.R.id.text1);
                textView.setText(transitionList.get(position).getDesc());
                return view;
            }
        };
        new AlertDialog.Builder(this)
                .setTitle(R.string.choose_transition)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final Transition transition = transitionList.get(i);
                        try {
                            dataSource.open();
                            dataSource.deleteTransition(transition);
                            transitions.remove(transition);
                            diagramView_configuration.removeTransition(transition);
                            dataSource.close();
                        } catch (Exception e) {
                            Toast.makeText(ChessGameActivity.this, R.string.transition_remove_error, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "unknown error occurred while removing transition", e);
                        }

                    }
                })
                .setCancelable(true)
                .show();
    }

    private void setChessField() {
        final Pair<Integer, Integer> startField = chessGame.getStartField();
        final Pair<Integer, Integer> finishField = chessGame.getFinishField();
        final List<Pair<Integer, Integer>> pathField = chessGame.getPathFields();
        final Pair<Integer, Integer> fieldSize = chessGame.getFieldSize();

        try {
            chessview_field.setChessFieldWidth(fieldSize.first);
            chessview_field.setChessFieldHeight(fieldSize.second);
            chessview_field.setStartField(startField);
            chessview_field.setFinishField(finishField);
            for (Pair<Integer, Integer> field : pathField) {
                chessview_field.addFieldToPath(field);
            }
        } catch (ChessView.OutOfChessFieldBoundariesException | ChessView.OutOfChessFieldException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public List<State> getStateList() {
        return stateList;
    }

    private State getInitialState() {
        for (State s : stateList) {
            if (s.isInitialState())
                return s;
        }

        return null;
    }

    private List<Transition> getStatesTransitions(State state) {
        List<Transition> transitionList = new ArrayList<>();
        for (Transition t : transitions) {
            if (t.getFromState().equals(state))
                transitionList.add(t);
        }

        return transitionList;
    }

    private void moveUp() {
        final Pair<Integer, Integer> activeField = chessview_field.getActiveField();
        Pair<Integer, Integer> targetField = new Pair<>(activeField.first, activeField.second - 1);

        try {
            chessview_field.setActiveField(targetField);
        } catch (ChessView.OutOfChessFieldException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void moveDown() {
        final Pair<Integer, Integer> activeField = chessview_field.getActiveField();
        Pair<Integer, Integer> targetField = new Pair<>(activeField.first, activeField.second + 1);

        try {
            chessview_field.setActiveField(targetField);
        } catch (ChessView.OutOfChessFieldException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void moveLeft() {
        final Pair<Integer, Integer> activeField = chessview_field.getActiveField();
        Pair<Integer, Integer> targetField = new Pair<>(activeField.first - 1, activeField.second);

        try {
            chessview_field.setActiveField(targetField);
        } catch (ChessView.OutOfChessFieldException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void moveRight() {
        final Pair<Integer, Integer> activeField = chessview_field.getActiveField();
        Pair<Integer, Integer> targetField = new Pair<>(activeField.first + 1, activeField.second);

        try {
            chessview_field.setActiveField(targetField);
        } catch (ChessView.OutOfChessFieldException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    Pair<Integer, Integer> startField = null;
    Pair<Integer, Integer> finishField = null;
    State currentState = null;
    Pair<Integer, Integer> activeField = null;
    boolean simulationStuck = false;


    private void runSimulationStep() {
        startField = chessview_field.getStartField();
        finishField = chessview_field.getFinishField();
        final State initialState = getInitialState();
        if (initialState == null) {
            Toast.makeText(this, R.string.error_initial_state, Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentState == null)
            currentState = initialState;
        List<Transition> currentStateTransitions = getStatesTransitions(currentState);

        int stepCounter = 0;

        if (activeField == null)
            try {
                chessview_field.reset();
                simulationStuck = false;
                chessview_field.setActiveField(startField);

            } catch (ChessView.OutOfChessFieldException e) {
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        activeField = chessview_field.getActiveField();

        //while (!activeField.equals(finishField)) {
        if (stepCounter++ > 100) {
            Toast.makeText(this, "AUTOMAT DLHO NESKONCIL", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean
                up = chessview_field.canGoUp(),
                down = chessview_field.canGoDown(),
                left = chessview_field.canGoLeft(),
                right = chessview_field.canGoRight();

        if (!up && !down && !left && !right) {
            simulationStuck = true;
        }

        if (currentStateTransitions.size() == 0)
            simulationStuck = true;

        int transitionsCount = currentStateTransitions.size();
        for (Transition t : currentStateTransitions) {
            Symbol s = t.getReadSymbol();
            String movement = s.getValue();

            switch (movement) {
                case Symbol.MOVEMENT_UP:
                    if (up) {
                        moveUp();
                        currentState = t.getToState();
                        currentStateTransitions = getStatesTransitions(currentState);
                        activeField = chessview_field.getActiveField();
                        return;
                    }
                    break;
                case Symbol.MOVEMENT_DOWN:
                    if (down) {
                        moveDown();
                        currentState = t.getToState();
                        currentStateTransitions = getStatesTransitions(currentState);
                        activeField = chessview_field.getActiveField();
                        return;
                    }
                    break;
                case Symbol.MOVEMENT_LEFT:
                    if (left) {
                        moveLeft();
                        currentState = t.getToState();
                        currentStateTransitions = getStatesTransitions(currentState);
                        activeField = chessview_field.getActiveField();
                        return;
                    }
                    break;
                case Symbol.MOVEMENT_RIGHT:
                    if (right) {
                        moveRight();
                        currentState = t.getToState();
                        currentStateTransitions = getStatesTransitions(currentState);
                        activeField = chessview_field.getActiveField();
                        return;
                    }
                    break;
            }
            if (--transitionsCount <= 0) {
                simulationStuck = true;
                return;
            }
        }
    }
}
