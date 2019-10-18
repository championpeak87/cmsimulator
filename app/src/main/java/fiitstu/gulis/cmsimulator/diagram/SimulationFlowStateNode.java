package fiitstu.gulis.cmsimulator.diagram;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import fiitstu.gulis.cmsimulator.elements.State;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.elements.MachineColor;
import fiitstu.gulis.cmsimulator.machines.MachineStep;

/**
 * A class that represents a state in a simulation flow diagram
 *
 * Created by Martin on 7. 3. 2017.
 */
public class SimulationFlowStateNode extends ShapeDrawable {

    //log tag
    private static final String TAG = SimulationFlowStateNode.class.getName();

    //constants
    private static final int STROKE_WIDTH = 5;

    //represents stateNode
    private State state;

    //position (used for transitionCurves)
    private int positionX;
    private int positionY;

    //with extended class creates node
    private ShapeDrawable bounds;

    //color
    private MachineColor machineColor;

    //to access dimensions and radius
    private SimulationFlowView simulationFlowView;

    SimulationFlowStateNode(State state, SimulationFlowView simulationFlowView) {
        Log.v(TAG, "SimulationFlowStateNode constructor called");
        this.state = state;
        this.simulationFlowView = simulationFlowView;

        setShape(new OvalShape());

        bounds = new ShapeDrawable(new OvalShape());
        bounds.getPaint().setStyle(Paint.Style.STROKE);
        bounds.getPaint().setStrokeWidth(STROKE_WIDTH);
        bounds.getPaint().setColor(ContextCompat.getColor(simulationFlowView.getContext(), R.color.new_state_bounds_color));
        bounds.getPaint().setAntiAlias(true);

        //set first color
        getPaint().setColor(ContextCompat.getColor(simulationFlowView.getContext(), R.color.new_state_color));
        Log.v(TAG, "new SimulationFlowStateNode initialized");
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        bounds.draw(canvas);
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        bounds.setBounds(left, top, right, bottom);
    }

    void setPosition(int centerX, int centerY, int radius) {
        positionX = centerX;
        positionY = centerY;
        setBounds(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
    }

    State getThisState() {
        return state;
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    Paint getBoundsPaint() {
        return bounds.getPaint();
    }

    void setMachineColor(MachineColor machineColor) {
        this.machineColor = machineColor;
        if (machineColor == null) {
            getPaint().setColor(ContextCompat.getColor(simulationFlowView.getContext(), R.color.state_color));
        } else {
            getPaint().setColor(machineColor.getValue());
        }
        bounds.getPaint().setColor(ContextCompat.getColor(simulationFlowView.getContext(), R.color.md_black_1000));
    }

    void setCompleteColor(int done) {
        if (done == MachineStep.DONE) {
            getPaint().setColor(ContextCompat.getColor(simulationFlowView.getContext(), R.color.machine_done_color));
        } else if (done == MachineStep.STUCK) {
            getPaint().setColor(ContextCompat.getColor(simulationFlowView.getContext(), R.color.machine_stuck_color));
        } else {
            setMachineColor(machineColor);
        }
        bounds.getPaint().setColor(ContextCompat.getColor(simulationFlowView.getContext(), R.color.md_black_1000));
    }
}
