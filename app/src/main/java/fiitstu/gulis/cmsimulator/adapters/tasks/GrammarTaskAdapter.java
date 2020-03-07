package fiitstu.gulis.cmsimulator.adapters.tasks;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.elements.Task;
import fiitstu.gulis.cmsimulator.models.tasks.grammar_tasks.GrammarTask;

import java.util.ArrayList;
import java.util.List;

public class GrammarTaskAdapter extends RecyclerView.Adapter<GrammarTaskAdapter.ItemHolder> {
    private static final String TAG = "GrammarTaskAdapter";

    public interface DatasetChangedListener {
        void onDataChange();
    }

    private Context mContext;
    private List<GrammarTask> grammarTaskList = new ArrayList<>();
    private DatasetChangedListener datasetChangedListener = null;

    public GrammarTaskAdapter(Context mContext, List<GrammarTask> grammarTasks) {
        this.mContext = mContext;
        this.grammarTaskList = grammarTasks;
        if (datasetChangedListener != null)
            datasetChangedListener.onDataChange();
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.list_element_grammar_task, parent, false);

        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        holder.taskName_TextView.setText(grammarTaskList.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return grammarTaskList.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        private TextView taskName_TextView;
        private ImageButton deleteTask_ImageButton;
        private ImageButton helpTask_ImageButton;
        private ImageButton flagTask_ImageButton;
        private FrameLayout content_FrameLayout;
        private CardView card_CardView;

        private ProgressBar taskLoading_ProgressBar;

        public ItemHolder(View itemView) {
            super(itemView);

            taskName_TextView = itemView.findViewById(R.id.textview_task_name);
            deleteTask_ImageButton = itemView.findViewById(R.id.button_delete_task);
            helpTask_ImageButton = itemView.findViewById(R.id.button_help_task);
            flagTask_ImageButton = itemView.findViewById(R.id.button_flag_task);
            taskLoading_ProgressBar = itemView.findViewById(R.id.progressbar_task_loading);
            content_FrameLayout = itemView.findViewById(R.id.framelayout_context);
            card_CardView = itemView.findViewById(R.id.cardview_task);
        }

        public void showLoadingProgressBar(boolean value, Task.TASK_STATUS status) {
            final int alphaValue = value ? 255 / 4 : 255;

            ColorDrawable myColor = new ColorDrawable();
            myColor.setColor(mContext.getColor(R.color.bootstrap_gray_light));
            if (value) {
                content_FrameLayout.setForeground(myColor);
                content_FrameLayout.getForeground().setAlpha(alphaValue);
            } else {
                content_FrameLayout.getForeground().setAlpha(0);
            }

            card_CardView.setEnabled(!value);
            deleteTask_ImageButton.setEnabled(!value);
            helpTask_ImageButton.setEnabled(!value);

            taskLoading_ProgressBar.setVisibility(value ? View.VISIBLE : View.GONE);
            switch (status)
            {
                case IN_PROGRESS:
                    taskLoading_ProgressBar.getIndeterminateDrawable().setColorFilter(mContext.getColor(R.color.in_progress_top_bar), PorterDuff.Mode.MULTIPLY);
                    break;
                case CORRECT:
                    taskLoading_ProgressBar.getIndeterminateDrawable().setColorFilter(mContext.getColor(R.color.correct_answer_top_bar), PorterDuff.Mode.MULTIPLY);
                    break;
                case WRONG:
                    taskLoading_ProgressBar.getIndeterminateDrawable().setColorFilter(mContext.getColor(R.color.wrong_answer_top_bar), PorterDuff.Mode.MULTIPLY);
                    break;
                case NEW:
                    taskLoading_ProgressBar.getIndeterminateDrawable().setColorFilter(mContext.getColor(R.color.primary_color), PorterDuff.Mode.MULTIPLY);
                    break;
                case TOO_LATE:
                    taskLoading_ProgressBar.getIndeterminateDrawable().setColorFilter(mContext.getColor(R.color.too_late_answer_top_bar), PorterDuff.Mode.MULTIPLY);
                    break;
            }
        }
    }

    public void setDatasetChangedListener(DatasetChangedListener datasetChangedListener) {
        this.datasetChangedListener = datasetChangedListener;
    }
}
