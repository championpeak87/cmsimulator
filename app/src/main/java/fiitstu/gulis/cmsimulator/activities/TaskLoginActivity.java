package fiitstu.gulis.cmsimulator.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.network.ServerController;
import fiitstu.gulis.cmsimulator.network.UrlManager;

import java.io.IOException;
import java.net.URL;

public class TaskLoginActivity extends FragmentActivity {
    private CheckBox rememberCheckBox;

    private ProgressBar loginProgressBar;

    private TextInputEditText usernameEditText, passwordEditText;

    private Button signInButton, signUpButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_login);

        rememberCheckBox = findViewById(R.id.checkbox_remember_password);

        usernameEditText = findViewById(R.id.edittext_username);
        passwordEditText = findViewById(R.id.edittext_password);

        signInButton = findViewById(R.id.button_sign_in);
        signUpButton = findViewById(R.id.button_sign_up);

        loginProgressBar = findViewById(R.id.progressbar_login);
    }


    public void signIn(View view) throws IOException {
        boolean canSignIn = verifyFields();

        if (canSignIn) {
            UrlManager urlManager = new UrlManager();
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();
            URL url = urlManager.getLoginUrl(username, password);

            class ServerResponseAsync extends AsyncTask<URL, Void, String> {

                @Override
                protected void onPreExecute() {
                    setLoginProgressBarVisibility(true);
                }

                @Override
                protected String doInBackground(URL... urls) {
                    String out;
                    ServerController serverController = new ServerController();
                    try {
                        out = serverController.getResponseFromServer(urls[0]);
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
                        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                    }
                    setLoginProgressBarVisibility(false);
                }
            }

            new ServerResponseAsync().execute(url);

        }
    }

    public void signUp(View view) {
        Toast toast = Toast.makeText(this, "SIGN UP INVOKED", Toast.LENGTH_LONG);
        toast.show();
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
            rememberCheckBox.setEnabled(false);
        } else {
            loginProgressBar.setVisibility(View.INVISIBLE);
            usernameEditText.setEnabled(true);
            passwordEditText.setEnabled(true);
            signUpButton.setEnabled(true);
            signInButton.setEnabled(true);
            rememberCheckBox.setEnabled(true);
        }
    }


}
