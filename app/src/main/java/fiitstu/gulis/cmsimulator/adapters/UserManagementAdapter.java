package fiitstu.gulis.cmsimulator.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import fiitstu.gulis.cmsimulator.R;
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
        holder.fullname.setText(currentUser.getLast_name() + ", " + currentUser.getFirst_name());
        holder.username.setText(currentUser.getUsername());

        Animation showUpAnimation = AnimationUtils.loadAnimation(mContext, R.anim.item_show_animation);

        holder.cardView.setAnimation(showUpAnimation);
    }

    @Override
    public int getItemCount() {
        return listOfUsers.size();
    }

    class CardViewBuilder extends RecyclerView.ViewHolder {
        private TextView username;
        private TextView fullname;
        private CardView cardView;
        public CardViewBuilder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.textview_user_username);
            fullname = itemView.findViewById(R.id.textview_full_name);
            cardView = itemView.findViewById(R.id.cardview_user);
        }
    }
}
