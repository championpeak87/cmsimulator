package fiitstu.gulis.cmsimulator.activities;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import android.view.*;
import android.widget.*;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.adapters.bulktest.TestScenarioListAdapter;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.database.FileHandler;
import fiitstu.gulis.cmsimulator.dialogs.EditTestDialog;
import fiitstu.gulis.cmsimulator.dialogs.GuideFragment;
import fiitstu.gulis.cmsimulator.dialogs.SaveMachineDialog;
import fiitstu.gulis.cmsimulator.dialogs.TaskDialog;
import fiitstu.gulis.cmsimulator.elements.*;
import fiitstu.gulis.cmsimulator.machines.*;
import fiitstu.gulis.cmsimulator.network.TaskResultSender;
import fiitstu.gulis.cmsimulator.util.ProgressWorker;

import java.util.ArrayList;
import java.util.List;


/**
 * The activity used for bulk-testing multiple inputs.
 *
 * Expected Intent arguments (extras) (KEY (TYPE) - MEANING):
 * MACHINE_TYPE (int) - type of the machine (one of MainActivity's static fields)
 * NEGATIVE (boolean) - if true, "incorrect inputs" are shown; for non-tasks should always be false
 * TASK_CONFIGURATION (int) - 0 (not a task), MainActivity.EditTask, or MainActivity.SolveTask
 * FILE_NAME (String) - the name of the currently open file (or the default filename)
 * TASK (Serializable - Task) - the task being solved (or null); TASK = null <=> TASK_CONFIGURATION = 0
 *
 * Created by Jakub Sedlář on 14.10.2017.
 */
public class BulkTestActivity extends FragmentActivity implements SaveMachineDialog.SaveDialogListener,
        TaskDialog.TaskDialogListener, EditTestDialog.EditTestDialogListener {

    //log tag
    private static final String TAG = BulkTestActivity.class.getName();

    public static final String NEGATIVE = "NEGATIVE";
    public static final String TASK_CONFIGURATION = "TASK_CONFIGURATION";
    private static final String TASK_DIALOG = "TASK_DIALOG";
    private static final String SAVE_DIALOG = "SAVE_DIALOG";
    private static final String EDIT_TEST_DIALOG = "EDIT_TEST_DIALOG";

    private TestScenarioListAdapter scenariosListAdapter;

    private TestScenario editTest;

    private int machineType;
    private boolean negative;
    private String filename;

    private Task task;
    private int taskConfiguration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulk_test);
        Log.v(TAG, "onCreate initialization started");

        // menu
        ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (taskConfiguration != 0) {
            if (negative) {
                actionBar.setTitle(R.string.incorrect_inputs);
            }
            else {
                actionBar.setTitle(R.string.correct_inputs);
            }
        }
        else {
            actionBar.setTitle(R.string.test_inputs);
        }

        Bundle inputBundle = getIntent().getExtras();
        machineType = inputBundle.getInt(MainActivity.MACHINE_TYPE);
        negative = inputBundle.getBoolean(NEGATIVE);
        filename = inputBundle.getString(MainActivity.FILE_NAME);
        taskConfiguration = inputBundle.getInt(TASK_CONFIGURATION);

        Button newTestButton = findViewById(R.id.button_bulk_test_add_test);
        newTestButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view){
                onNewTestClick();
            }
        });
        Button runTestsButton = findViewById(R.id.button_bulk_test_run);
        runTestsButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view){
                ProgressWorker worker = new ProgressWorker(500, findViewById(R.id.relativeLayout_bulktest_working),
                        new Runnable() {
                            @Override
                            public void run() {
                                runTests();
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
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                worker.execute();
            }
        });

        scenariosListAdapter = new TestScenarioListAdapter(this,
                taskConfiguration != MainActivity.SOLVE_TASK);
        scenariosListAdapter.setItemClickCallback(new TestScenarioListAdapter.ItemClickCallback() {
            @Override
            public void onLongClick(final TestScenario test) {
                CharSequence[] contextSource = new CharSequence[]{getResources().getString(R.string.view_in_simulation)};

                new AlertDialog.Builder(BulkTestActivity.this)
                        .setItems(contextSource, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //a very useless switch, but ignoring "which" feels more wrong than having this switch
                                switch (which) {
                                    case 0:
                                        //load the scenario's tape into the database
                                        DataSource dataSource = DataSource.getInstance();
                                        dataSource.dropTapeElements();
                                        int order = 0;
                                        for (Symbol symbol: test.getInputWord()) {
                                            dataSource.addTapeElement(symbol, order);
                                            order++;
                                        }

                                        //start simulation activity
                                        Intent nextActivityIntent = new Intent(BulkTestActivity.this, SimulationActivity.class);
                                        nextActivityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(nextActivityIntent);
                                        Log.i(TAG, "simulation activity intent executed");
                                        break;
                                }
                            }
                        })
                        .show();
            }

            public void onEditItemClick(TestScenario test) {
                Log.v(TAG, "edit test button click noted");
                editTest = test;
                FragmentManager fm = getSupportFragmentManager();
                EditTestDialog editTestDialog = EditTestDialog.newInstance(
                        machineType == MainActivity.LINEAR_BOUNDED_AUTOMATON
                        || machineType == MainActivity.TURING_MACHINE, editTest, false);
                editTestDialog.show(fm, EDIT_TEST_DIALOG);
            }
            public void onRemoveItemClick(TestScenario test) {
                Log.v(TAG, "remove test button click noted");
                scenariosListAdapter.clearRowColors();
                scenariosListAdapter.clearStatuses();
                scenariosListAdapter.removeItem(test);
                DataSource.getInstance().deleteTest(test, negative);
            }
        });



        RecyclerView recyclerView = findViewById(R.id.recyclerView_bulk_test_scenarios);
        recyclerView.setAdapter(scenariosListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.menu_bulk_test, menu);

        if (taskConfiguration != 0) {
            if (task.getPublicInputs() || taskConfiguration != MainActivity.SOLVE_TASK) {
                MenuItem otherTestItem = menu.findItem(R.id.menu_bulk_test_other_test);
                if (negative) {
                    otherTestItem.setTitle(R.string.correct_inputs);
                } else {
                    otherTestItem.setTitle(R.string.incorrect_inputs);
                }
                otherTestItem.setVisible(true);
            }
        }

        Bundle inputBundle = getIntent().getExtras();
        Button newTestButton = findViewById(R.id.button_bulk_test_add_test);
        if (taskConfiguration != 0) {
            task = (Task) inputBundle.getSerializable(MainActivity.TASK);
            MenuItem taskInfoButton = findViewById(R.id.menu_bulk_test_info);
            taskInfoButton.setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_bulk_test_info:
                if (taskConfiguration == MainActivity.SOLVE_TASK)
                {
                    FragmentManager fm = getSupportFragmentManager();
                    TaskDialog taskDialog = TaskDialog.newInstance(task, TaskDialog.SOLVING, machineType);
                    taskDialog.show(fm, TASK_DIALOG);
                }
                else
                {
                    FragmentManager fm = getSupportFragmentManager();
                    TaskDialog taskDialog = TaskDialog.newInstance(task, TaskDialog.EDITING, machineType);
                    taskDialog.show(fm, TASK_DIALOG);
                }
                return true;
            case R.id.menu_bulk_test_save_machine:
                if (Build.VERSION.SDK_INT > 15
                        && ContextCompat.checkSelfPermission(BulkTestActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(BulkTestActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MainActivity.REQUEST_WRITE_STORAGE);
                } else {
                    FragmentManager fm = getSupportFragmentManager();
                    SaveMachineDialog saveMachineDialog = SaveMachineDialog.newInstance(filename, null, false);
                    saveMachineDialog.show(fm, SAVE_DIALOG);
                }
                return true;
            case R.id.menu_bulk_test_configure:
                Bundle outputBundle = new Bundle();
                outputBundle.putInt(MainActivity.MACHINE_TYPE, machineType);
                DataSource dataSource = DataSource.getInstance();
                outputBundle.putLong(SimulationActivity.EMPTY_INPUT_SYMBOL, dataSource.getInputSymbolWithProperties(Symbol.EMPTY).getId());
                Symbol startStackSymbol = dataSource.getStackSymbolWithProperties(Symbol.STACK_BOTTOM);
                if (startStackSymbol != null) {
                    outputBundle.putLong(SimulationActivity.START_STACK_SYMBOL, startStackSymbol.getId());
                }
                outputBundle.putInt(TASK_CONFIGURATION, taskConfiguration);
                outputBundle.putSerializable(MainActivity.TASK, task);
                Intent nextActivityIntent = new Intent(BulkTestActivity.this, ConfigurationActivity.class);
                nextActivityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                nextActivityIntent.putExtras(outputBundle);
                startActivity(nextActivityIntent);
                Log.i(TAG, "configuration activity intent executed");
                return true;
            case R.id.menu_bulk_test_simulate:
                nextActivityIntent = new Intent(BulkTestActivity.this, SimulationActivity.class);
                nextActivityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(nextActivityIntent);
                Log.i(TAG, "simulation activity intent executed");
                return true;
            case R.id.menu_bulk_test_other_test:
                setNegative(!negative);
                return true;
            case R.id.menu_bulk_test_settings:
                nextActivityIntent = new Intent(BulkTestActivity.this, OptionsActivity.class);
                startActivity(nextActivityIntent);
                Log.i(TAG, "options activity intent executed");
                return true;
            case R.id.menu_bulk_test_help:
                FragmentManager fm = getSupportFragmentManager();
                GuideFragment guideFragment = GuideFragment.newInstance(GuideFragment.BULK_TEST);
                guideFragment.show(fm, "HELP_DIALOG");
                return true;
        }

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        DataSource.getInstance().open();
        scenariosListAdapter.setItems(DataSource.getInstance().getTestFullExtract(negative, DataSource.getInstance().getInputAlphabetFullExtract()));
    }

    @Override
    protected void onPause() {
        DataSource.getInstance().close();
        super.onPause();
    }

    private void onNewTestClick() {
        Log.v(TAG, "new test button click noted");
        FragmentManager fm = getSupportFragmentManager();
        editTest = new TestScenario(new ArrayList<Symbol>(),  null);
        EditTestDialog editTestDialog = EditTestDialog.newInstance(machineType == MainActivity.LINEAR_BOUNDED_AUTOMATON
                || machineType == MainActivity.TURING_MACHINE, editTest, true);
        editTestDialog.show(fm, EDIT_TEST_DIALOG);
    }

    private void runTests() {
        Log.v(TAG, "run tests method started");

        for (int i = 0; i < scenariosListAdapter.getItemCount(); i++) {
            final TestScenario testScenario = scenariosListAdapter.getItem(i);
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
                    }
                    else {
                        status = TestScenarioListAdapter.Status.REJECT;
                    }
                    break;
                case MachineStep.PROGRESS:
                    status = TestScenarioListAdapter.Status.TOOK_TOO_LONG;
                    break;
                case MachineStep.DONE:
                    if (testScenario.getOutputWord() == null
                            || machine.matchTapeNondeterministic(testScenario.getOutputWord())) {
                        status = TestScenarioListAdapter.Status.ACCEPT;
                    }
                    else {
                        status = TestScenarioListAdapter.Status.INCORRECT_OUTPUT;
                    }
                    break;
                default:
                    status = null;
                    break;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    scenariosListAdapter.setRowStatus(index, status);
                }
            });
        }
    }

    @Override
    public void onSaveTestClick(List<Symbol> input, List<Symbol> output, boolean isNew) {
        Log.v(TAG, "save test button click noted");

        boolean emptyTapeError = false;
        if ((machineType == MainActivity.FINITE_STATE_AUTOMATON || machineType == MainActivity.PUSHDOWN_AUTOMATON)
                && !input.isEmpty()) {
            for (Symbol symbol: input) {
                if (symbol.isEmpty()) {
                    emptyTapeError = true;
                    break;
                }
            }
        }

        if (emptyTapeError) {
            Toast.makeText(this, R.string.bulk_incomplete_tape_error, Toast.LENGTH_SHORT).show();
            return;
        }

        editTest.setInputWord(input);
        editTest.setOutputWord(output);
        editTest.persist(negative, DataSource.getInstance());
        scenariosListAdapter.clearRowColors();
        scenariosListAdapter.clearStatuses();
        if (isNew) {
            scenariosListAdapter.addItem(editTest);
        }
        else {
            scenariosListAdapter.notifyItemChanged(editTest);
        }
        Log.i(TAG, "TestScenario '" + editTest.getInputWord() + "' saved");
        editTest = null;

        FragmentManager fm = getSupportFragmentManager();
        EditTestDialog editTestDialog = (EditTestDialog) fm.findFragmentByTag(EDIT_TEST_DIALOG);
        if (editTestDialog != null) {
            editTestDialog.dismiss();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MainActivity.REQUEST_WRITE_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    FragmentManager fm = getSupportFragmentManager();
                    SaveMachineDialog saveMachineDialog = SaveMachineDialog.newInstance(filename, null, false);
                    saveMachineDialog.show(fm, SAVE_DIALOG);
                }
            }
        }
    }

    @Override
    public void saveDialogClick(String filename, FileHandler.Format format, boolean exit) {
        this.filename = filename;
        DataSource dataSource = DataSource.getInstance();
        try {
            for (int i = 0; i < scenariosListAdapter.getItemCount(); i++) {
                dataSource.addOrUpdateTest(scenariosListAdapter.getItem(i), negative);
            }
            FileHandler fileHandler = new FileHandler(format);
            if (taskConfiguration != MainActivity.SOLVE_TASK || task.getPublicInputs()) {
                fileHandler.setData(dataSource, machineType);
            }
            else { //test inputs are supposed to be hidden, do not save them
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
                        transitions, dataSource.getTapeFullExtract(inputAlphabet), new ArrayList<TestScenario>(),
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
        } catch (Exception e) {
            Log.e(TAG, "File was not saved", e);
            Toast.makeText(this, getResources().getString(R.string.file_not_saved), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Toggles between displaying positive and negative tests
     * @param negative if true, negative tests will be shown, if false, positive tests will be shown
     */
    private void setNegative(boolean negative) {
        this.negative = negative;
        scenariosListAdapter.clearStatuses();
        scenariosListAdapter.clearRowColors();
        scenariosListAdapter.setItems(DataSource.getInstance().getTestFullExtract(negative, DataSource.getInstance().getInputAlphabetFullExtract()));
        this.getActionBar().setTitle(negative ? R.string.incorrect_inputs : R.string.correct_inputs);
    }

    private class MenuItemClickListener implements PopupMenu.OnMenuItemClickListener{
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {

            }
            return false;
        }
    }

    @Override
    public void onTaskDialogClick(Task task, int machineType, int dialogMode) {
        final TaskDialog taskDialog = (TaskDialog) getSupportFragmentManager().findFragmentByTag(TASK_DIALOG);
        if (dialogMode == TaskDialog.EDITING) {
            Intent nextActivityIntent = new Intent(this, EditTaskActivity.class);
            nextActivityIntent.putExtras(new Bundle());
            nextActivityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(nextActivityIntent);
            taskDialog.dismiss();
        }
        else if (dialogMode == TaskDialog.SOLVING) {
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
                            Toast.makeText(BulkTestActivity.this, R.string.sending_result_error, Toast.LENGTH_LONG).show();
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
        }
        else if (dialogMode == TaskDialog.SOLVED) {
            DataSource.getInstance().open();
            DataSource.getInstance().globalDrop();
            DataSource.getInstance().close();
            TaskDialog.setStatusText(null);
            Intent nextActivityIntent = new Intent(BulkTestActivity.this, MainActivity.class);
            nextActivityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(nextActivityIntent);
            taskDialog.dismiss();
        }
    }
}
