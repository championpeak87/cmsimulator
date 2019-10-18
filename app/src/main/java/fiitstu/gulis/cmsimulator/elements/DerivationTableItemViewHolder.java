package fiitstu.gulis.cmsimulator.elements;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import fiitstu.gulis.cmsimulator.R;

public class DerivationTableItemViewHolder extends RecyclerView.ViewHolder {

    public TextView textView_derivation;
    public TextView textView_production;

    public DerivationTableItemViewHolder(View itemView){
        super(itemView);

        textView_derivation = itemView.findViewById(R.id.textView_derivation);
        textView_production = itemView.findViewById(R.id.textView_production);
    }
}
