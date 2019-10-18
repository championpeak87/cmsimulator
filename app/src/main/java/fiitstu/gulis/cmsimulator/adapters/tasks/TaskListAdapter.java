package fiitstu.gulis.cmsimulator.adapters.tasks;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.app.CMSimulator;
import fiitstu.gulis.cmsimulator.elements.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for listing tasks. Shows index, the task name, and a button that can be clicked for details.
 *
 * Created by Jakub Sedlář on 06.01.2018.
 */
public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> {

    public interface TaskClickListener {
        void onClick(Task task);
    }

    //log tag
    private static final String TAG = TaskListAdapter.class.getName();

    private List<Task> items = new ArrayList<>();
    private LayoutInflater inflater = LayoutInflater.from(CMSimulator.getContext());

    private TaskClickListener itemClickListener;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_element_tasks, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.valueTextView.setText(items.get(position).getTitle());
        holder.positionTextView.setText(String.valueOf(position + 1) + ".");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(Task task) {
        Log.v(TAG, "addItem item added");
        items.add(task);
        notifyItemInserted(items.size() - 1);
    }

    public void removeItem(Task item) {
        Log.v(TAG, "removeItem item removed");
        int position = items.indexOf(item);
        items.remove(item);
        notifyItemRemoved(position);
        //also change numbers of next elements
        notifyItemRangeChanged(position, items.size() - position);
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public void setItemClickListener(TaskClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        //listItem content
        private TextView positionTextView;
        private TextView valueTextView;
        private ImageButton viewImageButton;

        ViewHolder(View itemView) {
            super(itemView);
            positionTextView = itemView.findViewById(R.id.textView_list_tasks_position);
            valueTextView = itemView.findViewById(R.id.textView_list_tasks_value);
            viewImageButton = itemView.findViewById(R.id.imageButton_list_tasks_view);
            viewImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Task task = items.get(getAdapterPosition());
                    itemClickListener.onClick(task);
                }
            });
        }
    }
}
