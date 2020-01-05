package fiitstu.gulis.cmsimulator.activities;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import fiitstu.gulis.cmsimulator.adapters.InfiniteScrollListener;
import fiitstu.gulis.cmsimulator.adapters.SortController;
import fiitstu.gulis.cmsimulator.adapters.UserManagementAdapter;
import fiitstu.gulis.cmsimulator.adapters.tasks.AutomataTaskAdapter;
import fiitstu.gulis.cmsimulator.models.users.Admin;
import fiitstu.gulis.cmsimulator.models.users.Lector;
import fiitstu.gulis.cmsimulator.models.users.Student;
import fiitstu.gulis.cmsimulator.models.users.User;
import fiitstu.gulis.cmsimulator.network.ServerController;
import fiitstu.gulis.cmsimulator.network.UrlManager;
import fiitstu.gulis.cmsimulator.network.users.UserParser;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UsersManagmentActivity extends FragmentActivity implements InfiniteScrollListener.OnLoadMoreListener {

    public static String authkey;
    public static int logged_user_id;
    public static UserManagementAdapter adapter;
    public static RecyclerView.LayoutManager layout;
    private static List<User> userList;
    public static Context mContext;
    private int userCount;
    InfiniteScrollListener infiniteScrollListener;

    @Override
    public void onLoadMore() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new LoadMoreUsersAsync().execute();

                infiniteScrollListener.setLoaded();
            }
        }, 500);
    }

    class FetchUsersCountAsync extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            UrlManager urlManager = new UrlManager();
            ServerController serverController = new ServerController();
            URL url = urlManager.getUsersCountURL();
            String output = null;
            try {
                output = serverController.getResponseFromServer(url);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                return output;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s != null || !s.isEmpty()) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    userCount = jsonObject.getInt("count");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_users_management);
        mContext = this;

        new FetchUsersCountAsync().execute();

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

    public static List<User> getUserList() {
        return userList;
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

    private class LoadMoreUsersAsync extends AsyncTask<Void, Void, List<User>> {
        @Override
        protected List<User> doInBackground(Void... voids) {
            UrlManager urlManager = new UrlManager();
            int offset = 20 * ((adapter.getItemCount() / 20));
            URL getUsersURL = urlManager.getAllUsersUrl(authkey, offset);

            ServerController serverController = new ServerController();
            String output;
            List<User> listOfNewUsers = null;
            try {
                output = serverController.getResponseFromServer(getUsersURL);

                UserParser userParser = new UserParser();
                listOfNewUsers = userParser.getListOfUsers(output);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return listOfNewUsers;
        }

        @Override
        protected void onPostExecute(List<User> users) {
            adapter.removeNullData();
            adapter.addUsers(users);
            if (userList.size() != userCount)
                adapter.addNullData();
        }
    }

    private class getUsersAsync extends AsyncTask<Integer, Void, List<User>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            showLoadScreen(true);
        }

        @Override
        protected List<User> doInBackground(Integer... integers) {
            UrlManager urlManager = new UrlManager();
            URL getUsersURL = urlManager.getAllUsersUrl(authkey, integers[0]);

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

            showLoadScreen(false);
            adapter = new UserManagementAdapter(UsersManagmentActivity.mContext, users);
            if (users.size() != userCount)
                adapter.addNullData();
            setAdapter(adapter);
            setUserList(users);

        }
    }

    public void reloadUsers() {

        new getUsersAsync().execute(0);
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

        layout = new GridLayoutManager(this, noOfColumns);
        users.setLayoutManager(layout);

        infiniteScrollListener = new InfiniteScrollListener((GridLayoutManager) layout, this);
        infiniteScrollListener.setLoaded();
        users.addOnScrollListener(infiniteScrollListener);

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

    public static void notifyUpdate(int position, Bundle newValues, RecyclerView.LayoutManager layout) {
        View view = layout.findViewByPosition(position);
        TextView fullname = view.findViewById(R.id.textview_full_name);
        TextView usertype = view.findViewById(R.id.textview_user_type);
        TextView username = view.findViewById(R.id.textview_user_username);

        final String s_username = newValues.getString("USERNAME");
        final String s_fullname = newValues.getString("FULL_NAME");
        final String s_usertype = newValues.getString("USER_TYPE");
        final String s_firstname = newValues.getString("FIRST_NAME");
        final String s_lastname = newValues.getString("LAST_NAME");
        final String s_passwordhash = newValues.getString("PASSWORD_HASH");

        fullname.setText(s_fullname);
        usertype.setText(s_usertype);
        username.setText(s_username);

        final int userid = userList.get(position).getUser_id();
        User updatedUser;

        if (s_usertype == mContext.getString(R.string.lector)) {
            updatedUser = new Lector();
            updatedUser.setUsername(s_username);
            updatedUser.setAuth_key(s_passwordhash);
            updatedUser.setFirst_name(s_firstname);
            updatedUser.setLast_name(s_lastname);
            updatedUser.setUsername(s_username);
            updatedUser.setUser_id(userid);
        } else if (s_usertype == mContext.getString(R.string.admin)) {
            updatedUser = new Admin();
            updatedUser.setUsername(s_username);
            updatedUser.setAuth_key(s_passwordhash);
            updatedUser.setFirst_name(s_firstname);
            updatedUser.setLast_name(s_lastname);
            updatedUser.setUsername(s_username);
            updatedUser.setUser_id(userid);
        } else {
            // STUDENT
            updatedUser = new Student();
            updatedUser.setUsername(s_username);
            updatedUser.setAuth_key(s_passwordhash);
            updatedUser.setFirst_name(s_firstname);
            updatedUser.setLast_name(s_lastname);
            updatedUser.setUsername(s_username);
            updatedUser.setUser_id(userid);
        }

        userList.set(position, updatedUser);

        adapter.notifyItemChanged(position);
    }

}
