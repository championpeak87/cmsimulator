package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.transition.Fade;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import fiitstu.gulis.cmsimulator.R;

public class UserDetailActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_user_detail);

        //menu
        ActionBar actionBer = this.getActionBar();
        actionBer.setDisplayHomeAsUpEnabled(true);
        actionBer.setTitle(R.string.user_details);

        // handle data transfer
        TextView username = findViewById(R.id.textview_user_username);
        TextView fullname = findViewById(R.id.textview_full_name);

        Intent thisIntent = this.getIntent();
        String user_name = thisIntent.getStringExtra("USERNAME");
        String full_name = thisIntent.getStringExtra("FULLNAME");

        username.setText(user_name);
        fullname.setText(full_name);

        // handle connected transition
        setConnectedTransition();
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
