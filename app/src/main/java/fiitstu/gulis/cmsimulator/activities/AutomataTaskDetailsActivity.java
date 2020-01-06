package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.transition.Fade;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.elements.Task;

import java.sql.Time;

import static fiitstu.gulis.cmsimulator.app.CMSimulator.getContext;

public class AutomataTaskDetailsActivity extends FragmentActivity {
    private TextView type;
    private TextView name;
    private TextView determinismText;
    private TextView subheader;
    private EditText description;
    private EditText publicInputs;
    private EditText minutes;
    private LinearLayout bottomBar;

    private void setWindowColor(int color, int color2) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getColor(color2));
            window.setNavigationBarColor(getColor(color2));

            ActionBar actionBar = this.getActionBar();
            actionBar.setBackgroundDrawable(new ColorDrawable(getColor(color)));
        }
    }

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
        final Time time = (Time)intent.getSerializableExtra("TIME");
        final Task.TASK_STATUS status = (Task.TASK_STATUS)intent.getSerializableExtra("TASK_STATUS");

        type = findViewById(R.id.textview_automata_type);
        name = findViewById(R.id.textview_task_name);
        description = findViewById(R.id.edittext_task_description);
        publicInputs = findViewById(R.id.edittext_task_test_inputs);
        minutes = findViewById(R.id.edittext_task_solution_time);
        bottomBar = findViewById(R.id.task_bottom_bar);
        subheader = findViewById(R.id.textview_task_details);

        switch (status)
        {
            case IN_PROGRESS:
                type.setBackgroundColor(getColor(R.color.in_progress_top_bar));
                bottomBar.setBackgroundColor(getColor(R.color.in_progress_bottom_bar));
                setWindowColor(R.color.in_progress_top_bar, R.color.in_progress_dark);
                subheader.setTextColor(getColor(R.color.in_progress_top_bar));
                break;
            case CORRECT:
                type.setBackgroundColor(getColor(R.color.correct_answer_top_bar));
                bottomBar.setBackgroundColor(getColor(R.color.correct_answer_bottom_bar));
                setWindowColor(R.color.correct_answer_top_bar, R.color.correct_answer_dark);
                subheader.setTextColor(getColor(R.color.correct_answer_top_bar));
                break;
            case NEW:
                type.setBackgroundColor(getColor(R.color.primary_color));
                bottomBar.setBackgroundColor(getColor(R.color.primary_color_light));
                setWindowColor(R.color.primary_color, R.color.primary_color_dark);
                subheader.setTextColor(getColor(R.color.primary_color));
                break;
            case TOO_LATE:
                type.setBackgroundColor(getColor(R.color.too_late_answer_top_bar));
                bottomBar.setBackgroundColor(getColor(R.color.too_late_answer_bottom_bar));
                setWindowColor(R.color.too_late_answer_top_bar, R.color.too_late_answer_dark);
                int nightModeFlags =
                        getContext().getResources().getConfiguration().uiMode &
                                Configuration.UI_MODE_NIGHT_MASK;
                ImageView logo = findViewById(R.id.imageView_main_logo);
                switch (nightModeFlags) {

                    case Configuration.UI_MODE_NIGHT_YES:

                        subheader.setTextColor(getColor(R.color.md_white_1000));
                        break;

                    case Configuration.UI_MODE_NIGHT_NO:
                    case Configuration.UI_MODE_NIGHT_UNDEFINED:
                        subheader.setTextColor(getColor(R.color.too_late_answer_top_bar));
                        break;
                }
                break;
            case WRONG:
                type.setBackgroundColor(getColor(R.color.wrong_answer_top_bar));
                bottomBar.setBackgroundColor(getColor(R.color.wrong_answer_bottom_bar));
                setWindowColor(R.color.wrong_answer_top_bar, R.color.wrong_answer_dark);
                subheader.setTextColor(getColor(R.color.wrong_answer_top_bar));
                break;
        }

        type.setText(task_type);
        name.setText(task_name);
        description.setText(hint);
        if (time.getHours() > 0 || time.getMinutes() > 0 || time.getSeconds() > 0)
            minutes.setText(time.toString());
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

        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);
    }
}
