package fiitstu.gulis.cmsimulator.activities;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.aditya.filebrowser.Constants;
import com.aditya.filebrowser.FileChooser;
import fiitstu.gulis.cmsimulator.BuildConfig;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.database.FileHandler;
import fiitstu.gulis.cmsimulator.dialogs.ExampleGrammarDialog;
import fiitstu.gulis.cmsimulator.dialogs.ExampleMachineDialog;
import fiitstu.gulis.cmsimulator.dialogs.NewMachineDialog;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.dialogs.FileSelector;
import fiitstu.gulis.cmsimulator.elements.Timer;
import fiitstu.gulis.cmsimulator.network.ServerController;
import io.blushine.android.ui.showcase.MaterialShowcase;
import io.blushine.android.ui.showcase.MaterialShowcaseSequence;
import io.blushine.android.ui.showcase.MaterialShowcaseView;
import io.blushine.android.ui.showcase.ShowcaseConfig;

import java.io.File;
import java.io.IOException;
import java.sql.Time;

import static fiitstu.gulis.cmsimulator.app.CMSimulator.getContext;


/**
 * Activity that displays the main menu
 * <p>
 * Created by Martin on 7. 3. 2017.
 */
public class MainActivity extends FragmentActivity
        implements View.OnClickListener, NewMachineDialog.NewMachineDialogListener, ExampleMachineDialog.ExampleMachineDialogListener,
        ExampleGrammarDialog.ExampleGrammarDialogListener {

    //log tag
    private static final String TAG = MainActivity.class.getName();

    // FILE BROWSER
    private static final int READ_REQUEST_CODE = 42;

    //bundle values
    public static final String MACHINE_TYPE = "MACHINE_TYPE";
    public static final String CONFIGURATION_TYPE = "CONFIGURATION_TYPE";
    public static final String DEFAULT_FORMAT = "DEFAULT_FORMAT";
    public static final String FILE_NAME = "FILE_NAME";
    public static final String FIRST_LAUNCH = "FIRST_LAUNCH";
    public static final String TASK = "TASK";

    //dialog value
    private static final String NEW_MACHINE_DIALOG = "NEW_MACHINE_DIALOG";
    private static final String EXAMPLE_MACHINE_DIALOG = "EXAMPLE_MACHINE_DIALOG";
    private static final String EXAMPLE_GRAMMAR_DIALOG = "EXAMPLE_GRAMMAR_DIALOG";


    public static final int UNDEFINED = -1;

    //configuration types
    public static final int NEW_MACHINE = 0;
    public static final int LOAD_MACHINE = 1;
    public static final int EXAMPLE_MACHINE1 = 2;
    public static final int EXAMPLE_MACHINE2 = 3;
    public static final int EXAMPLE_MACHINE3 = 8;
    public static final int NEW_TASK = 4;
    public static final int EDIT_TASK = 5;
    public static final int SOLVE_TASK = 6;
    public static final int RESUME_MACHINE = 7;
    public static final int GAME_MACHINE = 9;
    public static final int PREVIEW_TASK = 18;

    //grammar examples
    public static final int EXAMPLE_GRAMMAR1 = 0;
    public static final int EXAMPLE_GRAMMAR2 = 1;
    public static final int EXAMPLE_GRAMMAR3 = 2;
    public static final int EXAMPLE_GRAMMAR4 = 3;
    public static final int EXAMPLE_GRAMMAR5 = 4;
    public static final int EXAMPLE_GRAMMAR6 = 5;
    public static final int EXAMPLE_GRAMMAR7 = 6;

    //machine types
    public static final int FINITE_STATE_AUTOMATON = 0;
    public static final int PUSHDOWN_AUTOMATON = 1;
    public static final int LINEAR_BOUNDED_AUTOMATON = 2;
    public static final int TURING_MACHINE = 3;

    //storage permissions
    public static final int REQUEST_READ_STORAGE = 0;
    public static final int REQUEST_WRITE_STORAGE = 1;

    //variables
    private boolean loadPermissions = false;

    private Timer timer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTheme(R.style.MainMenuTheme);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_main_landscape);
        } else {
            setContentView(R.layout.activity_main_portrait);
            ImageView image = findViewById(R.id.imageView_main_logo);
            TextView betaBanner = findViewById(R.id.beta_banner);
            String buildtype = BuildConfig.FLAVOR.toString();
            Log.d("BUILDTYPE", buildtype);
            if (buildtype != "normal") {
                betaBanner.setVisibility(View.VISIBLE);
                betaBanner.setText(buildtype);
            }
        }



        final int textColor = getColor(R.color.introContentText);

        MaterialShowcaseSequence firstLaunchSequence = new MaterialShowcaseSequence(this);
        MaterialShowcaseView firstLaunchMessage = new MaterialShowcaseView.Builder(this)
                .renderOverNavigationBar()
                .setTitleText(getString(R.string.welcome))
                .setContentTextColor(textColor)
                .setContentText(getString(R.string.welcome_message))
                .setDismissBackgroundColor(getColor(R.color.primary_color_dark))
                .setDismissText(R.string.understood)
                .setDelay(500)
                .build();

        Button newAutomata = findViewById(R.id.button_main_new),
                newGrammar = findViewById(R.id.button_main_grammar),
                tasks = findViewById(R.id.button_main_tasks);

        MaterialShowcaseView newAutomataMessage = new MaterialShowcaseView.Builder(this)
                .renderOverNavigationBar()
                .setTarget(newAutomata)
                .setContentTextColor(textColor)
                .setTitleText(getString(R.string.automatas))
                .setContentText(getString(R.string.automata_message))
                .setDismissBackgroundColor(getColor(R.color.primary_color_dark))
                .setDismissText(R.string.understood)
                .setDelay(500)
                .build();

        MaterialShowcaseView newGrammarMessage = new MaterialShowcaseView.Builder(this)
                .renderOverNavigationBar()
                .setTarget(newGrammar)
                .setTitleText(getString(R.string.grammar))
                .setContentTextColor(textColor)
                .setContentText(getString(R.string.grammar_message))
                .setDismissBackgroundColor(getColor(R.color.primary_color_dark))
                .setDismissText(R.string.understood)
                .setDelay(500)
                .build();

        MaterialShowcaseView tasksMessage = new MaterialShowcaseView.Builder(this)
                .renderOverNavigationBar()
                .setTarget(tasks)
                .setTitleText(getString(R.string.tasks))
                .setContentTextColor(textColor)
                .setContentText(getString(R.string.tasks_message))
                .setDismissBackgroundColor(getColor(R.color.primary_color_dark))
                .setDismissText(R.string.understood)
                .setDelay(500)
                .build();

        firstLaunchSequence.addSequenceItem(firstLaunchMessage);
        firstLaunchSequence.addSequenceItem(newAutomataMessage);
        firstLaunchSequence.addSequenceItem(newGrammarMessage);
        firstLaunchSequence.addSequenceItem(tasksMessage);
        firstLaunchSequence.setSingleUse(FIRST_LAUNCH);
        firstLaunchSequence.show();

        int nightModeFlags =
                getContext().getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        ImageView logo = findViewById(R.id.imageView_main_logo);
        switch (nightModeFlags) {

            case Configuration.UI_MODE_NIGHT_YES:

                logo.setImageResource(R.drawable.logo_v1_dark);
                break;

            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                logo.setImageResource(R.drawable.logo_v1_light);
                break;
        }

        Log.v(TAG, "onCreate initialization started");
        DataSource dataSource = DataSource.getInstance();
        dataSource.open();
        dataSource.globalDrop();
        dataSource.close();


        //main window buttons initializations
        //new machine
        Button newMachineButton = findViewById(R.id.button_main_new);
        newMachineButton.setOnClickListener(this);

        //example machine
        Button exampleMachineButton = findViewById(R.id.button_main_example);
        exampleMachineButton.setOnClickListener(this);

        //tasks
        Button tasksButton = findViewById(R.id.button_main_tasks);
        tasksButton.setOnClickListener(this);

        //load machine
        Button loadMachineButton = findViewById(R.id.button_main_load);
        loadMachineButton.setOnClickListener(this);

        //grammar
        Button grammarButton = findViewById(R.id.button_main_grammar);
        grammarButton.setOnClickListener(this);

        //example grammar
        Button exampleGrammarButton = findViewById(R.id.button_main_example_grammar);
        exampleGrammarButton.setOnClickListener(this);

        //options
        Button optionsButton = findViewById(R.id.button_main_options);
        optionsButton.setOnClickListener(this);

        //help
        Button helpButton = findViewById(R.id.button_main_help);
        helpButton.setOnClickListener(this);
        Log.v(TAG, "main buttons initialized");

        Log.i(TAG, "onCreate initialized");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume method started");

        if (loadPermissions) {
            loadMachine();
            loadPermissions = false;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_main_landscape);
        } else {
            setContentView(R.layout.activity_main_portrait);
        }

        int nightModeFlags =
                getContext().getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        ImageView logo = findViewById(R.id.imageView_main_logo);
        switch (nightModeFlags) {

            case Configuration.UI_MODE_NIGHT_YES:

                logo.setImageResource(R.drawable.logo_v1_dark);
                break;

            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                logo.setImageResource(R.drawable.logo_v1_light);
                break;
        }

        Button newMachineButton = findViewById(R.id.button_main_new);
        newMachineButton.setOnClickListener(this);

        Button exampleMachineButton = findViewById(R.id.button_main_example);
        exampleMachineButton.setOnClickListener(this);

        Button tasksButton = findViewById(R.id.button_main_tasks);
        tasksButton.setOnClickListener(this);

        Button loadMachineButton = findViewById(R.id.button_main_load);
        loadMachineButton.setOnClickListener(this);

        Button grammarButton = findViewById(R.id.button_main_grammar);
        grammarButton.setOnClickListener(this);

        Button optionsButton = findViewById(R.id.button_main_options);
        optionsButton.setOnClickListener(this);

        Button helpButton = findViewById(R.id.button_main_help);
        helpButton.setOnClickListener(this);

        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                String path = uri.getPath();
                String[] temp = path.split("\\.");
                if (temp[temp.length - 1].equals("cms") || temp[temp.length - 1].equals("cmst")) {
                    Bundle outputBundle = new Bundle();
                    outputBundle.putInt(CONFIGURATION_TYPE, LOAD_MACHINE);
                    outputBundle.putBoolean(DEFAULT_FORMAT, true);
                    outputBundle.putString(FILE_NAME, path);
                    Log.v(TAG, "outputBundle initialized");

                    Intent nextActivityIntent = new Intent(MainActivity.this, SimulationActivity.class);
                    nextActivityIntent.putExtras(outputBundle);
                    startActivity(nextActivityIntent);
                    Log.i(TAG, "simulation activity intent executed");
                } else {
                    Toast.makeText(this, R.string.file_not_loaded, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //new machine
            case R.id.button_main_new:
                FragmentManager fm = getSupportFragmentManager();
                NewMachineDialog newMachineDialog = new NewMachineDialog();
                newMachineDialog.show(fm, NEW_MACHINE_DIALOG);


                Log.v(TAG, "new machine button click noted");
                break;
            //example machine
            case R.id.button_main_example:
                Intent newIntent = new Intent(this, ExampleAutomatas.class);

                startActivity(newIntent);
                Log.v(TAG, "example machine button click noted");
                /*fm = getSupportFragmentManager();
                ExampleMachineDialog exampleMachineDialog = ExampleMachineDialog.newInstance();
                exampleMachineDialog.show(fm, EXAMPLE_MACHINE_DIALOG);*/
                break;
            //load machine
            case R.id.button_main_load:
                Log.v(TAG, "load machine click noted");

                Intent i2 = new Intent(getApplicationContext(), FileChooser.class);
                i2.putExtra(Constants.SELECTION_MODE, Constants.SELECTION_MODES.SINGLE_SELECTION.ordinal());
                i2.putExtra(Constants.ALLOWED_FILE_EXTENSIONS, "cms;cmst");
                startActivityForResult(i2, READ_REQUEST_CODE);

                break;
            //tasks
            case R.id.button_main_tasks:
                Log.v(TAG, "tasks click noted");
                Intent nextActivityIntent = new Intent(this, TaskLoginActivity.class);
                startActivity(nextActivityIntent);
                Log.i(TAG, "tasksActivity intent executed");
                break;
            //grammar
            case R.id.button_main_grammar:
                Log.v(TAG, "grammar click noted");
                nextActivityIntent = new Intent(this, GrammarActivity.class);
                startActivity(nextActivityIntent);
                Log.i(TAG, "grammarActivity intent executed");
                break;
            //example grammar
            case R.id.button_main_example_grammar:
                Intent exampleGrammarsIntent = new Intent(this, ExampleGrammars.class);
                startActivity(exampleGrammarsIntent);
//                Log.v(TAG, "grammar grammar button click noted");
//                fm = getSupportFragmentManager();
//                ExampleGrammarDialog exampleGrammarDialog = ExampleGrammarDialog.newInstance();
//                exampleGrammarDialog.show(fm, EXAMPLE_GRAMMAR_DIALOG);
                break;
            //options
            case R.id.button_main_options:
                Log.v(TAG, "options click noted");
                nextActivityIntent = new Intent(this, OptionsActivity.class);
                startActivity(nextActivityIntent);
                Log.i(TAG, "optionsActivity intent executed");
                break;
            //help
            case R.id.button_main_help:
                Log.v(TAG, "aboutHelp click noted");
                nextActivityIntent = new Intent(this, HelpActivity.class);
                startActivity(nextActivityIntent);
                Log.i(TAG, "aboutHelp activity intent executed");
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadPermissions = true;
                }
            }
        }
    }

    @Override
    public void newMachineDialogClick(Bundle outputBundle) {
        Intent nextActivityIntent = new Intent(this, SimulationActivity.class);
        nextActivityIntent.putExtras(outputBundle);
        startActivity(nextActivityIntent);
        NewMachineDialog newMachineDialog = (NewMachineDialog) getSupportFragmentManager()
                .findFragmentByTag(NEW_MACHINE_DIALOG);
        if (newMachineDialog != null) {
            newMachineDialog.dismiss();
        }
    }

    @Override
    public void exampleMachineDialogClick(Bundle outputBundle) {
        Intent nextActivityIntent = new Intent(this, SimulationActivity.class);
        nextActivityIntent.putExtras(outputBundle);
        startActivity(nextActivityIntent);
        ExampleMachineDialog newMachineDialog = (ExampleMachineDialog) getSupportFragmentManager()
                .findFragmentByTag(EXAMPLE_MACHINE_DIALOG);
        if (newMachineDialog != null) {
            newMachineDialog.dismiss();
        }
    }

    @Override
    public void exampleGrammarDialogClick(Bundle outputBundle) {
        Intent nextActivityIntent = new Intent(this, GrammarActivity.class);
        nextActivityIntent.putExtras(outputBundle);
        startActivity(nextActivityIntent);
        ExampleGrammarDialog newGrammarDialog = (ExampleGrammarDialog) getSupportFragmentManager()
                .findFragmentByTag(EXAMPLE_GRAMMAR_DIALOG);
        if (newGrammarDialog != null) {
            newGrammarDialog.dismiss();
        }
    }

    private void loadMachine() {
        FileSelector fileSelector = new FileSelector();
        fileSelector.setFileSelectedListener(new FileSelector.FileSelectedListener() {
            @Override
            public void onFileSelected(String filePath, FileHandler.Format format) {
                Bundle outputBundle = new Bundle();
                outputBundle.putInt(CONFIGURATION_TYPE, LOAD_MACHINE);
                outputBundle.putBoolean(DEFAULT_FORMAT, format == FileHandler.Format.CMS || format == FileHandler.Format.CMST);
                outputBundle.putString(FILE_NAME, filePath);
                Log.v(TAG, "outputBundle initialized");

                Intent nextActivityIntent = new Intent(MainActivity.this, SimulationActivity.class);
                nextActivityIntent.putExtras(outputBundle);
                startActivity(nextActivityIntent);
                Log.i(TAG, "simulation activity intent executed");
            }
        });
        fileSelector.setExceptionListener(new FileSelector.ExceptionListener() {
            @Override
            public void onException(Exception e) {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.file_not_loaded), Toast.LENGTH_SHORT).show();
            }
        });
        fileSelector.selectFile(this);
    }

}
