package fiitstu.gulis.cmsimulator.diagram;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import fiitstu.gulis.cmsimulator.elements.State;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.machines.MachineStep;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * A class that represents a state displayed in a state diagram
 *
 * Created by Martin on 28. 2. 2017.
 */
public class DiagramStateNode extends ShapeDrawable {

    //log tag
    private static final String TAG = DiagramStateNode.class.getName();

    //constants
    private static final int STROKE_WIDTH = 5;
    private static final int FINAL_STROKE_WIDTH = 5;
    private static final int FINAL_STROKE_OFFSET = 10;
    private static final int ACTUAL_STROKE_WIDTH = 8;
    private static final int ACTUAL_BOUNDS_OFFSET = 10;

    //represents stateNode
    private State state;

    //with extended class creates node
    private ShapeDrawable bounds;

    //creates initial state indicator (triangle)
    private Path initialBounds;
    private Paint initialPaint;
    private Paint initialPaintBounds;

    //creates final state indicator (inner circle)
    private ShapeDrawable finalBounds;

    //creates actual state indicators (outer circles) - indicates that machine is in this state
    //lists have same size
    private List<MachineStep> machineSteps; //has color to draw
    private List<ShapeDrawable> actualBounds; //has actual objects to draw

    //to access dimensions and radius
    private DiagramView diagramView;

    DiagramStateNode(State state, DiagramView diagramView) {
        Log.v(TAG, "DiagramStateNode constructor called");
        this.state = state;
        this.machineSteps = new ArrayList<>();
        this.actualBounds = new ArrayList<>();
        this.diagramView = diagramView;

        setShape(new OvalShape());
        getPaint().setColor(ContextCompat.getColor(diagramView.getContext(), R.color.state_color));

        bounds = new ShapeDrawable(new OvalShape());
        bounds.getPaint().setStyle(Paint.Style.STROKE);
        bounds.getPaint().setStrokeWidth(STROKE_WIDTH);
        bounds.getPaint().setColor(ContextCompat.getColor(diagramView.getContext(), R.color.md_black_1000));
        bounds.getPaint().setAntiAlias(true);

        initialBounds = new Path();
        initialPaint = new Paint();
        initialPaint.setStyle(Paint.Style.FILL);
        initialPaint.setStrokeCap(Paint.Cap.ROUND);
        initialPaint.setStrokeWidth(STROKE_WIDTH);
        initialPaint.setColor(ContextCompat.getColor(diagramView.getContext(), R.color.state_color));
        initialPaint.setAntiAlias(true);
        initialPaintBounds = new Paint();
        initialPaintBounds.setStyle(Paint.Style.STROKE);
        initialPaintBounds.setStrokeCap(Paint.Cap.ROUND);
        initialPaintBounds.setStrokeWidth(STROKE_WIDTH);
        initialPaintBounds.setColor(ContextCompat.getColor(diagramView.getContext(), R.color.md_black_1000));
        initialPaintBounds.setAntiAlias(true);

        finalBounds = new ShapeDrawable(new OvalShape());
        finalBounds.getPaint().setStyle(Paint.Style.STROKE);
        finalBounds.getPaint().setStrokeWidth(FINAL_STROKE_WIDTH);
        finalBounds.getPaint().setColor(ContextCompat.getColor(diagramView.getContext(), R.color.md_black_1000));
        finalBounds.getPaint().setAntiAlias(true);

        Log.v(TAG, "new DiagramStateNode initialized");
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        bounds.draw(canvas);
        if (state.isInitialState()) {
            canvas.drawPath(initialBounds, initialPaint);
            canvas.drawPath(initialBounds, initialPaintBounds);
        }
        if (state.isFinalState()) {
            finalBounds.draw(canvas);
        }
        for (ShapeDrawable actualBound : actualBounds) {
            actualBound.getPaint().setColor(machineSteps.get(actualBounds.indexOf(actualBound)).getColor().getValue());
            actualBound.draw(canvas);
        }
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        bounds.setBounds(left, top, right, bottom);
        finalBounds.setBounds(left + FINAL_STROKE_OFFSET, top + FINAL_STROKE_OFFSET,
                right - FINAL_STROKE_OFFSET, bottom - FINAL_STROKE_OFFSET);
        for (ShapeDrawable actualBound : actualBounds) {
            int offset = (actualBounds.indexOf(actualBound) + 1) * ACTUAL_BOUNDS_OFFSET;
            actualBound.setBounds(left - offset, top - offset, right + offset, bottom + offset);
        }
    }

    void setPosition(int centerX, int centerY, int cameraX, int cameraY) {
        int radius = diagramView.getNodeRadius();
        int dimensionX = diagramView.getDimensionX();
        int dimensionY = diagramView.getDimensionY();
        if (radius != 0 && dimensionX != 0 && dimensionY != 0) {
            state.setPositionX(centerX);
            state.setPositionY(centerY);
            initialBounds.reset();
            initialBounds.moveTo(cameraX + centerX - radius - ((int) (radius * cos(PI / 4))), cameraY + centerY - ((int) (radius * sin(PI / 4))));
            initialBounds.lineTo(cameraX + centerX - radius, cameraY + centerY);
            initialBounds.lineTo(cameraX + centerX - radius - ((int) (radius * cos(PI / 4))), cameraY + centerY + ((int) (radius * sin(PI / 4))));
            initialBounds.close();
            setBounds(cameraX + centerX - radius, cameraY + centerY - radius, cameraX + centerX + radius, cameraY + centerY + radius);
        }
    }

    State getThisState() {
        return state;
    }

    /**
     * Marks the state as currently occupied by the given MachineStep, creating a colored
     * outline in the MachineStep's color around it.
     * @param machineStep the machine step to be added
     * @param cameraX the x coordinate of the camera
     * @param cameraY the y coordinate of the camera
     */
    public void addMachineColor(MachineStep machineStep, int cameraX, int cameraY) {
        Log.v(TAG, "addMachineColor color to stateNode added");

        //prepare shapeDrawable
        ShapeDrawable newBound = new ShapeDrawable(new OvalShape());
        newBound.getPaint().setStyle(Paint.Style.STROKE);
        newBound.getPaint().setStrokeWidth(ACTUAL_STROKE_WIDTH);
        //color is set when drawn
        newBound.getPaint().setAntiAlias(true);
        int radius = diagramView.getNodeRadius();
        int offset = (actualBounds.size() + 1) * ACTUAL_BOUNDS_OFFSET;
        newBound.setBounds(
                cameraX + state.getPositionX() - radius - offset, cameraY + state.getPositionY() - radius - offset,
                cameraX + state.getPositionX() + radius + offset, cameraY + state.getPositionY() + radius + offset);

        //add shape and color
        actualBounds.add(newBound);
        machineSteps.add(machineStep);
    }

    /**
     * Marks the state as no longer occupied by the given MachineStep, removing its
     * colored outline
     * @param machineStep the machine step to e removed
     */
    public void removeMachineColor(MachineStep machineStep) {
        Log.v(TAG, "removeMachineColor from stateNode called");
        //check if removal is possible (because not always the machine is visible in this diagram)
        if (machineSteps.contains(machineStep)) {
            //always remove the last one (otherwise there will be blank space)
            actualBounds.remove(actualBounds.size() - 1);
            machineSteps.remove(machineStep);
            Log.v(TAG, "removeMachineColor from stateNode removed");
        }
    }
}
