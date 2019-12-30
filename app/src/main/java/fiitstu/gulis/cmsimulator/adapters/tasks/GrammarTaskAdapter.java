package fiitstu.gulis.cmsimulator.adapters.tasks;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GrammarTaskAdapter extends RecyclerView.Adapter<GrammarTaskAdapter.GrammarTaskViewHolder>{

    @NonNull
    @Override
    public GrammarTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull GrammarTaskViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class GrammarTaskViewHolder extends RecyclerView.ViewHolder
    {
        TextView grammarType;
        TextView task_name;
        LinearLayout bottomBar;
        ConstraintLayout topBar;
        CardView cardView;

        public GrammarTaskViewHolder(View itemView) {
            super(itemView);

            grammarType = itemView.findViewById(0);
            task_name = itemView.findViewById(0);
            bottomBar = itemView.findViewById(0);
            topBar = itemView.findViewById(0);
            cardView = itemView.findViewById(0);
        }
    }
}
