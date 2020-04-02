package fiitstu.gulis.cmsimulator.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.activities.MainActivity;
import fiitstu.gulis.cmsimulator.activities.TaskLoginActivity;
import fiitstu.gulis.cmsimulator.activities.TasksAdminActivity;
import fiitstu.gulis.cmsimulator.models.tasks.automata_type;
import fiitstu.gulis.cmsimulator.network.ServerController;
import fiitstu.gulis.cmsimulator.network.UrlManager;
import fiitstu.gulis.cmsimulator.network.users.PasswordManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import static fiitstu.gulis.cmsimulator.activities.TaskLoginActivity.loggedUser;

/**
 * A dialog for selecting tye of machine to be created
 * <p>
 * Created by Martin on 15. 4. 2017.
 */
public class ChangePasswordDialog extends DialogFragment {
    private TextInputEditText oldPassword;
    private TextInputEditText newPassword;
    private TextInputEditText newPasswordCheck;

    private automata_type selectedAutomata = automata_type.FINITE_AUTOMATA;

    private OnSubmitListener listener;

    public interface OnSubmitListener {
        void onSubmit(Bundle outputBundle);
    }

    public static ChangePasswordDialog newInstance() {
        return new ChangePasswordDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder changePasswordDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_password_change, null);

        oldPassword = view.findViewById(R.id.edittext_old_password);
        newPassword = view.findViewById(R.id.edittext_new_password);
        newPasswordCheck = view.findViewById(R.id.edittext_new_password_check);

        changePasswordDialog.setView(view)
                .setTitle(R.string.change_password)
                .setCancelable(false)
                .setPositiveButton(R.string.change_password, null)
                .setNeutralButton(android.R.string.cancel, null);

        AlertDialog dialog = changePasswordDialog.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        {
                            final String oldPassword_passwd = oldPassword.getText().toString();
                            final String newPassword_passwd = newPassword.getText().toString();
                            String newPasswordCheck_passwd = newPasswordCheck.getText().toString();

                            boolean oldPasswordEmpty = oldPassword_passwd.isEmpty();
                            boolean newPasswordEmpty = newPassword_passwd.isEmpty();
                            boolean passwordsMatch = newPassword_passwd.equals(newPasswordCheck_passwd);
                            boolean shortPassword = newPassword_passwd.length() < 6;

                            if (oldPasswordEmpty) {
                                oldPassword.setError(getString(R.string.password_empty));
                            }
                            if (newPasswordEmpty) {
                                newPassword.setError(getString(R.string.password_empty));
                            }
                            if (!passwordsMatch) {
                                newPasswordCheck.setError(getString(R.string.passwords_dont_match));
                            }
                            if (shortPassword) {
                                newPassword.setError(getString(R.string.password_length_error));
                            }

                            if (!oldPasswordEmpty && !newPasswordEmpty && passwordsMatch && !shortPassword) {
                                final ServerController serverController = new ServerController();
                                final UrlManager urlManager = new UrlManager();
                                final int user_id = loggedUser.getUser_id();
                                final String authkey = loggedUser.getAuth_key();

                                final URL getSaltURL = urlManager.getLoginSaltUrl(loggedUser.getUsername());

                                class ChangePasswordAsync extends AsyncTask<String, Void, Boolean> {
                                    @Override
                                    protected Boolean doInBackground(String... strings) {
                                        try {
                                            String salt = serverController.getResponseFromServer(getSaltURL);
                                            JSONObject reader = new JSONObject(salt);
                                            salt = reader.getString("salt");
                                            PasswordManager pm = new PasswordManager();
                                            String verify_authkey = pm.getAuthkey(oldPassword_passwd, salt);
                                            if (verify_authkey.equals(authkey)) {
                                                String newAuthkey = pm.getAuthkey(newPassword_passwd, salt);
                                                URL passwordChangeURL = urlManager.getChangePasswordUrl(user_id, newPassword_passwd, salt);
                                                serverController.getResponseFromServer(passwordChangeURL);
                                                TaskLoginActivity.loggedUser.setAuth_key(newAuthkey);
                                                loggedUser.setAuth_key(newAuthkey);
                                            } else {
                                                return false;
                                            }
                                        } catch (IOException | JSONException e) {
                                            e.printStackTrace();
                                            return false;
                                        }

                                        return true;
                                    }

                                    @Override
                                    protected void onPostExecute(Boolean aBoolean) {
                                        if (!aBoolean)
                                            setWrongPassword();
                                        else {
                                            dialog.dismiss();
                                            Toast.makeText(getActivity().getApplicationContext(), R.string.password_changed_successfully, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                new ChangePasswordAsync().execute();

                            }
                        }
                    }
                });
            }
        });

        return dialog;
    }

    private void setWrongPassword() {
        oldPassword.setError(getString(R.string.wrong_password));
    }

    private Bundle getOutputBundle() {
        Bundle outputBundle = new Bundle();
        outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.NEW_MACHINE);
        switch (selectedAutomata) {
            case FINITE_AUTOMATA:
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.FINITE_STATE_AUTOMATON);
                break;
            case PUSHDOWN_AUTOMATA:
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.PUSHDOWN_AUTOMATON);
                break;
            case LINEAR_BOUNDED_AUTOMATA:
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.LINEAR_BOUNDED_AUTOMATON);
                break;
            case TURING_MACHINE:
                outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.TURING_MACHINE);
                break;
        }

        return outputBundle;
    }

    public void setOnSubmitListener(OnSubmitListener listener) {
        this.listener = listener;
    }

}
