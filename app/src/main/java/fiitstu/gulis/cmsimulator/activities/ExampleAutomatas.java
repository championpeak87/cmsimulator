package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.adapters.tasks.AutomataTaskAdapter;
import fiitstu.gulis.cmsimulator.dialogs.ExampleMachineDialog;
import fiitstu.gulis.cmsimulator.dialogs.GuideFragment;
import fiitstu.gulis.cmsimulator.models.automata_tasks.AutomataTask;
import fiitstu.gulis.cmsimulator.models.automata_tasks.FiniteAutomataTask;

import java.util.ArrayList;
import java.util.List;

public class ExampleAutomatas extends FragmentActivity {

    private List<AutomataTask> listOfTasks;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        RecyclerView recyclerView = findViewById(R.id.recyclerview_automata_examples);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 180);

        GridLayoutManager layoutManager = new GridLayoutManager(this, noOfColumns);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example_automatas);

        // menu
        ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.example_machine);

        // create list of tasks
        listOfTasks = new ArrayList<>();
        listOfTasks.add(new FiniteAutomataTask("3k + 1",
                "pocet znakov musi byt 3k + 1",
                false,
                1, "file.xml"));
        listOfTasks.add(new FiniteAutomataTask("an",
                "pocet znakov musi byt 3k + 1",
                true,
                1, "file.xml"));
        listOfTasks.add(new FiniteAutomataTask("3k + 1",
                "pocet znakov musi byt 3k + 1",
                false,
                1, "file.xml"));
        listOfTasks.add(new FiniteAutomataTask("an",
                "pocet znakov musi byt 3k + 1",
                true,
                1, "file.xml"));
        listOfTasks.add(new FiniteAutomataTask("3k + 1",
                "pocet znakov musi byt 3k + 1",
                false,
                1, "file.xml"));
        listOfTasks.add(new FiniteAutomataTask("an",
                "pocet znakov musi byt 3k + 1",
                false,
                1, "file.xml"));
        listOfTasks.add(new FiniteAutomataTask("3k + 1",
                "pocet znakov musi byt 3k + 1",
                false,
                1, "file.xml"));
        listOfTasks.add(new FiniteAutomataTask("an",
                "pocet znakov musi byt 3k + 1",
                false,
                1, "file.xml"));
        listOfTasks.add(new FiniteAutomataTask("3k + 1",
                "pocet znakov musi byt 3k + 1",
                false,
                1, "file.xml"));
        listOfTasks.add(new FiniteAutomataTask("an",
                "pocet znakov musi byt 3k + 1",
                false,
                1, "file.xml"));
        listOfTasks.add(new FiniteAutomataTask("3k + 1",
                "pocet znakov musi byt 3k + 1",
                false,
                1, "file.xml"));
        listOfTasks.add(new FiniteAutomataTask("an",
                "pocet znakov musi byt 3k + 1",
                false,
                1, "file.xml"));
        listOfTasks.add(new FiniteAutomataTask("3k + 1",
                "pocet znakov musi byt 3k + 1",
                false,
                1, "file.xml"));
        listOfTasks.add(new FiniteAutomataTask("an",
                "pocet znakov musi byt 3k + 1",
                false,
                1, "file.xml"));listOfTasks.add(new FiniteAutomataTask("3k + 1",
                "pocet znakov musi byt 3k + 1",
                false,
                1, "file.xml"));
        listOfTasks.add(new FiniteAutomataTask("an",
                "pocet znakov musi byt 3k + 1",
                false,
                1, "file.xml"));
        listOfTasks.add(new FiniteAutomataTask("3k + 1",
                "pocet znakov musi byt 3k + 1",
                false,
                1, "file.xml"));
        listOfTasks.add(new FiniteAutomataTask("an",
                "pocet znakov musi byt 3k + 1",
                false,
                1, "file.xml"));
        listOfTasks.add(new FiniteAutomataTask("3k + 1",
                "pocet znakov musi byt 3k + 1",
                false,
                1, "file.xml"));
        listOfTasks.add(new FiniteAutomataTask("an",
                "pocet znakov musi byt 3k + 1",
                false,
                1, "file.xml"));listOfTasks.add(new FiniteAutomataTask("3k + 1",
                "pocet znakov musi byt 3k + 1",
                false,
                1, "file.xml"));
        listOfTasks.add(new FiniteAutomataTask("an",
                "pocet znakov musi byt 3k + 1",
                false,
                1, "file.xml"));



        RecyclerView recyclerView = findViewById(R.id.recyclerview_automata_examples);
        AutomataTaskAdapter adapter = new AutomataTaskAdapter(this, listOfTasks);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 180);

        GridLayoutManager layoutManager = new GridLayoutManager(this, noOfColumns);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.menu_examples_automatas, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_examples_automatas_help:
                // TODO: FIX SHOWN FRAGMENT TO NEWLY CREATED
                FragmentManager fm = getSupportFragmentManager();
                GuideFragment guideFragment = GuideFragment.newInstance(GuideFragment.TASKS);
                guideFragment.show(fm, "HELP_DIALOG");
                return true;
        }

        return false;
    }


    public void onExampleStart(View view) {
        Bundle outputBundle = new Bundle();
        switch (view.getId()) {
            case R.id.button_popup_main_example1_finite_state_automatom:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE1);
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.FINITE_STATE_AUTOMATON);
                break;
            case R.id.button_popup_main_example2_finite_state_automatom:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE2);
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.FINITE_STATE_AUTOMATON);
                break;
            case R.id.button_popup_main_example3_finite_state_automatom:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE3);
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.FINITE_STATE_AUTOMATON);
                break;
            case R.id.button_popup_main_example1_pushdown_automaton:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE1);
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.PUSHDOWN_AUTOMATON);
                break;
            case R.id.button_popup_main_example2_pushdown_automaton:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE2);
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.PUSHDOWN_AUTOMATON);
                break;
            case R.id.button_popup_main_example3_pushdown_automaton:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE3);
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.PUSHDOWN_AUTOMATON);
                break;
            case R.id.button_popup_main_example1_linear_bounded_automaton:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE1);
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.LINEAR_BOUNDED_AUTOMATON);
                break;
            case R.id.button_popup_main_example2_linear_bounded_automaton:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE2);
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.LINEAR_BOUNDED_AUTOMATON);
                break;
            case R.id.button_popup_main_example1_turing_machine:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE1);
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.TURING_MACHINE);
                break;
            case R.id.button_popup_main_example2_turing_machine:
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE2);
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.TURING_MACHINE);
                break;
        }
        Intent nextActivityIntent = new Intent(this, SimulationActivity.class);
        nextActivityIntent.putExtras(outputBundle);
        startActivity(nextActivityIntent);
    }
}
