package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.database.FileFormatException;
import fiitstu.gulis.cmsimulator.database.FileHandler;
import fiitstu.gulis.cmsimulator.dialogs.*;
import fiitstu.gulis.cmsimulator.elements.Task;
import fiitstu.gulis.cmsimulator.models.users.Admin;
import fiitstu.gulis.cmsimulator.models.users.Lector;
import fiitstu.gulis.cmsimulator.models.users.Student;
import fiitstu.gulis.cmsimulator.models.users.User;
import fiitstu.gulis.cmsimulator.network.ServerController;
import fiitstu.gulis.cmsimulator.network.UrlManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

/**
 * A main-ish menu for task-related activities.
 * <p>
 * Created by Jakub Sedlář on 05.01.2018.
 */
public class TasksPreviewActivity extends FragmentActivity {

    //log tag
    private static final String TAG = TasksPreviewActivity.class.getName();

    private static final String EXAMPLE_DIALOG = "EXAMPLE_DIALOG";
    private static final String GAME_DIALOG = "GAME_DIALOG";
    public static final String TASK_CONFIGURATION = "TASK_CONFIGURATION";
    public static final String GAME_EXAMPLE_NUMBER = "GAME_EXAMPLE_NUMBER";

    public static final int GAME_EXAMPLE_PREVIEW = 0;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_preview);
        Log.v(TAG, "onCreate initialization started");

        //menu
        ActionBar actionBar = this.getActionBar();
        actionBar.setTitle(R.string.tasks);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Button findAutomataTasks = findViewById(R.id.button_tasks_find_automata);
        findAutomataTasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent(TasksPreviewActivity.this, BrowseAutomataTasksActivity.class);
                    intent.putExtra("USER_ID", -1);
                    intent.putExtra("AUTHKEY", -1);
                    startActivity(intent);
            }
        });

        Button findGrammarButton = findViewById(R.id.button_tasks_find_grammar);
        findGrammarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextActivityIntent = new Intent(TasksPreviewActivity.this, BrowseGrammarTasksActivity.class);
                startActivity(nextActivityIntent);
            }
        });

        Button findGameButton = findViewById(R.id.button_tasks_find_game);
        findGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextActivityIntent = new Intent(TasksPreviewActivity.this, BrowseGamesActivity.class);
                startActivity(nextActivityIntent);
            }
        });

        Log.i(TAG, "onCreate initialized");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return false;
    }
}
