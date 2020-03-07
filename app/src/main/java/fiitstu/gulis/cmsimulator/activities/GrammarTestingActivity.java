package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
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
import fiitstu.gulis.cmsimulator.adapters.grammar.TestsAdapter;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.dialogs.NewGrammarTestDialog;
import fiitstu.gulis.cmsimulator.dialogs.NewMachineDialog;

import java.util.List;

public class GrammarTestingActivity extends FragmentActivity implements NewGrammarTestDialog.OnAddedTestListener {
    private static final String TAG = "GrammarTestingActivity";

    // UI ELEMENTS
    private RecyclerView recyclerView;
    private LinearLayout emptyTests_LinearLayout;
    private RelativeLayout progress_RelativeLayout;

    // RecyclerView Adapter
    TestsAdapter adapter = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_bulk_test);

        setActionBar();
        setUIElements();
        setRecyclerView();
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

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            if (recyclerView != null)
                recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        }
        else
        {
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
                // TODO: ADD TEST
                return true;
            case R.id.menu_add_test_regex:
                // TODO: ADD REGEX TEST
                return true;
            case R.id.menu_run_test:
                // TODO: EXECUTE TESTS
                return true;
            case R.id.menu_bulk_test_help:
                // TODO: IMPLEMENT HELP
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
            adapter = new TestsAdapter(this, tests);
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
}