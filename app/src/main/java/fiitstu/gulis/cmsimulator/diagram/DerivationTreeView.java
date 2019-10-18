package fiitstu.gulis.cmsimulator.diagram;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.List;

import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.elements.DerivationTreeStateNode;

public class DerivationTreeView extends View {

    private static final int REFERENCE_DIMENSION = 360;
    private static final int DEFAULT_STROKE_WIDTH = 3;
    private static int rightStartDifference = 0;
    private static int leftStartDifference = 0;
    private static int initial_render = 0;

    private int dimensionX;
    private int dimensionY;
    private int offsetX = 0;
    private int offsetY = 0;
    private int nodeRadius;
    private int startX = 0;
    private int startY = 0;
    private int rootX;
    private float scaleFactor = 1.0f;

    private ScaleGestureDetector scaleDetector;

    private Paint textPaint;
    private Paint paint = new Paint();

    private int cameraX;
    private int cameraY;

    private int leftMostNodeX = 9999;
    private int rightMostNodeX = -9999 ;

    private List<DerivationTreeStateNode> derivationTreeStateNodeList;
    private List<DerivationTreeNode> derivationTreeNodeList;
    private List<DerivationTreeNode> calculatedNodesList;
    private DerivationTreeStateNode lastNode;

    public DerivationTreeView(Context context, AttributeSet attrs) {
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
                        cameraX = dimensionX / 2;
                        cameraY = dimensionY / 6;
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
        rightStartDifference = 0;
        leftStartDifference = 0;
        initial_render = 0;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.save();
        canvas.scale(scaleFactor, scaleFactor);
        calculatedNodesList.clear();

        int posX = cameraX;
        int posY = cameraY;
        int currentLevel = 0;
        boolean leftTree = true;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(DEFAULT_STROKE_WIDTH);
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);

        for(int i = 0; i < derivationTreeNodeList.size(); i++){
            DerivationTreeNode derivationTreeNode = derivationTreeNodeList.get(i);
            DerivationTreeStateNode derivationTreeStateNode = derivationTreeStateNodeList.get(i);
            if(derivationTreeStateNode.getLevel() == 0) {
                derivationTreeStateNode.setPositionX(posX);
                derivationTreeStateNode.setPositionY(posY);
            }

            if(derivationTreeStateNode.getLevel() != currentLevel){
                currentLevel++;
                posY += 150;
                if(derivationTreeStateNode.getLevelLength() % 2 == 1) {
                    posX = derivationTreeStateNode.getPredecessor().getPositionX() - (derivationTreeStateNode.getLevelLength() / 2) * 160;
                }else{
                    posX = derivationTreeStateNode.getPredecessor().getPositionX() - (derivationTreeStateNode.getLevelLength() / 2) * 160 + 80;

                }
            }
            if(initial_render == 0) {
                int diff;
                if (posY > dimensionY) {
                    diff = posY - dimensionY + nodeRadius;
                    posY -= diff;
                    for(int j = 0; j < calculatedNodesList.size(); j++){
                        DerivationTreeNode node = calculatedNodesList .get(j);
                        DerivationTreeStateNode stateNode = derivationTreeStateNodeList.get(j);

                        node.setPosition(node.getPositionX(), node.getPositionY() - diff, nodeRadius);
                        stateNode.setPositionY(stateNode.getPositionY()- diff);
                    }
                    cameraY -= diff;
                }
                if (posX > dimensionX) {
                    diff = posX - dimensionX + nodeRadius;
                    posX -= diff;
                    for(int j = 0; j < calculatedNodesList.size(); j++){
                        DerivationTreeNode node = calculatedNodesList.get(j);
                        DerivationTreeStateNode stateNode = derivationTreeStateNodeList.get(j);

                        node.setPosition(node.getPositionX() - diff, node.getPositionY(), nodeRadius);
                        stateNode.setPositionX(stateNode.getPositionX() - diff);
                    }
                    cameraX -= diff;
                    leftTree = false;
                }
                else if (posX < 0) {
                    diff = 0 - posX + nodeRadius;
                    posX += diff;
                    for(int j = 0; j < calculatedNodesList.size(); j++){
                        DerivationTreeNode node = calculatedNodesList.get(j);
                        DerivationTreeStateNode stateNode = derivationTreeStateNodeList.get(j);

                        node.setPosition(node.getPositionX() + diff, node.getPositionY(), nodeRadius);
                        stateNode.setPositionX(stateNode.getPositionX() + diff);
                    }
                    cameraX += diff;
                    leftTree = true;

                }
            }

            derivationTreeStateNode.setPositionX(posX);
            derivationTreeStateNode.setPositionY(posY);
            derivationTreeNode.setPosition(posX, posY, nodeRadius);

            calculatedNodesList.add(derivationTreeNode);

            posX += 160;
            lastNode = derivationTreeStateNode;
        }

        if(calculatedNodesList.size() > 0) {
            rootX = calculatedNodesList.get(0).getPositionX();

            for (int i = 0; i < calculatedNodesList.size(); i++) {
                DerivationTreeNode derivationTreeNode = calculatedNodesList.get(i);
                DerivationTreeStateNode derivationTreeStateNode = derivationTreeStateNodeList.get(i);

                derivationTreeNode.draw(canvas);
                derivationTreeNode.setPositionY(derivationTreeNode.getPositionY() - (int) (textPaint.descent() + textPaint.ascent()) / 2);
                canvas.drawText(String.valueOf(derivationTreeNode.getNodeText()), derivationTreeNode.getPositionX(), derivationTreeNode.getPositionY(), textPaint);

                if (derivationTreeStateNode.getLevel() > 0) {
                    canvas.drawLine(derivationTreeStateNode.getPredecessor().getPositionX(), derivationTreeStateNode.getPredecessor().getPositionY() + nodeRadius, derivationTreeStateNode.getPositionX(), derivationTreeStateNode.getPositionY() - nodeRadius, paint);
                }

                if (derivationTreeNode.getPositionX() < leftMostNodeX) {
                    leftMostNodeX = derivationTreeNode.getPositionX();
                } else if (derivationTreeNode.getPositionX() > rightMostNodeX) {
                    rightMostNodeX = derivationTreeNode.getPositionX();
                }
            }

            if (leftTree) {
                final int difference = rightMostNodeX - rootX;
                if (rightStartDifference == 0 && currentLevel > 0) {
                    rightStartDifference = difference;
                }
                final int difference2 = cameraX - leftMostNodeX;
                if (leftStartDifference == 0 && currentLevel > 0) {
                    leftStartDifference = difference2;
                }
            } else {
                final int difference = rightMostNodeX - cameraX;
                if (rightStartDifference == 0 && currentLevel > 0) {
                    rightStartDifference = difference;
                }
                final int difference2 = rootX - leftMostNodeX;
                if (leftStartDifference == 0 && currentLevel > 0) {
                    leftStartDifference = difference2;
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        initial_render = 1;
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
                //check bounds
                if (touchX - startX - nodeRadius  + rightStartDifference < 0) {
                    cameraX = nodeRadius - rightStartDifference;
                }
                if (touchY - startY - nodeRadius + (lastNode.getLevel()) * 150 < 0) {
                    cameraY = nodeRadius - (lastNode.getLevel()) * 150;
                }
                if (touchX - startX + nodeRadius - leftStartDifference > (dimensionX / scaleFactor)) {
                    cameraX = Math.round(dimensionX / scaleFactor) - nodeRadius + leftStartDifference;
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

    public void drawTree(List<DerivationTreeStateNode> derivationTreeStateNodeList){
        this.derivationTreeStateNodeList = derivationTreeStateNodeList;
        derivationTreeNodeList = new ArrayList<>();
        calculatedNodesList = new ArrayList<>();
        for(DerivationTreeStateNode node : derivationTreeStateNodeList) {
            DerivationTreeNode derivationTreeNode = new DerivationTreeNode(node);
            derivationTreeNodeList.add(derivationTreeNode);
        }

    }
}
