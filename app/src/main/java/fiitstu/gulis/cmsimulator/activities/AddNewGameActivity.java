package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import fiitstu.gulis.cmsimulator.R;

public class AddNewGameActivity extends FragmentActivity {
    private static final String TAG = "AddNewGameActivity";

    private boolean taskModified = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_game);
    }

    private void setActionBar()
    {
        ActionBar actionBar = this.getActionBar();
        actionBar.setTitle(R.string.add_new_game);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setUIElements(){

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return false;
    }

    private boolean checkIfGameModified()
    {
        return false;
    }
}
