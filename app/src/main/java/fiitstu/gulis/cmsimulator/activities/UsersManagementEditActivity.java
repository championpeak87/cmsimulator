package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.transition.Fade;
import android.util.Log;
import android.view.*;
import android.widget.*;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.adapters.UserManagementAdapter;
import fiitstu.gulis.cmsimulator.exceptions.NotImplementedException;
import fiitstu.gulis.cmsimulator.network.ServerController;
import fiitstu.gulis.cmsimulator.network.UrlManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class UsersManagementEditActivity extends FragmentActivity {
    private TextView username;
    private TextView fullname;
    private TextView type;

    private EditText e_username;
    private EditText e_first_name;
    private EditText e_last_name;
    private Spinner userType;

    private String user_name;
    private String full_name;
    private String user_type;
    private String first_name;
    private String last_name;
    private String password_hash;
    private String auth_key;

    private int user_id;
    private int logged_user_id;
    private int item_position;

    private ArrayAdapter<CharSequence> adapter;

    private static final String TAG = "UsersManagementEditActi";

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
        user_name = thisIntent.getStringExtra("USERNAME");
        full_name = thisIntent.getStringExtra("FULLNAME");
        user_type = thisIntent.getStringExtra("USER_TYPE");
        first_name = thisIntent.getStringExtra("FIRST_NAME");
        last_name = thisIntent.getStringExtra("LAST_NAME");
        password_hash = thisIntent.getStringExtra("PASSWORD_HASH");
        user_id = thisIntent.getIntExtra("USER_ID", 0);
        logged_user_id = thisIntent.getIntExtra("LOGGED_USER_ID", 0);
        auth_key = thisIntent.getStringExtra("AUTHKEY");
        item_position = thisIntent.getIntExtra("ITEM_POSITION", 0);

        username.setText(user_name);
        fullname.setText(full_name);
        type.setText(user_type);

        e_last_name.setText(last_name);
        e_username.setText(user_name);
        e_first_name.setText(first_name);

        // handle spinner
        adapter = ArrayAdapter.createFromResource(this, R.array.usertype_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userType.setAdapter(adapter);
        if (user_type != null) {
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
                // COMPLETED: IMPLEMENT SAVE
                class updateUserAsync extends AsyncTask<Bundle, Void, String> {
                    @Override
                    protected void onPreExecute() {
                        ProgressBar progressBar = findViewById(R.id.progressbar_edit);
                        progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    protected String doInBackground(Bundle... bundles) {
                        final int user_id = bundles[0].getInt("USER_ID");
                        final int logged_user_id = bundles[0].getInt("LOGGED_USER_ID");
                        final String auth_key = bundles[0].getString("AUTHKEY");
                        final String username = bundles[0].getString("USERNAME");
                        final String first_name = bundles[0].getString("FIRST_NAME");
                        final String last_name = bundles[0].getString("LAST_NAME");
                        final String password_hash = bundles[0].getString("PASSWORD_HASH");
                        final String user_type = bundles[0].getString("USER_TYPE");

                        UrlManager urlManager = new UrlManager();
                        URL updateUserURL = urlManager.getUpdateUserUrl(
                                auth_key,
                                logged_user_id,
                                user_id,
                                first_name,
                                last_name,
                                password_hash,
                                user_type,
                                username
                        );

                        ServerController serverController = new ServerController();
                        try {
                            return serverController.getResponseFromServer(updateUserURL);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        super.onPostExecute(s);

                        if (s == null || s.isEmpty()) {
                            {
                                Toast.makeText(UsersManagementEditActivity.this, UsersManagementEditActivity.this.getString(R.string.generic_error), Toast.LENGTH_SHORT).show();
                                ProgressBar progressBar = findViewById(R.id.progressbar_edit);
                                progressBar.setVisibility(View.GONE);
                            }
                        } else {
                            try {
                                JSONObject object = new JSONObject(s);
                                if (object.getBoolean("updated")) {
                                    Toast.makeText(UsersManagementEditActivity.this, UsersManagementEditActivity.this.getString(R.string.user_updated), Toast.LENGTH_SHORT).show();
                                    updateSuccessful();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } finally {
                                ProgressBar progressBar = findViewById(R.id.progressbar_edit);
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    }
                }
                user_name = e_username.getText().toString();
                first_name = e_first_name.getText().toString();
                last_name = e_last_name.getText().toString();
                full_name = last_name + ", " + first_name;

                int spinnerPosition = userType.getSelectedItemPosition();
                final String newType = adapter.getItem(spinnerPosition).toString();
                Bundle updateBundle = new Bundle();
                if (newType == getString(R.string.student)) {
                    updateBundle.putString("USER_TYPE", "student");
                } else {
                    updateBundle.putString("USER_TYPE", "admin");
                }
                user_type = newType;

                updateBundle.putString("USERNAME", user_name);
                updateBundle.putString("FIRST_NAME", first_name);
                updateBundle.putString("LAST_NAME", last_name);
                updateBundle.putString("PASSWORD_HASH", password_hash);

                updateBundle.putInt("USER_ID", user_id);
                updateBundle.putInt("LOGGED_USER_ID", logged_user_id);
                updateBundle.putString("AUTHKEY", auth_key);


                new updateUserAsync().execute(updateBundle);
                return true;
            case R.id.menu_help:
                // TODO: IMPLEMENT HELP
                try {
                    throw new NotImplementedException(this);
                } catch (NotImplementedException e) {
                    Log.w(TAG, e.getMessage(), e);
                }
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

    public void updateSuccessful() {
        username.setText(user_name);
        fullname.setText(full_name);
        type.setText(user_type);

        Bundle newValuesBundle = new Bundle();
        newValuesBundle.putString("USERNAME", user_name);
        newValuesBundle.putString("FULL_NAME", full_name);
        newValuesBundle.putString("USER_TYPE", user_type);
        newValuesBundle.putString("FIRST_NAME", first_name);
        newValuesBundle.putString("LAST_NAME", last_name);
        newValuesBundle.putString("PASSWORD_HASH", password_hash);
        UsersManagmentActivity.notifyUpdate(item_position, newValuesBundle, UsersManagmentActivity.layout);
        onBackPressed();
    }

}
