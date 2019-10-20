package fiitstu.gulis.cmsimulator.diagram;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LongSparseArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;

import java.util.ArrayList;
import java.util.List;

import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.elements.FsaTransition;
import fiitstu.gulis.cmsimulator.elements.MachineColor;
import fiitstu.gulis.cmsimulator.elements.PdaTransition;
import fiitstu.gulis.cmsimulator.elements.State;
import fiitstu.gulis.cmsimulator.elements.Symbol;
import fiitstu.gulis.cmsimulator.elements.Transition;
import fiitstu.gulis.cmsimulator.machines.MachineStep;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * A View for displaying the state diagram of an automaton
 *
 * Created by Martin on 28. 2. 2017.
 */
public class DiagramView extends View {

    //log tag
    private static final String TAG = DiagramView.class.getName();

    //actions
    public static final int MOVE = 0;
    public static final int ADD_STATE = 1;
    public static final int ADD_TRANSITION = 2;
    public static final int EDIT = 3;
    public static final int REMOVE = 4;

    private static final float REFERENCE_DIMENSION = 360.0f;

    //variables
    private int dimensionX;
    private int dimensionY;
    private int nodeRadius;
    private long emptyInputSymbolId = -1;
    private boolean markNondeterminism;

    //to get stateNode from state id
    private LongSparseArray<DiagramStateNode> stateNodesMap;
    //to get transitionCurve from transition id
    private LongSparseArray<DiagramTransitionCurve> transitionCurveMap;
    //fromStateId -> transitionCurve
    private LongSparseArray<List<DiagramTransitionCurve>> fromTransitionCurveMap;
    //toStateId -> transitionCurve
    private LongSparseArray<List<DiagramTransitionCurve>> toTransitionCurveMap;

    //stores drawn objects, they are drawn in the order set by these lists
    private List<DiagramStateNode> nodes;
    private List<DiagramTransitionCurve> curves;

    //default paint for text
    private Paint textPaint;

    //for camera and touch events
    private int cameraX;
    private int cameraY;
    private DiagramStateNode topNode = null;
    private DiagramStateNode bottomNode = null;
    private DiagramStateNode leftNode = null;
    private DiagramStateNode rightNode = null;
    private DiagramStateNode touchedNode = null;
    private DiagramTransparentCurve transparentCurve = null;
    private int startMoveX = 0;
    private int startMoveY = 0;

    //the current zoom
    private float scaleFactor;

    //defines current action in diagram
    private int action;

    //positions for new state
    private int newStatePositionX;
    private int newStatePositionY;

    private ScaleGestureDetector scaleDetector;

    private ItemClickCallback itemClickCallback;

    public interface ItemClickCallback {
        void onAddState();

        void onAddTransition(State fromState, State toState);

        void onEditState(State stateEdit);

        void onEditTransition(List<Transition> transitionList);

        void onRemoveState(State stateRemove);

        void onRemoveTransition(List<Transition> transitionList);
    }

    public void setItemClickCallback(final ItemClickCallback itemClickCallback) {
        this.itemClickCallback = itemClickCallback;
    }

    public DiagramView(Context context, AttributeSet attrs) {
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

        nodeRadius = Math.round(Math.min(metrics.widthPixels, metrics.heightPixels) / 15);
        newStatePositionX = metrics.widthPixels / 2;
        newStatePositionY = metrics.heightPixels / 3;

        textPaint = new Paint();
        int nightModeFlags =
                getContext().getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                textPaint.setColor(ContextCompat.getColor(context, R.color.md_white_1000));
                break;

            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                textPaint.setColor(ContextCompat.getColor(context, R.color.md_black_1000));
                break;
        }
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
                        if (leftNode != null && rightNode != null) {
                            cameraX = -(leftNode.getThisState().getPositionX() + rightNode.getThisState().getPositionX()) / 2 + dimensionX / 2;
                        }
                        if (topNode != null && bottomNode != null) {
                            cameraY = -(topNode.getThisState().getPositionY() + bottomNode.getThisState().getPositionY()) / 2 + dimensionY / 2;
                        }
                        for (DiagramStateNode diagramStateNode : nodes) {
                            diagramStateNode.setPosition(
                                    diagramStateNode.getThisState().getPositionX(), diagramStateNode.getThisState().getPositionY(),
                                    cameraX, cameraY);
                        }
                        for (DiagramTransitionCurve diagramTransitionCurve : curves) {
                            diagramTransitionCurve.setPosition(cameraX, cameraY);
                        }
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



    //called manually after constructor, sets up additional things
    public void buildDiagram(Boolean markNondeterminism, long emptyInputSymbolId, List<State> stateList, List<Transition> transitionList) {
        Log.d(TAG, "buildDiagram method started");
        //resetCounter variables here, constructor is not always called
        stateNodesMap = new LongSparseArray<>();
        transitionCurveMap = new LongSparseArray<>();
        fromTransitionCurveMap = new LongSparseArray<>();
        toTransitionCurveMap = new LongSparseArray<>();
        nodes = new ArrayList<>();
        curves = new ArrayList<>();
        action = MOVE;
        cameraX = 0;
        cameraY = 0;
        this.emptyInputSymbolId = emptyInputSymbolId;
        this.markNondeterminism = markNondeterminism;

        if (stateList != null && !stateList.isEmpty()) {
            //create stateNodes
            for (State state : stateList) {
                DiagramStateNode diagramStateNode = new DiagramStateNode(state, this);
                nodes.add(diagramStateNode);
                stateNodesMap.put(state.getId(), diagramStateNode);
            }
            findBounds();
            if (leftNode != null && rightNode != null) {
                cameraX = -(leftNode.getThisState().getPositionX() + rightNode.getThisState().getPositionX()) / 2 + dimensionX / 2;
            }
            if (topNode != null) {
                cameraY = -(topNode.getThisState().getPositionY() + bottomNode.getThisState().getPositionY()) / 2 + dimensionY / 2;
            }
            for (DiagramStateNode stateNode : nodes) {
                stateNode.setPosition(stateNode.getThisState().getPositionX(), stateNode.getThisState().getPositionY(), cameraX, cameraY);
            }

            //create transitionCurves
            LongSparseArray<LongSparseArray<DiagramTransitionCurve>> doubleStateTransitionCurve = new LongSparseArray<>();
            for (Transition transition : transitionList) {
                LongSparseArray<DiagramTransitionCurve> singleTransitionCurve = doubleStateTransitionCurve.get(transition.getFromState().getId());
                if (singleTransitionCurve == null) {
                    singleTransitionCurve = new LongSparseArray<>();
                    doubleStateTransitionCurve.put(transition.getFromState().getId(), singleTransitionCurve);
                }
                DiagramTransitionCurve diagramTransitionCurve = singleTransitionCurve.get(transition.getToState().getId());
                if (diagramTransitionCurve == null) {
                    diagramTransitionCurve = new DiagramTransitionCurve(this);
                    singleTransitionCurve.put(transition.getToState().getId(), diagramTransitionCurve);
                    curves.add(diagramTransitionCurve);

                    List<DiagramTransitionCurve> diagramTransitionCurveList = fromTransitionCurveMap.get(transition.getFromState().getId());
                    if (diagramTransitionCurveList == null) {
                        diagramTransitionCurveList = new ArrayList<>();
                        fromTransitionCurveMap.put(transition.getFromState().getId(), diagramTransitionCurveList);
                    }
                    diagramTransitionCurveList.add(diagramTransitionCurve);
                    diagramTransitionCurveList = toTransitionCurveMap.get(transition.getToState().getId());
                    if (diagramTransitionCurveList == null) {
                        diagramTransitionCurveList = new ArrayList<>();
                        toTransitionCurveMap.put(transition.getToState().getId(), diagramTransitionCurveList);
                    }
                    diagramTransitionCurveList.add(diagramTransitionCurve);
                }
                diagramTransitionCurve.addFromTransition(transition);
                transitionCurveMap.put(transition.getId(), diagramTransitionCurve);
                diagramTransitionCurve.setPosition(cameraX, cameraY);
            }
        }
        findNonDeterminism();
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.save();
        canvas.scale(scaleFactor, scaleFactor);

        //////draw transitionCurve
        for (DiagramTransitionCurve diagramTransitionCurve : curves) {
            ////draw curves (from last to first)
            int counter = diagramTransitionCurve.getActualPaints().size() - 1;
            for (int i = diagramTransitionCurve.getMachineSteps().size() - 1; i >= 0; i--) {
                List<MachineStep> machineStepList = diagramTransitionCurve.getMachineSteps().get(i);
                for (int j = machineStepList.size() - 1; j >= 0; j--) {
                    MachineColor machineColor = machineStepList.get(j).getColor();
                    Paint paint = diagramTransitionCurve.getActualPaints().get(counter);
                    paint.setColor(machineColor.getValue());
                    canvas.drawPath(diagramTransitionCurve, paint);
                    canvas.drawPath(diagramTransitionCurve.getArrowHead(), paint);
                    counter--;
                }
            }
            //there is no machine that uses this diagramTransitionCurve, use default paint
            if (counter == diagramTransitionCurve.getActualPaints().size() - 1) {
                canvas.drawPath(diagramTransitionCurve, diagramTransitionCurve.getDefaultPaint());
                canvas.drawPath(diagramTransitionCurve.getArrowHead(), diagramTransitionCurve.getDefaultPaint());
            }

            ////draw text
            int textPosX = diagramTransitionCurve.getControlX();
            //center vertically
            int textPosY = diagramTransitionCurve.getControlY() - ((int) ((textPaint.descent() + textPaint.ascent()) / 2));
            canvas.save();
            //rotate text + check if upside-down
            int sign;
            if (diagramTransitionCurve.getRotAngle() < PI / 2 && diagramTransitionCurve.getRotAngle() > -PI / 2) {
                sign = 1;
                canvas.rotate((float) (diagramTransitionCurve.getRotAngle() * (180 / PI)), textPosX, textPosY);
            } else {
                sign = -1;
                canvas.rotate((float) ((diagramTransitionCurve.getRotAngle() + PI) * (180 / PI)), textPosX, textPosY);
            }
            for (Transition transition : diagramTransitionCurve.getTransitionList()) {
                int index = diagramTransitionCurve.getTransitionList().indexOf(transition);
                if (diagramTransitionCurve.getNondeterministic().get(index)) {
                    textPaint.setColor(ContextCompat.getColor(getContext(), R.color.nondeterministic_color));
                }
                canvas.drawText(transition.getDiagramDesc(), textPosX,
                        textPosY - (int) (sign * index * (textPaint.descent() - textPaint.ascent())), textPaint);
                for (MachineStep machineStep : diagramTransitionCurve.getMachineSteps().get(index)) {
                    textPaint.setColor(machineStep.getColor().getValue());
                    canvas.drawText(">",
                            textPosX - textPaint.measureText(transition.getDiagramDesc()) / 2 -
                                    (diagramTransitionCurve.getMachineSteps().get(index).indexOf(machineStep) + 1) * textPaint.measureText(">"),
                            textPosY - (int) (sign * index * (textPaint.descent() - textPaint.ascent())), textPaint);
                    canvas.drawText("<",
                            textPosX + textPaint.measureText(transition.getDiagramDesc()) / 2 +
                                    (diagramTransitionCurve.getMachineSteps().get(index).indexOf(machineStep) + 1) * textPaint.measureText("<"),
                            textPosY - (int) (sign * index * (textPaint.descent() - textPaint.ascent())), textPaint);
                }
                textPaint.setColor(ContextCompat.getColor(getContext(), R.color.md_black_1000));
            }
            canvas.restore();
        }
        //////draw stateNode
        for (DiagramStateNode diagramStateNode : nodes) {
            diagramStateNode.draw(canvas);
            int posX = diagramStateNode.getThisState().getPositionX() + cameraX;
            int posY = (int) (diagramStateNode.getThisState().getPositionY() - ((textPaint.descent() + textPaint.ascent()) / 2) + cameraY);
            canvas.drawText(diagramStateNode.getThisState().getValue(), posX, posY, textPaint);
        }
        //////draw transparentCurve
        if (transparentCurve != null) {
            transparentCurve.draw(canvas);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        dimensionX = getMeasuredWidth();
        dimensionY = getMeasuredHeight();
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        int touchX = (int) (event.getX() / scaleFactor);
        int touchY = (int) (event.getY() / scaleFactor);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                switch (action) {
                    case MOVE:
                        //find touched stateNode
                        for (int i = nodes.size() - 1; i >= 0; i--) {
                            DiagramStateNode diagramStateNode = nodes.get(i);
                            if ((diagramStateNode.getThisState().getPositionX() - touchX + cameraX) * (diagramStateNode.getThisState().getPositionX() - touchX + cameraX) +
                                    (diagramStateNode.getThisState().getPositionY() - touchY + cameraY) * (diagramStateNode.getThisState().getPositionY() - touchY + cameraY)
                                    <= nodeRadius * nodeRadius) {
                                touchedNode = diagramStateNode;
                                startMoveX = touchX - diagramStateNode.getThisState().getPositionX();
                                startMoveY = touchY - diagramStateNode.getThisState().getPositionY();
                                break;
                            }
                        }
                        if (touchedNode == null) {
                            startMoveX = touchX - cameraX;
                            startMoveY = touchY - cameraY;
                        }
                        break;
                    case ADD_TRANSITION:
                        //find touched stateNode
                        for (int i = nodes.size() - 1; i >= 0; i--) {
                            DiagramStateNode diagramStateNode = nodes.get(i);
                            if ((diagramStateNode.getThisState().getPositionX() - touchX + cameraX) * (diagramStateNode.getThisState().getPositionX() - touchX + cameraX) +
                                    (diagramStateNode.getThisState().getPositionY() - touchY + cameraY) * (diagramStateNode.getThisState().getPositionY() - touchY + cameraY)
                                    <= nodeRadius * nodeRadius) {
                                touchedNode = diagramStateNode;
                                startMoveX = touchX - diagramStateNode.getThisState().getPositionX();
                                startMoveY = touchY - diagramStateNode.getThisState().getPositionY();
                                transparentCurve = new DiagramTransparentCurve(this,
                                        diagramStateNode.getThisState().getPositionX() + cameraX, diagramStateNode.getThisState().getPositionY() + cameraY);
                                break;
                            }
                        }
                        break;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                switch (action) {
                    case MOVE:
                        if (touchedNode != null) {
                            int newX = touchX - startMoveX;
                            int newY = touchY - startMoveY;
                            //check bounds
                            if (newX + cameraX + nodeRadius > (dimensionX / scaleFactor)) {
                                newX = Math.round(dimensionX / scaleFactor) - cameraX - nodeRadius;
                            }
                            if (newX + cameraX - nodeRadius < 0) {
                               newX = -cameraX + nodeRadius;
                            }
                            if (newY + cameraY + nodeRadius > (dimensionY / scaleFactor)) {
                                newY = Math.round(dimensionY / scaleFactor) - cameraY - nodeRadius;
                            }
                            if (newY + cameraY - nodeRadius < 0) {
                                newY = -cameraY + nodeRadius;
                            }
                            touchedNode.setPosition(newX, newY, cameraX, cameraY);
                            List<DiagramTransitionCurve> diagramTransitionCurveList = fromTransitionCurveMap.get(touchedNode.getThisState().getId());
                            if (diagramTransitionCurveList != null) {
                                for (DiagramTransitionCurve diagramTransitionCurve : diagramTransitionCurveList) {
                                    diagramTransitionCurve.setPosition(cameraX, cameraY);
                                }
                            }
                            diagramTransitionCurveList = toTransitionCurveMap.get(touchedNode.getThisState().getId());
                            if (diagramTransitionCurveList != null) {
                                for (DiagramTransitionCurve diagramTransitionCurve : diagramTransitionCurveList) {
                                    diagramTransitionCurve.setPosition(cameraX, cameraY);
                                }
                            }
                        } else {
                            cameraX = touchX - startMoveX;
                            cameraY = touchY - startMoveY;
                            //check bounds
                            if (leftNode != null && leftNode.getThisState().getPositionX() + cameraX + nodeRadius > (dimensionX / scaleFactor)) {
                                cameraX = -leftNode.getThisState().getPositionX() + Math.round(dimensionX / scaleFactor) - nodeRadius;
                            }
                            if (rightNode != null && rightNode.getThisState().getPositionX() + cameraX - nodeRadius < 0) {
                                cameraX = -rightNode.getThisState().getPositionX() + nodeRadius;
                            }
                            if (topNode != null && topNode.getThisState().getPositionY() + cameraY + nodeRadius > (dimensionY / scaleFactor)) {
                                cameraY = -topNode.getThisState().getPositionY() + Math.round(dimensionY / scaleFactor) - nodeRadius;
                            }
                            if (bottomNode != null && bottomNode.getThisState().getPositionY() + cameraY - nodeRadius < 0) {
                                cameraY = -bottomNode.getThisState().getPositionY() + nodeRadius;
                            }
                            for (DiagramStateNode stateNode : nodes) {
                                stateNode.setPosition(stateNode.getThisState().getPositionX(), stateNode.getThisState().getPositionY(),
                                        cameraX, cameraY);
                            }
                            for (DiagramTransitionCurve transitionCurve : curves) {
                                transitionCurve.setPosition(cameraX, cameraY);
                            }
                        }
                        break;
                    case ADD_TRANSITION:
                        if (transparentCurve != null) {
                            transparentCurve.setEndPosition(touchX, touchY);
                        }
                        break;
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                switch (action) {
                    case MOVE:
                        touchedNode = null;
                        findBounds();
                        break;
                    case ADD_STATE:
                        //set position for new state
                        newStatePositionX = touchX - cameraX;
                        newStatePositionY = touchY - cameraY;
                        itemClickCallback.onAddState();
                        break;
                    case ADD_TRANSITION:
                        //find second touched stateNode
                        DiagramStateNode touchedNode2 = null;
                        for (int i = nodes.size() - 1; i >= 0; i--) {
                            DiagramStateNode diagramStateNode = nodes.get(i);
                            if ((diagramStateNode.getThisState().getPositionX() - touchX + cameraX) * (diagramStateNode.getThisState().getPositionX() - touchX + cameraX) +
                                    (diagramStateNode.getThisState().getPositionY() - touchY + cameraY) * (diagramStateNode.getThisState().getPositionY() - touchY + cameraY)
                                    <= nodeRadius * nodeRadius) {
                                touchedNode2 = diagramStateNode;
                                break;
                            }
                        }
                        if (touchedNode != null && touchedNode2 != null) {
                            itemClickCallback.onAddTransition(touchedNode.getThisState(), touchedNode2.getThisState());
                        }
                        touchedNode = null;
                        transparentCurve = null;
                        invalidate();
                        break;
                    case EDIT:
                        //find touched stateNode
                        for (int i = nodes.size() - 1; i >= 0; i--) {
                            DiagramStateNode diagramStateNode = nodes.get(i);
                            if ((diagramStateNode.getThisState().getPositionX() - touchX + cameraX) * (diagramStateNode.getThisState().getPositionX() - touchX + cameraX) +
                                    (diagramStateNode.getThisState().getPositionY() - touchY + cameraY) * (diagramStateNode.getThisState().getPositionY() - touchY + cameraY)
                                    <= nodeRadius * nodeRadius) {
                                itemClickCallback.onEditState(diagramStateNode.getThisState());
                                return true; //need to end here to not find transition
                            }
                        }
                        //find touched transitionCurve
                        for (int i = curves.size() - 1; i >= 0; i--) {
                            DiagramTransitionCurve diagramTransitionCurve = curves.get(i);
                            //calculate origin point
                            double originPointX = diagramTransitionCurve.getControlX();
                            double originPointY = diagramTransitionCurve.getControlY() - (textPaint.descent() + textPaint.ascent()) / 2;
                            //check if in bounds
                            if (diagramTransitionCurve.getRotAngle() < PI / 2 && diagramTransitionCurve.getRotAngle() > -PI / 2) {
                                //rotate touch point
                                double newPointX = originPointX + (touchX - originPointX) * cos(-diagramTransitionCurve.getRotAngle()) - (touchY - originPointY) * sin(-diagramTransitionCurve.getRotAngle());
                                double newPointY = originPointY + (touchX - originPointX) * sin(-diagramTransitionCurve.getRotAngle()) + (touchY - originPointY) * cos(-diagramTransitionCurve.getRotAngle());
                                if (newPointX > originPointX - textPaint.measureText(diagramTransitionCurve.getFirstFromTransition().getDiagramDesc()) / 2 - nodeRadius / 2
                                        && newPointX < originPointX + textPaint.measureText(diagramTransitionCurve.getFirstFromTransition().getDiagramDesc()) / 2 + nodeRadius / 2
                                        && newPointY > originPointY + (textPaint.descent() - textPaint.ascent()) / 2 - diagramTransitionCurve.getTransitionList().size() * (textPaint.descent() - textPaint.ascent()) - nodeRadius / 2
                                        && newPointY < originPointY + (textPaint.descent() - textPaint.ascent()) / 2 + nodeRadius / 2) {
                                    itemClickCallback.onEditTransition(diagramTransitionCurve.getTransitionList());
                                }
                            } else {
                                //rotate touch point
                                double newPointX = originPointX + (touchX - originPointX) * cos(-(diagramTransitionCurve.getRotAngle() + PI)) - (touchY - originPointY) * sin(-(diagramTransitionCurve.getRotAngle() + PI));
                                double newPointY = originPointY + (touchX - originPointX) * sin(-(diagramTransitionCurve.getRotAngle() + PI)) + (touchY - originPointY) * cos(-(diagramTransitionCurve.getRotAngle() + PI));
                                if (newPointX > originPointX - textPaint.measureText(diagramTransitionCurve.getFirstFromTransition().getDiagramDesc()) / 2 - nodeRadius / 2
                                        && newPointX < originPointX + textPaint.measureText(diagramTransitionCurve.getFirstFromTransition().getDiagramDesc()) / 2 + nodeRadius / 2
                                        && newPointY > originPointY - (textPaint.descent() - textPaint.ascent()) / 2 - nodeRadius / 2
                                        && newPointY < originPointY - (textPaint.descent() - textPaint.ascent()) / 2 + diagramTransitionCurve.getTransitionList().size() * (textPaint.descent() - textPaint.ascent()) + nodeRadius / 2) {
                                    itemClickCallback.onEditTransition(diagramTransitionCurve.getTransitionList());
                                }
                            }
                        }
                        break;
                    case REMOVE:
                        //find touched stateNode
                        for (int i = nodes.size() - 1; i >= 0; i--) {
                            DiagramStateNode diagramStateNode = nodes.get(i);
                            if ((diagramStateNode.getThisState().getPositionX() - touchX + cameraX) * (diagramStateNode.getThisState().getPositionX() - touchX + cameraX) +
                                    (diagramStateNode.getThisState().getPositionY() - touchY + cameraY) * (diagramStateNode.getThisState().getPositionY() - touchY + cameraY)
                                    <= nodeRadius * nodeRadius) {
                                itemClickCallback.onRemoveState(diagramStateNode.getThisState());
                                return true; //need to end here to not find transition
                            }
                        }
                        //find touched transitionCurve
                        for (int i = curves.size() - 1; i >= 0; i--) {
                            DiagramTransitionCurve diagramTransitionCurve = curves.get(i);
                            //calculate origin point
                            double originPointX = diagramTransitionCurve.getControlX();
                            double originPointY = diagramTransitionCurve.getControlY() - (textPaint.descent() + textPaint.ascent()) / 2;
                            //check if in bounds
                            if (diagramTransitionCurve.getRotAngle() < PI / 2 && diagramTransitionCurve.getRotAngle() > -PI / 2) {
                                //rotate touch point
                                double newPointX = originPointX + (touchX - originPointX) * cos(-diagramTransitionCurve.getRotAngle()) - (touchY - originPointY) * sin(-diagramTransitionCurve.getRotAngle());
                                double newPointY = originPointY + (touchX - originPointX) * sin(-diagramTransitionCurve.getRotAngle()) + (touchY - originPointY) * cos(-diagramTransitionCurve.getRotAngle());
                                if (newPointX > originPointX - textPaint.measureText(diagramTransitionCurve.getFirstFromTransition().getDiagramDesc()) / 2 - nodeRadius / 2
                                        && newPointX < originPointX + textPaint.measureText(diagramTransitionCurve.getFirstFromTransition().getDiagramDesc()) / 2 + nodeRadius / 2
                                        && newPointY > originPointY + (textPaint.descent() - textPaint.ascent()) / 2 - diagramTransitionCurve.getTransitionList().size() * (textPaint.descent() - textPaint.ascent()) - nodeRadius / 2
                                        && newPointY < originPointY + (textPaint.descent() - textPaint.ascent()) / 2 + nodeRadius / 2) {
                                    itemClickCallback.onRemoveTransition(diagramTransitionCurve.getTransitionList());
                                }
                            } else {
                                //rotate touch point
                                double newPointX = originPointX + (touchX - originPointX) * cos(-(diagramTransitionCurve.getRotAngle() + PI)) - (touchY - originPointY) * sin(-(diagramTransitionCurve.getRotAngle() + PI));
                                double newPointY = originPointY + (touchX - originPointX) * sin(-(diagramTransitionCurve.getRotAngle() + PI)) + (touchY - originPointY) * cos(-(diagramTransitionCurve.getRotAngle() + PI));
                                if (newPointX > originPointX - textPaint.measureText(diagramTransitionCurve.getFirstFromTransition().getDiagramDesc()) / 2 - nodeRadius / 2
                                        && newPointX < originPointX + textPaint.measureText(diagramTransitionCurve.getFirstFromTransition().getDiagramDesc()) / 2 + nodeRadius / 2
                                        && newPointY > originPointY - (textPaint.descent() - textPaint.ascent()) / 2 - nodeRadius / 2
                                        && newPointY < originPointY - (textPaint.descent() - textPaint.ascent()) / 2 + diagramTransitionCurve.getTransitionList().size() * (textPaint.descent() - textPaint.ascent()) + nodeRadius / 2) {
                                    itemClickCallback.onRemoveTransition(diagramTransitionCurve.getTransitionList());
                                }
                            }
                        }
                        break;
                }
                break;
        }
        scaleDetector.onTouchEvent(event);
        return true;
    }

    public void addState(State state) {
        Log.v(TAG, "onAddState in diagram added");
        DiagramStateNode diagramStateNode = new DiagramStateNode(state, this);
        nodes.add(diagramStateNode);
        stateNodesMap.put(state.getId(), diagramStateNode);
        diagramStateNode.setPosition(state.getPositionX(), state.getPositionY(), cameraX, cameraY);
        findBounds();
        invalidate();
    }

    public void removeState(State state) {
        Log.v(TAG, "removeState in diagram removed");
        DiagramStateNode diagramStateNode = stateNodesMap.get(state.getId());
        nodes.remove(diagramStateNode);
        stateNodesMap.remove(state.getId());
        findBounds();
        invalidate();
    }

    /**
     * Erases the entire content of the diagram
     */
    public void clear() {
        Log.v(TAG, "clear method started");
        clearNonDeterminism();

        curves.clear();
        fromTransitionCurveMap.clear();
        transitionCurveMap.clear();
        toTransitionCurveMap.clear();

        stateNodesMap.clear();
        nodes.clear();
        findNonDeterminism();
        findBounds();
        invalidate();
        Log.v(TAG, "clear method finished");
    }

    public void addTransition(Transition transition) {
        Log.v(TAG, "addTransition in diagram added");
        clearNonDeterminism();

        //try to find and add to existing transitionNode
        List<DiagramTransitionCurve> diagramTransitionCurveList = fromTransitionCurveMap.get(transition.getFromState().getId());
        if (diagramTransitionCurveList != null) {
            for (DiagramTransitionCurve diagramTransitionCurve : diagramTransitionCurveList) {
                Transition fromTransition = diagramTransitionCurve.getFirstFromTransition();
                if (fromTransition.getToState().getId() == transition.getToState().getId()) {
                    diagramTransitionCurve.addFromTransition(transition);
                    transitionCurveMap.put(transition.getId(), diagramTransitionCurve);
                    findNonDeterminism();
                    invalidate();
                    return;
                }
            }
        }
        //transitionNode not found, create new
        DiagramTransitionCurve diagramTransitionCurve = new DiagramTransitionCurve(this);
        diagramTransitionCurve.addFromTransition(transition);
        transitionCurveMap.put(transition.getId(), diagramTransitionCurve);
        diagramTransitionCurve.setPosition(cameraX, cameraY);
        curves.add(diagramTransitionCurve);

        diagramTransitionCurveList = fromTransitionCurveMap.get(transition.getFromState().getId());
        if (diagramTransitionCurveList == null) {
            diagramTransitionCurveList = new ArrayList<>();
            fromTransitionCurveMap.put(transition.getFromState().getId(), diagramTransitionCurveList);
        }
        diagramTransitionCurveList.add(diagramTransitionCurve);

        diagramTransitionCurveList = toTransitionCurveMap.get(transition.getToState().getId());
        if (diagramTransitionCurveList == null) {
            diagramTransitionCurveList = new ArrayList<>();
            toTransitionCurveMap.put(transition.getToState().getId(), diagramTransitionCurveList);
        }
        diagramTransitionCurveList.add(diagramTransitionCurve);
        findNonDeterminism();
        invalidate();
    }

    public void changeTransition(Transition transition, long oldFromStateId, long oldToStateId) {
        Log.v(TAG, "changeTransition in diagram changed");
        clearNonDeterminism();

        //remove the old position
        List<DiagramTransitionCurve> diagramTransitionCurveList = fromTransitionCurveMap.get(oldFromStateId);
        for (DiagramTransitionCurve diagramTransitionCurve : diagramTransitionCurveList) {
            if (diagramTransitionCurve.getTransitionList().contains(transition)) {
                if (diagramTransitionCurve.getTransitionList().size() == 1) { //the other map will remove it from this list
                    diagramTransitionCurveList.remove(diagramTransitionCurve);
                    curves.remove(diagramTransitionCurve);
                    if (diagramTransitionCurveList.isEmpty()) {
                        fromTransitionCurveMap.remove(oldFromStateId);
                    }
                }
                break;
            }
        }

        diagramTransitionCurveList = toTransitionCurveMap.get(oldToStateId);
        for (DiagramTransitionCurve diagramTransitionCurve : diagramTransitionCurveList) {
            if (diagramTransitionCurve.getTransitionList().contains(transition)) {
                diagramTransitionCurve.removeFromTransition(transition);
                transitionCurveMap.remove(transition.getId());
                if (diagramTransitionCurve.getTransitionList().isEmpty()) {
                    diagramTransitionCurveList.remove(diagramTransitionCurve);
                    if (diagramTransitionCurveList.isEmpty()) {
                        toTransitionCurveMap.remove(oldToStateId);
                    }
                }
                break;
            }
        }

        //add new transition
        //try to find and add to existing transitionNode
        diagramTransitionCurveList = fromTransitionCurveMap.get(transition.getFromState().getId());
        if (diagramTransitionCurveList != null) {
            for (DiagramTransitionCurve diagramTransitionCurve : diagramTransitionCurveList) {
                Transition fromTransition = diagramTransitionCurve.getFirstFromTransition();
                if (fromTransition.getToState() == transition.getToState()) {
                    diagramTransitionCurve.addFromTransition(transition);
                    transitionCurveMap.put(transition.getId(), diagramTransitionCurve);
                    findNonDeterminism();
                    invalidate();
                    return;
                }
            }
        }
        //transitionNode not found, create new
        DiagramTransitionCurve diagramTransitionCurve = new DiagramTransitionCurve(this);
        diagramTransitionCurve.addFromTransition(transition);
        transitionCurveMap.put(transition.getId(), diagramTransitionCurve);
        diagramTransitionCurve.setPosition(cameraX, cameraY);
        curves.add(diagramTransitionCurve);

        diagramTransitionCurveList = fromTransitionCurveMap.get(transition.getFromState().getId());
        if (diagramTransitionCurveList == null) {
            diagramTransitionCurveList = new ArrayList<>();
            fromTransitionCurveMap.put(transition.getFromState().getId(), diagramTransitionCurveList);
        }
        diagramTransitionCurveList.add(diagramTransitionCurve);

        diagramTransitionCurveList = toTransitionCurveMap.get(transition.getToState().getId());
        if (diagramTransitionCurveList == null) {
            diagramTransitionCurveList = new ArrayList<>();
            toTransitionCurveMap.put(transition.getToState().getId(), diagramTransitionCurveList);
        }
        diagramTransitionCurveList.add(diagramTransitionCurve);
        findNonDeterminism();
        invalidate();
    }

    public void removeTransition(Transition transition) {
        Log.v(TAG, "removeTransition in diagram removed");
        clearNonDeterminism();

        List<DiagramTransitionCurve> diagramTransitionCurveList = fromTransitionCurveMap.get(transition.getFromState().getId());
        for (DiagramTransitionCurve diagramTransitionCurve : diagramTransitionCurveList) {
            if (diagramTransitionCurve.getTransitionList().contains(transition)) {
                if (diagramTransitionCurve.getTransitionList().size() == 1) { //the other map will remove it from this list
                    diagramTransitionCurveList.remove(diagramTransitionCurve);
                    curves.remove(diagramTransitionCurve);
                    if (diagramTransitionCurveList.isEmpty()) {
                        fromTransitionCurveMap.remove(transition.getFromState().getId());
                    }
                }
                break;
            }
        }
        diagramTransitionCurveList = toTransitionCurveMap.get(transition.getToState().getId());
        for (DiagramTransitionCurve diagramTransitionCurve : diagramTransitionCurveList) {
            if (diagramTransitionCurve.getTransitionList().contains(transition)) {
                diagramTransitionCurve.removeFromTransition(transition);
                transitionCurveMap.remove(transition.getId());
                if (diagramTransitionCurve.getTransitionList().isEmpty()) {
                    diagramTransitionCurveList.remove(diagramTransitionCurve);
                    if (diagramTransitionCurveList.isEmpty()) {
                        toTransitionCurveMap.remove(transition.getToState().getId());
                    }
                }
                break;
            }
        }
        findNonDeterminism();
        invalidate();
    }

    public void addMachineColor(MachineStep machineStep) {
        stateNodesMap.get(machineStep.getCurrentState().getId()).addMachineColor(machineStep, cameraX, cameraY);
    }

    private void findBounds() {
        if (!nodes.isEmpty()) {
            topNode = nodes.get(0);
            bottomNode = nodes.get(0);
            leftNode = nodes.get(0);
            rightNode = nodes.get(0);
            for (DiagramStateNode stateNode : nodes) {
                if (stateNode.getThisState().getPositionX() < leftNode.getThisState().getPositionX()) {
                    leftNode = stateNode;
                }
                if (stateNode.getThisState().getPositionX() > rightNode.getThisState().getPositionX()) {
                    rightNode = stateNode;
                }
                if (stateNode.getThisState().getPositionY() < topNode.getThisState().getPositionY()) {
                    topNode = stateNode;
                }
                if (stateNode.getThisState().getPositionY() > bottomNode.getThisState().getPositionY()) {
                    bottomNode = stateNode;
                }
            }
        }
    }

    private void findNonDeterminism() {
        Log.v(TAG, "findNonDeterminism method started");
        if (markNondeterminism) {
            LongSparseArray<List<Transition>> transitionMap = new LongSparseArray<>();

            for (int i = 0; i < fromTransitionCurveMap.size(); i++) {
                transitionMap.clear();
                for (DiagramTransitionCurve transitionCurve : fromTransitionCurveMap.valueAt(i)) {
                    for (Transition transition : transitionCurve.getTransitionList()) {
                        List<Transition> actualTransitions = transitionMap.get(transition.getReadSymbol().getId());
                        if (actualTransitions != null) {
                            //transition for this read symbol already exists
                            actualTransitions.add(transition);
                            transitionMap.put(transition.getReadSymbol().getId(), actualTransitions);
                        } else {
                            actualTransitions = new ArrayList<>();
                            actualTransitions.add(transition);
                            transitionMap.put(transition.getReadSymbol().getId(), actualTransitions);
                        }
                        if (transition instanceof PdaTransition) {
                            //find match for not empty symbol transitions
                            if (transition.getReadSymbol().getId() != emptyInputSymbolId) {
                                //find match in not empty symbol transition (the same read symbol only)
                                for (Transition actualTransition : actualTransitions) {
                                    if (transition != actualTransition) {
                                        if (checkNondeterminismStack(
                                                ((PdaTransition) transition).getPopSymbolList(), ((PdaTransition) actualTransition).getPopSymbolList())) {
                                            transitionCurve.getNondeterministic().set(transitionCurve.getTransitionList().indexOf(transition), true);
                                            List<DiagramTransitionCurve> diagramTransitionCurveList = fromTransitionCurveMap.get(actualTransition.getFromState().getId());
                                            for (DiagramTransitionCurve diagramTransitionCurve : diagramTransitionCurveList) {
                                                if (diagramTransitionCurve.getTransitionList().contains(actualTransition)) {
                                                    diagramTransitionCurve.getNondeterministic().set(
                                                            diagramTransitionCurve.getTransitionList().indexOf(actualTransition), true);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                                //find match in empty symbol transitions
                                List<Transition> emptyTransitions = transitionMap.get(emptyInputSymbolId);
                                if (emptyTransitions != null) {
                                    for (Transition emptyTransition : emptyTransitions) {
                                        if (transition != emptyTransition) {
                                            if (checkNondeterminismStack(
                                                    ((PdaTransition) transition).getPopSymbolList(), ((PdaTransition) emptyTransition).getPopSymbolList())) {
                                                transitionCurve.getNondeterministic().set(transitionCurve.getTransitionList().indexOf(transition), true);
                                                List<DiagramTransitionCurve> diagramTransitionCurveList = fromTransitionCurveMap.get(emptyTransition.getFromState().getId());
                                                for (DiagramTransitionCurve diagramTransitionCurve : diagramTransitionCurveList) {
                                                    if (diagramTransitionCurve.getTransitionList().contains(emptyTransition)) {
                                                        diagramTransitionCurve.getNondeterministic().set(
                                                                diagramTransitionCurve.getTransitionList().indexOf(emptyTransition), true);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                //find match for empty symbol transitions (from all read symbols)
                            } else {
                                for (int j = 0; j < transitionMap.size(); j++) {
                                    List<Transition> forEmptyTransitions = transitionMap.valueAt(j);
                                    for (Transition forEmptyTransition : forEmptyTransitions) {
                                        if (transition != forEmptyTransition) {
                                            if (checkNondeterminismStack(
                                                    ((PdaTransition) transition).getPopSymbolList(), ((PdaTransition) forEmptyTransition).getPopSymbolList())) {
                                                transitionCurve.getNondeterministic().set(transitionCurve.getTransitionList().indexOf(transition), true);
                                                List<DiagramTransitionCurve> diagramTransitionCurveList = fromTransitionCurveMap.get(forEmptyTransition.getFromState().getId());
                                                for (DiagramTransitionCurve diagramTransitionCurve : diagramTransitionCurveList) {
                                                    if (diagramTransitionCurve.getTransitionList().contains(forEmptyTransition)) {
                                                        diagramTransitionCurve.getNondeterministic().set(
                                                                diagramTransitionCurve.getTransitionList().indexOf(forEmptyTransition), true);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            //empty symbol is nondeterministic only for fsa transition
                            if (transition instanceof FsaTransition && transition.getReadSymbol().getId() == emptyInputSymbolId) {
                                transitionCurve.getNondeterministic().set(transitionCurve.getTransitionList().indexOf(transition), true);
                            } else {
                                if (actualTransitions.size() > 1) {
                                    transitionCurve.getNondeterministic().set(transitionCurve.getTransitionList().indexOf(transition), true);
                                    if (actualTransitions.size() == 2) {
                                        List<DiagramTransitionCurve> diagramTransitionCurveList = fromTransitionCurveMap.get(actualTransitions.get(0).getFromState().getId());
                                        for (DiagramTransitionCurve diagramTransitionCurve : diagramTransitionCurveList) {
                                            if (diagramTransitionCurve.getTransitionList().contains(actualTransitions.get(0))) {
                                                diagramTransitionCurve.getNondeterministic().set(
                                                        diagramTransitionCurve.getTransitionList().indexOf(actualTransitions.get(0)), true);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void clearNonDeterminism() {
        if (markNondeterminism) {
            for (DiagramTransitionCurve transitionCurve : curves) {
                for (int i = 0; i < transitionCurve.getNondeterministic().size(); i++) {
                    transitionCurve.getNondeterministic().set(i, false);
                }
            }
        }
    }

    //method to check nondeterminism of stacks
    private boolean checkNondeterminismStack(List<Symbol> StackSymbolList1, List<Symbol> StackSymbolList2) {
        int size = StackSymbolList1.size() < StackSymbolList2.size() ? StackSymbolList1.size() : StackSymbolList2.size();
        for (int i = 0; i < size; i++) {
            if (StackSymbolList1.get(i).getId() != StackSymbolList2.get(i).getId()) {
                return false;
            }
        }
        return true;
    }

    public int getDimensionX() {
        return dimensionX;
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

    public LongSparseArray<DiagramStateNode> getStateNodesMap() {
        return stateNodesMap;
    }

    public LongSparseArray<DiagramTransitionCurve> getTransitionCurveMap() {
        return transitionCurveMap;
    }

    public void setAction(int action) {
        this.action = action;
        //so that swapping the tool while drawing a transition won't leave the curve behind
        if (action != ADD_TRANSITION) {
            transparentCurve = null;
        }
    }

    public int getNewStatePositionX() {
        return newStatePositionX;
    }

    public int getNewStatePositionY() {
        return newStatePositionY;
    }

    public void setDefaultNewStatePosition(Context context) {
        newStatePositionX = context.getApplicationContext().getResources().getDisplayMetrics().widthPixels / 2 - cameraX;
        newStatePositionY = context.getApplicationContext().getResources().getDisplayMetrics().heightPixels / 3 - cameraY;
    }
}
