package fiitstu.gulis.cmsimulator.adapters.grammar;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.elements.GrammarItemViewHolder;
import fiitstu.gulis.cmsimulator.elements.GrammarRule;

public class GrammarSimulationAdapter extends RecyclerView.Adapter<GrammarItemViewHolder> {

    private List<GrammarRule> grammarRuleList;

    public GrammarSimulationAdapter(List<GrammarRule> grammarRuleList) {
        this.grammarRuleList = grammarRuleList;
    }

    @Override
    public GrammarItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grammar_item, parent, false);

        return new GrammarItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GrammarItemViewHolder holder, int position) {
        final GrammarRule grammarRule = this.grammarRuleList.get(position);

        holder.editText_grammar_table_left.setText(grammarRule.getGrammarLeft());
        holder.editText_grammar_table_left.setEnabled(false);

        holder.editText_grammar_table_right.setText(grammarRule.getGrammarRight());
        holder.editText_grammar_table_right.setEnabled(false);
    }

    @Override
    public int getItemCount() {
        return this.grammarRuleList.size();
    }
}
