package fiitstu.gulis.cmsimulator.adapters.tasks;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.models.automata_tasks.*;

import java.util.List;

public class AutomataTaskAdapter extends RecyclerView.Adapter<AutomataTaskAdapter.CardViewBuilder> {

    private Context mContext;
    private List<AutomataTask> listOfTasks;

    public AutomataTaskAdapter(Context mContext, List<AutomataTask> listOfTasks) {
        this.mContext = mContext;
        this.listOfTasks = listOfTasks;
    }

    @NonNull
    @Override
    public CardViewBuilder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = inflater.inflate(R.layout.list_view_task_item, parent, false);

        return new CardViewBuilder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewBuilder holder, int position) {
        AutomataTask currentTask = listOfTasks.get(position);
        Animation showUpAnimation = AnimationUtils.loadAnimation(mContext, R.anim.item_show_animation);

        holder.cardView.setAnimation(showUpAnimation);

        if (currentTask instanceof FiniteAutomataTask) {
            holder.automataType.setText(R.string.finite_state_automaton);
        } else if (currentTask instanceof PushdownAutomataTask) {
            holder.automataType.setText(R.string.pushdown_automaton);
        } else if (currentTask instanceof LinearBoundedAutomataTask) {
            holder.automataType.setText(R.string.linear_bounded_automaton);
        } else if (currentTask instanceof TuringMachineTask) {
            holder.automataType.setText(R.string.turing_machine);
        }

        String task_name = currentTask.getTask_name();
        holder.task_name.setText(task_name);

        boolean solvedTask = currentTask.isSolved();
        if (solvedTask) {
            int green = mContext.getColor(R.color.md_green_400);
            int light_green = mContext.getColor(R.color.md_light_green_A400);
            holder.topBar.setBackgroundColor(light_green);
            holder.bottomBar.setBackgroundColor(green);
        } else {
            int primaryColor = mContext.getColor(R.color.primary_color);
            int primaryColorLight = mContext.getColor(R.color.primary_color_light);
            holder.topBar.setBackgroundColor(primaryColor);
            holder.bottomBar.setBackgroundColor(primaryColorLight);
        }
    }

    @Override
    public int getItemCount() {
        return listOfTasks.size();
    }

    public static class CardViewBuilder extends RecyclerView.ViewHolder {
        TextView automataType;
        TextView task_name;
        LinearLayout bottomBar;
        ConstraintLayout topBar;
        CardView cardView;


        public CardViewBuilder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardview_task);
            automataType = itemView.findViewById(R.id.textview_automata_type);
            task_name = itemView.findViewById(R.id.textview_task_name);
            bottomBar = itemView.findViewById(R.id.task_bottom_bar);
            topBar = itemView.findViewById(R.id.task_top_bar);
        }
    }

}
