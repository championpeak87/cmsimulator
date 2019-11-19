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
import fiitstu.gulis.cmsimulator.activities.UserDetailActivity;
import fiitstu.gulis.cmsimulator.activities.UsersManagementEditActivity;
import fiitstu.gulis.cmsimulator.activities.UsersManagmentActivity;
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
import java.net.URL;
import java.util.List;

public class UserManagementAdapter extends RecyclerView.Adapter<UserManagementAdapter.CardViewBuilder> {
    private Context mContext;
    private List<User> listOfUsers;

    public UserManagementAdapter(Context mContext, List<User> listOfUsers) {
        this.mContext = mContext;
        this.listOfUsers = listOfUsers;
    }

    @NonNull
    @Override
    public CardViewBuilder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = inflater.inflate(R.layout.list_element_user_management, parent, false);

        return new UserManagementAdapter.CardViewBuilder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewBuilder holder, final int position) {
        final User currentUser = listOfUsers.get(position);
        final String username = currentUser.getUsername();
        final String fullname = currentUser.getLast_name() + ", " + currentUser.getFirst_name();
        final String firstname = currentUser.getFirst_name();
        final String lastname = currentUser.getLast_name();
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

        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String contentMessage = mContext.getString(R.string.delete_user).replace("{0}", fullname);

                // TODO: wire positive button, add api to delete user
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
                                if (activity.logged_user_id == user_id)
                                {
                                    Toast.makeText(mContext, R.string.cant_delete_logged_user, Toast.LENGTH_LONG).show();
                                }
                                else
                                {

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
                mContext.startActivity(intent, options.toBundle());
            }
        });
    }

    @Override
    public int getItemCount() {
        return listOfUsers.size();
    }

    class CardViewBuilder extends RecyclerView.ViewHolder {
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

    private void removeUser(int position) {
        listOfUsers.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, listOfUsers.size());
    }

    private void updateUser(int position)
    {
        /*ImageView medicineSelected = (ImageView) view.findViewById(R.id.medicine_selected);
        medicineSelected.setVisibility(View.VISIBLE);
        TextView orderQuantity = (TextView) view.findViewById(R.id.order_quantity);
        orderQuantity.setVisibility(View.VISIBLE);
        orderQuantity.setText(quantity + " packet added!");*/

        this.notifyItemChanged(position);
    }

}
