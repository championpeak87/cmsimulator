package fiitstu.gulis.cmsimulator.activities;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.io.IOException;
import java.net.URL;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import fiitstu.gulis.cmsimulator.adapters.simulation.*;
import fiitstu.gulis.cmsimulator.database.FileHandler;
import fiitstu.gulis.cmsimulator.dialogs.FileSelector;
import fiitstu.gulis.cmsimulator.dialogs.GuideFragment;
import fiitstu.gulis.cmsimulator.dialogs.TaskDialog;
import fiitstu.gulis.cmsimulator.elements.*;
import fiitstu.gulis.cmsimulator.machines.*;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.diagram.DiagramView;
import fiitstu.gulis.cmsimulator.diagram.SimulationFlowView;
import fiitstu.gulis.cmsimulator.diagram.VerticalSeekBar;
import fiitstu.gulis.cmsimulator.dialogs.FormalSpecDialog;
import fiitstu.gulis.cmsimulator.dialogs.SaveMachineDialog;
import fiitstu.gulis.cmsimulator.network.ServerController;
import fiitstu.gulis.cmsimulator.network.TaskResultSender;
import fiitstu.gulis.cmsimulator.network.UrlManager;
import fiitstu.gulis.cmsimulator.views.AdaptiveRecyclerView;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The activity for simulating machines.
 * <p>
 * Expected Intent arguments (extras) (KEY (TYPE) - MEANING):
 * MACHINE_TYPE (int) - type of the machine (one of MainActivity's static fields)
 * CONFIGURATION_TYPE (int) - what kind of machine is being started (one of MainActivity's static fields, e.g. NEW_MACHINE)
 * FILE_NAME (String) - the name of the currently open file (or the default filename)
 * TASK (Serializable - Task) - the task being solved (or null)
 * <p>
 * Created by Martin on 7. 3. 2017.
 */
public class SimulationActivity extends FragmentActivity
        implements View.OnClickListener, DefaultTapeListAdapter.ItemClickCallback,
        PopupMenu.OnMenuItemClickListener, SaveMachineDialog.SaveDialogListener,
        TaskDialog.TaskDialogListener {

    private boolean timerRunOut = false;
    private static final int BACKGROUND_CHANGE_LENGTH = 1000;

    //log tag
    private static final String TAG = SimulationActivity.class.getName();

    //bundle values
    public static final String EMPTY_INPUT_SYMBOL = "EMPTY_INPUT_SYMBOL";
    public static final String START_STACK_SYMBOL = "START_STACK_SYMBOL";
    public static final String TASK_CONFIGURATION = "TASK_CONFIGURATION";

    //dialog value
    public static final String SAVE_DIALOG = "SAVE_DIALOG";
    public static final String FORMAL_SPEC_DIALOG = "FORMAL_SPEC_DIALOG";
    private static final String HELP_DIALOG = "HELP_DIALOG";

    //tape operations
    private static final int SET_INITIAL = 0;
    private static final int PLACE_BREAKPOINT = 1;
    private static final int REMOVE = 2;

    private DataSource dataSource;
    private DiagramView diagramView;
    private SimulationFlowView simulationFlowView;
    private ImageButton menuB;

    //variables
    private int machineType;
    private String filename;
    private Long emptyInputSymbolId;
    private Long startStackSymbolId;
    private int simulationDepth;
    private boolean simulating;
    private boolean leaving; //a little kludge to prevent spinner from throwing exception when switching activities
    private int tapeDimension;
    private State initialState;
    private State finalState;
    public static Task task;
    private int configurationType;
    private Timer timer;

    //tapes
    private RecyclerView defaultTapeRecyclerView;
    private AdaptiveRecyclerView machineTapeRecyclerView;
    private AdaptiveRecyclerView stackRecyclerView;

    //adapters in views, keeps list of machines too
    private MachineListAdapter machineAdapter;
    private StackListAdapter stackAdapter;
    private MachineTapeSpinnerAdapter machineTapeSpinnerAdapter;
    private StackTapeSpinnerAdapter stackTapeSpinnerAdapter;

    //data from database
    private List<Symbol> inputAlphabetList;
    private List<Symbol> stackAlphabetList;
    private List<State> stateList;
    private List<Transition> transitionList;
    private DefaultTapeListAdapter defaultTapeAdapter; //default tape and initial position
    private LongSparseArray<List<Transition>> transitionMap;
    private MachineColorsGenerator machineColorsGenerator;

    public static SimulationActivity mContext;


    /**
     * Initializes the machine, adding special symbols to its alphabet and creates the tape.
     * called when new machine is to be created (rather than loading an existing one).
     *
     * @param machineType the type of the machine
     */
    public void initMachine(int machineType) {
        switch (machineType) {
            case MainActivity.PUSHDOWN_AUTOMATON:
                startStackSymbolId = dataSource.addStackSymbol("Z", Symbol.STACK_BOTTOM).getId();
                //intentional fall-through
            case MainActivity.FINITE_STATE_AUTOMATON:
                Symbol emptyInputSymbol = dataSource.addInputSymbol("ε", Symbol.EMPTY);
                emptyInputSymbolId = emptyInputSymbol.getId();
                TapeElement tapeElement = dataSource.addTapeElement(emptyInputSymbol, 0);
                defaultTapeAdapter.addRightItem(tapeElement);
                defaultTapeAdapter.changeInitialTapeElement(tapeElement);
                break;
            case MainActivity.LINEAR_BOUNDED_AUTOMATON:
                emptyInputSymbol = dataSource.addInputSymbol("#", Symbol.EMPTY);
                emptyInputSymbolId = emptyInputSymbol.getId();
                dataSource.addInputSymbol("|<", Symbol.LEFT_BOUND);
                dataSource.addInputSymbol(">|", Symbol.RIGHT_BOUND);
                tapeElement = dataSource.addTapeElement(emptyInputSymbol, 0);
                defaultTapeAdapter.addRightItem(tapeElement);
                defaultTapeAdapter.changeInitialTapeElement(tapeElement);
                break;
            case MainActivity.TURING_MACHINE:
                emptyInputSymbol = dataSource.addInputSymbol("#", Symbol.EMPTY);
                emptyInputSymbolId = emptyInputSymbol.getId();
                for (int i = 0; i < 6; i++) {
                    tapeElement = dataSource.addTapeElement(emptyInputSymbol, i);
                    defaultTapeAdapter.addRightItem(tapeElement);
                }
                defaultTapeAdapter.changeInitialTapeElement(defaultTapeAdapter.getItems().get(0));
                break;
        }
    }

    /**
     * Sets the activity's title
     *
     * @param machineType the type of the machine
     */
    private void setActivityTitle(int machineType) {
        ActionBar actionBar = this.getActionBar();
        switch (machineType) {
            case MainActivity.FINITE_STATE_AUTOMATON:
                actionBar.setTitle(R.string.simulation_FSA);
                break;
            case MainActivity.PUSHDOWN_AUTOMATON:
                actionBar.setTitle(R.string.simulation_PDA);
                break;
            case MainActivity.LINEAR_BOUNDED_AUTOMATON:
                actionBar.setTitle(R.string.simulation_LBA);
                break;
            case MainActivity.TURING_MACHINE:
                actionBar.setTitle(R.string.simulation_TM);
                break;
        }
    }

    /**
     * Sets the filename based on the type of the machine or Intent paramaters
     *
     * @param machineType the type of the machine
     */
    private void setFilename(int machineType) {
        String filename = getIntent().getExtras().getString(MainActivity.FILE_NAME);
        if (filename == null) {
            switch (machineType) {
                case MainActivity.FINITE_STATE_AUTOMATON:
                    this.filename = "FSA";
                    break;
                case MainActivity.PUSHDOWN_AUTOMATON:
                    this.filename = "PDA";
                    break;
                case MainActivity.LINEAR_BOUNDED_AUTOMATON:
                    this.filename = "LBA";
                    break;
                case MainActivity.TURING_MACHINE:
                    this.filename = "TM";
                    break;
            }
        } else {
            this.filename = filename;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.menu_simulation, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_simulation_configure:
                if (simulating) {
                    stopSimulation();
                }

                Bundle outputBundle = new Bundle();
                outputBundle.putInt(MainActivity.MACHINE_TYPE, machineType);
                outputBundle.putString(MainActivity.FILE_NAME, filename);
                outputBundle.putLong(SimulationActivity.EMPTY_INPUT_SYMBOL, emptyInputSymbolId);
                if (machineType == MainActivity.PUSHDOWN_AUTOMATON) {
                    outputBundle.putLong(SimulationActivity.START_STACK_SYMBOL, startStackSymbolId);
                }
                if (configurationType == MainActivity.NEW_TASK || configurationType == MainActivity.EDIT_TASK) {
                    outputBundle.putInt(TASK_CONFIGURATION, MainActivity.EDIT_TASK);
                    outputBundle.putSerializable(MainActivity.TASK, task);
                } else if (configurationType == MainActivity.SOLVE_TASK) {
                    outputBundle.putInt(TASK_CONFIGURATION, MainActivity.SOLVE_TASK);
                    outputBundle.putSerializable("TIME", task.getRemaining_time());
                    outputBundle.putSerializable(MainActivity.TASK, task);
                }
                Log.v(TAG, "outputBundle initialized");

                Intent nextActivityIntent = new Intent(this, ConfigurationActivity.class);
                nextActivityIntent.putExtras(outputBundle);
                startActivity(nextActivityIntent);
                Log.i(TAG, "configuration activity intent executed");
                return true;
            case R.id.menu_simulation_save_machine:
                if (Build.VERSION.SDK_INT > 15
                        && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MainActivity.REQUEST_WRITE_STORAGE);
                } else {
                    showSaveMachineDialog(false);
                }
                return true;
            case R.id.menu_simulation_specification:
                FragmentManager fm = getSupportFragmentManager();
                FormalSpecDialog formalSpecDialog = FormalSpecDialog.newInstance(machineType,
                        inputAlphabetList, stackAlphabetList,
                        stateList, transitionList);
                formalSpecDialog.show(fm, FORMAL_SPEC_DIALOG);
                return true;
            case R.id.menu_simulation_settings:
                if (simulating) {
                    stopSimulation();
                }

                outputBundle = new Bundle();
                outputBundle.putInt(MainActivity.MACHINE_TYPE, machineType);
                Log.v(TAG, "outputBundle initialized");

                nextActivityIntent = new Intent(this, OptionsActivity.class);
                nextActivityIntent.putExtras(outputBundle);
                startActivity(nextActivityIntent);
                Log.i(TAG, "configuration activity intent executed");
                return true;
            case R.id.menu_simulation_reset_tape:
                if (!simulating) {
                    resetTape();
                } else {
                    Toast.makeText(SimulationActivity.this, R.string.unable_simulation, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.menu_simulation_help:
                fm = getSupportFragmentManager();
                GuideFragment guideFragment = GuideFragment.newInstance(GuideFragment.SIMULATION);
                guideFragment.show(fm, HELP_DIALOG);
                return true;
            case R.id.menu_simulation_negative_test:
            case R.id.menu_simulation_bulk_test:
                if (simulating) {
                    stopSimulation();
                }
                outputBundle = new Bundle();
                outputBundle.putInt(MainActivity.MACHINE_TYPE, machineType);
                outputBundle.putString(MainActivity.FILE_NAME, filename);
                if (configurationType == MainActivity.SOLVE_TASK
                        || configurationType == MainActivity.NEW_TASK
                        || configurationType == MainActivity.EDIT_TASK) {
                    outputBundle.putInt(BulkTestActivity.TASK_CONFIGURATION, configurationType);
                }
                outputBundle.putSerializable(MainActivity.TASK, task);
                if (item.getItemId() == R.id.menu_simulation_negative_test) {
                    outputBundle.putBoolean(BulkTestActivity.NEGATIVE, true);
                }
                outputBundle.putString(MainActivity.FILE_NAME, filename);
                Log.v(TAG, "outputBundle initialized");

                nextActivityIntent = new Intent(this, BulkTestActivity.class);
                nextActivityIntent.putExtras(outputBundle);
                startActivity(nextActivityIntent);
                Log.i(TAG, "bulk test activity intent executed");
                return true;
        }

        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulation);
        Log.v(TAG, "onCreate initialization started");

        mContext = this;

        //menu
        ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //dataSource initialization
        dataSource = DataSource.getInstance();
        dataSource.open();

        //diagram initialization
        diagramView = findViewById(R.id.diagramView_simulation);

        //simulationFlow initialization
        simulationFlowView = findViewById(R.id.simulationFlowView_simulation);

        //get data from bundle
        Bundle inputBundle = getIntent().getExtras();

        //calculate tapDimension
        tapeDimension = (int) (getApplicationContext().getResources().getDisplayMetrics().heightPixels * 0.13);

        //create colors generator
        machineColorsGenerator = new MachineColorsGenerator(this, dataSource);

        //build adapters
        machineTapeSpinnerAdapter = new MachineTapeSpinnerAdapter(this, new ArrayList<Symbol>());

        defaultTapeRecyclerView = findViewById(R.id.recyclerView_simulation_default_tape);
        defaultTapeRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        defaultTapeAdapter = new DefaultTapeListAdapter(this, machineColorsGenerator, tapeDimension, machineTapeSpinnerAdapter);
        defaultTapeRecyclerView.setAdapter(defaultTapeAdapter);
        defaultTapeAdapter.setItemClickCallback(this);

        machineTapeRecyclerView = findViewById(R.id.recyclerView_simulation_machine_tape);
        machineTapeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        machineAdapter = new MachineListAdapter(this, tapeDimension, diagramView, simulationFlowView, machineColorsGenerator);
        machineTapeRecyclerView.setAdapter(machineAdapter);
        machineTapeRecyclerView.setDimension(new AdaptiveRecyclerView.IntMethod() {
            @Override
            public int get() {
                return machineAdapter.getTapeDimension();
            }
        });
        machineTapeRecyclerView.setItemCount(new AdaptiveRecyclerView.IntMethod() {
            @Override
            public int get() {
                return machineAdapter.getItemCount();
            }
        });
        machineTapeRecyclerView.setMaxCount(3.4f);
        machineTapeRecyclerView.setVisibility(View.GONE);

        //get configuration type (defines if we work with empty activity or fields will be filled already
        configurationType = savedInstanceState == null
                ? inputBundle.getInt(MainActivity.CONFIGURATION_TYPE)
                : savedInstanceState.getInt(MainActivity.CONFIGURATION_TYPE);
        switch (configurationType) {
            case MainActivity.NEW_MACHINE:
                //get machine type
                machineType = inputBundle.getInt(MainActivity.MACHINE_TYPE);
                initMachine(machineType);
                setFilename(machineType);
                break;
            case MainActivity.EXAMPLE_MACHINE1:
            case MainActivity.EXAMPLE_MACHINE2:
            case MainActivity.EXAMPLE_MACHINE3:
                //get machine type
                machineType = inputBundle.getInt(MainActivity.MACHINE_TYPE);
                prepareExample(machineType);
                setFilename(machineType);
                break;
            case MainActivity.LOAD_MACHINE:
                setFilename(machineType);
                FileHandler.Format format = inputBundle.getBoolean(MainActivity.DEFAULT_FORMAT)
                        ? FileHandler.Format.CMS
                        : FileHandler.Format.JFF;

                loadMachine(filename, format);

                filename = filename.substring(filename.lastIndexOf("/") + 1, filename.lastIndexOf("."));
                break;
            case MainActivity.NEW_TASK:
                machineType = inputBundle.getInt(MainActivity.MACHINE_TYPE);
                if (savedInstanceState == null) {
                    initMachine(machineType);
                }
                //intentional fall-through
            case MainActivity.EDIT_TASK:
                task = (Task) inputBundle.getSerializable(MainActivity.TASK);

                filename = task.getTitle();
                emptyInputSymbolId = dataSource.getInputSymbolWithProperties(Symbol.EMPTY).getId();
                machineType = inputBundle.getInt(MainActivity.MACHINE_TYPE);
                if (machineType == MainActivity.PUSHDOWN_AUTOMATON) {
                    startStackSymbolId = dataSource.getStackSymbolWithProperties(Symbol.STACK_BOTTOM).getId();
                }
                break;
            case MainActivity.SOLVE_TASK:
                task = (Task) inputBundle.getSerializable(MainActivity.TASK);
                setFilename(machineType);
                FileHandler.Format formatx = inputBundle.getBoolean(MainActivity.DEFAULT_FORMAT)
                        ? FileHandler.Format.CMS
                        : FileHandler.Format.JFF;

                loadMachine(filename, formatx);

                filename = filename.substring(filename.lastIndexOf("/") + 1, filename.lastIndexOf("."));
                emptyInputSymbolId = dataSource.getInputSymbolWithProperties(Symbol.EMPTY).getId();
                machineType = inputBundle.getInt(MainActivity.MACHINE_TYPE);
                if (machineType == MainActivity.PUSHDOWN_AUTOMATON) {
                    startStackSymbolId = dataSource.getStackSymbolWithProperties(Symbol.STACK_BOTTOM).getId();
                }
                break;
            case MainActivity.RESUME_MACHINE:
                machineType = inputBundle.getInt(MainActivity.MACHINE_TYPE);
                emptyInputSymbolId = dataSource.getInputSymbolWithProperties(Symbol.EMPTY).getId();
                if (machineType == MainActivity.PUSHDOWN_AUTOMATON) {
                    startStackSymbolId = dataSource.getStackSymbolWithProperties(Symbol.STACK_BOTTOM).getId();
                }
                setFilename(machineType);
                break;
        }

        setActivityTitle(machineType);

        //only pushdown automaton needs stack
        stackRecyclerView = findViewById(R.id.recyclerView_simulation_stack);
        if (machineType == MainActivity.PUSHDOWN_AUTOMATON) {
            stackTapeSpinnerAdapter = new StackTapeSpinnerAdapter(this, new ArrayList<Symbol>());
            stackRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            stackAdapter = new StackListAdapter(tapeDimension, machineAdapter);
            stackRecyclerView.setAdapter(stackAdapter);
            stackRecyclerView.setDimension(new AdaptiveRecyclerView.IntMethod() {
                @Override
                public int get() {
                    return stackAdapter.getTapeDimension();
                }
            });
            stackRecyclerView.setItemCount(new AdaptiveRecyclerView.IntMethod() {
                @Override
                public int get() {
                    return stackAdapter.getItemCount();
                }
            });

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                stackRecyclerView.setMaxCount(4.4f);
            } else {
                stackRecyclerView.setMaxCount(2.4f);
            }
        } else {
            stackRecyclerView.setVisibility(View.GONE);
        }

        //full simulation
        ImageButton simulateFullB = findViewById(R.id.imageButton_simulation_full);
        simulateFullB.setOnClickListener(this);

        //step forward simulation
        ImageButton simulateStepFB = findViewById(R.id.imageButton_simulation_step_f);
        simulateStepFB.setOnClickListener(this);

        //step backwards simulation
        ImageButton simulateStepBB = findViewById(R.id.imageButton_simulation_step_b);
        simulateStepBB.setOnClickListener(this);

        //stop simulation
        ImageButton simulateStopB = findViewById(R.id.imageButton_simulation_stop);
        simulateStopB.setOnClickListener(this);

        //remove all breakpoints
        ImageButton simulateRemoveBrkpB = findViewById(R.id.imageButton_simulation_remove);
        simulateRemoveBrkpB.setOnClickListener(this);
        Log.v(TAG, "simulation buttons initialized");

        //separator initialization
        VerticalSeekBar verticalSeekBar = findViewById(R.id.verticalSeekBar_simulation_diagram_ratio);
        verticalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //set parameters to work with actual values
                diagramView.setDimensionY(diagramView.getMeasuredHeight());
                simulationFlowView.setDimensionY(simulationFlowView.getMeasuredHeight());
                int sum = diagramView.getDimensionY() + simulationFlowView.getDimensionY();
                float ratio = (float) i / (float) 100;
                diagramView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1 - ratio));
                simulationFlowView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, ratio));
                //need approximate values because getMeasuredHeight is not ready
                diagramView.setDimensionY((int) (sum * (1 - ratio)));
                simulationFlowView.setDimensionY((int) (sum * ratio));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        verticalSeekBar.setProgress(30);

        dataSource.close();
        Log.i(TAG, "onCreate initialized");

        //open configuration activity if new machine or a task
        if (configurationType == MainActivity.NEW_MACHINE
                || configurationType == MainActivity.NEW_TASK
                || configurationType == MainActivity.EDIT_TASK
                || configurationType == MainActivity.SOLVE_TASK) {
            Bundle outputBundle = new Bundle();
            outputBundle.putInt(MainActivity.MACHINE_TYPE, machineType);
            outputBundle.putString(MainActivity.FILE_NAME, filename);
            outputBundle.putLong(SimulationActivity.EMPTY_INPUT_SYMBOL, emptyInputSymbolId);
            if (machineType == MainActivity.PUSHDOWN_AUTOMATON) {
                outputBundle.putLong(SimulationActivity.START_STACK_SYMBOL, startStackSymbolId);
            }
            if (configurationType == MainActivity.NEW_TASK || configurationType == MainActivity.EDIT_TASK) {
                outputBundle.putInt(TASK_CONFIGURATION, MainActivity.EDIT_TASK);
                outputBundle.putSerializable(MainActivity.TASK, task);
            } else if (configurationType == MainActivity.SOLVE_TASK) {
                if (hasTimeSet(task)) {
                    timer = Timer.getInstance(task.getRemaining_time());
                    //timer.pauseTimer();
                }
                outputBundle.putInt(TASK_CONFIGURATION, MainActivity.SOLVE_TASK);
                outputBundle.putSerializable("TIME", task.getAvailable_time());
                outputBundle.putSerializable(MainActivity.TASK, task);
            }
            Log.v(TAG, "outputBundle initialized");

            Intent nextActivityIntent = new Intent(this, ConfigurationActivity.class);
            nextActivityIntent.putExtras(outputBundle);

            startActivity(nextActivityIntent);
            Log.i(TAG, "configuration activity intent executed");
        }
    }

    private boolean hasTimeSet(Task task) {
        final Time availableTime = task.getAvailable_time();
        final int hours = availableTime.getHours();
        final int minutes = availableTime.getMinutes();
        final int seconds = availableTime.getSeconds();
        if (hours == 0 && minutes == 0 && seconds == 0)
            return false;
        else return true;
    }

    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (configurationType != 0) {
            Bundle inputBundle = this.getIntent().getExtras();
            task = (Task) inputBundle.getSerializable(MainActivity.TASK);
            MenuItem saveButton = menu.getItem(0);
            MenuItem submitTaskButton = menu.getItem(1);

            if (configurationType == MainActivity.SOLVE_TASK) {
                saveButton.setVisible(true);
                submitTaskButton.setVisible(true);
            }
            if (configurationType == MainActivity.EDIT_TASK || (configurationType == MainActivity.SOLVE_TASK && task.getPublicInputs())) {
                menu.findItem(R.id.menu_simulation_bulk_test).setTitle(R.string.correct_inputs);
                menu.findItem(R.id.menu_simulation_bulk_test).setVisible(true);
                menu.findItem(R.id.menu_simulation_negative_test).setTitle(R.string.incorrect_inputs);
                menu.findItem(R.id.menu_simulation_negative_test).setVisible(true);
            }
        }

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume method started");

        if (configurationType == MainActivity.SOLVE_TASK && hasTimeSet(task))
        {
            timer = Timer.getInstance(null);
            timer.setOnTickListener(new Timer.OnTickListener(){
                @Override
                public void onTick(long millisUntilFinished) {
                    int hours = (int) (millisUntilFinished / 3600000);
                    int minutes = (int) ((millisUntilFinished - (hours * 3600000)) / 60000);
                    int seconds = (int) ((millisUntilFinished - (hours * 3600000) - (minutes * 60000)) / 1000);

                    if (hours == 0 && minutes <= 4 && !timerRunOut) {
                        timerRunOut = true;

                        final int s_dark = getColor(R.color.primary_color_dark);
                        final int s_normal = getColor(R.color.primary_color);
                        final int s_light = getColor(R.color.primary_color_light);

                        final int t_dark = getColor(R.color.in_progress_dark);
                        final int t_normal = getColor(R.color.in_progress_top_bar);
                        final int t_light = getColor(R.color.in_progress_bottom_bar);

                        changeActivityBackgroundColor(s_dark, s_normal, s_light, t_dark, t_normal, t_light);
                    }


                    String timerText = String.format("%02d:%02d:%02d", hours, minutes, seconds);

                    ActionBar actionBar = SimulationActivity.this.getActionBar();
                    actionBar.setTitle(timerText);
                }
            });

            timer.setOnTimeRunOutListener(new Timer.OnTimeRunOutListener(){
                @Override
                public void onTimeRunOut() {
                    new MarkAsTimeRunOutAsync().execute();
                    finish();
                    BrowseAutomataTasksActivity.adapter.setTaskStatus(task.getTask_id(), Task.TASK_STATUS.TOO_LATE);
                    AlertDialog timeRunOutAlert = new AlertDialog.Builder(BrowseAutomataTasksActivity.mContext)
                            .setTitle(R.string.time_ran_out_title)
                            .setMessage(R.string.time_ran_out_message)
                            .setPositiveButton(android.R.string.ok, null)
                            .create();

                    timeRunOutAlert.show();
                }
            });
        }

        simulating = false;
        leaving = false;
        initialState = null;
        finalState = null;

        //update lists and colors
        dataSource.open();
        machineColorsGenerator.loadColors(this, dataSource);

        //get options from database
        Boolean markNondeterminism = dataSource.getMarkNondeterminism();

        //input alphabet
        inputAlphabetList = dataSource.getInputAlphabetFullExtract();
        for (Symbol symbol : inputAlphabetList) {
            if (symbol.getId() == emptyInputSymbolId) {
                Log.v(TAG, "empty symbol found at position " + inputAlphabetList.indexOf(symbol));
                inputAlphabetList.remove(symbol);
                inputAlphabetList.add(0, symbol);
                break;
            }
        }
        //update of tape spinners
        List<Symbol> filteredInputAlphabetList = new ArrayList<>(inputAlphabetList);
        Symbol.removeSpecialSymbols(filteredInputAlphabetList, Symbol.LEFT_BOUND | Symbol.RIGHT_BOUND);
        machineTapeSpinnerAdapter.clear();
        machineTapeSpinnerAdapter.addAll(filteredInputAlphabetList);

        List<TapeElement> tapeElementList = dataSource.getTapeFullExtract(inputAlphabetList);
        defaultTapeAdapter.setItems(tapeElementList); //solves recolor of the first machine
        if (!defaultTapeAdapter.getItems().isEmpty()) {
            defaultTapeAdapter.changeInitialTapeElement(defaultTapeAdapter.getItems().get(0));
        }
        defaultTapeRecyclerView.setVisibility(View.VISIBLE);
        machineTapeRecyclerView.setVisibility(View.GONE);

        //states
        stateList = dataSource.getStateFullExtract();
        for (State state : stateList) {
            if (state.isInitialState()) {
                initialState = state;
            }
            if (state.isFinalState()) {
                finalState = state;
            }
        }

        //transition and stack alphabet
        switch (machineType) {
            case MainActivity.FINITE_STATE_AUTOMATON:
                transitionList = dataSource.getFsaTransitionFullExtract(inputAlphabetList, stateList);
                break;
            case MainActivity.PUSHDOWN_AUTOMATON:
                stackAlphabetList = dataSource.getStackAlphabetFullExtract();
                for (Symbol symbol : stackAlphabetList) {
                    if (symbol.getId() == startStackSymbolId) {
                        Log.v(TAG, "start stack found at position " + stackAlphabetList.indexOf(symbol));
                        stackAlphabetList.remove(symbol);
                        stackAlphabetList.add(0, symbol);
                        break;
                    }
                }
                stackTapeSpinnerAdapter.clear();
                stackTapeSpinnerAdapter.addAll(stackAlphabetList);
                transitionList = dataSource.getPdaTransitionFullExtract(inputAlphabetList, stackAlphabetList, stateList);
                break;
            case MainActivity.LINEAR_BOUNDED_AUTOMATON:
            case MainActivity.TURING_MACHINE:
                transitionList = dataSource.getTmTransitionFullExtract(inputAlphabetList, stateList);
                break;
        }
        transitionMap = Transition.createTransitionMap(transitionList);

        diagramView.buildDiagram(markNondeterminism, emptyInputSymbolId, stateList, transitionList);
        simulationFlowView.buildDiagram(transitionList);

        //to initialize the transition diagram
        createFirstMachine();
    }

    private void updateStatusBarColor(int s_dark, int t_dark) {
        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), s_dark, t_dark);
        animator.setDuration(BACKGROUND_CHANGE_LENGTH);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                SimulationActivity.this.getWindow().setStatusBarColor((int) animation.getAnimatedValue());
            }
        });
        animator.start();
    }

    private void updateNavigationBarColor(int s_dark, int t_dark) {
        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), s_dark, t_dark);
        animator.setDuration(BACKGROUND_CHANGE_LENGTH);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                SimulationActivity.this.getWindow().setNavigationBarColor((int) animation.getAnimatedValue());
            }
        });
        animator.start();
    }


    private void updateActionBarColor(int s_normal, int t_normal) {
        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), s_normal, t_normal);
        animator.setDuration(BACKGROUND_CHANGE_LENGTH);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                SimulationActivity.this.getActionBar().setBackgroundDrawable(new ColorDrawable((int) animation.getAnimatedValue()));
            }
        });
        animator.start();
    }

    /*private void updateInnerViewsColor(int s_light, int t_light) {
        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), s_light, t_light);
        animator.setDuration(BACKGROUND_CHANGE_LENGTH);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                HorizontalScrollView tabs = findViewById(R.id.tabview_configuration);
                List<ImageButton> imageButtonList = new ArrayList<>();
                imageButtonList.add((ImageButton) findViewById(R.id.imageButton_configuration_diagram_move));
                imageButtonList.add((ImageButton) findViewById(R.id.imageButton_configuration_diagram_state));
                imageButtonList.add((ImageButton) findViewById(R.id.imageButton_configuration_diagram_transition));
                imageButtonList.add((ImageButton) findViewById(R.id.imageButton_configuration_diagram_edit));
                imageButtonList.add((ImageButton) findViewById(R.id.imageButton_configuration_diagram_remove));

                final int currentColorValue = (int) animation.getAnimatedValue();
                tabs.setBackgroundColor(currentColorValue);
                for (ImageButton btn :
                        imageButtonList) {
                    int[][] states = new int[][]{
                            new int[]{0}
                    };

                    int[] color = new int[]{
                            currentColorValue
                    };
                    ColorStateList list = new ColorStateList(states, color);
                    if (lastPressedImageButton != btn)
                        btn.setBackgroundTintList(list);

                }
            }
        });
        animator.start();
    }*/

    private void publishRemainingTime(final Time remainingTime)
    {
        if (configurationType == MainActivity.SOLVE_TASK && hasTimeSet(task)) {
            final Task currentTask = task;
            final Time availableTime = task.getAvailable_time();

            long elapsed = (availableTime.getTime() - remainingTime.getTime());

            int elapsedHours = (int) (elapsed / 3600000);
            int elapsedMinutes = (int) ((elapsed - (elapsedHours * 3600000)) / 60000);
            int elapsedSeconds = (int) ((elapsed - (elapsedHours * 3600000) - (elapsedMinutes * 60000)) / 1000);


            final String sTime = String.format("%02d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds);
            final Time elapsedTime = Time.valueOf(sTime);


            class UpdateTimerAsync extends AsyncTask<Void, Void, String> {
                @Override
                protected String doInBackground(Void... voids) {
                    UrlManager urlManager = new UrlManager();
                    ServerController serverController = new ServerController();
                    URL updateTimeURL = urlManager.getUpdateTimerURL(elapsedTime, TaskLoginActivity.loggedUser.getUser_id(), currentTask.getTask_id());

                    String output = null;
                    try {
                        output = serverController.getResponseFromServer(updateTimeURL);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        return output;
                    }
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);

                    if (s != null || !s.isEmpty()) {
                        try {
                            JSONObject object = new JSONObject(s);
                            if (!object.getBoolean("updated")) {
                                Toast.makeText(SimulationActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                            } else {
                                BrowseAutomataTasksActivity.adapter.notifyTimeChange(currentTask.getTask_id(), remainingTime);
                            }
                        } catch (JSONException e) {
                            Toast.makeText(SimulationActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SimulationActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            new UpdateTimerAsync().execute();
        }
    }

    private void changeActivityBackgroundColor(int s_dark, int s_normal, int s_light, int t_dark, int t_normal, int t_light) {
        updateStatusBarColor(s_dark, t_dark);
        updateActionBarColor(s_normal, t_normal);
        updateNavigationBarColor(s_dark, t_dark);
        //updateInnerViewsColor(s_light, t_light);
    }

    private class MarkAsTimeRunOutAsync extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            UrlManager urlManager = new UrlManager();
            ServerController serverController = new ServerController();
            URL url = urlManager.getChangeFlagUrl(Task.TASK_STATUS.TOO_LATE, TaskLoginActivity.loggedUser.getUser_id(), task.getTask_id());

            String output = null;

            try {
                output = serverController.getResponseFromServer(url);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                return output;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s != null || !s.isEmpty()) {
                try {
                    JSONObject object = new JSONObject(s);
                    if (!object.getBoolean("updated")) {
                        Toast.makeText(SimulationActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(SimulationActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SimulationActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (task == null) {
            outState.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.RESUME_MACHINE);
        } else if (configurationType == MainActivity.NEW_TASK) {
            outState.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EDIT_TASK);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        tapeDimension = (int) ((getApplicationContext().getResources().getDisplayMetrics().heightPixels * 0.13) /
                Math.min(machineAdapter.getItemCount(), 2));

        defaultTapeAdapter.setTapeDimension(tapeDimension);
        defaultTapeRecyclerView.setAdapter(defaultTapeAdapter);

        machineAdapter.setTapeDimension(tapeDimension);
        machineTapeRecyclerView.setAdapter(machineAdapter);
        if (machineType == MainActivity.PUSHDOWN_AUTOMATON) {
            stackAdapter.setTapeDimension(tapeDimension);
            stackRecyclerView.setAdapter(stackAdapter);
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                stackRecyclerView.setMaxCount(4.4f);
            } else {
                stackRecyclerView.setMaxCount(2.4f);
            }
        }

        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //full simulation
            case R.id.imageButton_simulation_full:
                //check if initial and final states exist
                if (initialState == null || finalState == null) {
                    if (initialState == null && finalState == null) {
                        Toast.makeText(this, R.string.both_states_error, Toast.LENGTH_SHORT).show();
                    } else if (initialState == null) {
                        Toast.makeText(this, R.string.initial_state_error, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.final_state_error, Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                //check if initial tapeElement exists
                if (defaultTapeAdapter.getInitialTapeElement() == null) {
                    Toast.makeText(this, R.string.initial_tape_element_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                int tapeCheck = 0;
                //check if tape is completed (finite state or pushdown automaton only)
                if (machineType == MainActivity.FINITE_STATE_AUTOMATON || machineType == MainActivity.PUSHDOWN_AUTOMATON) {
                    for (TapeElement tapeElement : defaultTapeAdapter.getItems()) {
                        if (tapeElement.getSymbol().getId() != inputAlphabetList.get(0).getId()) {
                            if (tapeCheck == 2) {
                                Toast.makeText(this, R.string.simulation_incomplete_tape_error, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            tapeCheck = 1;
                        } else {
                            if (tapeCheck == 1) {
                                tapeCheck = 2;
                            }
                        }
                    }
                    if ((tapeCheck == 1 || tapeCheck == 2)
                            && defaultTapeAdapter.getInitialTapeElement().getSymbol().getId() == inputAlphabetList.get(0).getId()) {
                        Toast.makeText(this, R.string.initial_tape_symbol_error, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                startSimulation();

                //swap tapes
                defaultTapeRecyclerView.setVisibility(View.GONE);
                machineTapeRecyclerView.setVisibility(View.VISIBLE);

                //remove diagramView colors
                for (MachineStep machineStep : machineAdapter.getItems()) {
                    if (machineStep.getDepth() == simulationDepth) {
                        machineAdapter.removeDiagramColor(machineStep);
                    }
                }

                machineAdapter.getItems().get(0).simulateFull();
                checkSimulationStatus(true);

                //add diagramView colors
                for (MachineStep machineStep : machineAdapter.getItems()) {
                    if (machineStep.getDepth() == simulationDepth) {
                        machineAdapter.addDiagramColor(machineStep);
                    }
                }

                diagramView.invalidate();
                simulationFlowView.invalidate();
                break;
            //step forward simulation
            case R.id.imageButton_simulation_step_f:
                //check if initial and final states exist
                if (initialState == null || finalState == null) {
                    if (initialState == null && finalState == null) {
                        Toast.makeText(this, R.string.both_states_error, Toast.LENGTH_SHORT).show();
                    } else if (initialState == null) {
                        Toast.makeText(this, R.string.initial_state_error, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.final_state_error, Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                //check if initial tapeElement exists
                if (defaultTapeAdapter.getInitialTapeElement() == null) {
                    Toast.makeText(this, R.string.initial_tape_element_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                tapeCheck = 0;
                //check if tape is completed (finite state or pushdown automaton only)
                if (machineType == MainActivity.FINITE_STATE_AUTOMATON || machineType == MainActivity.PUSHDOWN_AUTOMATON) {
                    for (TapeElement tapeElement : defaultTapeAdapter.getItems()) {
                        if (tapeElement.getSymbol().getId() != inputAlphabetList.get(0).getId()) {
                            if (tapeCheck == 2) {
                                Toast.makeText(this, R.string.simulation_incomplete_tape_error, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            tapeCheck = 1;
                        } else {
                            if (tapeCheck == 1) {
                                tapeCheck = 2;
                            }
                        }
                    }
                    if ((tapeCheck == 1 || tapeCheck == 2)
                            && defaultTapeAdapter.getInitialTapeElement().getSymbol().getId() == inputAlphabetList.get(0).getId()) {
                        Toast.makeText(this, R.string.initial_tape_symbol_error, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                startSimulation();

                //swap tapes
                defaultTapeRecyclerView.setVisibility(View.GONE);
                machineTapeRecyclerView.setVisibility(View.VISIBLE);

                //remove diagramView colors
                for (MachineStep machineStep : machineAdapter.getItems()) {
                    if (machineStep.getDepth() == simulationDepth) {
                        machineAdapter.removeDiagramColor(machineStep);
                    }
                }

                machineAdapter.getItems().get(0).simulateStepF(simulationDepth);
                checkSimulationStatus(false);

                //add diagramView colors
                for (MachineStep machineStep : machineAdapter.getItems()) {
                    if (machineStep.getDepth() == simulationDepth) {
                        machineAdapter.addDiagramColor(machineStep);
                    }
                }

                diagramView.invalidate();
                simulationFlowView.invalidate();
                break;
            //step backwards simulation
            case R.id.imageButton_simulation_step_b:
                //check if initial and final states exist
                if (initialState == null || finalState == null) {
                    if (initialState == null && finalState == null) {
                        Toast.makeText(this, R.string.both_states_error, Toast.LENGTH_SHORT).show();
                    } else if (initialState == null) {
                        Toast.makeText(this, R.string.initial_state_error, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.final_state_error, Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                //check if initial tapeElement exists
                if (defaultTapeAdapter.getInitialTapeElement() == null) {
                    Toast.makeText(this, R.string.initial_tape_element_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                //check if simulating
                if (simulating) {
                    //remove diagramView colors
                    for (MachineStep machineStep : machineAdapter.getItems()) {
                        if (machineStep.getDepth() == simulationDepth) {
                            machineAdapter.removeDiagramColor(machineStep);
                        }
                    }

                    machineAdapter.getItems().get(0).simulateStepB(simulationDepth);
                    //check if start point of simulation
                    if (machineAdapter.getItems().isEmpty()) {
                        stopSimulation();
                        Toast.makeText(this, R.string.simulation_stopped, Toast.LENGTH_SHORT).show();
                    } else {
                        checkSimulationStatus(false);

                        //add diagramView colors
                        for (MachineStep machineStep : machineAdapter.getItems()) {
                            if (machineStep.getDepth() == simulationDepth) {
                                machineAdapter.addDiagramColor(machineStep);
                            }
                        }

                        diagramView.invalidate();
                        simulationFlowView.invalidate();
                    }
                } else {
                    Toast.makeText(this, R.string.simulation_needed, Toast.LENGTH_SHORT).show();
                }
                break;
            //stop simulation
            case R.id.imageButton_simulation_stop:
                if (simulating) {
                    stopSimulation();
                    Toast.makeText(this, R.string.simulation_stopped, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.simulation_needed, Toast.LENGTH_SHORT).show();
                }
                break;
            //remove all breakpoints
            case R.id.imageButton_simulation_remove:
                for (TapeElement tapeElement : defaultTapeAdapter.getItems()) {
                    if (tapeElement.isBreakpoint()) {
                        tapeElement.setBreakpoint(false);
                        dataSource.updateTapeElement(tapeElement, tapeElement.getSymbol(), tapeElement.getOrder());
                        //+1 because 0 is left button
                        defaultTapeAdapter.notifyItemChanged(defaultTapeAdapter.getItems().indexOf(tapeElement) + 1);
                    }
                }
                for (MachineStep machineStep : machineAdapter.getItems()) {
                    machineStep.getTape().clearBreakpoints();
                }
                machineAdapter.notifyDataSetChanged();
                break;
        }
    }

    //handle leaving from simulation activity (leaving means discard all changes)
    @Override
    public void onBackPressed() {
        Log.v(TAG, "onBackPressed method started");
        TaskDialog.setStatusText(null);
        if (configurationType == MainActivity.NEW_TASK
                || configurationType == MainActivity.EDIT_TASK) {
            SimulationActivity.this.finish();
            SimulationActivity.super.onBackPressed();
        } else if (configurationType == MainActivity.SOLVE_TASK) {
            if (hasTimeSet(task))
            {
                publishRemainingTime(timer.getCurrentTime());
                timer.pauseTimer();
                Timer.deleteTimer();
            }
            new AlertDialog.Builder(this)
                    .setTitle(R.string.warning)
                    .setMessage(R.string.task_leave_warning)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dataSource.globalDrop();
                            SimulationActivity.this.finish();
                            SimulationActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.warning)
                    .setMessage(R.string.machine_confirmation)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (Build.VERSION.SDK_INT > 15
                                    && ContextCompat.checkSelfPermission(SimulationActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(SimulationActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        MainActivity.REQUEST_WRITE_STORAGE);
                            } else {
                                if (filename == null)
                                    setFilename(machineType);
                                showSaveMachineDialog(true);
                            }
                        }
                    })
                    .setNeutralButton(R.string.cancel, null)
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dataSource.globalDrop();
                            SimulationActivity.this.finish();
                            SimulationActivity.super.onBackPressed();
                        }
                    })
                    .show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        leaving = true;
        if (stateList != null) {
            for (State state : stateList) {
                dataSource.updateState(state, state.getValue(), state.getPositionX(), state.getPositionY(), state.isInitialState(), state.isFinalState());
            }
        }
        dataSource.close();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MainActivity.REQUEST_WRITE_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showSaveMachineDialog(false);
                }
            }
        }
    }

    @Override
    public void onLeftButtonClick() {
        if (simulating) {
            Toast.makeText(this, R.string.unable_simulation, Toast.LENGTH_SHORT).show();
        } else {
            try {
                //default tape element
                TapeElement tapeElement = dataSource.addTapeElement(inputAlphabetList.get(0),
                        defaultTapeAdapter.getItems().isEmpty() ? 0 : defaultTapeAdapter.getItems().get(0).getOrder() - 1);
                defaultTapeAdapter.addLeftItem(tapeElement);

                //first machine tape element (need new one)
                machineAdapter.getItems().get(0).getTape().addToLeft(tapeElement.getSymbol());
                Log.i(TAG, "newTapeElement '" + tapeElement.getSymbol().getValue() + "' created");
            } catch (Exception e) {
                Toast.makeText(this, R.string.tape_element_save_error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "unknown error occurred while creating tape element", e);
            }
        }
    }

    @Override
    public void onRightButtonClick() {
        if (simulating) {
            Toast.makeText(this, R.string.unable_simulation, Toast.LENGTH_SHORT).show();
        } else {
            try {
                //default tape element
                TapeElement tapeElement = dataSource.addTapeElement(inputAlphabetList.get(0),
                        defaultTapeAdapter.getItems().isEmpty() ? 0 : defaultTapeAdapter.getItems().get(defaultTapeAdapter.getItems().size() - 1).getOrder() + 1);
                defaultTapeAdapter.addRightItem(tapeElement);

                //first machine tape element (need new one)
                machineAdapter.getItems().get(0).getTape().addToRight(tapeElement.getSymbol());
                //scroll right
                defaultTapeRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        // Call smooth scroll
                        defaultTapeRecyclerView.smoothScrollToPosition(defaultTapeAdapter.getItemCount() - 1);
                    }
                });
                Log.i(TAG, "newTapeElement '" + tapeElement.getSymbol().getValue() + "' created");
            } catch (Exception e) {
                Toast.makeText(this, R.string.tape_element_save_error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "unknown error occurred while creating tape element", e);
            }
        }
    }

    @Override
    public void onSpinnerItemSelected(int position, Symbol symbol) {
        //check if tape could change/simulation (because there are false calls)
        if (!simulating && !leaving) {
            try {
                //default tape element
                dataSource.updateTapeElement(defaultTapeAdapter.getItems().get(position),
                        symbol, defaultTapeAdapter.getItems().get(position).getOrder());

                //calculate new usable transitions, remove previous colors and add new ones if necessary
                if (defaultTapeAdapter.getItems().get(position) == defaultTapeAdapter.getInitialTapeElement()) {
                    machineAdapter.removeDiagramColor(machineAdapter.getItems().get(0));
                    simulationFlowView.removeTransition(machineAdapter.getItems().get(0));
                    machineAdapter.getItems().get(0).findUsableTransitions();
                    machineAdapter.addDiagramColor(machineAdapter.getItems().get(0));
                    simulationFlowView.addTransition(machineAdapter.getItems().get(0), machineAdapter.getItems().get(0), null, machineAdapter.getItems().get(0).getUsableTransitionList());

                    diagramView.invalidate();
                    simulationFlowView.invalidate();
                }

                Log.v(TAG, "tapeElement item selected");
            } catch (Exception e) {
                Toast.makeText(this, R.string.tape_element_save_error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "unknown error occurred while saving tape element", e);
            }
        }
    }

    @Override
    public void onSpinnerLongClick(int position) {
        if (simulating) {
            Toast.makeText(this, R.string.unable_simulation, Toast.LENGTH_SHORT).show();
        } else {
            final int finalPosition = position;
            final TapeElement defaultTapeElement = defaultTapeAdapter.getItems().get(position);

            CharSequence[] contextSource;
            //edit of initial tape symbol spinner
            if (defaultTapeAdapter.getInitialTapeElement() == defaultTapeElement) {
                contextSource = new CharSequence[]{getResources().getString(R.string.set_ordinary),
                        getResources().getString(R.string.place_breakpoint),
                        getResources().getString(R.string.remove)};
            }

            if (defaultTapeElement.isBreakpoint()) {
                contextSource = new CharSequence[]{getResources().getString(R.string.set_initial),
                        getResources().getString(R.string.remove_breakpoint),
                        getResources().getString(R.string.remove)};
            } else {
                contextSource = new CharSequence[]{getResources().getString(R.string.set_initial),
                        getResources().getString(R.string.place_breakpoint),
                        getResources().getString(R.string.remove)};
            }

            //dialog chooser
            new AlertDialog.Builder(this)
                    .setTitle(R.string.choose_action)
                    .setItems(contextSource, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case SET_INITIAL:
                                    if (defaultTapeAdapter.getInitialTapeElement() == defaultTapeElement) {
                                        defaultTapeAdapter.changeInitialTapeElement(null);
                                        //position+1 because 0 is left button
                                        defaultTapeAdapter.notifyItemChanged(finalPosition + 1);
                                        //no need to notify, tape is not visible
                                        machineAdapter.getItems().get(0).getTape().setCurrentPosition(-1);

                                        //calculate new usable transitions, remove previous colors and add new ones if necessary
                                        machineAdapter.removeDiagramColor(machineAdapter.getItems().get(0));
                                        simulationFlowView.removeTransition(machineAdapter.getItems().get(0));
                                        machineAdapter.getItems().get(0).findUsableTransitions();
                                        machineAdapter.addDiagramColor(machineAdapter.getItems().get(0));
                                        simulationFlowView.addTransition(machineAdapter.getItems().get(0), machineAdapter.getItems().get(0), null, machineAdapter.getItems().get(0).getUsableTransitionList());

                                        diagramView.invalidate();
                                        simulationFlowView.invalidate();
                                    } else {
                                        TapeElement oldInitialElement = defaultTapeAdapter.getInitialTapeElement();
                                        defaultTapeAdapter.changeInitialTapeElement(defaultTapeElement);
                                        //position+1 because 0 is left button
                                        defaultTapeAdapter.notifyItemChanged(finalPosition + 1);
                                        if (oldInitialElement != null) {
                                            defaultTapeAdapter.notifyItemChanged(defaultTapeAdapter.getItems().indexOf(oldInitialElement) + 1);
                                        }
                                        //no need to notify, tape is not visible
                                        machineAdapter.getItems().get(0).getTape().setCurrentPosition(finalPosition);

                                        //calculate new usable transitions, remove previous colors and add new ones if necessary
                                        machineAdapter.removeDiagramColor(machineAdapter.getItems().get(0));
                                        simulationFlowView.removeTransition(machineAdapter.getItems().get(0));
                                        machineAdapter.getItems().get(0).findUsableTransitions();
                                        machineAdapter.addDiagramColor(machineAdapter.getItems().get(0));
                                        simulationFlowView.addTransition(machineAdapter.getItems().get(0), machineAdapter.getItems().get(0), null, machineAdapter.getItems().get(0).getUsableTransitionList());

                                        diagramView.invalidate();
                                        simulationFlowView.invalidate();
                                    }
                                    break;
                                case PLACE_BREAKPOINT:
                                    /*if (defaultTapeAdapter.getInitialTapeElement() == defaultTapeElement) {
                                        Toast.makeText(SimulationActivity.this, R.string.initial_tape_element_breakpoint_error, Toast.LENGTH_SHORT).show();
                                    } else*/
                                    if (defaultTapeElement.isBreakpoint()) {
                                        defaultTapeElement.setBreakpoint(false);
                                        //position+1 because 0 is left button
                                        defaultTapeAdapter.notifyItemChanged(finalPosition + 1);
                                        machineAdapter.getItems().get(0).getTape().setBreakpoint(finalPosition, false);
                                    } else {
                                        defaultTapeElement.setBreakpoint(true);
                                        //position+1 because 0 is left button
                                        defaultTapeAdapter.notifyItemChanged(finalPosition + 1);
                                        machineAdapter.getItems().get(0).getTape().setBreakpoint(finalPosition, true);
                                    }

                                    break;
                                case REMOVE:
                                    if (defaultTapeAdapter.getInitialTapeElement() == defaultTapeElement) {
                                        new AlertDialog.Builder(SimulationActivity.this)
                                                .setTitle(R.string.warning)
                                                .setMessage(R.string.remove_tape_element)
                                                .setCancelable(true)
                                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dataSource.deleteTapeElement(defaultTapeElement);
                                                        defaultTapeAdapter.changeInitialTapeElement(null);
                                                        defaultTapeAdapter.removeItem(defaultTapeElement);
                                                        //no need to notify, tape is not visible
                                                        machineAdapter.getItems().get(0).getTape().setCurrentPosition(-1);
                                                        machineAdapter.getItems().get(0).getTape().removeElement(finalPosition);

                                                        //calculate new usable transitions, remove previous colors and add new ones if necessary
                                                        machineAdapter.removeDiagramColor(machineAdapter.getItems().get(0));
                                                        simulationFlowView.removeTransition(machineAdapter.getItems().get(0));
                                                        machineAdapter.getItems().get(0).findUsableTransitions();
                                                        machineAdapter.addDiagramColor(machineAdapter.getItems().get(0));
                                                        simulationFlowView.addTransition(machineAdapter.getItems().get(0), machineAdapter.getItems().get(0), null, machineAdapter.getItems().get(0).getUsableTransitionList());

                                                        diagramView.invalidate();
                                                        simulationFlowView.invalidate();
                                                    }
                                                })
                                                .setNegativeButton(R.string.no, null)
                                                .show();
                                    } else {
                                        new AlertDialog.Builder(SimulationActivity.this)
                                                .setTitle(R.string.warning)
                                                .setMessage(R.string.remove_tape_element)
                                                .setCancelable(true)
                                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dataSource.deleteTapeElement(defaultTapeElement);
                                                        defaultTapeAdapter.removeItem(defaultTapeElement);
                                                        //no need to notify, tape is not visible
                                                        machineAdapter.getItems().get(0).getTape().removeElement(finalPosition);
                                                    }
                                                })
                                                .setNegativeButton(R.string.no, null)
                                                .show();
                                    }

                                    break;
                            }
                        }
                    })
                    .show();
        }
    }

    @Override
    public void saveDialogClick(String filename, FileHandler.Format format, boolean exit) {
        this.filename = filename;
        try {
            for (State state : stateList) {
                dataSource.updateState(state, state.getValue(), state.getPositionX(), state.getPositionY(), state.isInitialState(), state.isFinalState());
            }
            FileHandler fileHandler = new FileHandler(format);
            if (configurationType != MainActivity.SOLVE_TASK || task.getPublicInputs()) {
                fileHandler.setData(dataSource, machineType);
            } else { //test inputs are supposed to be hidden, do not save them
                List<Transition> transitions;
                List<Symbol> inputAlphabet = dataSource.getInputAlphabetFullExtract();
                List<State> states = dataSource.getStateFullExtract();
                switch (machineType) {
                    case MainActivity.FINITE_STATE_AUTOMATON:
                        transitions = dataSource.getFsaTransitionFullExtract(inputAlphabet, states);
                        break;
                    case MainActivity.PUSHDOWN_AUTOMATON:
                        transitions = dataSource.getPdaTransitionFullExtract(inputAlphabet, dataSource.getStackAlphabetFullExtract(), states);
                        break;
                    default:
                        transitions = dataSource.getTmTransitionFullExtract(inputAlphabet, states);
                        break;
                }
                fileHandler.setData(states, dataSource.getInputAlphabetFullExtract(), null,
                        transitions, defaultTapeAdapter.getItems(), new ArrayList<TestScenario>(),
                        new ArrayList<TestScenario>(), machineType);
            }
            if (task != null) {
                fileHandler.writeTask(task);
            }
            fileHandler.writeFile(filename);

            Toast.makeText(this, FileHandler.PATH + "/" + filename + format.getExtension() + " " + getResources().getString(R.string.save_succ), Toast.LENGTH_SHORT).show();
            SaveMachineDialog saveMachineDialog = (SaveMachineDialog) getSupportFragmentManager()
                    .findFragmentByTag(SAVE_DIALOG);
            if (saveMachineDialog != null) {
                saveMachineDialog.dismiss();
            }

            if (exit) {
                dataSource.globalDrop();
                SimulationActivity.this.finish();
                SimulationActivity.super.onBackPressed();
            }
        } catch (Exception e) {
            Log.e(TAG, "File was not saved", e);
            Toast.makeText(this, getResources().getString(R.string.file_not_saved), Toast.LENGTH_SHORT).show();
        }
    }

    private void showSaveMachineDialog(boolean exit) {
        FragmentManager fm = getSupportFragmentManager();
        SaveMachineDialog saveMachineDialog = SaveMachineDialog.newInstance(filename, null, exit);
        saveMachineDialog.show(fm, SAVE_DIALOG);
    }

    //method to resetCounter tape
    private void resetTape() {
        Log.v(TAG, "resetTape method started");
        try {
            for (int i = 0; i < defaultTapeAdapter.getItems().size(); i++) { //could reset tape order numbers, but order is unique - could cause exception
                //default tape element
                dataSource.updateTapeElement(defaultTapeAdapter.getItems().get(i),
                        inputAlphabetList.get(0), defaultTapeAdapter.getItems().get(i).getOrder());
                //first machine tape element
                machineAdapter.getItems().get(0).getTape().setSymbol(i, inputAlphabetList.get(0));
            }
            defaultTapeAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(this, R.string.tape_element_save_error, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "unknown error occurred while saving tape element", e);
        }
    }

    /**
     * Loads machine from a file
     *
     * @param filename the name of the file
     * @param format   the file format
     */
    private void loadMachine(String filename, FileHandler.Format format) {
        try {
            FileHandler fileHandler = new FileHandler(format);
            fileHandler.loadFile(filename);
            machineType = fileHandler.getMachineType();
            fileHandler.getData(dataSource);
            emptyInputSymbolId = fileHandler.getEmptyInputSymbolId();
            if (machineType == MainActivity.PUSHDOWN_AUTOMATON) {
                startStackSymbolId = fileHandler.getStartStackSymbolId();
            }
        } catch (Exception e) {
            Log.e(TAG, "File was not loaded", e);
            Toast.makeText(this, getResources().getString(R.string.file_not_loaded), Toast.LENGTH_SHORT).show();
            dataSource.globalDrop();
            dataSource.close();
            finish();
        }
    }

    /**
     * Loads an example machine
     *
     * @param machineType the type of the machine
     */
    private void prepareExample(int machineType) {
        String file = null;
        switch (machineType) {
            case MainActivity.FINITE_STATE_AUTOMATON:
                switch (configurationType) {
                    case MainActivity.EXAMPLE_MACHINE1:
                        file = FileHandler.Examples.DFSA;
                        break;
                    case MainActivity.EXAMPLE_MACHINE2:
                        file = FileHandler.Examples.NFSA;
                        break;
                    case MainActivity.EXAMPLE_MACHINE3:
                        file = FileHandler.Examples.DFSA_AN;
                        break;
                }
                break;
            case MainActivity.PUSHDOWN_AUTOMATON:
                switch (configurationType) {
                    case MainActivity.EXAMPLE_MACHINE1:
                        file = FileHandler.Examples.DPDA;
                        break;
                    case MainActivity.EXAMPLE_MACHINE2:
                        file = FileHandler.Examples.NPDA;
                        break;
                    case MainActivity.EXAMPLE_MACHINE3:
                        file = FileHandler.Examples.DPDA_ANBN;
                        break;
                }
                break;
            case MainActivity.LINEAR_BOUNDED_AUTOMATON:
                switch (configurationType) {
                    case MainActivity.EXAMPLE_MACHINE1:
                        file = FileHandler.Examples.DLBA;
                        break;
                    case MainActivity.EXAMPLE_MACHINE2:
                        file = FileHandler.Examples.NLBA;
                        break;
                }
                break;
            case MainActivity.TURING_MACHINE:
                switch (configurationType) {
                    case MainActivity.EXAMPLE_MACHINE1:
                        file = FileHandler.Examples.DTM;
                        break;
                    case MainActivity.EXAMPLE_MACHINE2:
                        file = FileHandler.Examples.NTM;
                        break;
                }
                break;
        }

        try {
            FileHandler fileHandler = new FileHandler(FileHandler.Format.CMS);
            fileHandler.loadAsset(getAssets(), file);
            fileHandler.getData(dataSource);
            emptyInputSymbolId = fileHandler.getEmptyInputSymbolId();
            if (machineType == MainActivity.PUSHDOWN_AUTOMATON) {
                startStackSymbolId = fileHandler.getStartStackSymbolId();
            }
        } catch (Exception e) {
            Log.e(TAG, "File was not loaded", e);
            Toast.makeText(this, getResources().getString(R.string.file_not_loaded), Toast.LENGTH_SHORT).show();
            dataSource.globalDrop();
            dataSource.close();
            finish();
        }
    }

    /**
     * Initializes the first machine and enters simulating state
     */
    private void startSimulation() {
        if (!simulating) {
            simulating = true;
            //to apply changes made to the tape
            createFirstMachine();
        }
    }

    /**
     * Reverts the GUI to a non-simulating state
     */
    private void stopSimulation() {
        for (State state : stateList) {
            dataSource.updateState(state, state.getValue(), state.getPositionX(), state.getPositionY(), state.isInitialState(), state.isFinalState());
        }
        onResume();
        onConfigurationChanged(getResources().getConfiguration());
    }

    /**
     * Initializes the first thread of execution
     */
    private void createFirstMachine() {
        machineColorsGenerator.resetCounter();
        machineAdapter.removeAll();
        simulationDepth = 0;
        machineAdapter.setDepth(simulationDepth);
        if (machineType == MainActivity.PUSHDOWN_AUTOMATON) {
            //change tapeDimension
            int tapeDimension = (int) (getApplicationContext().getResources().getDisplayMetrics().heightPixels * 0.13);
            stackAdapter.setTapeDimension(tapeDimension);
            stackAdapter.notifyDataSetChanged();
            stackAdapter.setDepth(simulationDepth);
        }
        MachineStep machineStep = null;
        int maxSteps = dataSource.getMaxSteps();
        Symbol emptySymbol = dataSource.getInputSymbolWithProperties(Symbol.EMPTY);
        switch (machineType) {
            case MainActivity.FINITE_STATE_AUTOMATON:
                machineStep = new FiniteStateAutomatonStep(machineAdapter,
                        emptySymbol, transitionMap,
                        initialState, defaultTapeAdapter, maxSteps);
                break;
            case MainActivity.PUSHDOWN_AUTOMATON:
                List<Symbol> stack = new ArrayList<>();
                stack.add(stackAlphabetList.get(0));
                machineStep = new PushdownAutomatonStep(machineAdapter,
                        tapeDimension, emptySymbol, transitionMap,
                        initialState, defaultTapeAdapter,
                        stackAdapter, stackTapeSpinnerAdapter, stack, maxSteps);
                break;
            case MainActivity.LINEAR_BOUNDED_AUTOMATON:
                machineStep = new LinearBoundedAutomatonStep(machineAdapter,
                        emptySymbol,
                        dataSource.getInputSymbolWithProperties(Symbol.LEFT_BOUND),
                        dataSource.getInputSymbolWithProperties(Symbol.RIGHT_BOUND),
                        transitionMap,
                        initialState, defaultTapeAdapter, maxSteps);
                break;
            case MainActivity.TURING_MACHINE:
                machineStep = new TuringMachineStep(machineAdapter,
                        tapeDimension, emptySymbol, transitionMap,
                        initialState, defaultTapeAdapter, maxSteps);
                break;
        }

        if (machineStep != null) {
            ((MachineTapeListAdapter) machineStep.getTape()).setTapeDimension(tapeDimension);

            machineStep.findUsableTransitions();
            machineAdapter.addItem(machineStep);
            if (machineType == MainActivity.PUSHDOWN_AUTOMATON) {
                stackAdapter.notifyDataSetChanged();
            }
            machineAdapter.addDiagramColor(machineStep);
            simulationFlowView.addFirstStateNode(initialState);
            simulationFlowView.addTransition(machineStep, machineStep, null, machineStep.getUsableTransitionList());
            diagramView.invalidate();
            //move camera
            simulationFlowView.moveCamera();
        }
    }

    private void checkSimulationStatus(boolean fullSimulation) {
        Log.v(TAG, "checkSimulationStatus called");

        //find newMaxDepth
        int maxDepth = 0;
        for (MachineStep machineStep : machineAdapter.getItems()) {
            if (maxDepth < machineStep.getDepth()) {
                maxDepth = machineStep.getDepth();
            }
        }

        MachineStep machine = machineAdapter.getItems().get(0);
        int status = machine.getNondeterministicMachineStatus();

        if (status == MachineStep.DONE) {
            Toast.makeText(this, getResources().getString(R.string.solution_found), Toast.LENGTH_SHORT).show();
        } else if (status == MachineStep.STUCK) {
            Toast.makeText(this, R.string.all_stuck, Toast.LENGTH_SHORT).show();
        } else if (fullSimulation) {
            if (machine.isNondeterministicBreakpoint()) {
                Toast.makeText(this, R.string.breakpoint_hit, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.simulated_max_steps, dataSource.getMaxSteps()),
                        Toast.LENGTH_SHORT).show();
            }
        }
        simulationDepth = maxDepth;
        machineAdapter.setDepth(simulationDepth);
        if (machineType == MainActivity.PUSHDOWN_AUTOMATON) {
            stackAdapter.setDepth(simulationDepth);
        }
        //change tapeDimension
        int tapeDimension = (int) ((getApplicationContext().getResources().getDisplayMetrics().heightPixels * 0.13) /
                Math.min(machineAdapter.getItemCount(), 2));
        machineAdapter.setTapeDimension(tapeDimension);
        if (machineType == MainActivity.PUSHDOWN_AUTOMATON) {
            stackAdapter.setTapeDimension(tapeDimension);
        }
        List<MachineStep> doneMachineStepList = new ArrayList<>();
        List<MachineStep> progressMachineStepList = new ArrayList<>();
        List<MachineStep> stuckMachineStepList = new ArrayList<>();
        //change tapeDimension (from last because there is more of them, then there are shown)
        for (int i = machineAdapter.getItems().size() - 1; i >= 0; i--) {
            MachineStep machineStep = machineAdapter.getItems().get(i);

            ((MachineTapeListAdapter) machineStep.getTape()).setTapeDimension(tapeDimension);
            //when simulation is over, need to change tapeDimensions
            //machineAdapter.notifyItemChanged(machineAdapter.getItems().indexOf(machineStep));
            if (machineType == MainActivity.PUSHDOWN_AUTOMATON) {
                ((StackTapeListAdapter) ((PushdownAutomatonStep) machineStep).getStack()).setTapeDimension(tapeDimension);
                //stackAdapter.notifyItemChanged(machineAdapter.getItems().indexOf(machineStep));
            }
            //add to specific list according to status
            if (machineStep.getMachineStatus() == MachineStep.DONE) {
                doneMachineStepList.add(machineStep);
            } else if (machineStep.getMachineStatus() == MachineStep.PROGRESS) {
                progressMachineStepList.add(machineStep);
            } else {
                stuckMachineStepList.add(machineStep);
            }
        }
        //when simulation is over, need to change tapeDimensions
        machineAdapter.notifyDataSetChanged();
        if (machineType == MainActivity.PUSHDOWN_AUTOMATON) {
            stackAdapter.notifyDataSetChanged();
        }
        //color completed machines
        simulationFlowView.completeColor(doneMachineStepList, progressMachineStepList, stuckMachineStepList);
        //move camera
        simulationFlowView.moveCamera();
    }

    @Override
    public void onTaskDialogClick(Task task, int machineType, int dialogMode) {
        final TaskDialog taskDialog = (TaskDialog) getSupportFragmentManager().findFragmentByTag("TASK_DIALOG");
        if (dialogMode == TaskDialog.EDITING) {
            onBackPressed();
            taskDialog.dismiss();
        } else if (dialogMode == TaskDialog.SOLVING) {
            TaskResultSender resultSender = new TaskResultSender(task, machineType);

            resultSender.setOnFinish(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            taskDialog.unfreeze();
                        }
                    });
                }
            });

            resultSender.setOnFailure(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SimulationActivity.this, R.string.sending_result_error, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });

            resultSender.setOnSuccess(new TaskResultSender.OnSuccessAction() {
                @Override
                public void run(TaskResult result) {
                    String scoreText = "<font color=\"#000000\">Your score: </font>"
                            + "<font color=\"#009900\">" + result.getPositive() + "/" + result.getMaxPositive() + "</font>"
                            + " + "
                            + "<font color=\"red\">" + result.getNegative() + "/" + result.getMaxNegative() + "</font>";
                    Spanned scoreTextSpanned;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        scoreTextSpanned = Html.fromHtml(scoreText, Html.FROM_HTML_MODE_LEGACY);
                    } else {
                        scoreTextSpanned = Html.fromHtml(scoreText);
                    }
                    taskDialog.makeSolved(scoreTextSpanned);
                }
            });

            taskDialog.freeze();
            resultSender.execute();
        } else if (dialogMode == TaskDialog.SOLVED) {
            dataSource.globalDrop();
            TaskDialog.setStatusText(null);
            Intent nextActivityIntent = new Intent(SimulationActivity.this, MainActivity.class);
            nextActivityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(nextActivityIntent);
            taskDialog.dismiss();
        }
    }
}
