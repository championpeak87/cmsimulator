package fiitstu.gulis.cmsimulator.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.activities.UserDetailActivity;
import fiitstu.gulis.cmsimulator.adapters.tasks.ExampleAutomataAdapter;
import fiitstu.gulis.cmsimulator.models.users.Admin;
import fiitstu.gulis.cmsimulator.models.users.Lector;
import fiitstu.gulis.cmsimulator.models.users.Student;
import fiitstu.gulis.cmsimulator.models.users.User;

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
    public void onBindViewHolder(@NonNull CardViewBuilder holder, int position) {
        User currentUser = listOfUsers.get(position);
        final String username = currentUser.getUsername();
        final String fullname = currentUser.getLast_name() + ", " + currentUser.getFirst_name();
        holder.fullname.setText(fullname);
        holder.username.setText(username);


        final CardView cardView = holder.cardView;

        if (currentUser instanceof Admin)
        {
            holder.usertype.setText(R.string.admin);
        } else if (currentUser instanceof Lector)
        {
            holder.usertype.setText(R.string.lector);
        } else {
            // STUDENT
            holder.usertype.setText(R.string.student);
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, UserDetailActivity.class);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, cardView, ViewCompat.getTransitionName(cardView));
                intent.putExtra("USERNAME", username);
                intent.putExtra("FULLNAME", fullname);
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
        public CardViewBuilder(View itemView) {
            super(itemView);

            usertype = itemView.findViewById(R.id.textview_user_type);
            username = itemView.findViewById(R.id.textview_user_username);
            fullname = itemView.findViewById(R.id.textview_full_name);
            cardView = itemView.findViewById(R.id.cardview_user);
        }
    }
}
