package fiitstu.gulis.cmsimulator.diagram;

import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import fiitstu.gulis.cmsimulator.elements.Transition;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.machines.MachineStep;

import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.sin;
import static java.lang.Math.cos;

/**
 * A class that represents a transition curve displayed in a state diagram
 *
 * Created by Martin on 28. 2. 2017.
 */
public class DiagramTransitionCurve extends Path {

    //log tag
    private static final String TAG = DiagramTransitionCurve.class.getName();

    //constants
    private static final int DEFAULT_STROKE_WIDTH = 3;

    //represents transitionCurve
    private List<Transition> transitionList;

    //arrowHead with extended class creates curve
    private Path arrowHead;

    //default paint (when no transition is actual)
    private Paint defaultPaint;

    //paints for actual transition indicator - indicates that machine will use this transition
    //size of the outer list is the same as number of transitions in curve
    private List<List<MachineStep>> machineSteps; //has color to draw
    private List<Paint> actualPaints; //has paint to draw

    //indicates nondeterministic transition
    //size of the list is the same as number of transitions in curve
    private List<Boolean> nondeterministic;

    //control point of bezier curve, also location for text (transition description)
    private int controlX;
    private int controlY;

    //to rotate text (transition description)
    private double rotAngle;

    //to access radius
    private DiagramView diagramView;

    DiagramTransitionCurve(DiagramView diagramView) {
        Log.v(TAG, "DiagramTransitionCurve constructor called");
        this.arrowHead = new Path();
        this.transitionList = new ArrayList<>();
        this.machineSteps = new ArrayList<>();
        this.actualPaints = new ArrayList<>();
        this.nondeterministic = new ArrayList<>();
        this.diagramView = diagramView;

        defaultPaint = new Paint();
        defaultPaint.setStyle(Paint.Style.STROKE);
        defaultPaint.setStrokeCap(Paint.Cap.ROUND);
        defaultPaint.setStrokeWidth(DEFAULT_STROKE_WIDTH);
        defaultPaint.setColor(ContextCompat.getColor(diagramView.getContext(), R.color.md_black_1000));
        defaultPaint.setAntiAlias(true);

        //useless to set the position here because the list is empty
        Log.v(TAG, "DiagramTransitionCurve initialized");
    }

    void setPosition(int cameraX, int cameraY) {
        if (transitionList != null && !transitionList.isEmpty()) {
            int radius = diagramView.getNodeRadius();
            double endAngle;
            int endX;
            int endY;
            //check if self-transition
            if (transitionList.get(0).getFromState() == transitionList.get(0).getToState()) {
                ////calculate curve
                int posX = transitionList.get(0).getFromState().getPositionX() + cameraX;
                int posY = transitionList.get(0).getFromState().getPositionY() + cameraY;
                //calculate control points
                rotAngle = 0;
                controlX = posX;
                controlY = posY - 3 * radius;
                int control1X = posX + radius;
                int control1Y = posY - 3 * radius;
                int control2X = posX - radius;
                int control2Y = posY - 3 * radius;
                //calculate intersection between state and control points (bezier curve start and end points)
                double startAngle = atan2(control1Y - posY, control1X - posX);
                int startX = ((int) (radius * cos(startAngle))) + posX;
                int startY = ((int) (radius * sin(startAngle))) + posY;
                endAngle = atan2(control2Y - posY, control2X - posX);
                endX = ((int) (radius * cos(endAngle))) + posX;
                endY = ((int) (radius * sin(endAngle))) + posY;
                reset();
                moveTo(startX, startY);
                cubicTo(control1X, control1Y, control2X, control2Y, endX, endY);
            } else {
                ////calculate curve
                int fromPosX = transitionList.get(0).getFromState().getPositionX() + cameraX;
                int fromPosY = transitionList.get(0).getFromState().getPositionY() + cameraY;
                int toPosX = transitionList.get(0).getToState().getPositionX() + cameraX;
                int toPosY = transitionList.get(0).getToState().getPositionY() + cameraY;
                //calculate control points
                rotAngle = atan2(toPosY - fromPosY, toPosX - fromPosX);
                controlX = ((int) (2 * radius * cos(rotAngle - PI / 2))) + (toPosX + fromPosX) / 2;
                controlY = ((int) (2 * radius * sin(rotAngle - PI / 2))) + (toPosY + fromPosY) / 2;
                //calculate intersection between states and control point (bezier curve start and end points)
                double startAngle = atan2(controlY - fromPosY, controlX - fromPosX);
                int startX = ((int) (radius * cos(startAngle))) + fromPosX;
                int startY = ((int) (radius * sin(startAngle))) + fromPosY;
                endAngle = atan2(controlY - toPosY, controlX - toPosX);
                endX = ((int) (radius * cos(endAngle))) + toPosX;
                endY = ((int) (radius * sin(endAngle))) + toPosY;
                reset();
                moveTo(startX, startY);
                quadTo(controlX, controlY, endX, endY);
            }
            ////calculate arrowHead
            arrowHead.reset();
            arrowHead.moveTo(endX + ((int) (0.5f * radius * cos(endAngle + PI / 6))), endY + ((int) (0.5f * radius * sin(endAngle + PI / 6))));
            arrowHead.lineTo(endX, endY);
            arrowHead.lineTo(endX + ((int) (0.5f * radius * cos(endAngle - PI / 6))), endY + ((int) (0.5f * radius * sin(endAngle - PI / 6))));
        }
    }

    Transition getFirstFromTransition() {
        return transitionList.get(0);
    }

    public List<Transition> getTransitionList() {
        return transitionList;
    }

    void addFromTransition(Transition transition) {
        Log.v(TAG, "addFromTransition transition added");
        transitionList.add(transition);
        machineSteps.add(new ArrayList<MachineStep>());
        nondeterministic.add(false);
    }

    void removeFromTransition(Transition transition) {
        Log.v(TAG, "removeFromTransition transition removed");
        int position = transitionList.indexOf(transition);
        transitionList.remove(transition);
        machineSteps.remove(position);
        nondeterministic.remove(position);
    }

    Path getArrowHead() {
        return arrowHead;
    }

    Paint getDefaultPaint() {
        return defaultPaint;
    }

    List<List<MachineStep>> getMachineSteps() {
        return machineSteps;
    }

    //method to add new color and prepare paint
    public void addMachineColor(Transition transition, MachineStep machineStep) {
        Log.v(TAG, "addMachineColor color to transitionCurve added");

        //prepare paint
        Paint newPaint = new Paint();
        newPaint.setStyle(Paint.Style.STROKE);
        newPaint.setStrokeCap(Paint.Cap.ROUND);
        //color is set when drawn
        //newPaint.setColor(machineColor.getValue());
        newPaint.setAntiAlias(true);
        int newStrokeWidth = (actualPaints.size() + 1) * DEFAULT_STROKE_WIDTH;
        newPaint.setStrokeWidth(newStrokeWidth);

        //add paint and color
        actualPaints.add(newPaint);
        int index = transitionList.indexOf(transition);
        machineSteps.get(index).add(machineStep);
    }

    //method to remove color and paint
    public void removeMachineColor(Transition transition, MachineStep machineStep) {
        Log.v(TAG, "removeMachineColor from transitionCurve called");
        //check if removal is possible (because not always the machine is visible in this diagram)
        int index = transitionList.indexOf(transition);
        if (transitionList.contains(transition) && machineSteps.get(index).contains(machineStep)) {
            //always remove the last one (otherwise there will be blank space)
            actualPaints.remove(actualPaints.size() - 1);
            machineSteps.get(index).remove(machineStep);
            Log.v(TAG, "removeMachineColor from transitionCurve removed");
        }
    }

    List<Paint> getActualPaints() {
        return actualPaints;
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

    public List<Boolean> getNondeterministic() {
        return nondeterministic;
    }
}
