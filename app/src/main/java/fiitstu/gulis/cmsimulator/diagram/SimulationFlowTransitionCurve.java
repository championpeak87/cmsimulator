package fiitstu.gulis.cmsimulator.diagram;

import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import fiitstu.gulis.cmsimulator.elements.Transition;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.elements.MachineColor;
import fiitstu.gulis.cmsimulator.machines.MachineStep;

import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * A class that represents a connection in a simulation flow diagram
 *
 * Created by Martin on 7. 3. 2017.
 */
class SimulationFlowTransitionCurve extends Path {

    //log tag
    private static final String TAG = SimulationFlowTransitionCurve.class.getName();

    //constants
    private static final int DEFAULT_STROKE_WIDTH = 3;

    //represents transitionCurve
    private Transition transition;
    private SimulationFlowTransitionCurve parentCurve;
    private List<SimulationFlowTransitionCurve> childCurves;
    private SimulationFlowStateNode fromStateNode;
    private SimulationFlowStateNode toStateNode;

    //arrowHead with extended class creates curve
    private Path arrowHead;

    //paint and color
    private Paint paint;
    private MachineColor machineColor;

    //control point of bezier curve, also location for text (transition description)
    private int controlX;
    private int controlY;

    //to rotate text (transition description)
    private double rotAngle;

    //to access radius
    private SimulationFlowView simulationFlowView;

    SimulationFlowTransitionCurve(Transition transition, SimulationFlowTransitionCurve parentCurve,
                                         SimulationFlowStateNode fromStateNode,
                                         SimulationFlowStateNode toStateNode,
                                         SimulationFlowView simulationFlowView) {
        Log.v(TAG, "SimulationFlowTransitionCurve constructor called");
        this.transition = transition;
        this.parentCurve = parentCurve;
        this.childCurves = new ArrayList<>();
        this.fromStateNode = fromStateNode;
        this.toStateNode = toStateNode;
        this.arrowHead = new Path();
        this.simulationFlowView = simulationFlowView;

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(DEFAULT_STROKE_WIDTH);
        paint.setColor(ContextCompat.getColor(simulationFlowView.getContext(), R.color.md_black_1000));
        paint.setAntiAlias(true);

        //useless to set the position here because the list is empty
        Log.v(TAG, "DiagramTransitionCurve initialized");
    }

    void setPosition() {
        int radius = simulationFlowView.getNodeRadius();

        ////calculate curve
        int fromPosX = fromStateNode.getPositionX();
        int fromPosY = fromStateNode.getPositionY();
        int toPosX = toStateNode.getPositionX();
        int toPosY = toStateNode.getPositionY();
        //calculate control points
        rotAngle = atan2(toPosY - fromPosY, toPosX - fromPosX);
        controlX = ((int) (0.75f * radius * cos(rotAngle - PI / 2))) + (toPosX + fromPosX) / 2;
        controlY = ((int) (0.75f * radius * sin(rotAngle - PI / 2))) + (toPosY + fromPosY) / 2;
        //calculate intersection between states and control point (bezier curve start and end points)
        int startX = ((int) (radius * cos(rotAngle))) + fromPosX;
        int startY = ((int) (radius * sin(rotAngle))) + fromPosY;
        int endX = ((int) (radius * cos(rotAngle + PI))) + toPosX;
        int endY = ((int) (radius * sin(rotAngle + PI))) + toPosY;
        reset();
        moveTo(startX, startY);
        lineTo(endX, endY);

        ////calculate arrowHead
        arrowHead.reset();
        arrowHead.moveTo(endX + ((int) (0.5f * radius * cos(rotAngle + PI + PI / 6))), endY + ((int) (0.5f * radius * sin(rotAngle + PI + PI / 6))));
        arrowHead.lineTo(endX, endY);
        arrowHead.lineTo(endX + ((int) (0.5f * radius * cos(rotAngle + PI - PI / 6))), endY + ((int) (0.5f * radius * sin(rotAngle + PI - PI / 6))));
    }

    public Transition getTransition() {
        return transition;
    }

    SimulationFlowTransitionCurve getParentCurve() {
        return parentCurve;
    }

    List<SimulationFlowTransitionCurve> getChildCurves() {
        return childCurves;
    }

    SimulationFlowStateNode getFromStateNode() {
        return fromStateNode;
    }

    SimulationFlowStateNode getToStateNode() {
        return toStateNode;
    }

    Path getArrowHead() {
        return arrowHead;
    }

    Paint getPaint() {
        return paint;
    }

    void setMachineColor(MachineColor machineColor) {
        this.machineColor = machineColor;
        if (machineColor == null) {
            paint.setColor(ContextCompat.getColor(simulationFlowView.getContext(), R.color.md_black_1000));
        } else {
            paint.setColor(machineColor.getValue());
        }
    }

    void setCompleteColor(int done) {
        if (done == MachineStep.DONE) {
            paint.setColor(ContextCompat.getColor(simulationFlowView.getContext(), R.color.machine_done_color));
        } else if (done == MachineStep.STUCK) {
            paint.setColor(ContextCompat.getColor(simulationFlowView.getContext(), R.color.machine_stuck_color));
        } else {
            setMachineColor(machineColor);
        }
    }

    int getControlX() {
        return controlX;
    }

    int getControlY() {
        return controlY;
    }

    double getRotAngle() {
        return rotAngle;
    }
}
