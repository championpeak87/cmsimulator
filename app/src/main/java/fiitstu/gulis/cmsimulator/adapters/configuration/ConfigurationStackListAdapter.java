package fiitstu.gulis.cmsimulator.adapters.configuration;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import fiitstu.gulis.cmsimulator.elements.Symbol;

/**
 * Adapter for showing a list of stack symbols to be popped or pushed on transition.
 * Uses {@link ConfigurationSpinnerAdapter} for the individual symbols
 *
 * Created by Martin on 25. 3. 2017.
 */
public class ConfigurationStackListAdapter extends RecyclerView.Adapter<ConfigurationStackListAdapter.ViewHolder> {

    public interface AddItemListener {
        void onAddItem(int index);
    }

    //log tag
    private static final String TAG = ConfigurationStackListAdapter.class.getName();

    private List<Symbol> items;
    private Symbol emptySymbol;
    private Context context;
    private ConfigurationStackSpinnerAdapter stackAlphabetSpinnerAdapter;

    private AddItemListener addItemListener;

    public ConfigurationStackListAdapter(Context context, Symbol emptySymbol, ConfigurationStackSpinnerAdapter stackAlphabetSpinnerAdapter) {
        this.items = new ArrayList<>();
        this.emptySymbol = emptySymbol;
        this.context = context;
        this.stackAlphabetSpinnerAdapter = stackAlphabetSpinnerAdapter;
        removeAll();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = new Button(context);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.button.setText(items.get(position).getValue());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<Symbol> getItems() {
        return items;
    }

    public void setItemList(List<Symbol> symbolList) {
        Log.v(TAG, "setItemList elements set");
        items.clear();
        for (Symbol symbol : symbolList) {
            items.add(symbol);
        }
        items.add(emptySymbol);
        notifyDataSetChanged();
    }

    public void removeAll() {
        items.clear();
        items.add(emptySymbol);
        notifyDataSetChanged();
    }

    public void selectSymbol(int index, Symbol symbol) {
        if (symbol.isEmpty()) {
            if (index != items.size() - 1) {
                Log.v(TAG, "removeStackElement removed");
                items.remove(index);
                notifyItemRemoved(index);
            }
        } else {
            items.set(index, symbol);
            notifyItemChanged(index);
            if (index == items.size() - 1) {
                Log.v(TAG, "addStackElement added");
                items.add(emptySymbol);
                notifyItemInserted(items.size() - 1);
            }
        }
    }

    public ConfigurationStackSpinnerAdapter getStackAlphabetSpinnerAdapter() {
        return stackAlphabetSpinnerAdapter;
    }

    public void setAddItemListener(AddItemListener addItemListener) {
        this.addItemListener = addItemListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //listItem content
        private Button button;

        public ViewHolder(View itemView) {
            super(itemView);
            button = (Button) itemView;
            button.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            new AlertDialog.Builder(context)
                    //.setTitle(R.string.choose_transition)
                    .setAdapter(stackAlphabetSpinnerAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == stackAlphabetSpinnerAdapter.getCount() - 1) {
                                addItemListener.onAddItem(getAdapterPosition());
                            }
                            else {
                                Symbol symbol = stackAlphabetSpinnerAdapter.getItems().get(i);
                                selectSymbol(getAdapterPosition(), symbol);
                            }
                        }
                    })
                    .setCancelable(true)
                    .show();
        }
    }
}
