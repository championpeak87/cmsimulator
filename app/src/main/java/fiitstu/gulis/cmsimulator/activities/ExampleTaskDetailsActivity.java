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
import android.widget.TextView;
import fiitstu.gulis.cmsimulator.R;

public class ExampleTaskDetailsActivity extends FragmentActivity {
    private TextView type;
    private TextView name;
    private TextView determinismText;
    private EditText description;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_example_task_details);

        // menu
        ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.task_details);

        // handle intent extras

        Intent intent = this.getIntent();
        final String task_name = intent.getStringExtra("TASK_NAME");
        final String determinism = intent.getStringExtra("DETERMINISM");
        final String task_type = intent.getStringExtra("TASK_TYPE");
        final String hint = intent.getStringExtra("TASK_DESCRIPTION");

        type = findViewById(R.id.textview_automata_type);
        name = findViewById(R.id.textview_task_name);
        determinismText = findViewById(R.id.textview_automata_type_deterministic);
        description = findViewById(R.id.edittext_task_description);

        type.setText(task_type);
        name.setText(task_name);
        determinismText.setText(determinism);
        description.setText(hint);

        setConnectedTransition();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return false;
    }

    private void setConnectedTransition()
    {
        Fade fade = new Fade();
        View decor = getWindow().getDecorView();
        fade.excludeTarget(decor.findViewById(R.id.action_bar_container), true);
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);

        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);
    }
}
