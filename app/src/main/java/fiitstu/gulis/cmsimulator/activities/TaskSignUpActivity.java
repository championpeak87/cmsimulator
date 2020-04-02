package fiitstu.gulis.cmsimulator.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.models.users.Student;
import fiitstu.gulis.cmsimulator.models.users.User;
import fiitstu.gulis.cmsimulator.network.ServerController;
import fiitstu.gulis.cmsimulator.network.UrlManager;
import fiitstu.gulis.cmsimulator.network.users.PasswordManager;

import java.io.IOException;
import java.net.URL;

public class TaskSignUpActivity extends FragmentActivity {
    private ProgressBar signUpProgressBar;

    private EditText usernameEditText, passwordEditText, firstNameEditText, lastNameEditText, passwordConfirmEditText;

    private Button signUpButton, cancelButton;

    private int adminSwitchCounter = 0;

    private boolean signUpAdmin = false;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_task_signup_portrait);

        usernameEditText = findViewById(R.id.edittext_username);
        passwordEditText = findViewById(R.id.edittext_password);
        firstNameEditText = findViewById(R.id.edittext_firstname);
        lastNameEditText = findViewById(R.id.edittext_lastname);
        passwordConfirmEditText = findViewById(R.id.edittext_password_confirm);

        signUpButton = findViewById(R.id.button_sign_up);

        cancelButton = findViewById(R.id.button_sign_up_cancel);

        signUpProgressBar = findViewById(R.id.progressbar_signup);


    }

    public void adminOverride(View view) {
        if (adminSwitchCounter < 5 && !signUpAdmin)
            adminSwitchCounter++;
    }

    public void resetAdmin(View view) {
        if (!signUpAdmin) {
            adminSwitchCounter = 0;
            signUpAdmin = false;
        }
    }


    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.

        usernameEditText.setText(savedInstanceState.getString("username"));
        passwordEditText.setText(savedInstanceState.getString("password"));
        firstNameEditText.setText(savedInstanceState.getString("first_name"));
        lastNameEditText.setText(savedInstanceState.getString("last_name"));
        signUpAdmin = savedInstanceState.getBoolean("signup_admin");
        RadioGroup userTypeGroup = findViewById(R.id.radiogroup_usertype);
        userTypeGroup.setVisibility(View.VISIBLE);

        adminSwitchCounter = signUpAdmin ? 6 : 0;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.

        savedInstanceState.putString("username", usernameEditText.getText().toString());
        savedInstanceState.putString("password", passwordEditText.getText().toString());
        savedInstanceState.putString("first_name", firstNameEditText.getText().toString());
        savedInstanceState.putString("last_name", lastNameEditText.getText().toString());
        savedInstanceState.putBoolean("signup_admin", signUpAdmin);

        // etc.

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_signup_portrait);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            Window w = getWindow();
//            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        }

        usernameEditText = findViewById(R.id.edittext_username);
        passwordEditText = findViewById(R.id.edittext_password);
        firstNameEditText = findViewById(R.id.edittext_firstname);
        lastNameEditText = findViewById(R.id.edittext_lastname);
        passwordConfirmEditText = findViewById(R.id.edittext_password_confirm);

        signUpButton = findViewById(R.id.button_sign_up);
        cancelButton = findViewById(R.id.button_sign_up_cancel);

        signUpProgressBar = findViewById(R.id.progressbar_signup);

        signUpButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (adminSwitchCounter == 6) {
                    RadioGroup usertypeGroup = findViewById(R.id.radiogroup_usertype);
                    usertypeGroup.setVisibility(View.VISIBLE);
                    Toast.makeText(TaskSignUpActivity.this, R.string.sign_up_admin, Toast.LENGTH_LONG).show();
                    signUpAdmin = true;
                } else {
                    resetAdmin(null);
                }
                return true;
            }
        });
    }


    public void signUp(View view) throws IOException {
        setUserExistsError(false);
        boolean canSignUp = verifyFields();

        if (canSignUp) {
            final String username = usernameEditText.getText().toString().trim();
            final String password = passwordEditText.getText().toString();
            final String firstname = firstNameEditText.getText().toString().trim();
            final String lastname = lastNameEditText.getText().toString().trim();

            PasswordManager passwordManager = new PasswordManager();
            String generatedSalt = passwordManager.saltPassword();
            User newUser = new Student(username, firstname, lastname, -1, password, generatedSalt);

            RadioButton adminButton = findViewById(R.id.radiobutton_usertype_admin);
            RadioButton studentButton = findViewById(R.id.radiobutton_usertype_student);

            final User.user_type userType;
            if (adminButton.isChecked())
                userType = User.user_type.admin;
            else userType = User.user_type.student;

            // IMPLEMENT SIGN UP API CALL
            class addUserAsync extends AsyncTask<User, Void, String> {
                private User addedUser = null;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    setSignUpProgressBarVisibility(true);
                }

                @Override
                protected String doInBackground(User... users) {
                    ServerController serverController = new ServerController();
                    UrlManager urlManager = new UrlManager();
                    addedUser = users[0];
                    URL addUserUrl = urlManager.getAddUserUrl(
                            users[0].getUsername(),
                            users[0].getFirst_name(),
                            users[0].getLast_name(),
                            users[0].getAuth_key(),
                            userType,
                            users[0].getSalt()
                    );

                    String response = null;

                    try {
                        response = serverController.getResponseFromServer(addUserUrl);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return response;
                }

                @Override
                protected void onPostExecute(String s) {
                    if (s == null) {
                        Toast.makeText(TaskSignUpActivity.this, R.string.generic_error, Toast.LENGTH_LONG).show();
                    } else if (s.equals("USER EXISTS!")) {
                        setUserExistsError(true);
                    } else if (s.equals("USER WAS ADDED!")) {
                        setSuccessfulSigningUp(addedUser.getUsername());
                    }
                    setSignUpProgressBarVisibility(false);
                }
            }

            new addUserAsync().execute(newUser);
        }
    }

    @Override
    public void onBackPressed() {
        quitSignUp();
    }

    public void cancel(View view) {
        if (adminSwitchCounter == 5 && !signUpAdmin) {
            adminSwitchCounter = 6;
        } else {
            onBackPressed();
        }
    }

    private boolean verifyFields() {
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String passwordCheck = passwordConfirmEditText.getText().toString();

        boolean firstNameEmpty = firstName.isEmpty();
        boolean lastNameEmpty = lastName.isEmpty();
        boolean usernameEmpty = username.isEmpty();
        boolean passwordEmpty = password.isEmpty();
        boolean passwordLength = password.length() >= 6;
        boolean usernameLength = username.length() >= 6;
        boolean passwordMatch = password.equals(passwordCheck);

        if (firstNameEmpty) {
            firstNameEditText.setError(getString(R.string.first_name_empty));
        }
        if (lastNameEmpty) {
            lastNameEditText.setError(getString(R.string.last_name_empty));
        }
        if (usernameEmpty) {
            usernameEditText.setError(getString(R.string.username_empty));
        }
        if (passwordEmpty) {
            passwordEditText.setError(getString(R.string.password_empty));
        }
        if (!passwordMatch) {
            passwordEditText.setError(getString(R.string.passwords_dont_match));
        }
        if (!passwordLength) {
            passwordEditText.setError(getString(R.string.password_length_error));
        }
        if (!usernameLength) {
            usernameEditText.setError(getString(R.string.username_length_small));
        }

        return (!firstNameEmpty &&
                !lastNameEmpty &&
                !usernameEmpty &&
                !passwordEmpty &&
                passwordLength &&
                passwordMatch &&
                usernameLength);
    }

    private void setSignUpProgressBarVisibility(boolean value) {
        if (value) {
            signUpProgressBar.setVisibility(View.VISIBLE);
        } else {
            signUpProgressBar.setVisibility(View.INVISIBLE);
        }

        usernameEditText.setEnabled(!value);
        passwordEditText.setEnabled(!value);
        passwordConfirmEditText.setEnabled(!value);
        firstNameEditText.setEnabled(!value);
        lastNameEditText.setEnabled(!value);
        signUpButton.setEnabled(!value);
        cancelButton.setEnabled(!value);
    }

    private void setUserExistsError(boolean value) {
        if (value)
            usernameEditText.setError(getString(R.string.username_exists_error));
        else
            usernameEditText.setError(null);
    }

    private void setSuccessfulSigningUp(String username) {
        Intent returnIntent = new Intent();

        returnIntent.putExtra("username", username);
        setResult(0, returnIntent);

        Toast.makeText(this, getString(R.string.successful_signup_toast_message).replace("{0}", username), Toast.LENGTH_LONG).show();

        finish();
    }

    private void quitSignUp() {
        Intent returnIntent = new Intent();
        setResult(1, returnIntent);

        finish();
    }


}
