package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import fiitstu.gulis.cmsimulator.adapters.options.OptionsColorsListAdapter;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.dialogs.GuideFragment;
import fiitstu.gulis.cmsimulator.elements.MachineColor;
import fiitstu.gulis.cmsimulator.elements.Options;

import java.util.*;

/**
 * The activity for changing options.
 *
 * Created by Martin on 7. 3. 2017.
 */
public class    OptionsActivity extends FragmentActivity
        implements View.OnClickListener, PopupMenu.OnMenuItemClickListener, OptionsColorsListAdapter.ItemClickCallback {

    //log tag
    private static final String TAG = OptionsActivity.class.getName();

    private DataSource dataSource;
    private boolean markNondeterminism;
    private RecyclerView optionsColorsRecyclerView;
    private OptionsColorsListAdapter optionsColorsListAdapter;
    private ImageButton backB;
    private ImageButton menuB;
    private RadioButton yesNondeterminism;
    private RadioButton noNondeterminism;
    private EditText maxStepsEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        Log.v(TAG, "onCreate initialization started");

        //DataSource initialization
        dataSource = DataSource.getInstance();
        dataSource.open();

        //get options from database
        markNondeterminism = dataSource.getMarkNondeterminism();

        optionsColorsRecyclerView = findViewById(R.id.recyclerView_options_colors);
        optionsColorsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        optionsColorsListAdapter = new OptionsColorsListAdapter(this, dataSource);
        optionsColorsRecyclerView.setAdapter(optionsColorsListAdapter);
        optionsColorsListAdapter.setItemClickCallback(this);

        //menu
        ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.settings);

        //add color
        Button addColorB = findViewById(R.id.button_options_add_color);
        addColorB.setOnClickListener(this);

        //nondeterminism
        yesNondeterminism = findViewById(R.id.radioButton_options_nondeterminism_yes);
        noNondeterminism = findViewById(R.id.radioButton_options_nondeterminism_no);
        if (markNondeterminism) {
            yesNondeterminism.setChecked(true);
            noNondeterminism.setChecked(false);
        } else {
            noNondeterminism.setChecked(true);
            yesNondeterminism.setChecked(false);
        }

        maxStepsEditText = findViewById(R.id.editText_options_max_steps);
        maxStepsEditText.setText(String.valueOf(dataSource.getMaxSteps()));

        onConfigurationChanged(getResources().getConfiguration());

        Log.i(TAG, "onCreate initialized");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.menu_options, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_options_reset_settings:
                optionsColorsListAdapter.getMachineColorsGenerator().loadFromXML(this, dataSource);
                optionsColorsListAdapter.notifyDataSetChanged();
                markNondeterminism = Options.MARK_NONDETERMINISM_DEFAULT;
                yesNondeterminism.setChecked(markNondeterminism);
                noNondeterminism.setChecked(!markNondeterminism);
                dataSource.updateMarkNondeterminism(markNondeterminism);
                maxStepsEditText.setText(String.valueOf(Options.MAX_STEPS_DEFAULT));
                dataSource.updateMaxSteps(Options.MAX_STEPS_DEFAULT);
                return true;
            case R.id.menu_options_help:
                FragmentManager fm = getSupportFragmentManager();
                GuideFragment guideFragment = GuideFragment.newInstance(GuideFragment.SETTINGS);
                guideFragment.show(fm, "HELP_DIALOG");
                return true;
        }
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LinearLayout fromLayout;
        LinearLayout toLayout;
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            fromLayout = findViewById(R.id.linearLayout_options_left_bottom);
            toLayout = findViewById(R.id.linearLayout_options_right);
        }
        else {
            fromLayout = findViewById(R.id.linearLayout_options_right);
            toLayout = findViewById(R.id.linearLayout_options_left_bottom);
        }

        toLayout.setVisibility(View.VISIBLE);
        fromLayout.setVisibility(View.GONE);

        //hard-coded iteration because getChildCount + getChildAt skips the RecyclerView for some reason
        View[] views = { findViewById(R.id.textView_options_colors_title),
                findViewById(R.id.recyclerView_options_colors),
                findViewById(R.id.linearLayout_options_colors_buttons)};

        for (View view: views) {
            if (view.getParent() == fromLayout) {
                fromLayout.removeView(view);
                toLayout.addView(view);
            }
        }
    }

    @Override
    public void onBackPressed() {
        Log.v(TAG, "onBackPressed method started");
        dataSource.updateMarkNondeterminism(yesNondeterminism.isChecked());
        try {
            dataSource.updateMaxSteps(Integer.parseInt(maxStepsEditText.getText().toString()));
        }
        catch (NumberFormatException e) {
            Log.e(TAG, "Invalid input: max steps");
        }
        dataSource.close();
        finish();
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //add color
            case R.id.button_options_add_color:
                optionsColorsListAdapter.addItem();
                break;
        }
    }

    @Override
    public void onColorAddClick(int color, int order) {
        try {
            MachineColor machineColor = dataSource.addColor(color, order);
            optionsColorsListAdapter.getMachineColorsGenerator().getMachineColorsRawList().add(machineColor);
            optionsColorsListAdapter.notifyItemInserted(
                    optionsColorsListAdapter.getMachineColorsGenerator().getMachineColorsRawList().size() - 1);
            //scroll down
            optionsColorsRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    // Call smooth scroll
                    optionsColorsRecyclerView.smoothScrollToPosition(optionsColorsListAdapter.getItemCount() - 1);
                }
            });
        } catch (Exception e) {
            Toast.makeText(OptionsActivity.this, R.string.color_save_error, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "unknown error occurred while updating color", e);
        }
    }

    @Override
    public void onColorUpdateClick(int position, int color) {
        try {
            MachineColor machineColor = optionsColorsListAdapter.getMachineColorsGenerator().getMachineColorsRawList().get(position);
            dataSource.updateColor(machineColor, color, position);
            optionsColorsListAdapter.notifyItemChanged(position);
        } catch (Exception e) {
            Toast.makeText(OptionsActivity.this, R.string.color_save_error, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "unknown error occurred while updating color", e);
        }
    }

    @Override
    public void onColorRemoveClick(int position) {
        final MachineColor machineColor = optionsColorsListAdapter.getMachineColorsGenerator().getMachineColorsRawList().get(position);
        new AlertDialog.Builder(this)
                .setTitle(R.string.warning)
                .setMessage(R.string.remove_color)
                .setCancelable(true)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //check minimum 10 colors
                        if (optionsColorsListAdapter.getMachineColorsGenerator().getMachineColorsRawList().size() > 10) {
                            Log.v(TAG, "remove color yes button click noted");
                            try {
                                dataSource.deleteColor(machineColor);
                                optionsColorsListAdapter.removeItem(machineColor);
                            } catch (Exception e) {
                                Toast.makeText(OptionsActivity.this, R.string.color_remove_error, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "unknown error occurred while removing color", e);
                            }
                        } else {
                            Toast.makeText(OptionsActivity.this, R.string.color_remove10_error, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "cannot remove < 10 colors");
                        }
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }
}
