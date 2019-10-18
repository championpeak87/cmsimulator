package fiitstu.gulis.cmsimulator.adapters.tasks;

import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.app.CMSimulator;
import fiitstu.gulis.cmsimulator.elements.TaskResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for listing the results of tasks. Shows index, name of the solver and their
 * score, colored in green, yellow, or red based on their relative success
 *
 * Created by Jakub Sedlář on 15.01.2018.
 */
public class TaskResultListAdapter extends RecyclerView.Adapter<TaskResultListAdapter.ViewHolder> {

    //log tag
    private static final String TAG = TaskListAdapter.class.getName();

    private List<TaskResult> items = new ArrayList<>();
    private LayoutInflater inflater = LayoutInflater.from(CMSimulator.getContext());

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_element_task_results, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TaskResult result = items.get(position);
        holder.nameTextView.setText(result.getName());
        if (result.getVersion() == 0) {
            holder.scoreTextView.setText(result.getPositive() + "/" + result.getMaxPositive());
        }
        else {
            String scoreText = "<font color=\"#009900\">" + result.getPositive() + "/" + result.getMaxPositive() + "</font>"
                    + " + "
                    + "<font color=\"red\">" + result.getNegative() + "/" + result.getMaxNegative() + "</font>";
            Spanned scoreTextSpanned;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                scoreTextSpanned = Html.fromHtml(scoreText, Html.FROM_HTML_MODE_LEGACY);
            } else {
                scoreTextSpanned = Html.fromHtml(scoreText);
            }
            holder.scoreTextView.setText(scoreTextSpanned);
        }
        holder.positionTextView.setText(String.valueOf(position + 1) + ".");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public String serialize() {
        StringBuilder stringBuilder = new StringBuilder();
        for (TaskResult result: items) {
            stringBuilder.append(result.getName())
                    .append("\t")
                    .append(result.getPositive())
                    .append("/")
                    .append(result.getMaxPositive())
                    .append(" + ")
                    .append(result.getNegative())
                    .append("/")
                    .append(result.getMaxNegative())
                    .append("\n");
        }

        return stringBuilder.toString();
    }

    public void addItem(TaskResult result) {
        Log.v(TAG, "addItem item added");
        items.add(result);
        notifyItemInserted(items.size() - 1);
    }

    public void removeItem(TaskResult item) {
        Log.v(TAG, "removeItem item removed");
        int position = items.indexOf(item);
        items.remove(item);
        notifyItemRemoved(position);
        //also change numbers of next elements
        notifyItemRangeChanged(position, items.size() - position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        //listItem content
        private TextView positionTextView;
        private TextView nameTextView;
        private TextView scoreTextView;

        ViewHolder(View itemView) {
            super(itemView);
            positionTextView = itemView.findViewById(R.id.textView_list_task_results_position);
            nameTextView = itemView.findViewById(R.id.textView_list_task_results_name);
            scoreTextView = itemView.findViewById(R.id.textView_list_task_results_score);
        }
    }
}
