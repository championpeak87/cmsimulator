package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.adapters.SortController;
import fiitstu.gulis.cmsimulator.adapters.UserManagementAdapter;
import fiitstu.gulis.cmsimulator.adapters.tasks.AutomataTaskAdapter;
import fiitstu.gulis.cmsimulator.models.users.Admin;
import fiitstu.gulis.cmsimulator.models.users.User;
import fiitstu.gulis.cmsimulator.network.ServerController;
import fiitstu.gulis.cmsimulator.network.UrlManager;
import fiitstu.gulis.cmsimulator.network.users.UserParser;
import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UsersManagmentActivity extends FragmentActivity {

    public static String authkey;
    public static int logged_user_id;
    private static List<User> userList;
    public static Context mContext;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_users_management);
        mContext = this;

        authkey = this.getIntent().getStringExtra("LOGGED_USER_AUTHKEY");
        logged_user_id = this.getIntent().getIntExtra("LOGGED_USER_ID", 0);
        // menu
        ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.user_management);

        reloadUsers();
        setConnectedTransition();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.menu_user_management, menu);

        return true;
    }

    public void searchUsers() {
        final AlertDialog searchDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.search)
                .setView(R.layout.dialog_search_user)
                .setCancelable(true)
                .setNeutralButton(R.string.cancel, null)
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RadioGroup orderby = ((AlertDialog) dialog).findViewById(R.id.radiogroup_orderby);

                        RadioButton byFirstName = ((AlertDialog) dialog).findViewById(R.id.radiobutton_byFirstName);
                        RadioButton byLastName = ((AlertDialog) dialog).findViewById(R.id.radiobutton_byLastName);
                        RadioButton byUserName = ((AlertDialog) dialog).findViewById(R.id.radiobutton_byUserName);

                        EditText inputString = ((AlertDialog) dialog).findViewById(R.id.edittext_search_string);

                        if (orderby.getCheckedRadioButtonId() == -1 || inputString.getText().toString().isEmpty()) {
                            Toast.makeText(UsersManagmentActivity.this, "WRONG INPUT", Toast.LENGTH_SHORT).show();
                        } else {
                            class getSearchUsersAsync extends AsyncTask<String, Void, List<User>> {
                                @Override
                                protected void onPreExecute() {
                                    super.onPreExecute();

                                    showLoadScreen(true);
                                }

                                @Override
                                protected List<User> doInBackground(String... strings) {
                                    UrlManager urlManager = new UrlManager();
                                    URL getUsersURL = urlManager.getSearchedUser(authkey, strings[0]);

                                    ServerController serverController = new ServerController();
                                    String output;
                                    List<User> listOfAllUsers = null;
                                    try {
                                        output = serverController.getResponseFromServer(getUsersURL);

                                        UserParser userParser = new UserParser();
                                        listOfAllUsers = userParser.getListOfUsers(output);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    return listOfAllUsers;
                                }

                                @Override
                                protected void onPostExecute(List<User> users) {
                                    super.onPostExecute(users);

                                    UserManagementAdapter adapter = new UserManagementAdapter(UsersManagmentActivity.mContext, users);
                                    setAdapter(adapter);
                                    showLoadScreen(false);
                                    setUserList(users);
                                }
                            }

                            new getSearchUsersAsync().execute(inputString.getText().toString());
                        }
                    }
                })
                .create();

        searchDialog.show();
    }

    public void reloadUsers() {
        class getUsersAsync extends AsyncTask<Void, Void, List<User>> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                showLoadScreen(true);
            }

            @Override
            protected List<User> doInBackground(Void... voids) {
                UrlManager urlManager = new UrlManager();
                URL getUsersURL = urlManager.getAllUsersUrl(authkey);

                ServerController serverController = new ServerController();
                String output;
                List<User> listOfAllUsers = null;
                try {
                    output = serverController.getResponseFromServer(getUsersURL);

                    UserParser userParser = new UserParser();
                    listOfAllUsers = userParser.getListOfUsers(output);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return listOfAllUsers;
            }

            @Override
            protected void onPostExecute(List<User> users) {
                super.onPostExecute(users);

                UserManagementAdapter adapter = new UserManagementAdapter(UsersManagmentActivity.mContext, users);
                setAdapter(adapter);
                showLoadScreen(false);
                setUserList(users);
            }
        }

        new getUsersAsync().execute();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();

                return true;
            case R.id.menu_reload_users:
                reloadUsers();
                return true;
            case R.id.menu_users_help:
                // TODO: pridat pomocnika
                Toast.makeText(this, "NOT IMPLEMENTED", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_filter_users:
                filterUsers();
                return true;
            case R.id.menu_find_users:
                // TODO: pridat vyhladavanie
                searchUsers();
                return true;
        }

        return false;
    }

    public void filterUsers() {


        final AlertDialog filterDialog = new AlertDialog.Builder(this)
                .setView(R.layout.dialog_users_filter)
                .setTitle(R.string.filter)
                .setCancelable(true)
                .setPositiveButton(R.string.OK, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Comparator selectedComparator = null;

                        final int ASCENDING = 1;
                        final int DESCENDING = 2;

                        int orderPosition = 0;

                        RadioGroup order = ((AlertDialog) dialog).findViewById(R.id.radiogroup_order);
                        RadioGroup orderby = ((AlertDialog) dialog).findViewById(R.id.radiogroup_orderby);

                        RadioButton byFirstName = ((AlertDialog) dialog).findViewById(R.id.radiobutton_byFirstName);
                        RadioButton byLastName = ((AlertDialog) dialog).findViewById(R.id.radiobutton_byLastName);
                        RadioButton byUserName = ((AlertDialog) dialog).findViewById(R.id.radiobutton_byUserName);

                        RadioButton ascending = ((AlertDialog) dialog).findViewById(R.id.radiobutton_ascending);
                        RadioButton descending = ((AlertDialog) dialog).findViewById(R.id.radiobutton_descending);

                        String test = ascending.getText().toString();

                        // CHECK IF PICKED
                        if (order.getCheckedRadioButtonId() == -1 || orderby.getCheckedRadioButtonId() == -1) {
                            Toast.makeText(UsersManagmentActivity.this, "RADIOBUTTON NOT PICKED", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            if (byFirstName.isChecked()) {
                                selectedComparator = new SortController.SortByFirstName();
                            } else if (byLastName.isChecked()) {
                                selectedComparator = new SortController.SortByLastName();
                            } else if (byUserName.isChecked()) {
                                selectedComparator = new SortController.SortByUsername();
                            }

                            orderPosition = ascending.isChecked() ? ASCENDING : DESCENDING;
                        }

                        if (orderPosition == DESCENDING) {
                            Collections.sort(userList, Collections.reverseOrder(selectedComparator));
                        } else
                            Collections.sort(userList, selectedComparator);

                        UserManagementAdapter adapter = new UserManagementAdapter(mContext, userList);
                        setAdapter(adapter);
                    }
                })
                .setNeutralButton(R.string.cancel, null)
                .create();

        filterDialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void setAdapter(UserManagementAdapter adapter) {
        RecyclerView users = this.findViewById(R.id.recyclerview_user_management);
        users.setAdapter(adapter);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 180) / 2;

        GridLayoutManager layoutManager = new GridLayoutManager(this, noOfColumns);
        users.setLayoutManager(layoutManager);

        Animation showUpAnimation = AnimationUtils.loadAnimation(this, R.anim.item_show_animation);

        users.setAnimation(showUpAnimation);
    }

    public void showLoadScreen(boolean value) {
        ProgressBar progressBar = this.findViewById(R.id.progressbar_users);
        progressBar.setVisibility(value ? View.VISIBLE : View.GONE);

        RecyclerView recyclerView = this.findViewById(R.id.recyclerview_user_management);
        recyclerView.setVisibility(value ? View.GONE : View.VISIBLE);
    }

    public void setUserList(List<User> users) {
        userList = users;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        RecyclerView users = this.findViewById(R.id.recyclerview_user_management);

        // fix recyclerview layout
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 180) / 2;

        GridLayoutManager layoutManager = new GridLayoutManager(this, noOfColumns);
        users.setLayoutManager(layoutManager);
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
