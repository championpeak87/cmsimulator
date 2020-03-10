package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.adapters.grammar.MultipleTestsAdapter;
import fiitstu.gulis.cmsimulator.adapters.grammar.TestsAdapter;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.dialogs.NewGrammarTestDialog;
import fiitstu.gulis.cmsimulator.dialogs.NewMachineDialog;
import fiitstu.gulis.cmsimulator.dialogs.NewRegexTestDialog;
import fiitstu.gulis.cmsimulator.elements.*;
import fiitstu.gulis.cmsimulator.exceptions.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

public class GrammarTestingActivity extends FragmentActivity implements NewGrammarTestDialog.OnAddedTestListener {
    private static final String TAG = "GrammarTestingActivity";

    // UI ELEMENTS
    private RecyclerView recyclerView;
    private LinearLayout emptyTests_LinearLayout;
    private RelativeLayout progress_RelativeLayout;

    // INTENT EXTRA KEYS
    public static final String SOLVE_MODE = "SOLVE_MODE";
    public static final String CONFIGURATION_MODE = "CONFIGURATION_MODE";
    public static final String GRAMMAR_TYPE = "GRAMMAR_TYPE";

    private boolean solveMode = false;
    private boolean configurationMode = false;
    private String grammarType;

    // RecyclerView Adapter
    TestsAdapter adapter = null;

    private UniqueQueue<String> queue = new UniqueQueue<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_bulk_test);

        setActionBar();
        setUIElements();
        handleIntentExtras();
        setRecyclerView();
    }

    private void handleIntentExtras() {
        Intent intent = this.getIntent();
        solveMode = intent.getBooleanExtra(SOLVE_MODE, false);
        configurationMode = intent.getBooleanExtra(CONFIGURATION_MODE, false);
        grammarType = intent.getStringExtra(GRAMMAR_TYPE);
    }

    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        MenuItem addTest = menu.findItem(R.id.menu_add_test);
        MenuItem addRegexTest = menu.findItem(R.id.menu_add_test_regex);
        MenuItem configuration = menu.findItem(R.id.menu_bulk_test_configure);
        MenuItem simulate = menu.findItem(R.id.menu_bulk_test_simulate);
        MenuItem saveMachine = menu.findItem(R.id.menu_bulk_test_save_machine);
        MenuItem testSpecification = menu.findItem(R.id.menu_bulk_test_specification);

        configuration.setVisible(false);
        simulate.setVisible(false);
        saveMachine.setVisible(false);
        testSpecification.setVisible(false);

        if (solveMode) {
            addRegexTest.setVisible(false);
            addTest.setVisible(false);
        }

        return true;
    }

    private void setActionBar() {
        ActionBar actionBar = this.getActionBar();
        actionBar.setTitle(R.string.bulk_test);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.menu_bulk_test, menu);

        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (recyclerView != null)
                recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            if (recyclerView != null)
                recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_add_test:
                FragmentManager fm = getSupportFragmentManager();
                NewGrammarTestDialog newGrammarTestDialog = new NewGrammarTestDialog();
                newGrammarTestDialog.show(fm, TAG);
                return true;
            case R.id.menu_add_test_regex:
                NewRegexTestDialog dialog = new NewRegexTestDialog(RegexTest.TestVerification.GRAMMAR);
                dialog.setAdapter(adapter);
                dialog.show(getSupportFragmentManager(), TAG);
                return true;
            case R.id.menu_run_test:
                // TODO: EXECUTE TESTS

                List<String> stringList = adapter.getListOfInputWords();
                List<TestWord> testWordList = new ArrayList<>();
                for (String test : stringList) {
                    testWordList.add(new TestWord(test, 0, false));
                }

                String result;

                for (int i = 0; i < testWordList.size(); i++) {
                    TestWord testWord = testWordList.get(i);
                    if (testWord.getWord() != null) {
                        result = simulateGrammar(testWord.getWord(), grammarType);

                        if (result.equals(getString(R.string.accept))) {
                            testWord.setResult(true);
                            adapter.markTestResult(testWord.getWord(), true);
                        } else {
                            testWord.setResult(false);
                            adapter.markTestResult(testWord.getWord(), false);
                        }

                        testWordList.set(i, testWord);
                        queue.clear();
                    }
                }


                return true;
            case R.id.menu_bulk_test_help:
                // TODO: IMPLEMENT HELP
                try {
                    throw new NotImplementedException(this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
        }

        return false;
    }

    private void setUIElements() {
        this.recyclerView = findViewById(R.id.recyclerView_bulk_test_scenarios);
        this.emptyTests_LinearLayout = findViewById(R.id.linearLayout_empty_tests);
        this.progress_RelativeLayout = findViewById(R.id.relativeLayout_bulktest_working);
    }

    private void showEmptyScreen(boolean value) {
        this.recyclerView.setVisibility(value ? View.GONE : View.VISIBLE);
        this.emptyTests_LinearLayout.setVisibility(value ? View.VISIBLE : View.GONE);
    }

    private void showLoadingScreen(boolean value) {
        this.recyclerView.setForeground(value ? new ColorDrawable(0x4d757575) : new ColorDrawable(0x00000000));
        if (emptyTests_LinearLayout.getVisibility() == View.VISIBLE)
            emptyTests_LinearLayout.setVisibility(View.GONE);
        this.progress_RelativeLayout.setVisibility(value ? View.VISIBLE : View.GONE);
    }

    private void setRecyclerView() {
        DataSource dataSource = DataSource.getInstance();
        dataSource.open();
        List<String> tests = dataSource.getGrammarTests();
        showEmptyScreen(tests.size() <= 0);
        dataSource.close();

        if (adapter == null) {
            adapter = new TestsAdapter(this, tests, solveMode);
            adapter.setOnDataSetChangedListener(new TestsAdapter.OnDataSetChangedListener() {
                @Override
                public void OnDataChanged() {
                    int numberOfTests = adapter.getItemCount();
                    showEmptyScreen(numberOfTests <= 0);
                }
            });
        }

        recyclerView.setAdapter(adapter);
        Resources res = getApplication().getResources();
        int colSpan = res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 2 : 1;

        recyclerView.setLayoutManager(new GridLayoutManager(this, colSpan));
    }

    @Override
    public void OnAdd(String inputWord) {
        adapter.addNewTest(inputWord);
    }

    private String simulateGrammar(String input, String grammarType) {
        List<GrammarRule> startingRules = filterStartingRules();
        String current;
        StringBuilder temp;
        long startTime, stopTime;
        DataSource dataSource = DataSource.getInstance();
        dataSource.open();
        List<GrammarRule> grammarRuleList = dataSource.getGrammarRuleFullExtract();

        for (GrammarRule grammarRule : startingRules) {
            current = grammarRule.getGrammarRight();
            queue.add(current);
        }

        startTime = System.currentTimeMillis();
        while (!queue.isEmpty()) {
            current = queue.remove();
            stopTime = System.currentTimeMillis();

            if (current.equals(input)) {
                return getString(R.string.accept);
            }

            if (stopTime - startTime > 3000)
                return getString(R.string.reject);

            for (GrammarRule grammarRule : grammarRuleList) {
                int index = 0;

                String rightGrammarRule = grammarRule.getGrammarRight();
                String leftGrammarRule = grammarRule.getGrammarLeft();
                if (rightGrammarRule != null && leftGrammarRule != null)
                    while ((index = (current.indexOf(leftGrammarRule, index) + 1)) > 0) {
                        temp = new StringBuilder(current);
                        if (rightGrammarRule != null && leftGrammarRule != null && rightGrammarRule.equals("ε")) {
                            temp.replace(index - 1, index + leftGrammarRule.length() - 1, "");
                            if (temp.toString().equals(input)) {
                                return getString(R.string.accept);
                            }
                        } else {
                            if (leftGrammarRule != null && rightGrammarRule != null) {
                                temp.replace(index - 1, index + leftGrammarRule.length() - 1, rightGrammarRule);
                                if (temp.toString().equals(input)) {
                                    return getString(R.string.accept);
                                }
                            }
                        }

                        if (grammarType.equals("Unrestricted")) {
                            queue.add(temp.toString());
                        } else {
                            if (temp.length() <= input.length() + 1) {
                                queue.add(temp.toString());
                            }
                        }
                    }
            }
        }
        return getString(R.string.reject);
    }

    private List<GrammarRule> filterStartingRules() {
        List<GrammarRule> startingRules = new ArrayList<>();

        DataSource dataSource = DataSource.getInstance();
        dataSource.open();
        List<GrammarRule> grammarRuleList = dataSource.getGrammarRuleFullExtract();
        for (GrammarRule grammarRule : grammarRuleList) {
            if (grammarRule != null) {
                String leftRule = grammarRule.getGrammarLeft();
                if (leftRule != null && leftRule.equals("S")) {
                    startingRules.add(grammarRule);
                }
            }
        }

        return startingRules;
    }
}
