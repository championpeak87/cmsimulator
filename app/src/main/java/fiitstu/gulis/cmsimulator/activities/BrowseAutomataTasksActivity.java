package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.adapters.tasks.AutomataTaskAdapter;
import fiitstu.gulis.cmsimulator.elements.Task;
import fiitstu.gulis.cmsimulator.exceptions.NotImplementedException;
import fiitstu.gulis.cmsimulator.models.tasks.automata_tasks.FiniteAutomataTask;
import fiitstu.gulis.cmsimulator.models.tasks.automata_tasks.LinearBoundedAutomataTask;
import fiitstu.gulis.cmsimulator.models.users.User;
import fiitstu.gulis.cmsimulator.network.ServerController;
import fiitstu.gulis.cmsimulator.network.UrlManager;
import fiitstu.gulis.cmsimulator.network.automata_tasks.AutomataTaskParser;
import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class BrowseAutomataTasksActivity extends FragmentActivity {
    private static final String TAG = "BrowseAutomataTasksActi";

    private List<Task> listOfTasks;
    public static int user_id;
    private String authkey;
    public static Context mContext;
    public static BrowseAutomataTasksActivity context;
    public static AutomataTaskAdapter adapter = null;
    private boolean view_results;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRecyclerView();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_browse_automata_tasks);

        mContext = this;
        context = this;
        // menu
        ActionBar actionbar = this.getActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setTitle(getString(R.string.available_tasks));

        if (getIntent().hasExtra("BUNDLE")) {
            view_results = true;
            Bundle inputBundle = getIntent().getBundleExtra("BUNDLE");
            user_id = inputBundle.getInt("USER_ID");
            authkey = inputBundle.getString("AUTHKEY");
            final String first_name = inputBundle.getString("USER_FIRST_NAME");
            final String last_name = inputBundle.getString("USER_LAST_NAME");
            String actionBarTitle = getString(R.string.users_tasks);
            actionBarTitle = actionBarTitle.replace("{0}", last_name + ", " + first_name);
            actionbar.setTitle(actionBarTitle);
        } else {
            view_results = false;
            if (TaskLoginActivity.loggedUser != null) {
                user_id = TaskLoginActivity.loggedUser.getUser_id();
                authkey = TaskLoginActivity.loggedUser.getAuth_key();
            } else {
                user_id = -1;
                authkey = null;
            }
        }

        getListOfTasks();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        this.adapter = null;
        super.onBackPressed();
    }

    public void getListOfTasks() {

        class FetchTasksAsync extends AsyncTask<Void, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showLoadScreen(true);
            }

            @Override
            protected String doInBackground(Void... voids) {
                UrlManager urlManager = new UrlManager();
                URL url = urlManager.getFetchAllAutomataTasksUrl(user_id, authkey);

                ServerController serverController = new ServerController();
                try {
                    return serverController.getResponseFromServer(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                AutomataTaskParser automataTaskParser = new AutomataTaskParser();
                try {
                    List<Task> taskList = automataTaskParser.getTasksFromJsonArray(s);
                    setListOfTasks(taskList);
                    setRecyclerView();

                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    showLoadScreen(false);
                }
            }
        }

        new FetchTasksAsync().execute();
    }

    public void setListOfTasks(List<Task> listOfTasks) {
        this.listOfTasks = listOfTasks;
    }

    private void setRecyclerView() {
        // set recyclerview
        RecyclerView recyclerViewTasks = findViewById(R.id.recyclerview_tasks);
        if (adapter == null) {
            adapter = new AutomataTaskAdapter(this, listOfTasks, view_results);
        } else adapter.setListOfTasks(listOfTasks);
        if (listOfTasks.size() == 0) {
            LinearLayout emptyTasks = findViewById(R.id.linearLayout_empty_tasks);
            emptyTasks.setVisibility(View.VISIBLE);
            showLoadScreen(false);
            LinearLayout emptyRecycler = findViewById(R.id.linearLayout_empty_tasks);
            emptyRecycler.setVisibility(View.VISIBLE);
            recyclerViewTasks.setVisibility(View.GONE);
        } else {
            LinearLayout emptyTasks = findViewById(R.id.linearLayout_empty_tasks);
            emptyTasks.setVisibility(View.GONE);
            showLoadScreen(false);
            LinearLayout emptyRecycler = findViewById(R.id.linearLayout_empty_tasks);
            emptyRecycler.setVisibility(View.GONE);
            recyclerViewTasks.setVisibility(View.VISIBLE);
        }

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 180);

        GridLayoutManager layoutManager = new GridLayoutManager(this, noOfColumns);
        recyclerViewTasks.setLayoutManager(layoutManager);
        recyclerViewTasks.setAdapter(adapter);

        Animation showUpAnimation = AnimationUtils.loadAnimation(this, R.anim.item_show_animation);

        recyclerViewTasks.setAnimation(showUpAnimation);
    }

    public void showEmptyScreen(boolean value) {
        LinearLayout emptyRecycler = this.findViewById(R.id.linearLayout_empty_tasks);
        emptyRecycler.setVisibility(View.VISIBLE);
    }

    private void showLoadScreen(boolean value) {
        ProgressBar progressBar = findViewById(R.id.progressbar_users);
        RecyclerView recyclerView = findViewById(R.id.recyclerview_tasks);
        progressBar.setVisibility(value ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(value ? View.GONE : View.VISIBLE);
    }

}
