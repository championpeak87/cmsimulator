package fiitstu.gulis.cmsimulator.adapters.simulation;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import fiitstu.gulis.cmsimulator.elements.Symbol;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.machines.MachineStack;

/**
 * The adapter for showing the contents of a machine's stack
 *
 * Created by Martin on 25. 3. 2017.
 */
public class StackTapeListAdapter extends RecyclerView.Adapter<StackTapeListAdapter.ViewHolder> implements MachineStack {

    //log tag
    private static final String TAG = StackTapeListAdapter.class.getName();

    private List<Symbol> items;
    private LayoutInflater inflater;

    private int tapeDimension;

    private Context context;
    private StackTapeSpinnerAdapter stackTapeSpinnerAdapter;

    public StackTapeListAdapter(Context context, List<Symbol> items,
                                int tapeDimension, StackTapeSpinnerAdapter stackTapeSpinnerAdapter) {
        this.items = items;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.tapeDimension = tapeDimension;
        this.stackTapeSpinnerAdapter = stackTapeSpinnerAdapter;
    }

    public StackTapeListAdapter(StackTapeListAdapter stackTapeListAdapter) {
        this(stackTapeListAdapter.context,
                new ArrayList<>(stackTapeListAdapter.items),
                stackTapeListAdapter.tapeDimension,
                stackTapeListAdapter.stackTapeSpinnerAdapter);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_element_simulation_tape, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.spinner.setAdapter(stackTapeSpinnerAdapter);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(tapeDimension, tapeDimension);
        holder.spinner.setLayoutParams(params);
        int symbolPosition = getSymbolSpinnerIndex(holder.spinner, items.get(position).getId());
        holder.spinner.setSelection(symbolPosition);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<Symbol> getItems() {
        return items;
    }

    public void setTapeDimension(int tapeDimension) {
        this.tapeDimension = tapeDimension;
    }

    //method to get index of Symbol spinner content
    private int getSymbolSpinnerIndex(Spinner spinner, long id) {
        int index = -1;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (((Symbol) spinner.getItemAtPosition(i)).getId() == id) {
                index = i;
                break;
            }
        }
        return index;
    }

    @Override
    public void pushItems(List<Symbol> items) {
        Log.v(TAG, "stack element added");
        this.items.addAll(items);
        notifyItemRangeInserted(this.items.size() - items.size(), items.size());
    }

    @Override
    public void popItems(int count) {
        Log.v(TAG, "stack element removed");
        for (int i = 0; i < count; i++) {
            items.remove(items.size() - 1);
        }
        notifyItemRangeRemoved(items.size(), count);
    }

    @Override
    public Symbol getItem(int index) {
        return items.get(index);
    }

    @Override
    public StackTapeListAdapter copy() {
        return new StackTapeListAdapter(this);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        //listItem content
        private Spinner spinner;

        public ViewHolder(View itemView) {
            super(itemView);
            spinner = (Spinner) itemView;
            spinner.setClickable(false);
        }
    }
}
