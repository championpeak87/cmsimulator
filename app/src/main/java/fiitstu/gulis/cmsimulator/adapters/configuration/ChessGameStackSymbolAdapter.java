package fiitstu.gulis.cmsimulator.adapters.configuration;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.Button;
import android.widget.PopupMenu;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.elements.Symbol;

import java.util.*;

public class ChessGameStackSymbolAdapter extends RecyclerView.Adapter<ChessGameStackSymbolAdapter.ItemHolder> {

    private List<Symbol> stackSymbols;
    private List<Symbol> stackAlphabet;
    private Context mContext;

    private HashMap<String, Symbol> stackAlphabetMap = new HashMap<>();

    public ChessGameStackSymbolAdapter(List<Symbol> stackSymbols, List<Symbol> stackAlphabet, Context mContext) {
        this.stackSymbols = stackSymbols;
        this.stackAlphabet = stackAlphabet;
        this.mContext = mContext;

        buildAlphabetMap();
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.list_chess_game_stack_configurator, viewGroup, false);

        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemHolder itemHolder, int i) {
        final Symbol currentSymbol = stackSymbols.get(i);

        itemHolder.button_stack_configurator.setText(currentSymbol.getValue());
        itemHolder.button_stack_configurator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                PopupMenu popupMenu = new PopupMenu(mContext, v);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        String symbolValue = item.getTitle().toString();
                        if (symbolValue.equals(mContext.getString(R.string.remove))) {
                            removeSymbol(itemHolder.getLayoutPosition());
                            return true;
                        }
                        Symbol selectedSymbol = stackAlphabetMap.get(symbolValue);

                        itemHolder.button_stack_configurator.setText(selectedSymbol.getValue());

                        return true;
                    }
                });
                Menu menu = popupMenu.getMenu();
                for (Symbol s : stackAlphabet) {
                    menu.add(s.getValue());
                }
                if (itemHolder.getLayoutPosition() != 0)
                    menu.add(R.string.remove);

                popupMenu.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return stackSymbols.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        private Button button_stack_configurator;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);

            button_stack_configurator = itemView.findViewById(R.id.button_stack_configurator);
        }
    }

    private void buildAlphabetMap() {
        for (Symbol s : stackAlphabet) {
            stackAlphabetMap.put(s.getValue(), s);
        }
    }

    public void addSymbol(Symbol s) {
        stackSymbols.add(s);
        notifyItemInserted(stackSymbols.lastIndexOf(s));
    }

    public void removeSymbol(int position) {
        stackSymbols.remove(position);
        notifyItemRemoved(position);
    }
}
