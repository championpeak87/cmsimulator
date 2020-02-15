package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.dialogs.ExitDialog;
import fiitstu.gulis.cmsimulator.dialogs.NewMachineDialog;

public class NewGrammarTaskActivity extends FragmentActivity {
    private static final String TAG = "NewGrammarTaskActivity";

    // UI ELEMENTS
    private EditText taskNameEditText;
    private EditText taskTextEditText;
    private Button setTaskButton;
    private CheckBox publicTestsCheckBox;
    private CheckBox timerCheckBox;
    private EditText timerEditText;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_new_grammar_task);

        setActionBar();
        setUIElements();
        setEvents();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        ExitDialog exitDialog = new ExitDialog();
        exitDialog.show(fm, TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.menu_new_grammar_task, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_help:
                /* TODO: IMPLEMENT HELP */
                Toast.makeText(this, "NOT IMPLEMENTED", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_publish_grammar_task:
                // TODO: IMPLEMENT TASK UPLOADING
                Toast.makeText(this, "PUBLISHING NOT IMPLEMENTED", Toast.LENGTH_SHORT).show();
                return true;
        }

        return false;
    }

    private void setActionBar() {
        ActionBar actionBar = this.getActionBar();
        actionBar.setTitle(R.string.new_grammar_task);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setUIElements() {
        this.taskNameEditText = findViewById(R.id.edittext_task_name);
        this.taskTextEditText = findViewById(R.id.edittext_task_text);
        this.setTaskButton = findViewById(R.id.button_set_task_grammar);
        this.publicTestsCheckBox = findViewById(R.id.checkbox_input_tests);
        this.timerCheckBox = findViewById(R.id.checkbox_timer_grammar);
        this.timerEditText = findViewById(R.id.edittext_timer_grammar);
    }

    private void setEvents()
    {
        this.timerCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean timerEnabled = NewGrammarTaskActivity.this.timerCheckBox.isChecked();
                NewGrammarTaskActivity.this.timerEditText.setEnabled(timerEnabled);
            }
        });

        this.setTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: IMPLEMENT TASK SETUP
                Toast.makeText(NewGrammarTaskActivity.this, "NOT IMPLEMENTED", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
