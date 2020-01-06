package fiitstu.gulis.cmsimulator.activities;

import android.Manifest;
import android.animation.ArgbEvaluator;
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
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.support.annotation.NonNull;
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
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Time;
import java.util.ArrayList;
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
import fiitstu.gulis.cmsimulator.models.tasks.automata_tasks.FiniteAutomataTask;
import fiitstu.gulis.cmsimulator.models.tasks.automata_tasks.LinearBoundedAutomataTask;
import fiitstu.gulis.cmsimulator.models.tasks.automata_tasks.PushdownAutomataTask;
import fiitstu.gulis.cmsimulator.network.ServerController;
import fiitstu.gulis.cmsimulator.network.TaskResultSender;
import fiitstu.gulis.cmsimulator.network.UrlManager;
import fiitstu.gulis.cmsimulator.util.ProgressWorker;
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
public class ConfigurationActivity extends FragmentActivity
        implements View.OnClickListener, ConfigurationListAdapter.ItemClickCallback,
        DiagramView.ItemClickCallback,
        ConfigurationDialog.ConfigurationDialogListener, SaveMachineDialog.SaveDialogListener,
        TaskDialog.TaskDialogListener {

    private Timer timer;
    private boolean timerRunOut = false;
    private static final int BACKGROUND_CHANGE_LENGTH = 1000;

    //log tag
    private static final String TAG = ConfigurationActivity.class.getName();

    public static Activity activity;
    public static Bundle inputBundle;

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

    private DataSource dataSource;
    private TabHost tabHost;
    private DiagramView diagramView;
    private ImageButton menuB;

    //actual element action
    private int elementAction;
    //elements to edit
    private Symbol inputSymbolEdit;
    private Symbol stackSymbolEdit;
    private State stateEdit;
    private Transition transitionEdit;

    //variables
    private int machineType;
    private String filename;
    private Long emptyInputSymbolId;
    private Long startStackSymbolId;
    private boolean markNondeterminism;
    public static int gameNumber;

    private boolean saveAsDeterministic;

    /**
     * The task that is being solved (or null)
     */
    private Task task;
    /**
     * 0 if the machine is unrelated to a task, MainActivity.EDIT_TASK, or MainActivity.SOLVE_TASK
     */
    private int taskConfiguration;

    //diagram imageButtons
    private ImageButton moveImageButton;
    private ImageButton addStateImageButton;
    private ImageButton addTransitionImageButton;
    private ImageButton editImageButton;
    private ImageButton removeImageButton;
    //to simulate toggle effect - last pressed button
    private ImageButton lastPressedImageButton;

    //adapters in views
    private ConfigurationListAdapter inputAlphabetAdapter;
    private ConfigurationListAdapter stackAlphabetAdapter;
    private ConfigurationListAdapter stateAdapter;
    private ConfigurationListAdapter transitionAdapter;

    private List<TapeElement> tapeElementList;

    //initial state (can be only one)
    private State initialState = null;

    private TestScenarioListAdapter testScenarioListAdapter;

    private int runTests(boolean negative) {
        Log.v(TAG, "run tests method started");

        int successfulTests = 0;
        for (int i = 0; i < testScenarioListAdapter.getItemCount(); i++) {
            final TestScenario testScenario = testScenarioListAdapter.getItem(i);
            final MachineStep machine = task == null
                    ? testScenario.prepareMachine(machineType, DataSource.getInstance())
                    : testScenario.prepareMachine(machineType, DataSource.getInstance(), task.getMaxSteps());
            machine.simulateFull();
            final int index = i;
            final TestScenarioListAdapter.Status status;
            switch (machine.getNondeterministicMachineStatus()) {
                case MachineStep.STUCK:
                    if (testScenario.getOutputWord() != null && machine.getTape().matches(testScenario.getOutputWord())) {
                        status = TestScenarioListAdapter.Status.CORRECT_OUTPUT_REJECTED;
                        if (negative)
                            successfulTests++;
                    } else {
                        status = TestScenarioListAdapter.Status.REJECT;
                        if (negative)
                            successfulTests++;
                    }
                    break;
                case MachineStep.PROGRESS:
                    status = TestScenarioListAdapter.Status.TOOK_TOO_LONG;
                    if (negative)
                        successfulTests++;
                    break;
                case MachineStep.DONE:
                    if (testScenario.getOutputWord() == null
                            || machine.matchTapeNondeterministic(testScenario.getOutputWord())) {
                        status = TestScenarioListAdapter.Status.ACCEPT;
                        if (!negative)
                            successfulTests++;
                    } else {
                        status = TestScenarioListAdapter.Status.INCORRECT_OUTPUT;
                        if (negative)
                            successfulTests++;
                    }
                    break;
                default:
                    status = null;
                    break;
            }
        }

        return successfulTests;
    }

    private void setWindowColor(int color, int color2) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getColor(color2));
            window.setNavigationBarColor(getColor(color2));

            ActionBar actionBar = this.getActionBar();
            actionBar.setBackgroundDrawable(new ColorDrawable(getColor(color)));
        }
    }

    //onCreate method
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        Log.v(TAG, "onCreate initialization started");
        activity = this;

        //menu
        final ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //DataSource initialization
        dataSource = DataSource.getInstance();
        dataSource.open();

        //diagram initialization
        diagramView = findViewById(R.id.diagramView_configuration);
        diagramView.setItemClickCallback(this);

        //get data from bundle
        inputBundle = getIntent().getExtras();

        //set machineType
        machineType = inputBundle.getInt(MainActivity.MACHINE_TYPE);

        //set filename
        filename = inputBundle.getString(MainActivity.FILE_NAME);

        //set empty input symbol id
        emptyInputSymbolId = inputBundle.getLong(SimulationActivity.EMPTY_INPUT_SYMBOL);

        //set empty stack symbol id
        startStackSymbolId = inputBundle.getLong(SimulationActivity.START_STACK_SYMBOL);

        taskConfiguration = inputBundle.getInt(TASK_CONFIGURATION);

        if (taskConfiguration == MainActivity.GAME_MACHINE) {
            gameNumber = inputBundle.getInt(TasksActivity.GAME_EXAMPLE_NUMBER);
            GameShowcase gameShowcase = new GameShowcase();
            gameShowcase.showTutorial(gameNumber, this);
        }
        if (inputBundle.getInt(MainActivity.CONFIGURATION_TYPE) == MainActivity.LOAD_MACHINE) {
            setFilename(machineType);
            FileHandler.Format format = inputBundle.getBoolean(MainActivity.DEFAULT_FORMAT)
                    ? FileHandler.Format.CMS
                    : FileHandler.Format.JFF;

            loadMachine(filename, format);

            filename = filename.substring(filename.lastIndexOf("/") + 1, filename.lastIndexOf("."));
        }


        ////tabHost initialization
        tabHost = findViewById(R.id.tabHost_configuration);
        tabHost.setup();

        //diagram tab initialization
        TabHost.TabSpec tabSpec = tabHost.newTabSpec(getString(R.string.diagram));
        tabSpec.setContent(R.id.linearLayout_configuration_diagram_tab);
        tabSpec.setIndicator(getString(R.string.diagram));
        tabHost.addTab(tabSpec);

        //input and stack alphabet tab initialization
        if (machineType == MainActivity.PUSHDOWN_AUTOMATON) {
            //input alphabet tab initialization
            tabSpec = tabHost.newTabSpec(getString(R.string.input_alphabet));
            tabSpec.setContent(R.id.linearLayout_configuration_form_input_alphabet_tab);
            tabSpec.setIndicator(getString(R.string.input_alphabet));
            tabHost.addTab(tabSpec);

            //stack alphabet tab initialization (pushdown automaton only)
            tabSpec = tabHost.newTabSpec(getString(R.string.stack_alphabet));
            tabSpec.setContent(R.id.linearLayout_configuration_form_stack_alphabet_tab);
            tabSpec.setIndicator(getString(R.string.stack_alphabet));
            tabHost.addTab(tabSpec);
        } else {
            //input alphabet tab initialization
            tabSpec = tabHost.newTabSpec(getString(R.string.alphabet));
            tabSpec.setContent(R.id.linearLayout_configuration_form_input_alphabet_tab);
            tabSpec.setIndicator(getString(R.string.alphabet));
            tabHost.addTab(tabSpec);

            //stack alphabet tab is gone
            LinearLayout stackTab = findViewById(R.id.linearLayout_configuration_form_stack_alphabet_tab);
            stackTab.setVisibility(View.GONE);
        }

        //states tab initialization
        tabSpec = tabHost.newTabSpec(getString(R.string.states));
        tabSpec.setContent(R.id.linearLayout_configuration_form_state_tab);
        tabSpec.setIndicator(getString(R.string.states));
        tabHost.addTab(tabSpec);

        //transitions tab initialization
        tabSpec = tabHost.newTabSpec(getString(R.string.transitions));
        tabSpec.setContent(R.id.linearLayout_configuration_form_transition_tab);
        tabSpec.setIndicator(getString(R.string.transitions));
        tabHost.addTab(tabSpec);

        //build adapters
        //input alphabet
        RecyclerView recyclerView = findViewById(R.id.recyclerView_configuration_form_input_alphabet);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        inputAlphabetAdapter = new ConfigurationListAdapter(this, INPUT_SYMBOL,
                machineType == MainActivity.FINITE_STATE_AUTOMATON || machineType == MainActivity.PUSHDOWN_AUTOMATON);
        recyclerView.setAdapter(inputAlphabetAdapter);
        inputAlphabetAdapter.setItemClickCallback(this);

        //stack alphabet
        if (machineType == MainActivity.PUSHDOWN_AUTOMATON) {
            recyclerView = findViewById(R.id.recyclerView_configuration_form_stack_alphabet);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            stackAlphabetAdapter = new ConfigurationListAdapter(this, STACK_SYMBOL, true);
            recyclerView.setAdapter(stackAlphabetAdapter);
            stackAlphabetAdapter.setItemClickCallback(this);
        }

        //states
        recyclerView = findViewById(R.id.recyclerView_configuration_form_state);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        stateAdapter = new ConfigurationListAdapter(this, STATE, false);
        recyclerView.setAdapter(stateAdapter);
        stateAdapter.setItemClickCallback(this);

        //transition, definition depends on machine type, sets title
        recyclerView = findViewById(R.id.recyclerView_configuration_form_transition);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        switch (machineType) {
            case MainActivity.FINITE_STATE_AUTOMATON:
                actionBar.setTitle(R.string.configure_FSA);
                //title.setText(getString(R.string.configure_FSA));
                transitionAdapter = new ConfigurationListAdapter(this, FSA_TRANSITION, false);
                recyclerView.setAdapter(transitionAdapter);
                transitionAdapter.setItemClickCallback(this);
                break;
            case MainActivity.PUSHDOWN_AUTOMATON:
                actionBar.setTitle(R.string.configure_PDA);
                //title.setText(getString(R.string.configure_PDA));
                transitionAdapter = new ConfigurationListAdapter(this, PDA_TRANSITION, false);
                recyclerView.setAdapter(transitionAdapter);
                transitionAdapter.setItemClickCallback(this);
                break;
            case MainActivity.LINEAR_BOUNDED_AUTOMATON:
                actionBar.setTitle(R.string.configure_LBA);
                //title.setText(getString(R.string.configure_LBA));
                transitionAdapter = new ConfigurationListAdapter(this, TM_TRANSITION, false);
                recyclerView.setAdapter(transitionAdapter);
                transitionAdapter.setItemClickCallback(this);
                break;
            case MainActivity.TURING_MACHINE:
                actionBar.setTitle(R.string.configure_TM);
                //title.setText(getString(R.string.configure_TM));
                transitionAdapter = new ConfigurationListAdapter(this, TM_TRANSITION, false);
                recyclerView.setAdapter(transitionAdapter);
                transitionAdapter.setItemClickCallback(this);
                break;
        }

        //add input alphabet symbol
        Button addInputSymbolButton = findViewById(R.id.button_configuration_form_input_symbol);
        //only pushdown automaton has two alphabets
        if (machineType == MainActivity.PUSHDOWN_AUTOMATON) {
            addInputSymbolButton.setText(getString(R.string.add_input_symbol));
        }
        addInputSymbolButton.setOnClickListener(this);

        //add stack alphabet symbol
        Button addStackSymbolButton = findViewById(R.id.button_configuration_form_stack_symbol);
        addStackSymbolButton.setOnClickListener(this);

        //add state form
        Button addStateButton = findViewById(R.id.button_configuration_form_state);
        addStateButton.setOnClickListener(this);

        //add transition form
        Button addTransitionButton = findViewById(R.id.button_configuration_form_transition);
        addTransitionButton.setOnClickListener(this);

        //state diagram
        moveImageButton = findViewById(R.id.imageButton_configuration_diagram_move);
        moveImageButton.setOnClickListener(this);
        addStateImageButton = findViewById(R.id.imageButton_configuration_diagram_state);
        addStateImageButton.setOnClickListener(this);
        addTransitionImageButton = findViewById(R.id.imageButton_configuration_diagram_transition);
        addTransitionImageButton.setOnClickListener(this);
        editImageButton = findViewById(R.id.imageButton_configuration_diagram_edit);
        editImageButton.setOnClickListener(this);
        removeImageButton = findViewById(R.id.imageButton_configuration_diagram_remove);
        removeImageButton.setOnClickListener(this);
        Log.v(TAG, "configuration buttons initialized");

        if (taskConfiguration == MainActivity.SOLVE_TASK) {
            Time time = Time.valueOf("00:05:03");
            timer = new Timer(time);
            timer.setOnTickListener(new Timer.OnTickListener() {
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
                        final int t_light= getColor(R.color.in_progress_bottom_bar);

                        changeActivityBackgroundColor(s_dark, s_normal, s_light, t_dark, t_normal, t_light);
                    }


                    String timerText = String.format("%02d:%02d:%02d", hours, minutes, seconds);

                    actionBar.setTitle(timerText);
                }
            });
            timer.startTimer();
        }

        dataSource.close();
        Log.i(TAG, "onCreate initialized");
    }

    private void updateStatusBarColor(int s_dark, int t_dark)
    {
        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), s_dark, t_dark);
        animator.setDuration(BACKGROUND_CHANGE_LENGTH);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ConfigurationActivity.this.getWindow().setStatusBarColor((int)animation.getAnimatedValue());
            }
        });
        animator.start();
    }

    private void updateNavigationBarColor(int s_dark, int t_dark)
    {
        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), s_dark, t_dark);
        animator.setDuration(BACKGROUND_CHANGE_LENGTH);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ConfigurationActivity.this.getWindow().setNavigationBarColor((int)animation.getAnimatedValue());
            }
        });
        animator.start();
    }


    private void updateActionBarColor(int s_normal, int t_normal)
    {
        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), s_normal, t_normal);
        animator.setDuration(BACKGROUND_CHANGE_LENGTH);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ConfigurationActivity.this.getActionBar().setBackgroundDrawable(new ColorDrawable((int)animation.getAnimatedValue()));
            }
        });
        animator.start();
    }

    private void updateInnerViewsColor(int s_light, int t_light)
    {
        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), s_light, t_light);
        animator.setDuration(BACKGROUND_CHANGE_LENGTH);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                HorizontalScrollView tabs = findViewById(R.id.tabview_configuration);
                List<ImageButton> imageButtonList = new ArrayList<>();
                imageButtonList.add((ImageButton)findViewById(R.id.imageButton_configuration_diagram_move));
                imageButtonList.add((ImageButton)findViewById(R.id.imageButton_configuration_diagram_state));
                imageButtonList.add((ImageButton)findViewById(R.id.imageButton_configuration_diagram_transition));
                imageButtonList.add((ImageButton)findViewById(R.id.imageButton_configuration_diagram_edit));
                imageButtonList.add((ImageButton)findViewById(R.id.imageButton_configuration_diagram_remove));

                final int currentColorValue = (int)animation.getAnimatedValue();
                tabs.setBackgroundColor(currentColorValue);
                for (ImageButton btn:
                     imageButtonList) {
                    int[][] states = new int[][] {
                            new int[] { 0 }
                    };

                    int[] color = new int[] {
                            currentColorValue
                    };
                    ColorStateList list = new ColorStateList(states, color);
                    if (lastPressedImageButton != btn)
                        btn.setBackgroundTintList(list);

                }
            }
        });
        animator.start();
    }



    private void changeActivityBackgroundColor(int s_dark, int s_normal, int s_light, int t_dark, int t_normal, int t_light)
    {
        updateStatusBarColor(s_dark, t_dark);
        updateActionBarColor(s_normal, t_normal);
        updateNavigationBarColor(s_dark, t_dark);
        updateInnerViewsColor(s_light, t_light);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.menu_configuration, menu);

        return true;
    }

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
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (taskConfiguration != 0) {
            task = (Task) inputBundle.getSerializable(MainActivity.TASK);
            MenuItem saveButton = menu.getItem(0);
            MenuItem submitTaskButton = menu.getItem(1);
            //MenuItem taskInfoButton = findViewById(R.id.menu_configuration_task_info);

            if (taskConfiguration == MainActivity.SOLVE_TASK) {
                saveButton.setVisible(true);
                submitTaskButton.setVisible(true);
            }
            if (taskConfiguration == MainActivity.EDIT_TASK || task.getPublicInputs()) {
                menu.findItem(R.id.menu_configuration_bulk_test).setTitle(R.string.correct_inputs);
                menu.findItem(R.id.menu_configuration_negative_test).setVisible(true);
            } else {
                menu.findItem(R.id.menu_configuration_bulk_test).setVisible(false);
            }
        }
        if (machineType != MainActivity.FINITE_STATE_AUTOMATON) {
            menu.findItem(R.id.menu_configuration_convert).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_configuration_simulate:
                Intent nextActivityIntent = new Intent(this, SimulationActivity.class);
                nextActivityIntent.putExtra(MainActivity.CONFIGURATION_TYPE, MainActivity.SOLVE_TASK);
                if (task instanceof FiniteAutomataTask)
                    nextActivityIntent.putExtra(MainActivity.MACHINE_TYPE, MainActivity.FINITE_STATE_AUTOMATON);
                else if (task instanceof PushdownAutomataTask)
                    nextActivityIntent.putExtra(MainActivity.MACHINE_TYPE, MainActivity.PUSHDOWN_AUTOMATON);
                else if (task instanceof LinearBoundedAutomataTask)
                    nextActivityIntent.putExtra(MainActivity.MACHINE_TYPE, MainActivity.LINEAR_BOUNDED_AUTOMATON);
                else nextActivityIntent.putExtra(MainActivity.MACHINE_TYPE, MainActivity.TURING_MACHINE);
                nextActivityIntent.putExtra(MainActivity.TASK, task);
                nextActivityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(nextActivityIntent);
                Log.i(TAG, "simulation activity intent executed");
                return true;
            case R.id.menu_configuration_save_machine:
                if (Build.VERSION.SDK_INT > 15
                        && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MainActivity.REQUEST_WRITE_STORAGE);
                } else {
                    showSaveMachineDialog();
                }
                return true;
            case R.id.menu_save_task:
                // COMPLETED: SAVE TASK TO CLOUD
                final String file_name = Integer.toString(task.getTask_id()) + "." + FileHandler.Format.CMST.toString().toLowerCase();
                final int user_id = BrowseAutomataTasksActivity.user_id;
                class SaveTaskToCloudAsync extends AsyncTask<File, Void, String> {
                    @Override
                    protected String doInBackground(File... files) {
                        UrlManager urlManager = new UrlManager();
                        ServerController serverController = new ServerController();
                        URL pushToCloudURL = urlManager.getSaveTaskURL(file_name, user_id);

                        String output = null;
                        try {
                            output = serverController.doPostRequest(pushToCloudURL, files[0]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            return output;
                        }
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        if (s.equalsIgnoreCase("OK"))
                            Toast.makeText(ConfigurationActivity.this, R.string.save_complete, Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(ConfigurationActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                    }
                }

                FileHandler fileHandler = new FileHandler(FileHandler.Format.CMST);

                File file = null;
                try {
                    DataSource dataSource = DataSource.getInstance();
                    dataSource.open();
                    fileHandler.setData(dataSource, machineType);
                    task.setResultVersion(TaskResult.CURRENT_VERSION);
                    fileHandler.writeTask(task);

                    String taskDoc = fileHandler.writeToString();

                    file = new File(this.getFilesDir(), task.getTitle() + ".cmst");
                    FileOutputStream outputStream;

                    outputStream = openFileOutput(task.getTitle() + ".cmst", Context.MODE_PRIVATE);
                    outputStream.write(taskDoc.getBytes());
                    outputStream.close();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (TransformerException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                new SaveTaskToCloudAsync().execute(file);
                return true;
            case R.id.menu_submit_task:

                testScenarioListAdapter = new TestScenarioListAdapter(this, false);
                List<TestScenario> tests = dataSource.getTestFullExtract(false, DataSource.getInstance().getInputAlphabetFullExtract());
                testScenarioListAdapter.setItems(tests);

                int positiveTestCount = tests.size();
                int positiveTestSuccessful = runTests(false);

                tests = dataSource.getTestFullExtract(true, DataSource.getInstance().getInputAlphabetFullExtract());
                testScenarioListAdapter.setItems(tests);

                int negativeTestCount = tests.size();
                int negativeTestSuccessful = runTests(true);

                final Task.TASK_STATUS submittedStatus;
                if (positiveTestCount == positiveTestSuccessful && negativeTestCount == negativeTestSuccessful)
                    submittedStatus = Task.TASK_STATUS.CORRECT;
                else
                    submittedStatus = Task.TASK_STATUS.WRONG;

                final File filesDir = this.getFilesDir();

                SubmitTaskDialog submitTaskDialog = new SubmitTaskDialog(positiveTestCount, positiveTestSuccessful, negativeTestCount, negativeTestSuccessful);
                submitTaskDialog.setOnClickListener(new SubmitTaskDialog.SubmitTaskDialogListener() {
                    @Override
                    public void submitTaskDialogClick() {

                        final String file_name = Integer.toString(task.getTask_id()) + "." + FileHandler.Format.CMST.toString().toLowerCase();
                        final int user_id = BrowseAutomataTasksActivity.user_id;
                        class SaveTaskToCloudAsync extends AsyncTask<File, Void, String> {
                            @Override
                            protected String doInBackground(File... files) {
                                UrlManager urlManager = new UrlManager();
                                ServerController serverController = new ServerController();
                                URL pushToCloudURL = urlManager.getSaveTaskURL(file_name, user_id);

                                String output = null;
                                try {
                                    output = serverController.doPostRequest(pushToCloudURL, files[0]);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    return output;
                                }
                            }
                        }

                        FileHandler fileHandler = new FileHandler(FileHandler.Format.CMST);

                        File file = null;
                        try {
                            DataSource dataSource = DataSource.getInstance();
                            dataSource.open();
                            fileHandler.setData(dataSource, machineType);
                            task.setResultVersion(TaskResult.CURRENT_VERSION);
                            fileHandler.writeTask(task);

                            String taskDoc = fileHandler.writeToString();

                            file = new File(filesDir, task.getTitle() + ".cmst");
                            FileOutputStream outputStream;

                            outputStream = openFileOutput(task.getTitle() + ".cmst", Context.MODE_PRIVATE);
                            outputStream.write(taskDoc.getBytes());
                            outputStream.close();
                        } catch (ParserConfigurationException e) {
                            e.printStackTrace();
                        } catch (TransformerException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        new SaveTaskToCloudAsync().execute(file);

                        final int task_id = task.getTask_id();

                        class SubmitTaskAsync extends AsyncTask<Void, Void, String> {
                            @Override
                            protected String doInBackground(Void... voids) {
                                UrlManager urlManager = new UrlManager();
                                ServerController serverController = new ServerController();
                                URL url = urlManager.getSubmitAutomataTaskUrl(user_id, task_id, submittedStatus);

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

                                AutomataTaskAdapter adapter = BrowseAutomataTasksActivity.adapter;
                                adapter.notifyStatusChange(task_id, submittedStatus);

                                if (s == null || s.isEmpty()) {
                                    Toast.makeText(ConfigurationActivity.this, R.string.generic_error, Toast.LENGTH_LONG).show();
                                } else {
                                    try {
                                        JSONObject object = new JSONObject(s);
                                        if (object.getBoolean("submitted")) {
                                            Toast.makeText(ConfigurationActivity.this, R.string.submit_successful, Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(ConfigurationActivity.this, R.string.generic_error, Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        Toast.makeText(ConfigurationActivity.this, R.string.generic_error, Toast.LENGTH_LONG).show();
                                    }
                                }

                                ConfigurationActivity.this.finish();
                                SimulationActivity.mContext.finish();
                            }
                        }

                        new SubmitTaskAsync().execute();
                    }
                });
                submitTaskDialog.show(this.getSupportFragmentManager(), "HELLO");
                return true;
            case R.id.menu_configuration_convert:
                if (initialState == null) {
                    Toast.makeText(this, R.string.convert_error_no_initial_state, Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                    dialogBuilder.setTitle(R.string.convert_to_deterministic);
                    dialogBuilder.setMessage(R.string.convert_to_deterministic_dialog_text);
                    dialogBuilder.setInverseBackgroundForced(true);
                    dialogBuilder.setPositiveButton(R.string.convert_to_deterministic_dialog_convert,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    ProgressWorker worker = new ProgressWorker(500, findViewById(R.id.relativeLayout_configuration_working),
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    makeDeterministicMachine();
                                                }
                                            });
                                    worker.setPostAction(new Runnable() {
                                        @Override
                                        public void run() {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                }
                                            });
                                        }
                                    });
                                    worker.execute();
                                }
                            });
                    dialogBuilder.setNeutralButton(R.string.convert_to_deterministic_dialog_create_file,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    saveAsDeterministic = true;
                                    showSaveMachineDialog();
                                }
                            });
                    dialogBuilder.setNegativeButton(R.string.cancel, null);
                    dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            saveAsDeterministic = false;
                        }
                    });

                    dialogBuilder.create().show();
                }
                return true;
            case R.id.menu_configuration_specification:
                FragmentManager fm = getSupportFragmentManager();
                FormalSpecDialog formalSpecDialog = FormalSpecDialog.newInstance(machineType,
                        inputAlphabetAdapter == null ? null : inputAlphabetAdapter.getItems(),
                        stackAlphabetAdapter == null ? null : stackAlphabetAdapter.getItems(),
                        stateAdapter == null ? null : stateAdapter.getItems(),
                        transitionAdapter == null ? null : transitionAdapter.getItems());
                formalSpecDialog.show(fm, SimulationActivity.FORMAL_SPEC_DIALOG);
                return true;
            case R.id.menu_configuration_settings:
                for (Object object : stateAdapter.getItems()) {
                    State state = (State) object;
                    dataSource.updateState(state, state.getValue(), state.getPositionX(), state.getPositionY(), state.isInitialState(), state.isFinalState());
                }
                dataSource.close();
                nextActivityIntent = new Intent(this, OptionsActivity.class);
                startActivity(nextActivityIntent);
                Log.i(TAG, "options activity intent executed");
                return true;
            case R.id.menu_configuration_bulk_test:
            case R.id.menu_configuration_negative_test:
                for (Object object : stateAdapter.getItems()) {
                    State state = (State) object;
                    dataSource.updateState(state, state.getValue(), state.getPositionX(), state.getPositionY(), state.isInitialState(), state.isFinalState());
                }
                dataSource.close();

                Bundle outputBundle = new Bundle();
                outputBundle.putInt(MainActivity.MACHINE_TYPE, machineType);
                outputBundle.putString(MainActivity.FILE_NAME, filename);
                outputBundle.putInt(BulkTestActivity.TASK_CONFIGURATION, taskConfiguration);
                outputBundle.putSerializable(MainActivity.TASK, task);
                if (item.getItemId() == R.id.menu_configuration_negative_test) {
                    outputBundle.putBoolean(BulkTestActivity.NEGATIVE, true);
                }
                Log.v(TAG, "outputBundle initialized");

                nextActivityIntent = new Intent(this, BulkTestActivity.class);
                nextActivityIntent.putExtras(outputBundle);
                startActivity(nextActivityIntent);
                Log.i(TAG, "bulk test activity intent executed");
                return true;
            case R.id.menu_configuration_help:
                fm = getSupportFragmentManager();
                GuideFragment guideFragment = GuideFragment.newInstance(GuideFragment.CONFIGURATION);
                guideFragment.show(fm, HELP_DIALOG);
                return true;
        }

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume method started");

        //update lists
        dataSource.open();

        //get options from database
        markNondeterminism = dataSource.getMarkNondeterminism();

        List<Symbol> inputAlphabetList = dataSource.getInputAlphabetFullExtract();
        for (Symbol symbol : inputAlphabetList) {
            if (symbol.getId() == emptyInputSymbolId) {
                Log.v(TAG, "empty symbol found at position " + inputAlphabetList.indexOf(symbol));
                inputAlphabetList.remove(symbol);
                inputAlphabetList.add(0, symbol);
                break;
            }
        }
        inputAlphabetAdapter.setItems(inputAlphabetList);

        List<State> stateList = dataSource.getStateFullExtract();
        stateAdapter.setItems(stateList);
        for (State state : stateList) {
            if (state.isInitialState()) {
                initialState = state;
            }
        }

        List<Transition> transitionList = null;

        switch (machineType) {
            case MainActivity.FINITE_STATE_AUTOMATON:
                transitionList = dataSource.getFsaTransitionFullExtract(inputAlphabetList, stateList);
                break;
            case MainActivity.PUSHDOWN_AUTOMATON:
                List<Symbol> stackAlphabetList = dataSource.getStackAlphabetFullExtract();
                for (Symbol symbol : stackAlphabetList) {
                    if (symbol.getId() == startStackSymbolId) {
                        Log.v(TAG, "start stack found at position " + stackAlphabetList.indexOf(symbol));
                        stackAlphabetList.remove(symbol);
                        stackAlphabetList.add(0, symbol);
                        break;
                    }
                }
                stackAlphabetList.add(0, inputAlphabetList.get(0));
                stackAlphabetAdapter.setItems(stackAlphabetList);
                transitionList = dataSource.getPdaTransitionFullExtract(inputAlphabetList, stackAlphabetList, stateList);
                break;
            case MainActivity.LINEAR_BOUNDED_AUTOMATON:
            case MainActivity.TURING_MACHINE:
                transitionList = dataSource.getTmTransitionFullExtract(inputAlphabetList, stateList);
                break;
        }

        tapeElementList = dataSource.getTapeFullExtract(inputAlphabetList);

        transitionAdapter.setItems(transitionList);

        diagramView.buildDiagram(markNondeterminism, emptyInputSymbolId, stateList, transitionList);
        //default click in diagram
        if (Build.VERSION.SDK_INT < 15) {
            moveImageButton.performClick();
        } else {
            moveImageButton.callOnClick();
        }
    }

    @Override
    public void onClick(View view) {
            int[][] states = new int[][] {
                    new int[] { 0 }
            };

            int[] color = new int[] {
                    getColor(R.color.in_progress_bottom_bar)
            };
            ColorStateList list = new ColorStateList(states, color);
        switch (view.getId()) {
            //add input alphabet symbol
            case R.id.button_configuration_form_input_symbol:
                Log.v(TAG, "addInputSymbolButton click noted");
                inputSymbolEdit = null;
                showConfigurationDialog(INPUT_SYMBOL, NEW);
                break;
            //add stack alphabet symbol
            case R.id.button_configuration_form_stack_symbol:
                Log.v(TAG, "addStackSymbolButton click noted");
                stackSymbolEdit = null;
                showConfigurationDialog(STACK_SYMBOL, NEW);
                break;
            //add state
            case R.id.button_configuration_form_state:
                Log.v(TAG, "addStateButton click noted");
                stateEdit = null;
                //resetCounter position to create in diagram
                diagramView.setDefaultNewStatePosition(this);
                showConfigurationDialog(STATE, NEW);
                break;
            //add transition
            case R.id.button_configuration_form_transition:
                Log.v(TAG, "addTransitionButton click noted");

                //if not a single state exist, dialog window will not be created
                if (stateAdapter.getItemCount() == 0) {
                    Toast.makeText(this, R.string.need_state, Toast.LENGTH_SHORT).show();
                } else {
                    transitionEdit = null;
                    //different type of transition dialog window for different types of machines
                    switch (machineType) {
                        case MainActivity.FINITE_STATE_AUTOMATON:
                            showConfigurationDialog(FSA_TRANSITION, NEW);
                            break;
                        case MainActivity.PUSHDOWN_AUTOMATON:
                            showConfigurationDialog(PDA_TRANSITION, NEW);
                            break;
                        case MainActivity.LINEAR_BOUNDED_AUTOMATON:
                        case MainActivity.TURING_MACHINE:
                            showConfigurationDialog(TM_TRANSITION, NEW);
                            break;
                    }
                }

                break;
            ////diagram buttons
            //move
            case R.id.imageButton_configuration_diagram_move:
                if (lastPressedImageButton != null) {
                    lastPressedImageButton.getBackground().clearColorFilter();
                }
                lastPressedImageButton = moveImageButton;
                lastPressedImageButton.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.toggle_color), PorterDuff.Mode.MULTIPLY);
                if (timerRunOut)
                    moveImageButton.setBackgroundTintList(list);
                diagramView.setAction(DiagramView.MOVE);
                break;
            //add state
            case R.id.imageButton_configuration_diagram_state:
                if (lastPressedImageButton != null) {
                    lastPressedImageButton.getBackground().clearColorFilter();
                }
                lastPressedImageButton = addStateImageButton;
                lastPressedImageButton.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.toggle_color), PorterDuff.Mode.MULTIPLY);
                if (timerRunOut)
                    moveImageButton.setBackgroundTintList(list);
                diagramView.setAction(DiagramView.ADD_STATE);
                break;
            //add transition
            case R.id.imageButton_configuration_diagram_transition:
                if (lastPressedImageButton != null) {
                    lastPressedImageButton.getBackground().clearColorFilter();
                }
                lastPressedImageButton = addTransitionImageButton;
                lastPressedImageButton.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.toggle_color), PorterDuff.Mode.MULTIPLY);
                if (timerRunOut)
                    moveImageButton.setBackgroundTintList(list);
                diagramView.setAction(DiagramView.ADD_TRANSITION);
                break;
            //edit state or transition
            case R.id.imageButton_configuration_diagram_edit:
                if (lastPressedImageButton != null) {
                    lastPressedImageButton.getBackground().clearColorFilter();
                }
                lastPressedImageButton = editImageButton;
                lastPressedImageButton.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.toggle_color), PorterDuff.Mode.MULTIPLY);
                if (timerRunOut)
                    moveImageButton.setBackgroundTintList(list);
                diagramView.setAction(DiagramView.EDIT);
                break;
            //remove state or transition
            case R.id.imageButton_configuration_diagram_remove:
                if (lastPressedImageButton != null) {
                    lastPressedImageButton.getBackground().clearColorFilter();
                }
                lastPressedImageButton = removeImageButton;
                lastPressedImageButton.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.toggle_color), PorterDuff.Mode.MULTIPLY);
                if (timerRunOut)
                    moveImageButton.setBackgroundTintList(list);
                diagramView.setAction(DiagramView.REMOVE);
                break;
        }
    }

    //handle leaving from configuration activity
    //update states to save their position
    @Override
    public void onBackPressed() {
        Log.v(TAG, "onBackPressed method started");
        if (inputBundle.getInt(MainActivity.CONFIGURATION_TYPE) == MainActivity.LOAD_MACHINE && inputBundle.getInt(TASK_CONFIGURATION) == MainActivity.SOLVE_TASK) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.task_leave_message)
                    .setTitle(R.string.task_leave_title)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dataSource.globalDrop();
                            dataSource.close();
                            ConfigurationActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .create();

            dialog.show();
            return;
        }
        for (Object object : stateAdapter.getItems()) {
            State state = (State) object;
            dataSource.updateState(state, state.getValue(), state.getPositionX(), state.getPositionY(), state.isInitialState(), state.isFinalState());
        }
        /*int config = inputBundle.getInt(MainActivity.CONFIGURATION_TYPE);
        if (config != MainActivity.EDIT_TASK)
            dataSource.globalDrop();*/
        dataSource.close();
        finish();
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MainActivity.REQUEST_WRITE_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showSaveMachineDialog();
                }
            }
        }
    }

    private void saveMachine(final String filename, FileHandler.Format format) {
        this.filename = filename;
        try {
            for (Object object : stateAdapter.getItems()) {
                State state = (State) object;
                dataSource.updateState(state, state.getValue(), state.getPositionX(), state.getPositionY(), state.isInitialState(), state.isFinalState());
            }
            FileHandler fileHandler = new FileHandler(format);

            if (saveAsDeterministic) {
                Pair<List<State>, List<Transition>> deterministicMachine =
                        FiniteStateAutomatonStep.createDeterministic(getStateList(), transitionAdapter.getItems(),
                                diagramView.getDimensionY(), diagramView.getDimensionX(), diagramView.getNodeRadius());
                fileHandler.setData(deterministicMachine.first, getInputAlphabetList(), null,
                        deterministicMachine.second, tapeElementList, dataSource.getTestFullExtract(false, getInputAlphabetList()),
                        dataSource.getTestFullExtract(true, getInputAlphabetList()), machineType);
                saveAsDeterministic = false;
            } else {
                if (taskConfiguration != MainActivity.SOLVE_TASK || task.getPublicInputs()) {
                    fileHandler.setData(dataSource, machineType);
                } else {
                    List<Transition> transitions;
                    switch (machineType) {
                        case MainActivity.FINITE_STATE_AUTOMATON:
                            transitions = dataSource.getFsaTransitionFullExtract(getInputAlphabetList(), getStateList());
                            break;
                        case MainActivity.PUSHDOWN_AUTOMATON:
                            transitions = dataSource.getPdaTransitionFullExtract(getInputAlphabetList(), getStackAlphabetList(), getStateList());
                            break;
                        default:
                            transitions = dataSource.getTmTransitionFullExtract(getInputAlphabetList(), getStateList());
                            break;
                    }
                    List<Symbol> stackAlphabet = machineType == MainActivity.PUSHDOWN_AUTOMATON
                            ? getStackAlphabetList()
                            : null;
                    fileHandler.setData(getStateList(), getInputAlphabetList(), stackAlphabet,
                            transitions, tapeElementList, new ArrayList<TestScenario>(),
                            new ArrayList<TestScenario>(), machineType);
                }
            }

            if (task != null) {
                fileHandler.writeTask(task);
            }

            fileHandler.writeFile(filename);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ConfigurationActivity.this,
                            FileHandler.PATH + "/" + filename + " " + getResources().getString(R.string.save_succ),
                            Toast.LENGTH_SHORT).show();
                    SaveMachineDialog saveMachineDialog = (SaveMachineDialog) getSupportFragmentManager()
                            .findFragmentByTag(SimulationActivity.SAVE_DIALOG);
                    if (saveMachineDialog != null) {
                        saveMachineDialog.dismiss();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "File was not saved", e);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ConfigurationActivity.this,
                            getResources().getString(R.string.file_not_saved), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void saveDialogClick(final String filename, final FileHandler.Format format, boolean exit) {
        ProgressWorker worker = new ProgressWorker(500, findViewById(R.id.relativeLayout_configuration_working),
                new Runnable() {
                    @Override
                    public void run() {
                        saveMachine(filename, format);
                    }
                }
        );
        worker.setPostAction(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }
                });
            }
        });
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        worker.execute();
    }

    private void showSaveMachineDialog() {
        FragmentManager fm = getSupportFragmentManager();
        SaveMachineDialog saveMachineDialog = SaveMachineDialog.newInstance(filename, null, false);
        saveMachineDialog.show(fm, SimulationActivity.SAVE_DIALOG);
    }

    @Override
    public void inputSymbolConfigurationDialogClick(String value) {
        Log.v(TAG, "saveInputSymbol button click noted");
        //empty field check
        if (!"".equals(value)) {
            try {
                ConfigurationDialog configurationDialog = (ConfigurationDialog) getSupportFragmentManager()
                        .findFragmentByTag(CONFIGURATION_DIALOG);
                ConfigurationDialog supportDialog = (ConfigurationDialog) getSupportFragmentManager()
                        .findFragmentByTag(SUPPORT_CONFIGURATION_DIALOG);
                if (supportDialog != null) {
                    Symbol newSymbol = dataSource.addInputSymbol(value, 0);
                    inputAlphabetAdapter.addItem(newSymbol);
                    Log.i(TAG, "newInputSymbol '" + newSymbol.getValue() + "' created");
                    configurationDialog.addAndSelectInputSymbol(newSymbol);
                    supportDialog.dismiss();
                } else {
                    switch (elementAction) {
                        case NEW:
                            Symbol newSymbol = dataSource.addInputSymbol(value, 0);
                            inputAlphabetAdapter.addItem(newSymbol);
                            Log.i(TAG, "newInputSymbol '" + newSymbol.getValue() + "' created");
                            break;
                        case UPDATE:
                            dataSource.updateInputSymbol(inputSymbolEdit,
                                    value);
                            if (machineType == MainActivity.FINITE_STATE_AUTOMATON || machineType == MainActivity.PUSHDOWN_AUTOMATON) {
                                inputAlphabetAdapter.notifyItemChanged(inputAlphabetAdapter.getItems().indexOf(inputSymbolEdit) - 1);
                            } else {
                                inputAlphabetAdapter.notifyItemChanged(inputAlphabetAdapter.getItems().indexOf(inputSymbolEdit));
                            }
                            transitionAdapter.notifyDataSetChanged();
                            diagramView.invalidate();
                            Log.i(TAG, "editInputSymbol '" + inputSymbolEdit.getValue() + "' edited");
                            break;
                    }
                    if (configurationDialog != null) {
                        configurationDialog.dismiss();
                    }
                }
            } catch (SQLiteConstraintException e) {
                Toast.makeText(this, R.string.symbol_duplicity, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "input symbol '" + value + "' already exists", e);
            } catch (Exception e) {
                Toast.makeText(this, R.string.symbol_save_error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "unknown error occurred while saving input symbol '" + value + "'", e);
            }
        } else {
            Toast.makeText(this, R.string.symbol_missing, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "empty value!");
        }
    }

    @Override
    public void stackSymbolConfigurationDialogClick(String value) {
        Log.v(TAG, "saveStackSymbol button click noted");
        //empty field check
        if (!"".equals(value)) {
            try {
                ConfigurationDialog configurationDialog = (ConfigurationDialog) getSupportFragmentManager()
                        .findFragmentByTag(CONFIGURATION_DIALOG);
                ConfigurationDialog supportDialog = (ConfigurationDialog) getSupportFragmentManager()
                        .findFragmentByTag(SUPPORT_CONFIGURATION_DIALOG);
                if (supportDialog != null) {
                    Symbol newSymbol = dataSource.addStackSymbol(value, 0);
                    stackAlphabetAdapter.addItem(newSymbol);
                    Log.i(TAG, "newStackSymbol '" + newSymbol.getValue() + "' created");
                    configurationDialog.addAndSelectStackSymbol(newSymbol);
                    supportDialog.dismiss();
                } else {
                    switch (elementAction) {
                        case NEW:
                            Symbol newSymbol = dataSource.addStackSymbol(value, 0);
                            stackAlphabetAdapter.addItem(newSymbol);
                            Log.i(TAG, "newStackSymbol '" + newSymbol.getValue() + "' created");
                            break;
                        case UPDATE:
                            dataSource.updateStackSymbol(stackSymbolEdit,
                                    value);
                            //-1 because 0 is empty symbol
                            stackAlphabetAdapter.notifyItemChanged(stackAlphabetAdapter.getItems().indexOf(stackSymbolEdit) - 1);
                            transitionAdapter.notifyDataSetChanged();
                            diagramView.invalidate();
                            Log.i(TAG, "editStackSymbol '" + stackSymbolEdit.getValue() + "' edited");
                            break;
                    }

                    if (configurationDialog != null) {
                        configurationDialog.dismiss();
                    }
                }
            } catch (SQLiteConstraintException e) {
                Toast.makeText(this, R.string.symbol_duplicity, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "stack symbol '" + value + "' already exists", e);
            } catch (Exception e) {
                Toast.makeText(this, R.string.symbol_save_error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "unknown error occurred while saving stack symbol '" + value + "'", e);
            }
        } else {
            Toast.makeText(this, R.string.symbol_missing, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "empty value!");
        }
    }

    @Override
    public void stateConfigurationDialogClick(String value, boolean initialState, boolean finalState) {
        Log.v(TAG, "saveState button click noted");
        //empty textfield check
        if (!"".equals(value)) {
            //check if is possible to create due to initial state situation
            if ((initialState && this.initialState != null && (stateEdit == null || !stateEdit.isInitialState()))) {
                Toast.makeText(this, R.string.error_initial_state, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "initialState already exists");
            } else {
                try {
                    ConfigurationDialog configurationDialog = (ConfigurationDialog) getSupportFragmentManager()
                            .findFragmentByTag(CONFIGURATION_DIALOG);
                    ConfigurationDialog supportDialog = (ConfigurationDialog) getSupportFragmentManager()
                            .findFragmentByTag(SUPPORT_CONFIGURATION_DIALOG);
                    if (supportDialog != null) {
                        State newState = dataSource.addState(value,
                                diagramView.getNewStatePositionX(), diagramView.getNewStatePositionY(),
                                initialState,
                                finalState);

                        //set existence of initial state if necessary
                        if (newState.isInitialState()) {
                            this.initialState = newState;
                        }

                        stateAdapter.addItem(newState);
                        diagramView.addState(newState);
                        Log.i(TAG, "newState '" + newState.getValue() + "' created");
                        configurationDialog.addAndSelectState(newState);
                        supportDialog.dismiss();
                    } else {
                        switch (elementAction) {
                            case NEW:
                                State newState = dataSource.addState(value,
                                        diagramView.getNewStatePositionX(), diagramView.getNewStatePositionY(),
                                        initialState,
                                        finalState);

                                //set existence of initial state if necessary
                                if (newState.isInitialState()) {
                                    this.initialState = newState;
                                }

                                stateAdapter.addItem(newState);
                                diagramView.addState(newState);
                                Log.i(TAG, "newState '" + newState.getValue() + "' created");
                                break;
                            case UPDATE:
                                if (stateEdit != null) {
                                    boolean oldInitialState = stateEdit.isInitialState();
                                    dataSource.updateState(stateEdit,
                                            value,
                                            stateEdit.getPositionX(), stateEdit.getPositionY(), //position will not change
                                            initialState,
                                            finalState);

                                    //set existence of initial state if necessary
                                    if (oldInitialState && !stateEdit.isInitialState()) {
                                        this.initialState = null;
                                    }
                                    if (stateEdit.isInitialState()) {
                                        this.initialState = stateEdit;
                                    }

                                    stateAdapter.notifyItemChanged(stateAdapter.getItems().indexOf(stateEdit));
                                    transitionAdapter.notifyDataSetChanged();
                                    diagramView.invalidate();
                                    Log.i(TAG, "editState '" + stateEdit.getValue() + "' edited");
                                }
                                stateEdit = null; //need to reset because of the if
                                break;
                        }

                        if (configurationDialog != null) {
                            configurationDialog.dismiss();
                        }
                    }
                } catch (SQLiteConstraintException e) {
                    Toast.makeText(this, R.string.state_duplicity, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "state '" + value + "' already exists", e);
                } catch (Exception e) {
                    Toast.makeText(this, R.string.state_save_error, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "unknown error occurred while saving state '" + value + "'", e);
                }
            }
        } else {
            Toast.makeText(this, R.string.state_missing, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "empty value!");
        }
    }

    @Override
    public void fsaTransitionConfigurationDialogClick(State fromState, Symbol readSymbol, State toState) {
        Log.v(TAG, "saveFSATransition button click noted");
        try {
            switch (elementAction) {
                case NEW:
                    Transition newTransition = dataSource.addFsaTransition(
                            fromState,
                            readSymbol,
                            toState,
                            emptyInputSymbolId);
                    transitionAdapter.addItem(newTransition);
                    diagramView.addTransition(newTransition);
                    Log.i(TAG, "newFSATransition '" + newTransition.getDesc() + "' created");
                    break;
                case UPDATE:
                    long oldFromStateId = transitionEdit.getFromState().getId();
                    long oldToStateId = transitionEdit.getToState().getId();
                    dataSource.updateFsaTransition((FsaTransition) transitionEdit,
                            fromState,
                            readSymbol,
                            toState,
                            emptyInputSymbolId);
                    transitionAdapter.notifyItemChanged(transitionAdapter.getItems().indexOf(transitionEdit));
                    diagramView.changeTransition(transitionEdit, oldFromStateId, oldToStateId);
                    Log.i(TAG, "editFSATransition '" + transitionEdit.getDesc() + "' edited");
                    break;
            }
            ConfigurationDialog configurationDialog = (ConfigurationDialog) getSupportFragmentManager()
                    .findFragmentByTag(CONFIGURATION_DIALOG);
            if (configurationDialog != null) {
                configurationDialog.dismiss();
            }
        } catch (SQLiteConstraintException e) {
            Toast.makeText(this, R.string.transition_duplicity, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "FSATransition '" + fromState.getValue() + ", " +
                    readSymbol.getValue() + ", " +
                    toState.getValue() + "' already exists", e);
        } catch (Exception e) {
            Toast.makeText(this, R.string.transition_save_error, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "unknown error occurred while saving transition", e);
        }
    }

    @Override
    public void pdaTransitionConfigurationDialogClick(State fromState, Symbol readSymbol, State toState,
                                                      List<Symbol> popSymbolList, List<Symbol> pushSymbolList) {
        Log.v(TAG, "savePDATransition button click noted");
        try {
            switch (elementAction) {
                case NEW:
                    Transition newTransition = dataSource.addPdaTransition(
                            fromState,
                            readSymbol,
                            toState,
                            emptyInputSymbolId,
                            popSymbolList,
                            pushSymbolList);
                    transitionAdapter.addItem(newTransition);
                    diagramView.addTransition(newTransition);
                    Log.i(TAG, "newPDATransition '" + newTransition.getDesc() + "' created");
                    break;
                case UPDATE:
                    long oldFromStateId = transitionEdit.getFromState().getId();
                    long oldToStateId = transitionEdit.getToState().getId();
                    dataSource.updatePdaTransition((PdaTransition) transitionEdit,
                            fromState,
                            readSymbol,
                            toState,
                            emptyInputSymbolId,
                            popSymbolList,
                            pushSymbolList);
                    transitionAdapter.notifyItemChanged(transitionAdapter.getItems().indexOf(transitionEdit));
                    diagramView.changeTransition(transitionEdit, oldFromStateId, oldToStateId);
                    Log.i(TAG, "editPDATransition '" + transitionEdit.getDesc() + "' edited");
                    break;
            }
            ConfigurationDialog configurationDialog = (ConfigurationDialog) getSupportFragmentManager()
                    .findFragmentByTag(CONFIGURATION_DIALOG);
            if (configurationDialog != null) {
                configurationDialog.dismiss();
            }
        } catch (SQLiteConstraintException e) {
            Toast.makeText(this, R.string.transition_duplicity, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "PDATransition '" + fromState.getValue() + ", " +
                    readSymbol.getValue() + ", " +
                    toState.getValue() + ", POP, PUSH' already exists", e);
        } catch (Exception e) {
            Toast.makeText(this, R.string.transition_save_error, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "unknown error occurred while saving transition", e);
        }
    }

    @Override
    public void tmTransitionConfigurationDialogClick(State fromState, Symbol readSymbol, State toState,
                                                     Symbol writeSymbol, TmTransition.Direction direction) {
        Log.v(TAG, "saveTMTransition button click noted");
        try {
            switch (elementAction) {
                case NEW:
                    Transition newTransition = dataSource.addTmTransition(
                            fromState,
                            readSymbol,
                            toState,
                            writeSymbol,
                            direction);
                    transitionAdapter.addItem(newTransition);
                    diagramView.addTransition(newTransition);
                    Log.i(TAG, "newTMTransition '" + newTransition.getDesc() + "' created");
                    break;
                case UPDATE:
                    long oldFromStateId = transitionEdit.getFromState().getId();
                    long oldToStateId = transitionEdit.getToState().getId();
                    dataSource.updateTmTransition((TmTransition) transitionEdit,
                            fromState,
                            readSymbol,
                            toState,
                            writeSymbol,
                            direction);
                    transitionAdapter.notifyItemChanged(transitionAdapter.getItems().indexOf(transitionEdit));
                    diagramView.changeTransition(transitionEdit, oldFromStateId, oldToStateId);
                    Log.i(TAG, "editTMTransition '" + transitionEdit.getDesc() + "' edited");
                    break;
            }
            ConfigurationDialog configurationDialog = (ConfigurationDialog) getSupportFragmentManager()
                    .findFragmentByTag(CONFIGURATION_DIALOG);
            if (configurationDialog != null) {
                configurationDialog.dismiss();
            }
        } catch (SQLiteConstraintException e) {
            Toast.makeText(this, R.string.transition_duplicity, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "TMTransition '" + fromState.getValue() + ", " +
                    readSymbol.getValue() + ", " +
                    toState.getValue() + ", " +
                    writeSymbol.getValue() + "' already exists", e);
        } catch (Exception e) {
            Toast.makeText(this, R.string.transition_save_error, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "unknown error occurred while saving transition", e);
        }
    }

    @Override
    public void onEditItemClick(int position, int elementType) {
        //prepare edited element and create dialogWindow
        switch (elementType) {
            case INPUT_SYMBOL:
                inputSymbolEdit = (Symbol) inputAlphabetAdapter.getItems().get(position);
                showConfigurationDialog(INPUT_SYMBOL, UPDATE);
                break;
            case STACK_SYMBOL:
                stackSymbolEdit = (Symbol) stackAlphabetAdapter.getItems().get(position);
                showConfigurationDialog(STACK_SYMBOL, UPDATE);
                break;
            case STATE:
                stateEdit = (State) stateAdapter.getItems().get(position);
                showConfigurationDialog(STATE, UPDATE);
                break;
            case FSA_TRANSITION:
                transitionEdit = (Transition) transitionAdapter.getItems().get(position);
                showConfigurationDialog(FSA_TRANSITION, UPDATE);
                break;
            case PDA_TRANSITION:
                transitionEdit = (Transition) transitionAdapter.getItems().get(position);
                showConfigurationDialog(PDA_TRANSITION, UPDATE);
                break;
            case TM_TRANSITION:
                transitionEdit = (Transition) transitionAdapter.getItems().get(position);
                showConfigurationDialog(TM_TRANSITION, UPDATE);
                break;
        }
    }

    @Override
    public void onRemoveItemClick(final int position, int elementType) {
        switch (elementType) {
            case ConfigurationActivity.INPUT_SYMBOL:
                final Symbol inputSymbol = (Symbol) inputAlphabetAdapter.getItems().get(position);
                try {
                    dataSource.deleteInputSymbol(inputSymbol, (Symbol) inputAlphabetAdapter.getItems().get(0), tapeElementList);
                    inputAlphabetAdapter.removeItem(inputSymbol);
                } catch (SQLiteConstraintException e) {
                    Toast.makeText(ConfigurationActivity.this, R.string.unable_remove_symbol, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "input symbol used in transition", e);
                } catch (Exception e) {
                    Toast.makeText(ConfigurationActivity.this, R.string.symbol_remove_error, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "unknown error occurred while removing input symbol", e);
                }
                break;
            case ConfigurationActivity.STACK_SYMBOL:
                final Symbol stackSymbol = (Symbol) stackAlphabetAdapter.getItems().get(position);
                try {
                    dataSource.deleteStackSymbol(stackSymbol);
                    stackAlphabetAdapter.removeItem(stackSymbol);
                } catch (SQLiteConstraintException e) {
                    Toast.makeText(ConfigurationActivity.this, R.string.unable_remove_symbol, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "stack symbol used in transition", e);
                } catch (Exception e) {
                    Toast.makeText(ConfigurationActivity.this, R.string.symbol_remove_error, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "unknown error occurred while removing stack symbol", e);
                }
                break;
            case ConfigurationActivity.STATE:
                final State state = (State) stateAdapter.getItems().get(position);
                try {
                    dataSource.deleteState(state);
                    stateAdapter.removeItem(state);
                    diagramView.removeState(state);

                    //set existence of initial state if necessary
                    if (state.isInitialState()) {
                        initialState = null;
                    }

                } catch (SQLiteConstraintException e) {
                    Toast.makeText(ConfigurationActivity.this, R.string.unable_remove_state, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "state used in transition", e);
                } catch (Exception e) {
                    Toast.makeText(ConfigurationActivity.this, R.string.state_remove_error, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "unknown error occurred while removing state", e);
                }
                break;
            default:
                //transitions
                final Transition transition = (Transition) transitionAdapter.getItems().get(position);
                try {
                    dataSource.deleteTransition(transition);
                    transitionAdapter.removeItem(transition);
                    diagramView.removeTransition(transition);
                } catch (Exception e) {
                    Toast.makeText(ConfigurationActivity.this, R.string.transition_remove_error, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "unknown error occurred while removing transition", e);
                }
                break;
        }
    }

    @Override
    public void onAddState() {
        Log.v(TAG, "onAddState from diagram noted");
        showConfigurationDialog(STATE, NEW);
    }

    @Override
    public void onAddTransition(State fromState, State toState) {
        Log.v(TAG, "onAddTransition from diagram noted");
        //different type of transition dialog window for different types of machines
        switch (machineType) {
            case MainActivity.FINITE_STATE_AUTOMATON:
                showConfigurationDialog(FSA_TRANSITION, NEW, fromState.getId(), toState.getId());
                break;
            case MainActivity.PUSHDOWN_AUTOMATON:
                showConfigurationDialog(PDA_TRANSITION, NEW, fromState.getId(), toState.getId());
                break;
            case MainActivity.LINEAR_BOUNDED_AUTOMATON:
            case MainActivity.TURING_MACHINE:
                showConfigurationDialog(TM_TRANSITION, NEW, fromState.getId(), toState.getId());
                break;
        }
    }

    @Override
    public void onEditState(State stateEdit) {
        Log.v(TAG, "onEditState from diagram noted");
        this.stateEdit = stateEdit;
        showConfigurationDialog(STATE, UPDATE);
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
                    public void onClick(DialogInterface dialogInterface, int i) {
                        transitionEdit = transitionList.get(i);
                        switch (machineType) {
                            case MainActivity.FINITE_STATE_AUTOMATON:
                                showConfigurationDialog(FSA_TRANSITION, UPDATE);
                                break;
                            case MainActivity.PUSHDOWN_AUTOMATON:
                                showConfigurationDialog(PDA_TRANSITION, UPDATE);
                                break;
                            case MainActivity.LINEAR_BOUNDED_AUTOMATON:
                            case MainActivity.TURING_MACHINE:
                                showConfigurationDialog(TM_TRANSITION, UPDATE);
                                break;
                        }
                    }
                })
                .setCancelable(true)
                .show();
    }

    @Override
    public void onRemoveState(final State stateRemove) {
        Log.v(TAG, "onRemoveState from diagram noted");
        try {
            dataSource.deleteState(stateRemove);
            stateAdapter.removeItem(stateRemove);
            diagramView.removeState(stateRemove);

            //set existence of initial state if necessary
            if (stateRemove.isInitialState()) {
                initialState = null;
            }

        } catch (SQLiteConstraintException e) {
            Toast.makeText(ConfigurationActivity.this, R.string.unable_remove_state, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "state used in transition", e);
        } catch (Exception e) {
            Toast.makeText(ConfigurationActivity.this, R.string.state_remove_error, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "unknown error occurred while removing state", e);
        }
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
                            dataSource.deleteTransition(transition);
                            transitionAdapter.removeItem(transition);
                            diagramView.removeTransition(transition);
                        } catch (Exception e) {
                            Toast.makeText(ConfigurationActivity.this, R.string.transition_remove_error, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "unknown error occurred while removing transition", e);
                        }

                    }
                })
                .setCancelable(true)
                .show();
    }

    private void showConfigurationDialog(int elementType, int elementAction) {
        this.elementAction = elementAction;
        FragmentManager fm = getSupportFragmentManager();
        ConfigurationDialog configurationDialog = ConfigurationDialog.newInstance(elementType, elementAction);
        configurationDialog.show(fm, CONFIGURATION_DIALOG);
    }

    private void showConfigurationDialog(int elementType, int elementAction, long fromStateId, long toStateId) {
        this.elementAction = elementAction;
        FragmentManager fm = getSupportFragmentManager();
        ConfigurationDialog configurationDialog = ConfigurationDialog.newInstance(elementType, elementAction, fromStateId, toStateId);
        configurationDialog.show(fm, CONFIGURATION_DIALOG);
    }

    public void showSupportConfigurationDialog(int elementType) {
        FragmentManager fm = getSupportFragmentManager();
        final ConfigurationDialog supportDialog = ConfigurationDialog.newInstance(elementType, NEW);
        supportDialog.show(fm, SUPPORT_CONFIGURATION_DIALOG);
    }

    public void showStackSupportConfigurationDialog(int index) {
        FragmentManager fm = getSupportFragmentManager();
        ConfigurationDialog supportDialog = ConfigurationDialog.newInstance(STACK_SYMBOL, NEW, index);
        ConfigurationDialog cd = (ConfigurationDialog) getSupportFragmentManager().findFragmentByTag(CONFIGURATION_DIALOG);
        cd.getArguments().putInt(ConfigurationDialog.INDEX, index);
        supportDialog.show(fm, SUPPORT_CONFIGURATION_DIALOG);
    }

    public Symbol getInputSymbolEdit() {
        return inputSymbolEdit;
    }

    public Symbol getStackSymbolEdit() {
        return stackSymbolEdit;
    }

    public State getStateEdit() {
        return stateEdit;
    }

    public Transition getTransitionEdit() {
        return transitionEdit;
    }

    public List<Symbol> getInputAlphabetList() {
        if (inputAlphabetAdapter == null) {
            return new ArrayList<>();
        }
        return inputAlphabetAdapter.getItems();
    }

    public List<Symbol> getStackAlphabetList() {
        if (stackAlphabetAdapter == null) {
            return new ArrayList<>();
        }
        return stackAlphabetAdapter.getItems();
    }

    public List<State> getStateList() {
        if (stateAdapter == null) {
            return new ArrayList<>();
        }
        return stateAdapter.getItems();
    }

    private void makeDeterministicMachine() {
        dataSource.dropTransitions();
        dataSource.dropStates();
        Pair<List<State>, List<Transition>> deterministicMachine =
                FiniteStateAutomatonStep.createDeterministic(getStateList(), transitionAdapter.getItems(),
                        diagramView.getDimensionY(), diagramView.getDimensionX(), diagramView.getNodeRadius());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                diagramView.clear();
                stateAdapter.setItems(new ArrayList());
                transitionAdapter.setItems(new ArrayList());
            }
        });

        for (State state : deterministicMachine.first) {
            final State s = dataSource.addState(state.getValue(), state.getPositionX(), state.getPositionY(), state.isInitialState(), state.isFinalState());
            if (s.isInitialState()) {
                ConfigurationActivity.this.initialState = s;
            }

            for (Transition transition : deterministicMachine.second) {
                if (transition.getFromState().equals(state)) {
                    transition.setFromState(s);
                }
                if (transition.getToState().equals(state)) {
                    transition.setToState(s);
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    diagramView.addState(s);
                    stateAdapter.addItem(s);
                }
            });
        }
        for (Transition transition : deterministicMachine.second) {
            final Transition t = dataSource.addFsaTransition(transition.getFromState(), transition.getReadSymbol(), transition.getToState(), emptyInputSymbolId);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    diagramView.addTransition(t);
                    transitionAdapter.addItem(t);
                }
            });
        }
    }

    @Override
    public void onTaskDialogClick(Task task, int machineType, int dialogMode) {
        final TaskDialog taskDialog = (TaskDialog) getSupportFragmentManager().findFragmentByTag(TASK_DIALOG);
        if (dialogMode == TaskDialog.EDITING) {
            for (Object object : stateAdapter.getItems()) {
                State state = (State) object;
                dataSource.updateState(state, state.getValue(), state.getPositionX(), state.getPositionY(), state.isInitialState(), state.isFinalState());
            }

            Intent nextActivityIntent = new Intent(this, EditTaskActivity.class);
            nextActivityIntent.putExtras(new Bundle());
            nextActivityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(nextActivityIntent);
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
                            Toast.makeText(ConfigurationActivity.this, R.string.sending_result_error, Toast.LENGTH_LONG).show();
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
            dataSource.close();
            TaskDialog.setStatusText(null);
            Intent nextActivityIntent = new Intent(ConfigurationActivity.this, MainActivity.class);
            nextActivityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(nextActivityIntent);
            taskDialog.dismiss();
        }
    }

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
}
