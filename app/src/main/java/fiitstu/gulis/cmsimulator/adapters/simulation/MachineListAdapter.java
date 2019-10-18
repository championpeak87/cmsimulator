package fiitstu.gulis.cmsimulator.adapters.simulation;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.diagram.DiagramView;
import fiitstu.gulis.cmsimulator.diagram.SimulationFlowView;
import fiitstu.gulis.cmsimulator.elements.MachineColorsGenerator;
import fiitstu.gulis.cmsimulator.elements.Transition;
import fiitstu.gulis.cmsimulator.machines.MachineList;
import fiitstu.gulis.cmsimulator.machines.MachineStep;

/**
 * Adapter that maintains a list of machines and displays their respective tapes
 *
 * Created by Martin on 17. 2. 2017.
 */
public class MachineListAdapter extends RecyclerView.Adapter<MachineListAdapter.ViewHolder> implements MachineList {

    //log tag
    private static final String TAG = MachineListAdapter.class.getName();

    private List<MachineStep> items;
    private Context context;
    private LayoutInflater inflater;
    private int tapeDimension;
    private DiagramView diagramView;
    private SimulationFlowView simulationFlowView;
    private MachineColorsGenerator machineColorsGenerator;
    private int depth;

    public MachineListAdapter(Context context, int tapeDimension,
                              DiagramView diagramView, SimulationFlowView simulationFlowView,
                              MachineColorsGenerator machineColorsGenerator) {
        this.items = new ArrayList<>();
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.tapeDimension = tapeDimension;
        this.diagramView = diagramView;
        this.simulationFlowView = simulationFlowView;
        this.machineColorsGenerator = machineColorsGenerator;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_element_simulation_machine, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int index = 0;
        int count = -1;
        //find the right element
        while (count != position) {
            if (items.get(index).getDepth() == depth) {
                count++;
                if (count == position) {
                    break;
                }
            }
            index++;
        }

        ViewGroup.LayoutParams params = holder.machineButton.getLayoutParams();
        params.width = tapeDimension;
        params.height = tapeDimension;
        holder.machineButton.setLayoutParams(params);
        holder.machineButton.getBackground().setColorFilter(items.get(index).getColor().getValue(), PorterDuff.Mode.MULTIPLY);
        MachineTapeListAdapter tapeListAdapter = (MachineTapeListAdapter)items.get(index).getTape();
        tapeListAdapter.setTapeDimension(tapeDimension);
        holder.machineTapeView.setAdapter(tapeListAdapter);

        //scroll right
        if (items.get(index).getTape().getCurrentElement() != null) {
            //+1 to be in the middle (approximately)
            holder.machineTapeView.smoothScrollToPosition(
                    items.get(index).getTape().getCurrentPosition() + 1);
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;
        //count the right elements
        for (MachineStep machineStep : items) {
            if (machineStep.getDepth() == depth) {
                count++;
            }
        }
        return count;
        //return items.size();
    }

    public List<MachineStep> getItems() {
        return items;
    }

    public void addItem(MachineStep item) {
        Log.v(TAG, "addItem machine added");
        this.items.add(item);
        item.setColor(machineColorsGenerator.getNextColor());
        //notifyItemInserted(items.size() - 1); //would need to find the right index
        notifyDataSetChanged();
    }

    public void addItem(int position, MachineStep item) {
        Log.v(TAG, "addItem to position machine added");
        this.items.add(position, item);
        item.setColor(machineColorsGenerator.getNextColor());
        notifyItemInserted(position);
    }

    public void removeItem(MachineStep item) {
        Log.v(TAG, "removeItem machine removed");
        int position = items.indexOf(item);
        items.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void recordTransition(MachineStep oldMachineStep, MachineStep machineStep, Transition lastTransition, List<Transition> newTransitionList) {
        addSimulationFlowTransition(oldMachineStep, machineStep, lastTransition, newTransitionList);
    }

    @Override
    public void undoTransition(MachineStep machineStep) {
        removeSimulationFlowTransition(machineStep);
        if (machineStep.getLastTransitionList().size() > 1) {
            addSimulationFlowTransitionColor(machineStep);
        }
    }

    public void removeAll() {
        Log.v(TAG, "removeAll machines removed");
        //remove colors from diagram (traverse machines should be faster than traverse every stateNode)
        for (MachineStep machineStep : items) {
            if (machineStep.getCurrentState() != null && diagramView.getStateNodesMap().get(machineStep.getCurrentState().getId()) != null) {
                diagramView.getStateNodesMap().get(machineStep.getCurrentState().getId()).removeMachineColor(machineStep);
                for (Transition transition : machineStep.getUsableTransitionList()) {
                    if (transition != null && diagramView.getTransitionCurveMap().get(transition.getId()) != null) {
                        diagramView.getTransitionCurveMap().get(transition.getId()).removeMachineColor(transition, machineStep);
                    }
                }
            }
        }
        items.clear();
        simulationFlowView.removeAllTransitions();
        notifyDataSetChanged();
    }

    public void addDiagramColor(MachineStep machineStep) {
        if (machineStep.getCurrentState() != null) {
            diagramView.addMachineColor(machineStep);
            if (machineStep.getUsableTransitionList() != null) {
                for (Transition transition : machineStep.getUsableTransitionList()) {
                    diagramView.getTransitionCurveMap().get(transition.getId()).addMachineColor(transition, machineStep);
                }
            }
        }
    }

    public void removeDiagramColor(MachineStep machineStep) {
        if (machineStep.getCurrentState() != null) {
            diagramView.getStateNodesMap().get(machineStep.getCurrentState().getId()).removeMachineColor(machineStep);
            if (machineStep.getUsableTransitionList() != null) {
                for (Transition transition : machineStep.getUsableTransitionList()) {
                    diagramView.getTransitionCurveMap().get(transition.getId()).removeMachineColor(transition, machineStep);
                }
            }
        }
    }

    private void addSimulationFlowTransition(MachineStep oldMachineStep, MachineStep machineStep, Transition lastTransition, List<Transition> newTransitionList) {
        simulationFlowView.addTransition(oldMachineStep, machineStep, lastTransition, newTransitionList);
    }

    private void removeSimulationFlowTransition(MachineStep machineStep) {
        simulationFlowView.removeTransition(machineStep);
    }

    private void addSimulationFlowTransitionColor(MachineStep machineStep) {
        simulationFlowView.addTransitionColor(machineStep);
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

        private ImageButton machineButton;
        private RecyclerView machineTapeView;

        public ViewHolder(View itemView) {
            super(itemView);
            machineButton = itemView.findViewById(R.id.imageButton_simulation_machine_button);
            machineButton.setClickable(false);
            machineTapeView = itemView.findViewById(R.id.recyclerView_simulation_machine_tape);
            machineTapeView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        }
    }

}
