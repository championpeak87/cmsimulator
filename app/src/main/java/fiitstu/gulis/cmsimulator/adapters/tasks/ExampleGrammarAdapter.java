package fiitstu.gulis.cmsimulator.adapters.tasks;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.activities.GrammarActivity;
import fiitstu.gulis.cmsimulator.activities.MainActivity;
import fiitstu.gulis.cmsimulator.dialogs.ExampleGrammarDialog;
import fiitstu.gulis.cmsimulator.models.tasks.grammar_tasks.GrammarTask;

import java.util.ArrayList;
import java.util.List;

public class ExampleGrammarAdapter extends RecyclerView.Adapter<ExampleGrammarAdapter.ItemHolder> {

    private Context mContext;
    private List<GrammarTask> listOfTasks = new ArrayList<>();

    public ExampleGrammarAdapter(Context mContext) {
        this.mContext = mContext;

        this.listOfTasks.add(new GrammarTask(mContext.getString(R.string.example_grammar1), "Pocet pismen a", null, false));
        this.listOfTasks.add(new GrammarTask(mContext.getString(R.string.example_grammar2), "Pocet pismen a", null, false));
        this.listOfTasks.add(new GrammarTask(mContext.getString(R.string.example_grammar3), "Pocet pismen a", null, false));
        this.listOfTasks.add(new GrammarTask(mContext.getString(R.string.example_grammar4), "Pocet pismen a", null, false));
        this.listOfTasks.add(new GrammarTask(mContext.getString(R.string.example_grammar5), "Pocet pismen a", null, false));
        this.listOfTasks.add(new GrammarTask(mContext.getString(R.string.example_grammar6), "Pocet pismen a", null, false));
        this.listOfTasks.add(new GrammarTask(mContext.getString(R.string.example_grammar7), "Pocet pismen a", null, false));
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.list_element_example_grammar_task, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, final int position) {
        final String regular_grammar = mContext.getString(R.string.regular_grammar);
        final String context_free_grammar = mContext.getString(R.string.context_free_grammar);
        final String context_sensitive_grammar = mContext.getString(R.string.context_sensitive_grammar);

        switch (position) {
            case 0:
            case 1:
            case 2:
                holder.grammar_type.setText(regular_grammar);
                break;
            case 3:
            case 4:
            case 5:
                holder.grammar_type.setText(context_free_grammar);
                break;
            case 6:
                holder.grammar_type.setText(context_sensitive_grammar);
                break;
            default:
                holder.grammar_type.setText("UNKNOWN");
                break;
        }
        switch (position) {
            case 0:
                holder.task_name.setText(R.string.example_grammar1);
                break;
            case 1:
                holder.task_name.setText(R.string.example_grammar2);
                break;
            case 2:
                holder.task_name.setText(R.string.example_grammar3);
                break;
            case 3:
                holder.task_name.setText(R.string.example_grammar4);
                break;
            case 4:
                holder.task_name.setText(R.string.example_grammar5);
                break;
            case 5:
                holder.task_name.setText(R.string.example_grammar6);
                break;
            case 6:
                holder.task_name.setText(R.string.example_grammar7);
                break;
        }
        
        holder.help_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {

                Toast.makeText(mContext, "NOT IMPLEMENTED", Toast.LENGTH_SHORT).show();
            }
        });
        
        holder.cardView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                Bundle outputBundle = new Bundle();
                outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, position);

                Intent nextActivityIntent = new Intent(mContext, GrammarActivity.class);
                nextActivityIntent.putExtras(outputBundle);
                mContext.startActivity(nextActivityIntent);
            }
        });
        
    }

    @Override
    public int getItemCount() {
        return listOfTasks.size();
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        private TextView grammar_type;
        private TextView task_name;
        private ImageButton help_button;
        private CardView cardView;

        public ItemHolder(View itemView) {
            super(itemView);

            this.grammar_type = itemView.findViewById(R.id.textview_grammar_type);
            this.task_name = itemView.findViewById(R.id.textview_task_name);
            this.help_button = itemView.findViewById(R.id.button_task_hint);
            this.cardView = itemView.findViewById(R.id.cardview_example_grammar_task);
        }
    }
}
