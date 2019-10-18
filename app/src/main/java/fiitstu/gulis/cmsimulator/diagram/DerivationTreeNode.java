package fiitstu.gulis.cmsimulator.diagram;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.Log;

import fiitstu.gulis.cmsimulator.elements.DerivationTreeStateNode;

public class DerivationTreeNode extends ShapeDrawable {
    //log tag
    private static final String TAG = DerivationTreeNode.class.getName();

    //constants
    private static final int STROKE_WIDTH = 5;

    private char nodeText;

    //position (used for transitionCurves)
    private int positionX;
    private int positionY;

    //with extended class creates node
    private ShapeDrawable bounds;

    public DerivationTreeNode(DerivationTreeStateNode derivationTreeStateNode) {
        Log.v(TAG, "DerivationTreeNode constructor called");
        this.nodeText = derivationTreeStateNode.getSymbol();
        setShape(new OvalShape());

        bounds = new ShapeDrawable(new OvalShape());
        bounds.getPaint().setStyle(Paint.Style.STROKE);
        bounds.getPaint().setStrokeWidth(STROKE_WIDTH);
        bounds.getPaint().setColor(derivationTreeStateNode.getColor());
        bounds.getPaint().setAntiAlias(true);

        //set first color
        getPaint().setColor(derivationTreeStateNode.getColor());
        Log.v(TAG, "new DerivationTreeNode initialized");
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

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public char getNodeText() {
        return nodeText;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    public void setNodeText(char nodeText) {
        this.nodeText = nodeText;
    }
}
