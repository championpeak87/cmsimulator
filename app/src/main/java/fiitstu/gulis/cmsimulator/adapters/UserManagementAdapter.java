package fiitstu.gulis.cmsimulator.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.activities.*;
import fiitstu.gulis.cmsimulator.adapters.tasks.ExampleAutomataAdapter;
import fiitstu.gulis.cmsimulator.models.users.Admin;
import fiitstu.gulis.cmsimulator.models.users.Lector;
import fiitstu.gulis.cmsimulator.models.users.Student;
import fiitstu.gulis.cmsimulator.models.users.User;
import fiitstu.gulis.cmsimulator.network.ServerController;
import fiitstu.gulis.cmsimulator.network.UrlManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.List;

public class UserManagementAdapter extends RecyclerView.Adapter<UserManagementAdapter.CardViewBuilder> {
    private Context mContext;
    private List<User> listOfUsers;
    private boolean view_results;

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    OnBottomReachedListener onBottomReachedListener;

    public static int EDIT_ACTIVITY_RETURN_CODE = 234;

    public interface OnBottomReachedListener {
        void onBottomReached(int position);
    }

    public void setOnBottomReachedListener(OnBottomReachedListener onBottomReachedListener) {
        this.onBottomReachedListener = onBottomReachedListener;
    }

    public UserManagementAdapter(Context mContext, List<User> listOfUsers, boolean view_results) {
        this.mContext = mContext;
        this.listOfUsers = listOfUsers;
        this.view_results = view_results;
    }

    @NonNull
    @Override
    public CardViewBuilder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_ITEM) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.list_element_user_management, parent, false);
        } else {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.list_element_loading_user_management, parent, false);
        }

        return new UserManagementAdapter.CardViewBuilder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewBuilder holder, final int position) {
        if (holder instanceof CardViewBuilder && listOfUsers.get(position) != null) {
            final User currentUser = listOfUsers.get(position);
            final String username = currentUser.getUsername();
            final String fullname = currentUser.getLast_name() + ", " + currentUser.getFirst_name();
            final String firstname = currentUser.getFirst_name();
            final String lastname = currentUser.getLast_name();
            final String password_hash = currentUser.getAuth_key();
            final int user_id = currentUser.getUser_id();

            holder.fullname.setText(fullname);
            holder.username.setText(username);


            final CardView cardView = holder.cardView;

            final String usertype;
            if (currentUser instanceof Admin) {
                usertype = mContext.getString(R.string.admin);
            } else if (currentUser instanceof Lector) {
                usertype = mContext.getString(R.string.lector);
            } else {
                // STUDENT
                usertype = mContext.getString(R.string.student);
            }

            holder.usertype.setText(usertype);

            if (view_results) {
                holder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent nextActivityIntent = new Intent(UserManagementAdapter.this.mContext, BrowseAutomataTasksActivity.class);
                        Bundle outputBundle = new Bundle();
                        outputBundle.putString("AUTHKEY", currentUser.getAuth_key());
                        outputBundle.putInt("USER_ID", currentUser.getUser_id());
                        nextActivityIntent.putExtra("BUNDLE", outputBundle);
                        UserManagementAdapter.this.mContext.startActivity(nextActivityIntent);
                    }
                });
            } else {
                holder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, UserDetailActivity.class);
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, cardView, ViewCompat.getTransitionName(cardView));
                        intent.putExtra("USERNAME", username);
                        intent.putExtra("FULLNAME", fullname);
                        intent.putExtra("USER_TYPE", usertype);
                        mContext.startActivity(intent, options.toBundle());
                    }
                });
            }

            if (TaskLoginActivity.loggedUser instanceof Lector)
                holder.removeButton.setVisibility(View.GONE);
            holder.removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String contentMessage = mContext.getString(R.string.delete_user).replace("{0}", fullname);

                    // COMPLETED: wire positive button, add api to delete user
                    AlertDialog deleteUser = new AlertDialog.Builder(mContext)
                            .setTitle(R.string.delete_user_title)
                            .setMessage(contentMessage)
                            .setNeutralButton(android.R.string.cancel, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    class deleteUserAsync extends AsyncTask<Bundle, Void, String> {
                                        @Override
                                        protected String doInBackground(Bundle... bundles) {
                                            final int logged_user_id = bundles[0].getInt("LOGGED_USER_ID", 0);
                                            final int user_id = bundles[0].getInt("USER_ID", 0);
                                            final String auth_key = bundles[0].getString("AUTH_KEY");

                                            UrlManager urlManager = new UrlManager();
                                            URL deleteUserUrl = urlManager.getDeleteUserUrl(logged_user_id, user_id, auth_key);

                                            ServerController serverController = new ServerController();
                                            try {
                                                return serverController.getResponseFromServer(deleteUserUrl);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                            return null;
                                        }

                                        @Override
                                        protected void onPostExecute(String s) {
                                            super.onPostExecute(s);

                                            if (s == null || s.isEmpty()) {
                                                Toast.makeText(mContext, mContext.getString(R.string.generic_error), Toast.LENGTH_SHORT).show();
                                            } else {
                                                try {
                                                    JSONObject object = new JSONObject(s);
                                                    if (object.getBoolean("deleted")) {
                                                        Toast.makeText(mContext, mContext.getString(R.string.user_deleted), Toast.LENGTH_SHORT).show();
                                                        removeUser(position);
                                                    }

                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                            }
                                        }
                                    }

                                    Bundle deleteUserBundle = new Bundle();
                                    UsersManagmentActivity activity = (UsersManagmentActivity) mContext;
                                    if (activity.logged_user_id == user_id) {
                                        Toast.makeText(mContext, R.string.cant_delete_logged_user, Toast.LENGTH_LONG).show();
                                    } else {

                                        deleteUserBundle.putInt("LOGGED_USER_ID", activity.logged_user_id);
                                        deleteUserBundle.putInt("USER_ID", user_id);
                                        deleteUserBundle.putString("AUTH_KEY", activity.authkey);
                                        new deleteUserAsync().execute(deleteUserBundle);
                                    }
                                }
                            })
                            .create();

                    deleteUser.show();
                }
            });

            holder.editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, UsersManagementEditActivity.class);
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, cardView, ViewCompat.getTransitionName(cardView));
                    intent.putExtra("USERNAME", username);
                    intent.putExtra("FULLNAME", fullname);
                    intent.putExtra("USER_TYPE", usertype);
                    intent.putExtra("FIRST_NAME", firstname);
                    intent.putExtra("LAST_NAME", lastname);
                    intent.putExtra("PASSWORD_HASH", password_hash);
                    intent.putExtra("USER_ID", user_id);
                    UsersManagmentActivity activity = (UsersManagmentActivity) mContext;
                    intent.putExtra("LOGGED_USER_ID", activity.logged_user_id);
                    intent.putExtra("AUTHKEY", activity.authkey);
                    intent.putExtra("ITEM_POSITION", position);
                    ((UsersManagmentActivity) mContext).startActivity(intent, options.toBundle());
                }
            });
        }
    }

    public void addNullData() {
        this.listOfUsers.add(null);
        notifyItemInserted(this.listOfUsers.size() - 1);
    }

    public void removeNullData() {
        this.listOfUsers.remove(this.listOfUsers.size() - 1);
        notifyItemRemoved(this.listOfUsers.size());
    }

    @Override
    public int getItemCount() {
        return listOfUsers.size();
    }

    class CardViewBuilder extends CustomCardViewBuilder {
        private TextView username;
        private TextView fullname;
        private TextView usertype;
        private CardView cardView;
        private ImageButton removeButton;
        private ImageButton editButton;

        public CardViewBuilder(View itemView) {
            super(itemView);

            usertype = itemView.findViewById(R.id.textview_user_type);
            username = itemView.findViewById(R.id.textview_user_username);
            fullname = itemView.findViewById(R.id.textview_full_name);
            cardView = itemView.findViewById(R.id.cardview_user);
            removeButton = itemView.findViewById(R.id.button_delete_user);
            editButton = itemView.findViewById(R.id.button_edit_user);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (listOfUsers.get(position) != null)
            return VIEW_TYPE_ITEM;
        else
            return VIEW_TYPE_LOADING;
    }

    class CustomCardViewBuilder extends RecyclerView.ViewHolder {


        public CustomCardViewBuilder(View itemView) {
            super(itemView);
        }
    }

    class ProgressCardViewBuilder extends CustomCardViewBuilder {
        private TextView username;
        private TextView fullname;
        private TextView usertype;
        private CardView cardView;
        private ImageButton removeButton;
        private ImageButton editButton;

        public ProgressCardViewBuilder(View itemView) {
            super(itemView);

            usertype = itemView.findViewById(R.id.textview_user_type);
            username = itemView.findViewById(R.id.textview_user_username);
            fullname = itemView.findViewById(R.id.textview_full_name);
            cardView = itemView.findViewById(R.id.cardview_user);
            removeButton = itemView.findViewById(R.id.button_delete_user);
            editButton = itemView.findViewById(R.id.button_edit_user);
        }
    }


    private void removeUser(int position) {
        listOfUsers.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, listOfUsers.size());
    }

    private void notifyNewUser(int start, int numberOfUsers) {
        notifyItemRangeInserted(start, numberOfUsers);
    }

    public void addUsers(List<User> listOfUsers) {
        int userSizeStart = this.listOfUsers.size();
        for (User user :
                listOfUsers) {
            this.listOfUsers.add(user);
        }
        notifyNewUser(userSizeStart, 20);
    }

}
