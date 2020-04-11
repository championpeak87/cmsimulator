package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import fiitstu.gulis.cmsimulator.R;

public class GameDetailsActivity extends FragmentActivity {
    // INTENT KEYS
    public static final String TASK_NAME_KEY = "TASK_NAME";
    public static final String TASK_DESCRIPTION_KEY = "TASK_DESCRIPTION";
    public static final String AUTOMATA_TYPE_KEY = "AUTOMATA_TYPE";

    // UI ELEMENTS
    private TextView textview_task_name;
    private EditText edittext_task_description;
    private EditText edittext_automata_type;

    // INTENT DATA
    private String task_name;
    private String task_description;
    private String automata_type;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_details);

        setActionBar();
        setUIElements();
        fetchIntentData();
        setData();
    }

    private void setActionBar() {
        ActionBar actionBar = this.getActionBar();
        actionBar.setTitle(R.string.game_details);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void fetchIntentData() {
        Intent thisIntent = getIntent();
        task_name = thisIntent.getStringExtra(TASK_NAME_KEY);
        task_description = thisIntent.getStringExtra(TASK_DESCRIPTION_KEY);
        automata_type = thisIntent.getStringExtra(AUTOMATA_TYPE_KEY);
    }

    private void setData() {
        textview_task_name.setText(task_name);
        edittext_task_description.setText(task_description);
        edittext_automata_type.setText(automata_type);
    }

    private void setUIElements() {
        textview_task_name = findViewById(R.id.textview_task_name);
        edittext_task_description = findViewById(R.id.edittext_task_description);
        edittext_automata_type = findViewById(R.id.edittext_automata_type);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
