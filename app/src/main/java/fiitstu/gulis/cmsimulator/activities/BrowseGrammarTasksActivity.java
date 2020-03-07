package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.exceptions.NotImplementedException;

public class BrowseGrammarTasksActivity extends FragmentActivity {
    private static final String TAG = "BrowseGrammarTasksActiv";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_browse_grammar_tasks);

        setActionBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.menu_browse_automata_tasks, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_help:
                // TODO: IMPLEMENT HELP
                try {
                    throw new NotImplementedException(this);
                } catch (NotImplementedException e) {
                    Log.w(TAG, "onOptionsItemSelected: " + e.getMessage(), e);
                } finally {
                    return true;
                }
        }

        return false;
    }

    private void setActionBar() {
        ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.available_tasks);
    }
}
