package fiitstu.gulis.cmsimulator.adapters.simulation;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.app.CMSimulator;
import fiitstu.gulis.cmsimulator.elements.MachineColorsGenerator;
import fiitstu.gulis.cmsimulator.elements.Symbol;
import fiitstu.gulis.cmsimulator.elements.TapeElement;

/**
 * The adapter for the user-editable tape, showing its contents as well as buttons
 * for adding more symbols to the right and left ends.
 *
 * Created by Martin on 21. 2. 2017.
 */
public class DefaultTapeListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //log tag
    private static final String TAG = DefaultTapeListAdapter.class.getName();

    //viewTypes
    private static final int TYPE_BUTTON = 0;
    private static final int TYPE_TAPE = 1;

    private List<TapeElement> items;
    private TapeElement initialTapeElement;
    private Context context;
    private LayoutInflater inflater;
    private MachineColorsGenerator machineColorsGenerator;
    private int tapeDimension;

    private MachineTapeSpinnerAdapter machineTapeSpinnerAdapter;

    private ItemClickCallback itemClickCallback;

    public interface ItemClickCallback {
        void onLeftButtonClick();

        void onRightButtonClick();

        void onSpinnerItemSelected(int position, Symbol symbol);

        void onSpinnerLongClick(int position);
    }

    public MachineTapeSpinnerAdapter getMachineTapeSpinnerAdapter() {
        return machineTapeSpinnerAdapter;
    }

    public void setItemClickCallback(final ItemClickCallback itemClickCallback) {
        this.itemClickCallback = itemClickCallback;
    }

    public DefaultTapeListAdapter(Context context, MachineColorsGenerator machineColorsGenerator,
                                  int tapeDimension, MachineTapeSpinnerAdapter machineTapeSpinnerAdapter) {
        this.items = new ArrayList<>();
        this.initialTapeElement = null;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.machineColorsGenerator = machineColorsGenerator;
        this.tapeDimension = tapeDimension;
        this.machineTapeSpinnerAdapter = machineTapeSpinnerAdapter;
    }

    public void setTapeDimension(int tapeDimension) {
        this.tapeDimension = tapeDimension;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_BUTTON) {
            View view = inflater.inflate(R.layout.list_element_simulation_add_tape, parent, false);
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration((Button)view,
                    1,
                    CMSimulator.getTextSize(getContext(), android.R.attr.textAppearanceLarge),
                    1,
                    TypedValue.COMPLEX_UNIT_PX);
            return new AddTapeViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.list_element_simulation_tape, parent, false);
            return new TapeViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_TAPE) {
            TapeViewHolder tapeViewHolder = (TapeViewHolder) holder;


            tapeViewHolder.spinner.setAdapter(machineTapeSpinnerAdapter);
            //position-1 because 0 is left button
            int symbolPosition = getSymbolSpinnerIndex(tapeViewHolder.spinner, items.get(position - 1).getSymbol().getId());
            tapeViewHolder.spinner.setSelection(symbolPosition);
            //position-1 because 0 is left button
            if (items.get(position - 1) == initialTapeElement) {
                tapeViewHolder.spinner.getBackground().setColorFilter(machineColorsGenerator.getInitialColor().getValue(), PorterDuff.Mode.MULTIPLY);
            } else if (items.get(position - 1).isBreakpoint()) {
                tapeViewHolder.spinner.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.break_point_color), PorterDuff.Mode.MULTIPLY);
            } else {
                tapeViewHolder.spinner.getBackground().clearColorFilter();
            }
        } else {
            AddTapeViewHolder addTapeViewHolder = (AddTapeViewHolder) holder;
            addTapeViewHolder.addB.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.md_black_1000), PorterDuff.Mode.MULTIPLY);
        }
    }

    @Override
    public int getItemCount() {
        return items.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == items.size() + 1) {
            return TYPE_BUTTON;
        } else {
            return TYPE_TAPE;
        }
    }

    public List<TapeElement> getItems() {
        return items;
    }

    public void setItems(List<TapeElement> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void addLeftItem(TapeElement item) {
        Log.v(TAG, "addLeftItem default tape element added");
        items.add(0, item);
        //1 because 0 is left button
        notifyItemInserted(1);
    }

    public void addRightItem(TapeElement item) {
        Log.v(TAG, "addRightItem default tape element added");
        items.add(item);
        //not -1 because last is right button
        notifyItemInserted(items.size());
    }

    public void removeItem(TapeElement item) {
        Log.v(TAG, "removeItem default tape element removed");
        int position = items.indexOf(item);
        items.remove(item);
        //position+1 because 0 is left button
        notifyItemRemoved(position + 1);
    }

    public TapeElement getInitialTapeElement() {
        return initialTapeElement;
    }

    public void changeInitialTapeElement(TapeElement newElement) {
        Log.v(TAG, "changeActualTapeElement actual tape element changed");
        initialTapeElement = newElement;
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

    public Context getContext() {
        return context;
    }

    private class AddTapeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //listItem content
        private Button addB;

        AddTapeViewHolder(View itemView) {
            super(itemView);
            addB = (Button) itemView;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(tapeDimension, tapeDimension);
            addB.setLayoutParams(params);
            addB.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (getAdapterPosition() == 0) {
                Log.v(TAG, "addLeft button click noted");
                itemClickCallback.onLeftButtonClick();
            } else {
                Log.v(TAG, "addRight button click noted");
                itemClickCallback.onRightButtonClick();
            }
        }
    }

    private class TapeViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, AdapterView.OnItemSelectedListener {

        //listItem content
        private Spinner spinner;

        TapeViewHolder(View itemView) {
            super(itemView);
            spinner = (Spinner) itemView;
            spinner.setFocusable(true);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(tapeDimension, tapeDimension);
            spinner.setLayoutParams(params);
            spinner.setOnItemSelectedListener(this);
            spinner.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            Log.v(TAG, "tapeElement onLongClick noted");
            //position-1 because 0 is left button
            itemClickCallback.onSpinnerLongClick(getAdapterPosition() - 1);
            return false;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            Log.v(TAG, "tapeElement onItemSelected noted");
            //position-1 because 0 is left button
            itemClickCallback.onSpinnerItemSelected(getAdapterPosition() - 1, (Symbol) adapterView.getItemAtPosition(i));
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }
}
