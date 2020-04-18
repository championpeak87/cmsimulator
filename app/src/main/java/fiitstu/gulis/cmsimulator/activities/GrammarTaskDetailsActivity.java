package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.transition.Fade;
import android.util.Log;
import android.view.*;
import android.widget.*;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.elements.Task;
import fiitstu.gulis.cmsimulator.exceptions.NotImplementedException;

import java.sql.Time;

import static fiitstu.gulis.cmsimulator.app.CMSimulator.getContext;

public class GrammarTaskDetailsActivity extends FragmentActivity {
    private static final String TAG = "GrammarTaskDetailsActiv";

    // INTENT EXTRA KEYS
    public static final String EXAMPLE_TASK_KEY = "EXAMPLE_TASK";
    public static final String TASK_NAME_KEY = "TASK_NAME";
    public static final String TASK_DETAILS_KEY = "TASK_DETAILS";
    public static final String GRAMMAR_TYPE_KEY = "GRAMMAR_TYPE";
    public static final String PUBLIC_INPUTS_KEY = "PUBLIC_INPUTS";
    public static final String AVAILABLE_TIME_KEY = "AVAILABLE_TIME";
    public static final String TASK_STATUS_KEY = "TASK_STATUS";

    // EXTRA INTENT VALUES
    private boolean exampleTask;
    private String task_name;
    private String task_description;
    private String grammar_type;
    private Time available_time;
    private boolean public_input;
    private Task.TASK_STATUS task_status;

    // UI ELEMENTS
    private TextView textView_grammarType;
    private TextView textView_taskName;
    private EditText textView_taskDescription;
    private LinearLayout available_time_layout;
    private LinearLayout public_inputs;
    private EditText edittext_task_test_inputs;
    private EditText edittext_task_solution_time;

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
        exampleTask = intent.getBooleanExtra(EXAMPLE_TASK_KEY, false);
        task_name = intent.getStringExtra(TASK_NAME_KEY);
        task_description = intent.getStringExtra(TASK_DETAILS_KEY);
        grammar_type = intent.getStringExtra(GRAMMAR_TYPE_KEY);
        available_time = (Time) intent.getSerializableExtra(AVAILABLE_TIME_KEY);
        public_input = intent.getBooleanExtra(PUBLIC_INPUTS_KEY, false);
        task_status = (Task.TASK_STATUS) intent.getSerializableExtra(TASK_STATUS_KEY);

        if (exampleTask) {
            available_time_layout.setVisibility(View.GONE);
            public_inputs.setVisibility(View.GONE);
        }
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
        this.edittext_task_solution_time = findViewById(R.id.edittext_task_solution_time);
        this.edittext_task_test_inputs = findViewById(R.id.edittext_task_test_inputs);
    }

    private void setData() {
        this.textView_taskName.setText(task_name);
        this.textView_taskDescription.setText(task_description);
        this.textView_grammarType.setText(grammar_type);

        if (!exampleTask) {
            this.edittext_task_test_inputs.setText(public_input ? R.string.yes : R.string.no);
            this.edittext_task_solution_time.setText(available_time.toString());
            setWindowsColor(task_status);
        }
    }

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

    private void setWindowsColor(Task.TASK_STATUS status) {
        final int primaryColor;
        final int darkColor;
        final int lightColor;

        switch (status) {
            case IN_PROGRESS:
                lightColor = R.color.in_progress_bottom_bar;
                primaryColor = R.color.in_progress_top_bar;
                darkColor = R.color.in_progress_dark;
                break;
            case CORRECT:
                lightColor = R.color.correct_answer_bottom_bar;
                primaryColor = R.color.correct_answer_top_bar;
                darkColor = R.color.correct_answer_dark;
                break;
            case WRONG:
                lightColor = R.color.wrong_answer_bottom_bar;
                primaryColor = R.color.wrong_answer_top_bar;
                darkColor = R.color.wrong_answer_dark;
                break;
            default:
            case NEW:
                lightColor = R.color.primary_color_light;
                primaryColor = R.color.primary_color;
                darkColor = R.color.primary_color_dark;
                break;
            case TOO_LATE:
                lightColor = R.color.too_late_answer_bottom_bar;
                primaryColor = R.color.too_late_answer_top_bar;
                darkColor = R.color.too_late_answer_dark;
                break;
        }

        this.textView_grammarType.setBackgroundColor(getColor(primaryColor));
        findViewById(R.id.task_bottom_bar).setBackgroundColor(getColor(lightColor));
        TextView detailsTextView = findViewById(R.id.textview_task_details);

        if (status == Task.TASK_STATUS.TOO_LATE) {
            int nightModeFlags =
                    getContext().getResources().getConfiguration().uiMode &
                            Configuration.UI_MODE_NIGHT_MASK;

            

            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
                    detailsTextView.setTextColor(getColor(R.color.md_white_1000));
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    detailsTextView.setTextColor(getColor(lightColor));
                    break;
            }
        }
        else detailsTextView.setTextColor(getColor(primaryColor));


        setWindowColor(primaryColor, darkColor);
    }

    private void setConnectedTransition() {
        Fade fade = new Fade();
        View decor = getWindow().getDecorView();

        this.getWindow().setEnterTransition(fade);
        this.getWindow().setExitTransition(fade);
    }
}
