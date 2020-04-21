package fiitstu.gulis.cmsimulator.adapters.simulation;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.elements.Symbol;

import java.util.List;

public class ChessGameStackAdapter extends RecyclerView.Adapter<ChessGameStackAdapter.ItemHolder> {

    private List<Symbol> listOfStackSymbols;
    private Context mContext;

    public ChessGameStackAdapter(List<Symbol> listOfStackSymbols, Context mContext) {
        this.listOfStackSymbols = listOfStackSymbols;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.item_chess_game_stack_symbol, viewGroup, false);

        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder itemHolder, int i) {
        final Symbol currentSymbol = listOfStackSymbols.get(i);

        itemHolder.textview_stack_symbol.setText(currentSymbol.getValue());
    }

    @Override
    public int getItemCount() {
        return listOfStackSymbols.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        private TextView textview_stack_symbol;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);

            textview_stack_symbol = itemView.findViewById(R.id.textview_stack_symbol);
        }
    }

    public void pushSymbol(List<Symbol> symbols) {
        if (symbols.size() > 0) {
            final int preSize = listOfStackSymbols.size();
            for (int i = symbols.size() - 1; i >= 0; i--) {
                Symbol s = symbols.get(i);
                listOfStackSymbols.add(s);
            }
            notifyItemRangeInserted(preSize, symbols.size());
        }
    }

    public Symbol popSymbol(List<Symbol> symbols) {
        if (symbols.size() > 0) {
            final Symbol popSymbol = listOfStackSymbols.get(listOfStackSymbols.size() - 1);
            notifyItemRemoved(listOfStackSymbols.size() - 1);
            listOfStackSymbols.remove(popSymbol);
            return popSymbol;
        } else return null;
    }

    public void reset() {
        notifyItemRangeRemoved(0, listOfStackSymbols.size());
        listOfStackSymbols.clear();
    }
}
