package fiitstu.gulis.cmsimulator.activities;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.transition.Fade;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.animation.AccelerateInterpolator;
import android.widget.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import fiitstu.gulis.cmsimulator.adapters.bulktest.TestScenarioListAdapter;
import fiitstu.gulis.cmsimulator.adapters.tasks.AutomataTaskAdapter;
import fiitstu.gulis.cmsimulator.animation.AnimatedColor;
import fiitstu.gulis.cmsimulator.app.CMSimulator;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.database.FileHandler;
import fiitstu.gulis.cmsimulator.dialogs.*;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.adapters.configuration.ConfigurationListAdapter;
import fiitstu.gulis.cmsimulator.diagram.DiagramView;
import fiitstu.gulis.cmsimulator.elements.*;
import fiitstu.gulis.cmsimulator.machines.FiniteStateAutomatonStep;
import fiitstu.gulis.cmsimulator.machines.MachineStep;
import fiitstu.gulis.cmsimulator.models.ChessGame;
import fiitstu.gulis.cmsimulator.models.tasks.automata_tasks.FiniteAutomataTask;
import fiitstu.gulis.cmsimulator.models.tasks.automata_tasks.LinearBoundedAutomataTask;
import fiitstu.gulis.cmsimulator.models.tasks.automata_tasks.PushdownAutomataTask;
import fiitstu.gulis.cmsimulator.models.tasks.automata_type;
import fiitstu.gulis.cmsimulator.network.ServerController;
import fiitstu.gulis.cmsimulator.network.TaskResultSender;
import fiitstu.gulis.cmsimulator.network.UrlManager;
import fiitstu.gulis.cmsimulator.util.ProgressWorker;
import fiitstu.gulis.cmsimulator.views.ChessView;
import io.blushine.android.ui.showcase.MaterialShowcaseSequence;
import io.blushine.android.ui.showcase.MaterialShowcaseView;
import io.blushine.android.ui.showcase.ShowcaseListener;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

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
    private int machineType;
    private long emptyInputSymbolId;
    private long startStackSymbolId;
    private ChessGame chessGame;

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

        // SET INPUT SYMBOLS
        List<Symbol> listOfSymbols = ChessGame.getMovementSymbolList();
        dataSource.open();
        for (Symbol s : listOfSymbols) {
            dataSource.addInputSymbol(s.getValue(), 0);
        }

        setActionBar();
        setUIElements();
        setEvents();
        loadGame();
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
                valueAnimator.setInterpolator(new AccelerateInterpolator());
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        chessview_field.getLayoutParams().height = (int) animation.getAnimatedValue();
                        chessview_field.requestLayout();
                        if (isFieldVisible) {
                            if ((int) animation.getAnimatedValue() == 0) {
                                isFieldVisible = false;
                                imagebutton_drop_up.setImageDrawable(getResources().getDrawable(R.drawable.baseline_arrow_drop_down_24));
                            }
                        } else {
                            if ((int) animation.getAnimatedValue() == DEFAULT_CHESS_FIELD_HEIGHT) {
                                isFieldVisible = true;
                                imagebutton_drop_up.setImageDrawable(getResources().getDrawable(R.drawable.baseline_arrow_drop_up_24));
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

        diagramView_configuration.buildDiagram(false, 0, new ArrayList<State>(), new ArrayList<Transition>());
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
        }
    }

    private void loadGame() {
        try {
            final String filename = getApplicationInfo().dataDir + "/" + DEFAULT_FILE_NAME;
            FileHandler fileHandler = new FileHandler(FileHandler.Format.CMSC);
            fileHandler.loadFile(filename);
            chessGame = fileHandler.getChessGame();
            automata_type automata_type = chessGame.getAutomata_type();
            switch (automata_type) {

                case FINITE_AUTOMATA:
                    machineType = MainActivity.FINITE_STATE_AUTOMATON;
                    break;
                case PUSHDOWN_AUTOMATA:
                    machineType = MainActivity.PUSHDOWN_AUTOMATON;
                    break;
                default:
                    machineType = -1;
                    break;
            }
            fileHandler.getData(dataSource);
        } catch (Exception e) {
            Log.e(TAG, "File was not loaded", e);
            Toast.makeText(this, getResources().getString(R.string.file_not_loaded), Toast.LENGTH_SHORT).show();
            dataSource.globalDrop();
            dataSource.close();
            finish();
        }
    }

    @Override
    public void onAddState() {
        Toast.makeText(this, "ADD STATE", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAddTransition(State fromState, State toState) {
        Toast.makeText(this, "ADD TRANSITION", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditState(State stateEdit) {
        Toast.makeText(this, "EDIT STATE", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditTransition(List<Transition> transitionList) {
        Toast.makeText(this, "EDIT TRANSITION", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRemoveState(State stateRemove) {
        Toast.makeText(this, "REMOVE STATE", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRemoveTransition(List<Transition> transitionList) {
        Toast.makeText(this, "REMOVE TRANSITION", Toast.LENGTH_SHORT).show();
    }
}
