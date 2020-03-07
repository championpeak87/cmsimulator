package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.adapters.tasks.GrammarTaskAdapter;
import fiitstu.gulis.cmsimulator.exceptions.NotImplementedException;
import fiitstu.gulis.cmsimulator.models.tasks.grammar_tasks.GrammarTask;
import fiitstu.gulis.cmsimulator.network.ServerController;
import fiitstu.gulis.cmsimulator.network.UrlManager;
import fiitstu.gulis.cmsimulator.network.grammar_tasks.GrammarTasksParser;
import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BrowseGrammarTasksActivity extends FragmentActivity {
    private static final String TAG = "BrowseGrammarTasksActiv";

    // RECYCLERVIEW ADAPTER
    private GrammarTaskAdapter adapter = null;
    private List<GrammarTask> grammarTaskList = new ArrayList<>();

    // UI ELEMENTS
    private RecyclerView recyclerView;
    private LinearLayout empty_LinearLayout;
    private ProgressBar loading_ProgressBar;
    private ConstraintLayout content_ConstraintLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_browse_grammar_tasks);

        setActionBar();
        setUIElements();
        fetchTasks();
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
            recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        else
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    }

    private void setActionBar() {
        ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.available_tasks);
    }

    private void setUIElements() {
        this.recyclerView = findViewById(R.id.recycler_view_grammar_tasks);
        this.empty_LinearLayout = findViewById(R.id.linearLayout_empty_tasks);
        this.loading_ProgressBar = findViewById(R.id.progressbar_loading);
        this.content_ConstraintLayout = findViewById(R.id.constraintLayout_content);
    }

    private void fetchTasks() {
        class FetchGrammarTasksAsync extends AsyncTask<Void, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                showLoadingScreen(true);
            }

            @Override
            protected String doInBackground(Void... voids) {
                UrlManager urlManager = new UrlManager();
                URL fetchTasksURL = urlManager.getAllGrammarTasksUrl(TaskLoginActivity.loggedUser.getUser_id());
                ServerController serverController = new ServerController();
                String output = null;

                try {
                    output = serverController.getResponseFromServer(fetchTasksURL);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    return output;
                }

            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                if (s == null || s.isEmpty()) {
                    Toast.makeText(BrowseGrammarTasksActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                } else {
                    GrammarTasksParser grammarTasksParser = GrammarTasksParser.getInstance();
                    try {
                        grammarTaskList = grammarTasksParser.getTasksFromJSONArray(s);
                    } catch (JSONException e) {
                        Toast.makeText(BrowseGrammarTasksActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                    }
                }

                showLoadingScreen(false);
                setRecyclerView();
            }
        }

        new FetchGrammarTasksAsync().execute();
    }

    private void setRecyclerView() {
        if (adapter == null)
            adapter = new GrammarTaskAdapter(this, grammarTaskList);

        adapter.setDatasetChangedListener(new GrammarTaskAdapter.DatasetChangedListener() {
            @Override
            public void onDataChange() {
                final int tasksCount = adapter.getItemCount();
                boolean taskEmpty = tasksCount <= 0;

                showEmptyScreen(taskEmpty);
            }
        });


        recyclerView.setAdapter(adapter);

        final int orientation = Resources.getSystem().getConfiguration().orientation;
        recyclerView.setLayoutManager(new GridLayoutManager(this, orientation == Configuration.ORIENTATION_LANDSCAPE ? 4 : 2));
        showEmptyScreen(grammarTaskList.size() <= 0);
    }

    private void showLoadingScreen(boolean value) {
        this.loading_ProgressBar.setVisibility(value ? View.VISIBLE : View.GONE);
        this.content_ConstraintLayout.setForeground(value ? new ColorDrawable(0x4d757575) : new ColorDrawable(0x00000000));
    }

    private void showEmptyScreen(boolean value) {
        this.recyclerView.setVisibility(value ? View.GONE : View.VISIBLE);
        this.empty_LinearLayout.setVisibility(value ? View.VISIBLE : View.GONE);
    }
}
