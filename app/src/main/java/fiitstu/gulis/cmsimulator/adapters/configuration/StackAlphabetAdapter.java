package fiitstu.gulis.cmsimulator.adapters.configuration;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.elements.Symbol;

import java.util.List;

public class StackAlphabetAdapter extends RecyclerView.Adapter<StackAlphabetAdapter.ItemHolder> {

    private List<Symbol> stackAlphabet;
    private Context mContext;

    public StackAlphabetAdapter(List<Symbol> stackAlphabet, Context mContext) {
        this.stackAlphabet = stackAlphabet;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.item_stack_alphabet_symbol, viewGroup, false);

        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder itemHolder, int i) {
        final Symbol currentSymbol = stackAlphabet.get(i);

        if (currentSymbol.isStackBotom())
            itemHolder.imagebutton_stack_symbol_delete.setVisibility(View.INVISIBLE);

        itemHolder.textview_stack_symbol_value.setText(currentSymbol.getValue());
        itemHolder.imagebutton_stack_symbol_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSymbol(currentSymbol);
                stackAlphabet.remove(currentSymbol);
                DataSource dataSource = DataSource.getInstance();
                dataSource.open();
                dataSource.deleteStackSymbol(currentSymbol);
            }
        });
    }

    @Override
    public int getItemCount() {
        return stackAlphabet.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        private ImageButton imagebutton_stack_symbol_delete;
        private TextView textview_stack_symbol_value;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);

            imagebutton_stack_symbol_delete = itemView.findViewById(R.id.imagebutton_stack_symbol_delete);
            textview_stack_symbol_value = itemView.findViewById(R.id.textview_stack_symbol_value);
        }
    }

    public void addSymbol(Symbol s) {
        if (!stackAlphabet.contains(s)) {
            stackAlphabet.add(s);
            notifyItemInserted(stackAlphabet.size() - 1);
        }
    }

    public void deleteSymbol(Symbol s) {
        if (stackAlphabet.contains(s)) {
            final int deletedSymbolPosition = stackAlphabet.indexOf(s);
            stackAlphabet.remove(s);
            notifyItemRemoved(deletedSymbolPosition);
        }
    }
}
