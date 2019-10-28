package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import java.util.ArrayList;
import java.util.List;

import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.adapters.grammar.MultipleTestsAdapter;
import fiitstu.gulis.cmsimulator.dialogs.GuideFragment;
import fiitstu.gulis.cmsimulator.elements.GrammarRule;
import fiitstu.gulis.cmsimulator.elements.TestWord;
import fiitstu.gulis.cmsimulator.elements.UniqueQueue;

/**
 * Multiple tests activity.
 *
 * Created by Krisztian Toth.
 */
public class MultipleTestsActivity extends FragmentActivity {

    //log tag
    private static final String TAG = GrammarActivity.class.getName();

    //variables
    private RecyclerView recyclerView;
    private MultipleTestsAdapter multipleTestsAdapter;
    private int multipleTestsTableSize = 10;
    private List<TestWord> testWordList;
    private String grammarType;
    private List<GrammarRule> grammarRuleList;
    private UniqueQueue<String> queue = new UniqueQueue<>();

    /**
     * Defines the usage of all the buttons in the activity
     * @param savedInstaceState Bundle of arguments passed to this activity
     */
    @Override
    public void onCreate(Bundle savedInstaceState) {
        super.onCreate(savedInstaceState);
        setContentView(R.layout.activity_multiple_tests);

        recyclerView = findViewById(R.id.recyclerView_multiple_tests);
        multipleTestsAdapter = new MultipleTestsAdapter(multipleTestsTableSize);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(multipleTestsAdapter);
        recyclerView.setItemViewCacheSize(multipleTestsTableSize);

        //menu
        ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.bulk_test);

        Bundle bundle = getIntent().getExtras();

        if(bundle != null){
            grammarType = (String) bundle.getSerializable("GrammarType");
            grammarRuleList = (List<GrammarRule>) bundle.getSerializable("GrammarList");
        }

        final Button testAllButton = findViewById(R.id.button_multiple_tests_test_all);
        testAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testWordList = multipleTestsAdapter.getTestWordList();

                String result;

                for(int i = 0; i < testWordList.size(); i++){
                    TestWord testWord = testWordList.get(i);
                    if(testWord.getWord() != null) {
                        result = simulateGrammar(testWord.getWord(), grammarType);

                        if(result.equals(getString(R.string.accept))){
                            testWord.setResult(true);
                        }else{
                            testWord.setResult(false);
                        }

                        testWordList.set(i,testWord);
                        queue.clear();
                    }
                }

                multipleTestsAdapter = new MultipleTestsAdapter(multipleTestsTableSize);
                multipleTestsAdapter.setTestWordList(testWordList);
                recyclerView.setAdapter(multipleTestsAdapter);
                recyclerView.setItemViewCacheSize(multipleTestsTableSize);
            }
        });

        final Button clearButton = findViewById(R.id.button_multiple_tests_clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(testWordList != null) {
                    testWordList.clear();

                    multipleTestsAdapter = new MultipleTestsAdapter(multipleTestsTableSize);
                    multipleTestsAdapter.setTestWordList(testWordList);
                    recyclerView.setAdapter(multipleTestsAdapter);
                    recyclerView.setItemViewCacheSize(multipleTestsTableSize);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.menu_multiple_tests, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_multiple_tests_help:
                FragmentManager fm = getSupportFragmentManager();
                GuideFragment guideFragment = GuideFragment.newInstance(GuideFragment.MULTIPLE_TESTS);
                guideFragment.show(fm, "HELP_DIALOG");
                return true;
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
            if(grammarRule.getGrammarLeft().equals("S")){
                startingRules.add(grammarRule);
            }
        }

        return startingRules;
    }

    /**
     * A modified version of the simulation method from the simulation grammar activity. This version
     * does not include the hash map creation for backtracking since in the multiple tests activity
     * no backtracking is needed.
     * @param input a string to check if it belongs to the defined grammar
     * @param grammarType type of the used grammar
     * @return string that means if the input word belongs to the defined grammar
     */
    private String simulateGrammar(String input, String grammarType){
        List<GrammarRule> startingRules = filterStartingRules();
        String current;
        StringBuilder temp;
        long startTime, stopTime;

        for(GrammarRule grammarRule : startingRules){
            current = grammarRule.getGrammarRight();
            queue.add(current);
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
                    if(grammarRule.getGrammarRight().equals("ε")) {
                        temp.replace(index-1, index+grammarRule.getGrammarLeft().length()-1, "");
                        if(temp.toString().equals(input)){
                            return getString(R.string.accept);
                        }
                    }
                    else {
                        temp.replace(index-1, index+grammarRule.getGrammarLeft().length()-1, grammarRule.getGrammarRight());
                        if(temp.toString().equals(input)){
                            return getString(R.string.accept);
                        }
                    }

                    if(grammarType.equals("Unrestricted")){
                        queue.add(temp.toString());
                    }else {
                        if (temp.length() <= input.length()+1) {
                            queue.add(temp.toString());
                        }
                    }
                }
            }
        }
        return getString(R.string.reject);
    }
}
