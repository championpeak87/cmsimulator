package fiitstu.gulis.cmsimulator.adapters.simulation;

import android.graphics.PorterDuff;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import fiitstu.gulis.cmsimulator.app.CMSimulator;
import fiitstu.gulis.cmsimulator.machines.MachineStep;
import fiitstu.gulis.cmsimulator.machines.PushdownAutomatonStep;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.machines.StackListObserver;

/**
 * Adapter for showing the list of stacks of all machines
 *
 * Created by Martin on 25. 3. 2017.
 */
public class StackListAdapter extends RecyclerView.Adapter<StackListAdapter.ViewHolder> implements StackListObserver {

    //log tag
    private static final String TAG = StackListAdapter.class.getName();

    private MachineListAdapter items;
    private LayoutInflater inflater;
    private int tapeDimension;
    private int depth;

    public StackListAdapter(int tapeDimension, MachineListAdapter machineListAdapter) {
        this.items = machineListAdapter;
        this.inflater = LayoutInflater.from(CMSimulator.getContext());
        this.tapeDimension = tapeDimension;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_element_simulation_stack, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int index = 0;
        int count = -1;
        //find the right element
        while (count != position) {
            if (items.getItems().get(index).getDepth() == depth) {
                count++;
                if (count == position) {
                    break;
                }
            }
            index++;
        }

        ViewGroup.LayoutParams params = holder.stackButton.getLayoutParams();
        params.width = tapeDimension;
        params.height = tapeDimension;
        holder.stackButton.setLayoutParams(params);
        holder.stackButton.getBackground().setColorFilter(items.getItems().get(index).getColor().getValue(), PorterDuff.Mode.MULTIPLY);
        StackTapeListAdapter stackAdapter = (StackTapeListAdapter)((PushdownAutomatonStep) items.getItems().get(index)).getStack();
        stackAdapter.setTapeDimension(tapeDimension);
        holder.stackTapeView.setAdapter(stackAdapter);
        //scroll up
        if (((PushdownAutomatonStep) items.getItems().get(index)).getStack().getItemCount() - 1 >= 0) {
            //-1 because size-1, to be at the top
            holder.stackTapeView.smoothScrollToPosition(
                    ((PushdownAutomatonStep) items.getItems().get(index)).getStack().getItemCount() - 1);
        }
    }


    @Override
    public int getItemCount() {
        int count = 0;
        //count the right elements
        for (MachineStep machineStep : items.getItems()) {
            if (machineStep.getDepth() == depth) {
                count++;
            }
        }
        return count;
        //return items.getItems().size();
    }

    public void setTapeDimension(int tapeDimension) {
        this.tapeDimension = tapeDimension;
    }

    public int getTapeDimension() {
        return tapeDimension;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageButton stackButton;
        private RecyclerView stackTapeView;

        public ViewHolder(View itemView) {
            super(itemView);
            stackButton = itemView.findViewById(R.id.imageButton_simulation_stack_button);
            stackButton.setClickable(false);
            stackTapeView = itemView.findViewById(R.id.recyclerView_simulation_stack_tape);
            stackTapeView.setLayoutManager(new LinearLayoutManager(CMSimulator.getContext(), LinearLayoutManager.VERTICAL, true));
        }
    }

}
