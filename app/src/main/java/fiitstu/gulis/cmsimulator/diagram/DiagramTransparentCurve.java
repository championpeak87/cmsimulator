package fiitstu.gulis.cmsimulator.diagram;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import fiitstu.gulis.cmsimulator.R;

import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * A class that represents a faint transition curve shown while the user is
 * drawing a new transition.
 *
 * Created by Martin on 18. 3. 2017.
 */
class DiagramTransparentCurve {

    //log tag
    private static final String TAG = DiagramTransparentCurve.class.getName();

    //constants
    private static final int DEFAULT_STROKE_WIDTH = 3;

    //the line segment of the curve
    private Path line;
    //the arrowhead segment
    private Path arrowHead;

    //paint
    private Paint paint;

    //start points
    private final int startX;
    private final int startY;

    //to access radius
    private DiagramView diagramView;

    /**
     * Creates a new transparent curve
     * @param diagramView the DiagramView where the curve will be displayed
     * @param startX the x coordinate of the curve's starting point
     * @param startY the y coordinate of the curve's starting point
     */
    DiagramTransparentCurve(DiagramView diagramView, int startX, int startY) {
        Log.v(TAG, "DiagramTransparentCurve constructor called");
        this.arrowHead = new Path();
        this.line = new Path();
        this.startX = startX;
        this.startY = startY;
        this.diagramView = diagramView;

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(DEFAULT_STROKE_WIDTH);
        paint.setColor(ContextCompat.getColor(diagramView.getContext(), R.color.transparent_transition_color));
        paint.setAntiAlias(true);

        Log.v(TAG, "DiagramTransparentCurve initialized");
    }

    /**
     * Sets the position of the curve's end-point (the pointy end)
     * @param endX the x coordinate of the curve's end-point
     * @param endY the y coordinate of the curve's end-point
     */
    void setEndPosition(int endX, int endY) {
        int radius = diagramView.getNodeRadius();
        ////calculate line
        line.reset();
        line.moveTo(startX, startY);
        line.lineTo(endX, endY);
        ////calculate arrowHead
        double endAngle = atan2(startY - endY, startX - endX);
        arrowHead.reset();
        arrowHead.moveTo(endX + ((int) (0.5f * radius * cos(endAngle + PI / 6))), endY + ((int) (0.5f * radius * sin(endAngle + PI / 6))));
        arrowHead.lineTo(endX, endY);
        arrowHead.lineTo(endX + ((int) (0.5f * radius * cos(endAngle - PI / 6))), endY + ((int) (0.5f * radius * sin(endAngle - PI / 6))));
    }

    /**
     * Draws the transparent curve on a canvas
     * @param canvas the canvas where the curve will be drawn
     */
    void draw(Canvas canvas) {
        canvas.drawPath(line, paint);
        canvas.drawPath(arrowHead, paint);
    }
}
