package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.transition.Fade;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.exceptions.NotImplementedException;

import java.sql.Time;

public class GrammarTaskDetailsActivity extends FragmentActivity {
    private static final String TAG = "GrammarTaskDetailsActiv";

    // EXTRA INTENT VALUES
    private boolean exampleTask;
    private String task_name;
    private String task_description;
    private String grammar_type;
    private Time available_time;
    private boolean public_input;

    // UI ELEMENTS
    private TextView textView_grammarType;
    private TextView textView_taskName;
    private EditText textView_taskDescription;
    private LinearLayout available_time_layout;
    private LinearLayout public_inputs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_grammar_task_details);

        setActionBar();
        setUIElements();
        handleIntentExtras();
        setData();
        setConnectedTransition();
    }

    private void handleIntentExtras() {
        Intent intent = this.getIntent();
        exampleTask = intent.getBooleanExtra("EXAMPLE_TASK", false);
        task_name = intent.getStringExtra("TASK_NAME");
        task_description = intent.getStringExtra("TASK_DETAILS");
        grammar_type = intent.getStringExtra("GRAMMAR_TYPE");

        if (exampleTask) {
            available_time_layout.setVisibility(View.GONE);
            public_inputs.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.menu_task_details, menu);

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
                    Log.w(TAG, e.getMessage(), e);
                }
                return true;
        }

        return false;
    }

    private void setActionBar() {
        ActionBar actionBar = this.getActionBar();
        actionBar.setTitle(R.string.task_details);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setUIElements() {
        this.textView_taskName = findViewById(R.id.textview_task_name);
        this.textView_taskDescription = findViewById(R.id.edittext_task_description);
        this.available_time_layout = findViewById(R.id.linearlayout_solution_time);
        this.public_inputs = findViewById(R.id.linearlayout_public_tests);
        this.textView_grammarType = findViewById(R.id.textview_grammar_type);
    }

    private void setData()
    {
        this.textView_taskName.setText(task_name);
        this.textView_taskDescription.setText(task_description);
        this.textView_grammarType.setText(grammar_type);
    }

    private void setConnectedTransition() {
        Fade fade = new Fade();
        View decor = getWindow().getDecorView();

        this.getWindow().setEnterTransition(fade);
        this.getWindow().setExitTransition(fade);
    }
}
