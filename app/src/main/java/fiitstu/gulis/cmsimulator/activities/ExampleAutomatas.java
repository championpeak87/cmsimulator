package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.dialogs.GuideFragment;

public class ExampleAutomatas extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example_automatas);

        // menu
        ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.example_machine);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.menu_examples_automatas, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_examples_automatas_help:
                // TODO: FIX SHOWN FRAGMENT TO NEWLY CREATED
                FragmentManager fm = getSupportFragmentManager();
                GuideFragment guideFragment = GuideFragment.newInstance(GuideFragment.TASKS);
                guideFragment.show(fm, "HELP_DIALOG");
                return true;
        }

        return false;
    }
}
