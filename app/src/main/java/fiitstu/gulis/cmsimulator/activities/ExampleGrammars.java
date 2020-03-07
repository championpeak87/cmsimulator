package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.adapters.tasks.ExampleGrammarAdapter;
import fiitstu.gulis.cmsimulator.exceptions.NotImplementedException;

public class ExampleGrammars extends FragmentActivity {
    private static final String TAG = "ExampleGrammars";
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_browse_example_grammar_tasks);

        setActionBar();
        setUIElements();
        setRecyclerView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.menu_browse_automata_tasks, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_help:
                // TODO: CREATE HELP MENU
                try {
                    throw new NotImplementedException(this);
                } catch (NotImplementedException e) {
                    Log.w(TAG, e.getMessage(), e);
                }
                return true;
        }

        return false;
    }

    private void setActionBar()
    {
        ActionBar actionBar = this.getActionBar();
        actionBar.setTitle(R.string.example_grammar);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setUIElements()
    {
        this.recyclerView = findViewById(R.id.recyclerView_example_grammars);
    }

    private void setRecyclerView()
    {
        ExampleGrammarAdapter adapter = new ExampleGrammarAdapter(this);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 180);

        GridLayoutManager layoutManager = new GridLayoutManager(this, noOfColumns);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }
}
