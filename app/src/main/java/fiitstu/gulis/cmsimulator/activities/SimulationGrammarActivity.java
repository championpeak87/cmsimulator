package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.adapters.grammar.GrammarSimulationAdapter;
import fiitstu.gulis.cmsimulator.dialogs.GuideFragment;
import fiitstu.gulis.cmsimulator.elements.GeneratedWord;
import fiitstu.gulis.cmsimulator.elements.GrammarRule;
import fiitstu.gulis.cmsimulator.elements.UniqueQueue;
import fiitstu.gulis.cmsimulator.fragments.DerivationTableFragment;
import fiitstu.gulis.cmsimulator.fragments.DerivationTreeFragment;
import fiitstu.gulis.cmsimulator.fragments.FixedDerivationFragment;
import fiitstu.gulis.cmsimulator.fragments.LinearDerivationFragment;

/**
 * Grammar simulation activity.
 *
 * Created by Krisztian Tóth.
 *
 */
public class SimulationGrammarActivity extends FragmentActivity {
    //log tag
    private static final String TAG = GrammarActivity.class.getName();

    //strings
    private static final String DERIVATION_TABLE = "Derivation Table";
    private static final String LINEAR_DERIVATION = "Linear Derivation";
    private static final String FIXED_DERIVATION = "Fixed Derivation";
    private static final String DERIVATION_TREE = "Derivation Tree";
    private static final String REGULAR = "Regular";
    private static final String CONTEXT_FREE = "Context-Free";
    private static final String HELP_DIALOG = "HELP_DIALOG";
    private static final String GRAMMAR_LIST = "GrammarList";
    private static final String GRAMMAR_TYPE = "GrammarType";
    private static final String EPSILON  = "ε";
    private static final String UNRESTRICTED = "Unrestricted";
    private static final String START = "S";

    //variables
    private List<GrammarRule> grammarRuleList;
    private String grammarType;
    private Map<String, GeneratedWord> path = new HashMap<>();
    private UniqueQueue<String> queue = new UniqueQueue<>();
    private List<GeneratedWord> backTrackList = new ArrayList<>();
    private String selectedVisualization = DERIVATION_TABLE;
    private int StepCount = 0;

    /**
     * Defines the usage of all the buttons in the activity
     * @param savedInstaceState Bundle of arguments passed to this activity
     */
    @Override
    public void onCreate(Bundle savedInstaceState) {
        super.onCreate(savedInstaceState);
        setContentView(R.layout.activity_simulation_grammar);

        // menu
        ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.grammar_simulation);

        //test input
        Button testInputButton = findViewById(R.id.button_grammar_simulation_test);
        testInputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText inputEditText = findViewById(R.id.editText_grammar_simulation_input);
                TextView resultTextView = findViewById(R.id.textView_grammar_simulation_result);
                String result = simulateGrammar(inputEditText.getText().toString(), grammarType);
                queue.clear();

                resultTextView.setText(result);

               if(result.equals(getString(R.string.accept))) {
                    StepCount = 0;
                    backTrackList = backTrackPath(inputEditText.getText().toString());
                    List<GeneratedWord> stepList = stepSimulation();
                    switch (selectedVisualization){
                        case DERIVATION_TABLE:
                            Fragment derivationTableFragment = DerivationTableFragment.newInstance(stepList);
                            replaceFragment(derivationTableFragment);
                            break;
                        case LINEAR_DERIVATION:
                            Fragment derivationLinearFragment;
                            if(StepCount == backTrackList.size()-1){
                                derivationLinearFragment = LinearDerivationFragment.newInstance(stepList, true);
                            }else{
                                derivationLinearFragment = LinearDerivationFragment.newInstance(stepList, false);
                            }
                            replaceFragment(derivationLinearFragment);
                            break;
                        case FIXED_DERIVATION:
                            Fragment derivationFixedFragment;
                            if(StepCount == backTrackList.size()-1) {
                                derivationFixedFragment = FixedDerivationFragment.newInstance(stepList, true);
                            }else{
                                derivationFixedFragment = FixedDerivationFragment.newInstance(stepList, false);
                            }
                            replaceFragment(derivationFixedFragment);
                            break;
                        case DERIVATION_TREE:
                            Fragment derivationTreeFragment = DerivationTreeFragment.newInstance(stepList);
                            replaceFragment(derivationTreeFragment);
                    }
               }else{
                   backTrackList.clear();
                   switch(selectedVisualization){
                       case DERIVATION_TABLE:
                           Fragment derivationTableFragment = DerivationTableFragment.newInstance(backTrackList);
                           replaceFragment(derivationTableFragment);
                           break;
                       case LINEAR_DERIVATION:
                           Fragment derivationLineaFragment = LinearDerivationFragment.newInstance(backTrackList, false);
                           replaceFragment(derivationLineaFragment);
                           break;
                       case FIXED_DERIVATION:
                           Fragment derivationFixedFragment = FixedDerivationFragment.newInstance(backTrackList, false);
                           replaceFragment(derivationFixedFragment);
                           break;
                       case DERIVATION_TREE:
                           Fragment derivationTreeFragment = DerivationTreeFragment.newInstance(backTrackList);
                           replaceFragment(derivationTreeFragment);
                   }
               }

                LinearLayout grammarSimulationMainLayout = findViewById(R.id.linearLayout_grammar_simulation);

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm != null) {
                    imm.hideSoftInputFromWindow(grammarSimulationMainLayout.getWindowToken(), 0);
                }
            }
        });

        //step
        Button stepButton = findViewById(R.id.button_grammar_simulation_step);
        stepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<GeneratedWord> stepList;
                if(StepCount < backTrackList.size() - 1) {
                    StepCount++;
                    stepList = stepSimulation();

                    switch (selectedVisualization) {
                        case DERIVATION_TABLE:
                            Fragment derivationTableFragment = DerivationTableFragment.newInstance(stepList);
                            replaceFragment(derivationTableFragment);
                            break;
                        case LINEAR_DERIVATION:
                            Fragment derivationLinearFragment;
                            if(StepCount == backTrackList.size()-1){
                                derivationLinearFragment = LinearDerivationFragment.newInstance(stepList, true);
                            }else{
                                derivationLinearFragment = LinearDerivationFragment.newInstance(stepList, false);
                            }
                            replaceFragment(derivationLinearFragment);
                            break;
                        case FIXED_DERIVATION:
                            Fragment derivationFixedFragment;
                            if(StepCount == backTrackList.size()-1) {
                                derivationFixedFragment = FixedDerivationFragment.newInstance(stepList, true);
                            }else{
                                derivationFixedFragment = FixedDerivationFragment.newInstance(stepList, false);
                            }
                            replaceFragment(derivationFixedFragment);
                            break;
                        case DERIVATION_TREE:
                            Fragment derivationTreeFragment = DerivationTreeFragment.newInstance(stepList);
                            replaceFragment(derivationTreeFragment);
                    }
                }else{
                    Toast.makeText(SimulationGrammarActivity.this, R.string.grammar_simulation_end, Toast.LENGTH_SHORT).show();
                }

                LinearLayout grammarSimulationMainLayout = findViewById(R.id.linearLayout_grammar_simulation);

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm != null) {
                    imm.hideSoftInputFromWindow(grammarSimulationMainLayout.getWindowToken(), 0);
                }
            }
        });

        //step back
        Button stepBackButton = findViewById(R.id.button_grammar_simulation_step_back);
        stepBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<GeneratedWord> stepList;
                if(StepCount < backTrackList.size() && StepCount > 0) {
                    StepCount--;
                    stepList = stepSimulation();

                    switch (selectedVisualization) {
                        case DERIVATION_TABLE:
                            Fragment derivationTableFragment = DerivationTableFragment.newInstance(stepList);
                            replaceFragment(derivationTableFragment);
                            break;
                        case LINEAR_DERIVATION:
                            Fragment derivationLinearFragment = LinearDerivationFragment.newInstance(stepList, false);
                            replaceFragment(derivationLinearFragment);
                            break;
                        case FIXED_DERIVATION:
                            Fragment derivationFixedFragment = FixedDerivationFragment.newInstance(stepList, false);
                            replaceFragment(derivationFixedFragment);
                            break;
                        case DERIVATION_TREE:
                            Fragment derivationTreeFragment = DerivationTreeFragment.newInstance(stepList);
                            replaceFragment(derivationTreeFragment);
                    }
                }else{
                    Toast.makeText(SimulationGrammarActivity.this, R.string.grammar_simulation_end, Toast.LENGTH_SHORT).show();
                }

                LinearLayout grammarSimulationMainLayout = findViewById(R.id.linearLayout_grammar_simulation);

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm != null) {
                    imm.hideSoftInputFromWindow(grammarSimulationMainLayout.getWindowToken(), 0);
                }
            }
        });

        final Button clearButton = findViewById(R.id.button_grammar_simulation_clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText inputEditText = findViewById(R.id.editText_grammar_simulation_input);
                inputEditText.setText("");
            }
        });

        final Button multipleTestsButton = findViewById(R.id.button_grammar_simulation_multiple_tests);
        multipleTestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextActivityIntent = new Intent(SimulationGrammarActivity.this, MultipleTestsActivity.class);
                nextActivityIntent.putExtra(GRAMMAR_LIST, (Serializable)grammarRuleList);
                nextActivityIntent.putExtra(GRAMMAR_TYPE, (Serializable)grammarType);
                startActivity(nextActivityIntent);
            }
        });

        RecyclerView recyclerViewRules = findViewById(R.id.recyclerViewRules);
        Bundle bundle = getIntent().getExtras();

        if(bundle != null) {
            grammarRuleList = (List<GrammarRule>) bundle.getSerializable(GRAMMAR_LIST);
            grammarType = (String) bundle.getSerializable(GRAMMAR_TYPE);

            GrammarSimulationAdapter grammarSimulationAdapter = new GrammarSimulationAdapter(grammarRuleList);
            recyclerViewRules.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewRules.setAdapter(grammarSimulationAdapter);

        }

        Fragment derivationTableFragment = DerivationTableFragment.newInstance(backTrackList);
        replaceFragment(derivationTableFragment);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.menu_grammar_simulation, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        List<GeneratedWord> stepList = new ArrayList<>();
        if(backTrackList.size() > 0) {
            stepList = stepSimulation();
        }
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_grammar_simulation_help:
                FragmentManager fm = getSupportFragmentManager();
                GuideFragment guideFragment = GuideFragment.newInstance(GuideFragment.SIMULATION_GRAMMAR);
                guideFragment.show(fm, HELP_DIALOG);
                return true;
            case R.id.menu_derivation_table:
                Fragment derivationTableFragment = DerivationTableFragment.newInstance(stepList);
                replaceFragment(derivationTableFragment);
                selectedVisualization = DERIVATION_TABLE;
                break;
            case R.id.menu_derivation_linear:
                Fragment derivationLinearFragment;
                if(StepCount == backTrackList.size()-1){
                    derivationLinearFragment = LinearDerivationFragment.newInstance(stepList, true);
                }else{
                    derivationLinearFragment = LinearDerivationFragment.newInstance(stepList, false);
                }
                replaceFragment(derivationLinearFragment);
                selectedVisualization = LINEAR_DERIVATION;
                break;
            case R.id.menu_derivation_fixed:
                Fragment derivationFixedFragment;
                if(StepCount == backTrackList.size()-1) {
                    derivationFixedFragment = FixedDerivationFragment.newInstance(stepList, true);
                }else{
                    derivationFixedFragment = FixedDerivationFragment.newInstance(stepList, false);
                }
                replaceFragment(derivationFixedFragment);
                selectedVisualization = FIXED_DERIVATION;
                break;
            case R.id.menu_derivation_tree:
                if(grammarType.equals(REGULAR) || grammarType.equals(CONTEXT_FREE)) {
                    Fragment derivationTreeFragment = DerivationTreeFragment.newInstance(stepList);
                    replaceFragment(derivationTreeFragment);
                    selectedVisualization = DERIVATION_TREE;
                }else{
                    Toast.makeText(SimulationGrammarActivity.this, R.string.grammar_simulation_tree_constraint, Toast.LENGTH_SHORT).show();
                }
        }
        return false;
    }

    /**
     * Method for handling the back button press
     */
    @Override
    public void onBackPressed() {
        Log.v(TAG, "onBackPressed method started");
        finish();
        super.onBackPressed();
    }

    /**
     * Method the extracts all the rules with a starting non-terminal symbols. It is used in the simulation process
     * so the algorithm knows where to start the simulation
     * @return list of grammar rules containing the rules with a starting non-terminal symbol
     */
    private List<GrammarRule> filterStartingRules(){
        List<GrammarRule> startingRules = new ArrayList<>();

        for(GrammarRule grammarRule : grammarRuleList){
            if(grammarRule.getGrammarLeft().equals(START)){
                startingRules.add(grammarRule);
            }
        }

        return startingRules;
    }

    /**
     * Method for checking if the input word belongs to the defined grammar. Method uses breadth first search algorithm
     * with the UniqueQueue data structure. The UniqueQueue is used for having FIFO behaviour and for filtering
     * duplicate states. The method also creates a hash map which contains all the generated words, this has map
     * will be used for backtracking the path of the simulation. It uses the GeneratedWord object which contains
     * the word and the rule used for its generation.
     * @param input a string to check if it belongs to the defined grammar
     * @param grammarType type of the used grammar
     * @return string that means if the input word belongs to the defined grammar
     */
    private String simulateGrammar(String input, String grammarType){
        List<GrammarRule> startingRules = filterStartingRules();
        String current;
        StringBuilder temp;
        long startTime, stopTime;
        path.clear();
        path.put(START, null);

        if(input.equals("")){
            input = EPSILON;
            EditText inputEditText = findViewById(R.id.editText_grammar_simulation_input);
            inputEditText.setText(EPSILON);
        }

        for(GrammarRule grammarRule : startingRules){
            current = grammarRule.getGrammarRight();
            queue.add(current);
            path.put(current, new GeneratedWord(START, grammarRule));
        }

        startTime = System.currentTimeMillis();
        while(!queue.isEmpty()){
            current = queue.remove();
            stopTime = System.currentTimeMillis();

            if(current.equals(input)){
                return getString(R.string.accept);
            }

            if(stopTime - startTime > 3000)
                return getString(R.string.reject);

            for(GrammarRule grammarRule : grammarRuleList){
                int index =0;

                while((index=(current.indexOf(grammarRule.getGrammarLeft(),index)+1))>0){
                    temp = new StringBuilder(current);
                    if(grammarRule.getGrammarRight().equals(EPSILON)) {
                        temp.replace(index-1, index+grammarRule.getGrammarLeft().length()-1, "");
                        if(temp.toString().equals(input)){
                            path.put(temp.toString(), new GeneratedWord(current, grammarRule));
                            return getString(R.string.accept);
                        }
                    }
                    else {
                        temp.replace(index-1, index+grammarRule.getGrammarLeft().length()-1, grammarRule.getGrammarRight());
                        if(temp.toString().equals(input)){
                            path.put(temp.toString(), new GeneratedWord(current, grammarRule));
                            return getString(R.string.accept);
                        }
                    }

                    if(grammarType.equals(UNRESTRICTED)){
                        queue.add(temp.toString());
                        path.put(temp.toString(), new GeneratedWord(current, grammarRule));
                    }else {
                        if (temp.length() <= input.length()+1) {
                            queue.add(temp.toString());
                            path.put(temp.toString(), new GeneratedWord(current, grammarRule));
                        }
                    }
                }
            }
        }
        return getString(R.string.reject);
    }

    /**
     * Method for backtracking the simulation path. At the end of the simulation method a hash map is created.
     * This method iterates through this hash map starting from the end and backtracks the path to the beginning
     * through all the predecessors.
     * @param input string that is at the end of the simulation
     * @return a list of generated words containing the path from the beginning to the end of the simulation
     */
    private List<GeneratedWord> backTrackPath(String input){
        List<GeneratedWord> backPath = new ArrayList<>();
        GeneratedWord parent = path.get(input);

        backPath.add(new GeneratedWord(input, parent.getUsedRule()));

        while(parent != null){
            if(path.get(parent.getWord()) != null)
                backPath.add(new GeneratedWord(parent.getWord(), path.get(parent.getWord()).getUsedRule()));
            else
                backPath.add(new GeneratedWord(parent.getWord(), null));
            parent = path.get(parent.getWord());

        }

        for(int i = 0; i < backPath.size() / 2; i++)
        {
            GeneratedWord temp = backPath.get(i);
            backPath.set(i, backPath.get(backPath.size() - i - 1));
            backPath.set(backPath.size()-i-1, temp);
        }

        return backPath;
    }

    /**
     * Method for handling the fragment replacement
     * @param newFragment a fragment to be shown on the screen
     */
    public void replaceFragment(Fragment newFragment){
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_derivation, newFragment);
        fragmentTransaction.commit();
    }

    /**
     * Method for handling the stepping of the simulation
     * @return list of generated words containing the required amount of data for the specific step
     */
    private List<GeneratedWord> stepSimulation(){
        List<GeneratedWord> stepList = new ArrayList<>();
        for(int i = 0; i <= StepCount; i++){
            stepList.add(backTrackList.get(i));
        }

        return stepList;
    }
}
