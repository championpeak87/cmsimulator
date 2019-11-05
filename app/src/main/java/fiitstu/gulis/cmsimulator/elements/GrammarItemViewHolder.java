package fiitstu.gulis.cmsimulator.elements;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import android.widget.ImageView;
import fiitstu.gulis.cmsimulator.R;

public class GrammarItemViewHolder extends RecyclerView.ViewHolder {

    public EditText editText_grammar_table_left;
    public EditText editText_grammar_table_right;

    public GrammarItemViewHolder(View itemView) {
        super(itemView);

        editText_grammar_table_left = itemView.findViewById(R.id.editText_grammar_table_left);
        editText_grammar_table_right = itemView.findViewById(R.id.editText_grammar_table_right);
    }
}