package fiitstu.gulis.cmsimulator.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.FragmentActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.models.users.Admin;
import fiitstu.gulis.cmsimulator.models.users.Lector;
import fiitstu.gulis.cmsimulator.models.users.Student;
import fiitstu.gulis.cmsimulator.models.users.User;
import fiitstu.gulis.cmsimulator.network.ServerController;
import fiitstu.gulis.cmsimulator.network.UrlManager;
import fiitstu.gulis.cmsimulator.network.users.PasswordManager;
import fiitstu.gulis.cmsimulator.network.users.UserParser;
import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

public class TaskLoginActivity extends FragmentActivity {
    private ProgressBar loginProgressBar;

    private EditText usernameEditText, passwordEditText;

    private Button signInButton, signUpButton;

    private Bundle onPauseBundle;

    public static User loggedUser = null;

    public static final String SETTINGS_KEY = "SETTINGS";
    public static final String AUTOLOGIN_SETTING = "AUTOLOGIN";
    public static final String AUTOLOGIN_USERNAME = "AUTOLOGIN_USERNAME";
    public static final String AUTOLOGIN_AUTHKEY = "AUTOLOGIN_AUTHKEY";

    private boolean password_visible = false;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_task_login_portrait);

        findViewById(R.id.imageView_main_logo).setBackgroundTintMode(null);

        usernameEditText = findViewById(R.id.edittext_username);
        passwordEditText = findViewById(R.id.edittext_password);

        signInButton = findViewById(R.id.button_sign_in);
        signUpButton = findViewById(R.id.button_sign_up);

        loginProgressBar = findViewById(R.id.progressbar_login);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.

        usernameEditText.setText(savedInstanceState.getString("username"));
        passwordEditText.setText(savedInstanceState.getString("password"));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.

        savedInstanceState.putString("username", usernameEditText.getText().toString());
        savedInstanceState.putString("password", passwordEditText.getText().toString());

        // etc.

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_login_portrait);
        findViewById(R.id.imageView_main_logo).setBackgroundTintMode(null);

        usernameEditText = findViewById(R.id.edittext_username);
        passwordEditText = findViewById(R.id.edittext_password);

        signInButton = findViewById(R.id.button_sign_in);
        signUpButton = findViewById(R.id.button_sign_up);

        loginProgressBar = findViewById(R.id.progressbar_login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    public void showPassword(View view) {
        if (password_visible) {
            passwordEditText.setTransformationMethod(new PasswordTransformationMethod());
        } else {
            passwordEditText.setTransformationMethod(null);
        }
        password_visible = !password_visible;
    }

    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isConnected = connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
        return isConnected;
    }

    public void signIn(View view) throws IOException {
        boolean canSignIn = verifyFields();

        if (canSignIn) {
            if (!isConnected()) {
                Toast.makeText(this, R.string.device_not_connected_internet, Toast.LENGTH_LONG).show();
                return;
            }
            final String username = usernameEditText.getText().toString().trim();
            final String password = passwordEditText.getText().toString();

            class ServerResponseAsync extends AsyncTask<Void, Void, String> {

                @Override
                protected void onPreExecute() {
                    setLoginProgressBarVisibility(true);
                }

                @Override
                protected String doInBackground(Void... voids) {
                    UrlManager urlManager = new UrlManager();
                    URL getSaltURL = urlManager.getLoginSaltUrl(username);
                    ServerController serverController = new ServerController();
                    String saltJson = null;
                    try {
                        saltJson = serverController.getResponseFromServer(getSaltURL);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    UserParser userParser = new UserParser();
                    String salt = null;
                    try {
                        salt = userParser.getSaltFromJson(saltJson);
                        if (salt.length() % 2 != 0)
                            salt += '0';
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    PasswordManager passwordManager = new PasswordManager();
                    String authkey = passwordManager.getAuthkey(password, salt);
                    URL loginURL = urlManager.getLoginUrl(username, authkey);
                    String out;
                    try {
                        out = serverController.getResponseFromServer(loginURL);
                        return out;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(String s) {
                    if (s == null || s.isEmpty()) {
                        loginUnsuccessful();
                    } else {
                        User loggedUser = returnLoggedUser(s);
                        showMainTaskActivity(loggedUser);

                    }
                    setLoginProgressBarVisibility(false);
                }
            }

            new ServerResponseAsync().execute();

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(0, resultCode, data);

        if (requestCode == 0) {
            if (resultCode == 0) {
                String username = data.getStringExtra("username");
                usernameEditText.setText(username);
            }
        }
    }

    public void signUp(View view) {
        Intent signUpIntent = new Intent(this, TaskSignUpActivity.class);

        startActivityForResult(signUpIntent, 0);
    }

    private boolean verifyFields() {
        String username = usernameEditText.getText().toString().trim();
        boolean isUsernameEmpty = username.isEmpty();

        if (isUsernameEmpty) {
            usernameEditText.setError(getString(R.string.username_empty));
        }

        String password = passwordEditText.getText().toString();
        boolean isPasswordEmpty = password.isEmpty();

        if (isPasswordEmpty) {
            passwordEditText.setError(getString(R.string.password_empty));
        }

        return !(isUsernameEmpty || isPasswordEmpty);
    }

    private void loginUnsuccessful() {
        usernameEditText.setError(getString(R.string.login_incorrect));
        passwordEditText.setError(getString(R.string.login_incorrect));
    }

    private void setLoginProgressBarVisibility(boolean value) {
        if (value) {
            loginProgressBar.setVisibility(View.VISIBLE);
            usernameEditText.setEnabled(false);
            passwordEditText.setEnabled(false);
            signUpButton.setEnabled(false);
            signInButton.setEnabled(false);
        } else {
            loginProgressBar.setVisibility(View.INVISIBLE);
            usernameEditText.setEnabled(true);
            passwordEditText.setEnabled(true);
            signUpButton.setEnabled(true);
            signInButton.setEnabled(true);
        }
    }

    private User returnLoggedUser(String json) {
        UserParser userParser = new UserParser();
        try {
            loggedUser = userParser.getUserFromJson(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return loggedUser;
    }

    private void showMainTaskActivity(User user) {
        Intent showMainTaskActivity = null;

        if (user instanceof Admin) {
            showMainTaskActivity = new Intent(this, TasksAdminActivity.class);
        } else if (user instanceof Lector) {
            showMainTaskActivity = new Intent(this, TasksLectorActivity.class);
        } else if (user instanceof Student) {
            showMainTaskActivity = new Intent(this, TasksStudentActivity.class);
        }
        showMainTaskActivity.putExtra(User.USER_TYPE_KEY, user.getClass().getName());
        showMainTaskActivity.putExtra(User.USERNAME_KEY, user.getUsername());
        showMainTaskActivity.putExtra(User.FIRST_NAME_KEY, user.getFirst_name());
        showMainTaskActivity.putExtra(User.LAST_NAME_KEY, user.getLast_name());
        showMainTaskActivity.putExtra(User.AUTHKEY_KEY, user.getAuth_key());
        showMainTaskActivity.putExtra(User.USER_ID_KEY, user.getUser_id());

        startActivity(showMainTaskActivity);
        finish();
    }


}
