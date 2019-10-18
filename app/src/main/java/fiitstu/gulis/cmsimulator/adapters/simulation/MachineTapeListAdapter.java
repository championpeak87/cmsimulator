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

import fiitstu.gulis.cmsimulator.app.CMSimulator;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.elements.Symbol;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.elements.TapeElement;
import fiitstu.gulis.cmsimulator.machines.MachineStep;
import fiitstu.gulis.cmsimulator.machines.MachineTape;

/**
 * Adapter for displaying the contents of a machine's tape.
 *
 * Created by Martin on 22. 2. 2017.
 */
public class MachineTapeListAdapter extends RecyclerView.Adapter<MachineTapeListAdapter.ViewHolder> implements MachineTape {

    //log tag
    private static final String TAG = MachineTapeListAdapter.class.getName();

    private List<TapeElement> items;
    private int currentPosition;
    private LayoutInflater inflater;
    private int tapeDimension;

    private Context context;
    private MachineTapeSpinnerAdapter machineTapeSpinnerAdapter;
    private MachineStep machineStep;

    public MachineTapeListAdapter(Context context, List<TapeElement> items, MachineTapeSpinnerAdapter machineTapeSpinnerAdapter, MachineStep machineStep) {
        this.items = items;
        this.currentPosition = 0;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.tapeDimension = 0;
        this.machineTapeSpinnerAdapter = machineTapeSpinnerAdapter;
        this.machineStep = machineStep;
    }

    /**
     * Creates an identical copy of the MachineListAdapter object
     * @param machineTapeListAdapter
     */
    public MachineTapeListAdapter(MachineTapeListAdapter machineTapeListAdapter) {
        this.items = new ArrayList<>();
        for (TapeElement tapeElement: machineTapeListAdapter.items) {
            this.items.add(new TapeElement(tapeElement));
        }
        this.currentPosition = machineTapeListAdapter.currentPosition;
        this.inflater = LayoutInflater.from(machineTapeListAdapter.context);
        this.context = machineTapeListAdapter.context;
        this.tapeDimension = machineTapeListAdapter.tapeDimension;
        this.machineTapeSpinnerAdapter = machineTapeListAdapter.machineTapeSpinnerAdapter;
        this.machineStep = machineTapeListAdapter.machineStep;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_element_simulation_tape_static, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(tapeDimension, tapeDimension);
        holder.button.setLayoutParams(params);
        //position+1 because 0 is the left most tapeElement
        holder.button.setText(items.get(position + 1).getSymbol().getValue());
        if (currentPosition == position) {
            holder.button.getBackground().setColorFilter(machineStep.getColor().getValue(), PorterDuff.Mode.MULTIPLY);
        } else if (items.get(position + 1).isBreakpoint()) {
            holder.button.getBackground().setColorFilter(ContextCompat.getColor(CMSimulator.getContext(), R.color.break_point_color), PorterDuff.Mode.MULTIPLY);
        } else {
            holder.button.getBackground().clearColorFilter();
        }

        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(holder.button,
                1,
                CMSimulator.getTextSize(holder.button.getContext(), android.R.attr.textAppearanceListItemSmall),
                1,
                TypedValue.COMPLEX_UNIT_PX);
    }

    @Override
    public int getItemCount() {
        return items.size() - 2;
    }

    @Override
    public boolean matches(List<Symbol> symbolList) {
        Symbol emptySymbol = DataSource.getInstance().getInputSymbolWithProperties(Symbol.EMPTY);
        int myFirstNonempty = 1;
        while (myFirstNonempty < (items.size() - 1) && items.get(myFirstNonempty).getSymbol().getId() == emptySymbol.getId()) {
            myFirstNonempty++;
        }
        int argFirstNonempty = 0;
        while (argFirstNonempty < symbolList.size() && symbolList.get(argFirstNonempty).getId() == emptySymbol.getId()) {
            argFirstNonempty++;
        }

        //resolve the case when either tape consists entirely out of empty symbols
        if (argFirstNonempty == symbolList.size()) {
            if (myFirstNonempty == items.size() - 1) {
                return true; //both are empty, thus equivalent
            }
            else {
                return false; //tape is not empty, but argument is
            }
        }
        else if (myFirstNonempty == items.size() - 1) {
            return false; //tape is empty, but argument is not
        }

        int myLastNonempty = items.size() - 2;
        while (items.get(myLastNonempty).getSymbol().getId() == emptySymbol.getId()) {
            myLastNonempty--;
        }
        int argLastNonempty = symbolList.size() - 1;
        while (symbolList.get(argLastNonempty).getId() == emptySymbol.getId()) {
            argLastNonempty--;
        }

        if (argLastNonempty - argFirstNonempty != myLastNonempty - myFirstNonempty) {
            return false; //different lengths
        }

        for (int i = 0; i <= myLastNonempty - myFirstNonempty; i++) {
            if (!symbolList.get(argFirstNonempty + i).getValue().equals(items.get(myFirstNonempty + i).getSymbol().getValue())) {
                return false;
            }
        }

        return true;
    }

    public void setTapeDimension(int tapeDimension) {
        this.tapeDimension = tapeDimension;
    }

    @Override
    public void setMachineStep(MachineStep machineStep) {
        this.machineStep = machineStep;
    }

    @Override
    public void addToLeft(Symbol symbol) {
        items.add(1, new TapeElement(symbol, items.get(0).getOrder() - 1));
        notifyItemInserted(1);
    }

    @Override
    public void addToRight(Symbol symbol) {
        items.add(items.size() - 1, new TapeElement(symbol, items.get(items.size() - 1).getOrder() + 1));
        notifyItemInserted(items.size() - 1);
    }

    @Override
    public void setSymbol(int position, Symbol symbol) {
        items.get(position + 1).setSymbol(symbol);
    }

    @Override
    public void setBreakpoint(int position, boolean breakpoint) {
        items.get(position + 1).setBreakpoint(breakpoint);
    }

    @Override
    public void clearBreakpoints() {
        //changed i=0 to i=1, untested, may have cause bad things
        for (int i = 1; i < items.size() - 1; i++) {
            if (items.get(i).isBreakpoint()) {
                items.get(i).setBreakpoint(false);
                notifyItemChanged(i);
            }
        }
    }

    @Override
    public int size() {
        return getItemCount();
    }

    @Override
    public TapeElement getCurrentElement() {
        if (currentPosition == -1) {
            return null;
        }
        else {
            return items.get(currentPosition + 1);
        }
    }

    @Override
    public void removeElement(int index) {
        Log.v(TAG, "removeElement actual tape element removed");
        items.remove(index);
    }

    @Override
    public int getCurrentPosition() {
        return currentPosition;
    }

    @Override
    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    @Override
    public void moveRight() {
        if (currentPosition == items.size() - 3) {
            addToRight(DataSource.getInstance().getInputSymbolWithProperties(Symbol.EMPTY));
        }
        currentPosition++;
    }

    @Override
    public void moveLeft() {
        if (currentPosition == 0) {
            addToLeft(DataSource.getInstance().getInputSymbolWithProperties(Symbol.EMPTY));
        }
        else {
            currentPosition--;
        }
    }

    @Override
    public MachineTape copy() {
        return new MachineTapeListAdapter(this);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        //listItem content
        private Button button;

        public ViewHolder(View itemView) {
            super(itemView);
            button = (Button) itemView;
            button.setClickable(false);
        }
    }
}
