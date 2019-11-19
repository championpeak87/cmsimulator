package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.transition.Fade;
import android.view.*;
import android.widget.*;
import fiitstu.gulis.cmsimulator.R;

public class UsersManagementEditActivity extends FragmentActivity {
    private TextView username;
    private TextView fullname;
    private TextView type;

    private EditText e_username;
    private EditText e_first_name;
    private EditText e_last_name;
    private Spinner userType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_users_management_edit);

        //menu
        ActionBar actionBer = this.getActionBar();
        actionBer.setDisplayHomeAsUpEnabled(true);
        actionBer.setTitle(R.string.user_details);

        // handle data transfer
        username = findViewById(R.id.textview_user_username);
        fullname = findViewById(R.id.textview_full_name);
        type = findViewById(R.id.textview_user_type);

        e_username = findViewById(R.id.edittext_username);
        e_first_name = findViewById(R.id.edittext_first_name);
        e_last_name = findViewById(R.id.edittext_last_name);

        userType = findViewById(R.id.spinner_user_type);


        Intent thisIntent = this.getIntent();
        final String user_name = thisIntent.getStringExtra("USERNAME");
        final String full_name = thisIntent.getStringExtra("FULLNAME");
        final String user_type = thisIntent.getStringExtra("USER_TYPE");
        final String first_name = thisIntent.getStringExtra("FIRST_NAME");
        final String last_name = thisIntent.getStringExtra("LAST_NAME");

        username.setText(user_name);
        fullname.setText(full_name);
        type.setText(user_type);

        e_last_name.setText(last_name);
        e_username.setText(user_name);
        e_first_name.setText(first_name);

        // handle spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.usertype_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userType.setAdapter(adapter);
        if (user_type != null)
        {
            int spinnerPosition = adapter.getPosition(user_type);
            userType.setSelection(spinnerPosition);
        }
        // handle connected transition
        setConnectedTransition();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.menu_user_edit, menu);
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_user_save:
                // TODO: IMPLEMENT SAVE
                Toast.makeText(this, "NOT IMPLEMENTED", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_help:
                // TODO: IMPLEMENT HELP
                Toast.makeText(this, "NOT IMPLEMENTED", Toast.LENGTH_SHORT).show();
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
