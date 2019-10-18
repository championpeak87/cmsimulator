package fiitstu.gulis.cmsimulator.adapters.grammar;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.elements.DerivationTableItemViewHolder;
import fiitstu.gulis.cmsimulator.elements.GeneratedWord;

public class DerivationTableAdapter extends RecyclerView.Adapter<DerivationTableItemViewHolder> {

    private List<GeneratedWord> derivationSequence;

    public DerivationTableAdapter(List<GeneratedWord> derivationSequence){
        this.derivationSequence = derivationSequence;
    }

    @Override
    public DerivationTableItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.derivation_table_item, parent, false);

        return new DerivationTableItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DerivationTableItemViewHolder holder, int position) {
            final GeneratedWord generatedWord = derivationSequence.get(position);

            if(generatedWord.getUsedRule() == null) {
                holder.textView_production.setText(generatedWord.getWord());
            }else{
                holder.textView_derivation.setText(generatedWord.getUsedRule().getGrammarLeft() + "->" + generatedWord.getUsedRule().getGrammarRight());
                holder.textView_production.setText(generatedWord.getWord());
            }

    }

    @Override
    public int getItemCount() {
        return this.derivationSequence.size();
    }
}
