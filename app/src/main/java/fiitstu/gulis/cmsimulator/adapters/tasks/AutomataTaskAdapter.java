package fiitstu.gulis.cmsimulator.adapters.tasks;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.activities.BrowseAutomataTasksActivity;
import fiitstu.gulis.cmsimulator.activities.MainActivity;
import fiitstu.gulis.cmsimulator.elements.Task;
import fiitstu.gulis.cmsimulator.models.tasks.automata_tasks.FiniteAutomataTask;
import fiitstu.gulis.cmsimulator.models.tasks.automata_tasks.LinearBoundedAutomataTask;
import fiitstu.gulis.cmsimulator.models.tasks.automata_tasks.PushdownAutomataTask;
import fiitstu.gulis.cmsimulator.models.tasks.automata_type;

import java.util.List;

public class AutomataTaskAdapter extends RecyclerView.Adapter<AutomataTaskAdapter.CardViewBuilder> {

    private List<Task> listOfTasks = null;
    private Context mContext;

    public AutomataTaskAdapter(List<Task> listOfTasks, Context mContext) {
        this.listOfTasks = listOfTasks;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public CardViewBuilder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = inflater.inflate(R.layout.list_view_automata_task_item, parent, false);

        return new AutomataTaskAdapter.CardViewBuilder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewBuilder holder, int position) {
        final Task currentTask = listOfTasks.get(position);
        holder.task_name.setText(currentTask.getTitle());
        String automataType = null;
        if (currentTask instanceof FiniteAutomataTask) {
            automataType = automata_type.FINITE_AUTOMATA.toString();
        } else if (currentTask instanceof PushdownAutomataTask) {
            automataType = automata_type.PUSHDOWN_AUTOMATA.toString();
        } else if (currentTask instanceof LinearBoundedAutomataTask) {
            automataType = automata_type.LINEAR_BOUNDED_AUTOMATA.toString();
        } else {
            automataType = automata_type.TURING_MACHINE.toString();
        }
        holder.automata_type.setText(automataType);

        holder.help_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        new AlertDialog.Builder(mContext)
                                .setTitle(R.string.task_hint)
                                .setMessage(currentTask.getText())
                                .setPositiveButton("OK", null)
                                .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (listOfTasks == null)
            return 0;
        else
            return listOfTasks.size();
    }

    public void setListOfTasks(List<Task> listOfTasks) {
        this.listOfTasks = listOfTasks;
    }

    public static class CardViewBuilder extends RecyclerView.ViewHolder {
        private TextView task_name;
        private TextView automata_type;
        private ImageButton delete_task;
        private ImageButton edit_task;
        private ImageButton start_task;
        private ImageButton help_task;

        public CardViewBuilder(View itemView) {
            super(itemView);
            this.task_name = itemView.findViewById(R.id.textview_task_name);
            this.automata_type = itemView.findViewById(R.id.textview_automata_type);
            this.delete_task = itemView.findViewById(R.id.button_delete_task);
            this.edit_task = itemView.findViewById(R.id.button_edit_task);
            this.start_task = itemView.findViewById(R.id.button_start_task);
            this.help_task = itemView.findViewById(R.id.button_help_task);
        }
    }
}
