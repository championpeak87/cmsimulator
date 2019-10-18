package fiitstu.gulis.cmsimulator.diagram;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import fiitstu.gulis.cmsimulator.elements.State;
import fiitstu.gulis.cmsimulator.elements.Transition;
import fiitstu.gulis.cmsimulator.machines.MachineStep;
import fiitstu.gulis.cmsimulator.R;

import static java.lang.Math.PI;

/**
 * A View for displaying a diagram that shows the transition history of an automaton
 *
 * Created by Martin on 7. 3. 2017.
 */
public class SimulationFlowView extends View {

    //log tag
    private static final String TAG = SimulationFlowView.class.getName();

    private static final int REFERENCE_DIMENSION = 360;

    //variables
    private int dimensionX;
    private int dimensionY;
    private int offsetX = 0;
    private int offsetY = 0;
    private int nodeRadius;

    //machine -> transitionCurve
    private Map<MachineStep, SimulationFlowTransitionCurve> machineTransitionCurveMap;

    //stores drawn objects
    private SimulationFlowStateNode firstStateNode;
    private List<SimulationFlowTransitionCurve> rootCurves;

    //queue for breadth-first traverse
    private Queue<SimulationFlowTransitionCurve> queue;

    //default paint for text
    private Paint textPaint;

    //for camera and touch events
    private int cameraX;
    private int cameraY;
    private int startX = 0;
    private int startY = 0;
    private int maxX = 0;
    private int maxY = 0;

    private float scaleFactor = 1.0f;

    private ScaleGestureDetector scaleDetector;

    public SimulationFlowView(Context context, AttributeSet attrs) {
        super(context, attrs);

        Activity activity = (Activity)context;
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int smallerDim = Math.round(Math.min(metrics.heightPixels, metrics.widthPixels) / metrics.density);
        scaleFactor = (float)Math.sqrt(REFERENCE_DIMENSION / smallerDim);
        final float minScale = 0.3f * scaleFactor;
        final float maxScale = 3.0f * scaleFactor;

        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(minScale, Math.min(scaleFactor, maxScale));
                invalidate();
                return true;
            }
        });

        nodeRadius = Math.round(Math.min(metrics.heightPixels, metrics.widthPixels) / 20);

        textPaint = new Paint();
        textPaint.setColor(ContextCompat.getColor(context, R.color.md_black_1000));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.diagram_text_size));

        //find view width and height at runtime
        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    dimensionX = getMeasuredWidth();
                    dimensionY = getMeasuredHeight();
                    //refresh positions when dimensions are known
                    if (dimensionX != 0 && dimensionY != 0) {
                        cameraX = dimensionX / 6;
                        cameraY = dimensionY / 4;
                        //check minimal width
                        if (offsetX < dimensionX / 4 + 2 * nodeRadius) {
                            offsetX = dimensionX / 4 + 2 * nodeRadius;
                        }
                        offsetY = dimensionY / 2;
                        if (Build.VERSION.SDK_INT < 16) {
                            getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        invalidate();
                    }
                }
            });
        }
    }

    //called manually after constructor, sets up additional things, resets all lists
    public void buildDiagram(List<Transition> transitionList) {
        //find max width to not overlap the transition desc with state node
        for (Transition transition : transitionList) {
            if (offsetX < textPaint.measureText(transition.getDiagramDesc()) + 2 * nodeRadius) {
                offsetX = (int) textPaint.measureText(transition.getDiagramDesc()) + 2 * nodeRadius;
            }
        }
        machineTransitionCurveMap = new HashMap<>();
        firstStateNode = null;
        rootCurves = new ArrayList<>();
        queue = new LinkedList<>();
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.save();
        canvas.scale(scaleFactor, scaleFactor);

        if (firstStateNode != null) {
            //////draw firstStateNode
            int posX = cameraX;
            int posY = cameraY;
            firstStateNode.setPosition(posX, posY, nodeRadius);
            firstStateNode.draw(canvas);
            posY = posY - (int) (textPaint.descent() + textPaint.ascent()) / 2;
            canvas.drawText(firstStateNode.getThisState().getValue(), posX, posY, textPaint);

            queue.clear();
            int size = rootCurves.size();
            queue.addAll(rootCurves);
            int indexX = 1;
            int indexY = 0;
            while (!queue.isEmpty()) {
                SimulationFlowTransitionCurve simulationFlowTransitionCurve = queue.remove();

                //////draw stateNode
                SimulationFlowStateNode stateNode = simulationFlowTransitionCurve.getToStateNode();
                posX = indexX * offsetX + cameraX;
                posY = indexY * offsetY + cameraY;
                stateNode.setPosition(posX, posY, nodeRadius);
                stateNode.draw(canvas);
                textPaint.setColor(stateNode.getBoundsPaint().getColor());
                posY = posY - (int) (textPaint.descent() + textPaint.ascent()) / 2;
                canvas.drawText(stateNode.getThisState().getValue(), posX, posY, textPaint);
                textPaint.setColor(ContextCompat.getColor(getContext(), R.color.md_black_1000));

                //////draw transitionCurve
                simulationFlowTransitionCurve.setPosition();
                ////draw curve
                canvas.drawPath(simulationFlowTransitionCurve, simulationFlowTransitionCurve.getPaint());
                canvas.drawPath(simulationFlowTransitionCurve.getArrowHead(), simulationFlowTransitionCurve.getPaint());
                ////draw text
                int textPosX = simulationFlowTransitionCurve.getControlX();
                //center vertically
                int textPosY = simulationFlowTransitionCurve.getControlY() - ((int) ((textPaint.descent() + textPaint.ascent()) / 2));
                canvas.save();
                canvas.rotate((float) (simulationFlowTransitionCurve.getRotAngle() * (180 / PI)), textPosX, textPosY);
                Transition transition = simulationFlowTransitionCurve.getTransition();
                canvas.drawText(transition.getDiagramDesc(), textPosX, textPosY, textPaint);
                canvas.restore();

                //update queue
                queue.addAll(simulationFlowTransitionCurve.getChildCurves());

                //prepare index for the next curve
                indexY++;
                if (indexY == size) {
                    indexX++;
                    indexY = 0;
                    size = queue.size();
                }
            }
        }
    }@Override
    public boolean onTouchEvent(final MotionEvent event) {
        int touchX = (int) event.getX();
        int touchY = (int) event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startX = touchX - cameraX;
                startY = touchY - cameraY;
                break;
            case MotionEvent.ACTION_MOVE:
                cameraX = touchX - startX;
                cameraY = touchY - startY;
                findMax();
                //check bounds
                if (touchX - startX - nodeRadius + (maxX - 1) * offsetX < 0) {
                    cameraX = nodeRadius - (maxX - 1) * offsetX;
                }
                if (touchY - startY - nodeRadius + (maxY - 1) * offsetY < 0) {
                    cameraY = nodeRadius - (maxY - 1) * offsetY;
                }
                if (touchX - startX + nodeRadius > (dimensionX / scaleFactor)) {
                    cameraX = Math.round(dimensionX / scaleFactor) - nodeRadius;
                }
                if (touchY - startY + nodeRadius > (dimensionY / scaleFactor)) {
                    cameraY = Math.round(dimensionY / scaleFactor) - nodeRadius;
                }
                invalidate();
                break;
        }



        scaleDetector.onTouchEvent(event);
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        dimensionX = getMeasuredWidth();
        dimensionY = getMeasuredHeight();
    }

    public void addFirstStateNode(State state) {
        Log.v(TAG, "addFirstStateNode in simulationFlow added");
        if (state == null) {
            firstStateNode = null;
        } else {
            firstStateNode = new SimulationFlowStateNode(state, this);
        }
    }

    public void addTransition(MachineStep oldMachineStep, MachineStep machineStep, Transition lastTransition, List<Transition> newTransitionList) {
       Log.v(TAG, "addTransitionList in simulationFlow added");

        if (firstStateNode != null) {
            //check if root transitions
            if (lastTransition == null) {
                ////add root transitions
                //prepare fromStateNode
                SimulationFlowStateNode fromStateNode = firstStateNode;
                fromStateNode.setMachineColor(machineStep.getColor());

                //add new TransitionCurves
                for (Transition transition : newTransitionList) {
                    SimulationFlowStateNode toStateNode = new SimulationFlowStateNode(transition.getToState(), this);
                    SimulationFlowTransitionCurve simulationFlowTransitionCurve =
                            new SimulationFlowTransitionCurve(transition, null, fromStateNode, toStateNode, this);
                    simulationFlowTransitionCurve.setMachineColor(machineStep.getColor());
                    rootCurves.add(simulationFlowTransitionCurve);
                }
                machineTransitionCurveMap.put(machineStep, null);
            } else {
                SimulationFlowTransitionCurve machineTransitionCurve = machineTransitionCurveMap.get(oldMachineStep);
                List<SimulationFlowTransitionCurve> lastTransitionCurveList;

                ////add after root transitions
                if (machineTransitionCurve == null) {
                    lastTransitionCurveList = rootCurves;
                    ////add anywhere in the rest of the tree
                } else {
                    lastTransitionCurveList = machineTransitionCurve.getChildCurves();
                }

                //find one of the usable transitions
                for (SimulationFlowTransitionCurve lastTransitionCurve : lastTransitionCurveList) {
                    if (lastTransitionCurve.getTransition().getId() == lastTransition.getId()) {
                        //remove previous colors
                        lastTransitionCurve.getFromStateNode().setMachineColor(null);
                        lastTransitionCurve.setMachineColor(null);

                        //prepare fromStateNode
                        SimulationFlowStateNode fromStateNode = lastTransitionCurve.getToStateNode();
                        fromStateNode.setMachineColor(machineStep.getColor());

                        //add new TransitionCurves
                        for (Transition transition : newTransitionList) {
                            SimulationFlowStateNode toStateNode = new SimulationFlowStateNode(transition.getToState(), this);
                            SimulationFlowTransitionCurve simulationFlowTransitionCurve =
                                    new SimulationFlowTransitionCurve(transition, lastTransitionCurve, fromStateNode, toStateNode, this);
                            simulationFlowTransitionCurve.setMachineColor(machineStep.getColor());
                            lastTransitionCurve.getChildCurves().add(simulationFlowTransitionCurve);
                        }
                        machineTransitionCurveMap.put(machineStep, lastTransitionCurve);
                        break;
                    }
                }
            }
        }
    }

    public void removeTransition(MachineStep machineStep) {
        Log.v(TAG, "removeTransitionList in simulationFlow removed");
        SimulationFlowTransitionCurve machineTransitionCurve = machineTransitionCurveMap.get(machineStep);
        if (machineTransitionCurve == null) {
            rootCurves.clear();
            machineTransitionCurveMap.clear();
        } else {
            //revert color and set progress
            machineTransitionCurve.setCompleteColor(MachineStep.PROGRESS);
            machineTransitionCurve.getToStateNode().getPaint()
                    .setColor(ContextCompat.getColor(getContext(), R.color.new_state_color));
            machineTransitionCurve.getToStateNode().getBoundsPaint()
                    .setColor(ContextCompat.getColor(getContext(), R.color.new_state_bounds_color));
            machineTransitionCurve.getChildCurves().clear();
        }
    }

    public void addTransitionColor(MachineStep machineStep) {
        Log.v(TAG, "addColorTransitionList in simulationFlow added");
        SimulationFlowTransitionCurve machineTransitionCurve = machineTransitionCurveMap.get(machineStep);
        SimulationFlowTransitionCurve newLastTransitionCurve = machineTransitionCurve.getParentCurve();
        //check if transitionCurve exists
        if (newLastTransitionCurve == null) {
            firstStateNode.setMachineColor(machineStep.getColor());
            for (SimulationFlowTransitionCurve simulationFlowTransitionCurve : rootCurves) {
                simulationFlowTransitionCurve.setCompleteColor(MachineStep.PROGRESS);
                simulationFlowTransitionCurve.setMachineColor(machineStep.getColor());
            }
            machineTransitionCurveMap.put(machineStep, null);
        } else {
            newLastTransitionCurve.getToStateNode().setMachineColor(machineStep.getColor());
            for (SimulationFlowTransitionCurve simulationFlowTransitionCurve : newLastTransitionCurve.getChildCurves()) {
                simulationFlowTransitionCurve.setCompleteColor(MachineStep.PROGRESS);
                simulationFlowTransitionCurve.setMachineColor(machineStep.getColor());
            }
            machineTransitionCurveMap.put(machineStep, newLastTransitionCurve);
        }
    }

    public void completeColor(List<MachineStep> doneMachineStepList,
                              List<MachineStep> progressMachineStepList, List<MachineStep> stuckMachineStepList) {
        Log.v(TAG, "completeColor in simulationFlow called");
        if (firstStateNode != null) {
            for (MachineStep machineStep : stuckMachineStepList) {
                SimulationFlowTransitionCurve machineTransitionCurve = machineTransitionCurveMap.get(machineStep);
                while (machineTransitionCurve != null) {
                    machineTransitionCurve.getToStateNode().setCompleteColor(machineStep.getMachineStatus());
                    machineTransitionCurve.setCompleteColor(machineStep.getMachineStatus());
                    machineTransitionCurve = machineTransitionCurve.getParentCurve();
                }
                firstStateNode.setCompleteColor(machineStep.getMachineStatus());
            }
            for (MachineStep machineStep : progressMachineStepList) {
                SimulationFlowTransitionCurve machineTransitionCurve = machineTransitionCurveMap.get(machineStep);
                while (machineTransitionCurve != null) {
                    machineTransitionCurve.getToStateNode().setCompleteColor(machineStep.getMachineStatus());
                    machineTransitionCurve.setCompleteColor(machineStep.getMachineStatus());
                    machineTransitionCurve = machineTransitionCurve.getParentCurve();
                }
                firstStateNode.setCompleteColor(machineStep.getMachineStatus());
            }
            for (MachineStep machineStep : doneMachineStepList) {
                SimulationFlowTransitionCurve machineTransitionCurve = machineTransitionCurveMap.get(machineStep);
                while (machineTransitionCurve != null) {
                    machineTransitionCurve.getToStateNode().setCompleteColor(machineStep.getMachineStatus());
                    machineTransitionCurve.setCompleteColor(machineStep.getMachineStatus());
                    machineTransitionCurve = machineTransitionCurve.getParentCurve();
                }
                firstStateNode.setCompleteColor(machineStep.getMachineStatus());
            }
        }
    }

    public void removeAllTransitions() {
        rootCurves.clear();
        machineTransitionCurveMap.clear();
        maxX = 0;
        maxY = 0;
    }

    private void findMax() {
        queue.clear();
        int size = rootCurves.size();
        queue.addAll(rootCurves);
        int indexX = 1;
        int indexY = 0;
        maxX = 1;
        maxY = 1;
        while (!queue.isEmpty()) {
            SimulationFlowTransitionCurve simulationFlowTransitionCurve = queue.remove();
            //update queue
            queue.addAll(simulationFlowTransitionCurve.getChildCurves());

            //prepare index for the next curve
            indexY++;
            if (maxY < indexY) {
                maxY = indexY;
            }
            if (indexY == size) {
                indexX++;
                if (maxX < indexX) {
                    maxX = indexX;
                }
                indexY = 0;
                size = queue.size();
            }
        }
    }

    public void moveCamera() {
        Log.v(TAG, "camera moved");
        findMax();
        cameraX = dimensionX / 2 - (maxX - 2) * offsetX - ((int) (0.5 * offsetX));
        invalidate();
    }

    public int getDimensionY() {
        return dimensionY;
    }

    public void setDimensionY(int dimensionY) {
        this.dimensionY = dimensionY;
    }

    public int getNodeRadius() {
        return nodeRadius;
    }
}
