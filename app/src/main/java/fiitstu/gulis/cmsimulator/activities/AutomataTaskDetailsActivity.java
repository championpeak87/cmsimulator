package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.transition.Fade;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import fiitstu.gulis.cmsimulator.R;

public class AutomataTaskDetailsActivity extends FragmentActivity {
    private TextView type;
    private TextView name;
    private TextView determinismText;
    private EditText description;
    private EditText publicInputs;
    private EditText minutes;
    private LinearLayout bottomBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_automata_task_details);

        // menu
        ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.task_details);

        // handle intent extras

        Intent intent = this.getIntent();
        final String task_name = intent.getStringExtra("TASK_NAME");
        final String task_type = intent.getStringExtra("TASK_TYPE");
        final String hint = intent.getStringExtra("TASK_DESCRIPTION");
        final boolean public_inputs = intent.getBooleanExtra("PUBLIC_INPUT", false);
        final int time = intent.getIntExtra("TIME", 0);
        final int primary = intent.getIntExtra("PRIMARY_COLOR", 0);
        final int light_primary = intent.getIntExtra("LIGHT_PRIMARY", 0);


        type = findViewById(R.id.textview_automata_type);
        name = findViewById(R.id.textview_task_name);
        description = findViewById(R.id.edittext_task_description);
        publicInputs = findViewById(R.id.edittext_task_test_inputs);
        minutes = findViewById(R.id.edittext_task_solution_time);
        bottomBar = findViewById(R.id.task_bottom_bar);

        if (primary != 0 && light_primary != 0) {
            type.setBackgroundColor(primary);
            bottomBar.setBackgroundColor(light_primary);
        }

        type.setText(task_type);
        name.setText(task_name);
        description.setText(hint);
        if (time > 0)
            minutes.setText(Integer.toString(time) + " min");
        else
            minutes.setText(R.string.unlimited_time);

        publicInputs.setText(public_inputs ? R.string.yes : R.string.no);

        setConnectedTransition();
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

    private void setConnectedTransition() {
        Fade fade = new Fade();
        View decor = getWindow().getDecorView();
        fade.excludeTarget(decor.findViewById(R.id.action_bar_container), true);
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);

        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);
    }
}
