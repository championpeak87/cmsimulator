package fiitstu.gulis.cmsimulator.views;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import fiitstu.gulis.cmsimulator.R;

import java.util.ArrayList;
import java.util.List;

public class ChessView extends View {
    private static final String TAG = "ChessView";

    // DEFAULT VALUES
    private static final int DEFAULT_ACTIVE_FIELD_COLOR = R.color.toggle_color;
    private static final int DEFAULT_CHESS_FIELD_HEIGHT = 8;
    private static final int DEFAULT_CHESS_FIELD_WIDTH = 8;
    private static final int DEFAULT_MAX_CHESS_FIELD_HEIGHT = 20;
    private static final int DEFAULT_MAX_CHESS_FIELD_WIDTH = 20;
    private static final int DEFAULT_MIN_CHESS_FIELD_HEIGHT = 5;
    private static final int DEFAULT_MIN_CHESS_FIELD_WIDTH = 5;
    private static final Pair<Integer, Integer> DEFAULT_ACTIVE_FIELD_POSITION = new Pair<Integer, Integer>(0, 0);
    private static final int DEFAULT_DARK_FIELD_COLOR = R.color.colorPrimaryDark;
    private static final int DEFAULT_LIGHT_FIELD_COLOR = R.color.colorPrimary;
    private static final int DEFAULT_VALUE_ANIMATOR_DURATION = 1000;
    private static final int DEFAULT_LIGHT_PATH_FIELD_COLOR = Color.LTGRAY;
    private static final int DEFAULT_DARK_PATH_FIELD_COLOR = Color.DKGRAY;

    // VALUES
    private int ACTIVE_FIELD_COLOR;
    private int ANIMATED_ACTIVE_FIELD_COLOR;
    private int CHESS_FIELD_HEIGHT;
    private int CHESS_FIELD_WIDTH;
    private int DARK_FIELD_COLOR;
    private int LIGHT_FIELD_COLOR;
    private int VALUE_ANIMATOR_DURATION;
    private int DARK_PATH_FIELD_COLOR;
    private int LIGHT_PATH_FIELD_COLOR;
    private int MAX_CHESS_FIELD_HEIGHT;
    private int MAX_CHESS_FIELD_WIDTH;
    private int MIN_CHESS_FIELD_HEIGHT;
    private int MIN_CHESS_FIELD_WIDTH;
    private Pair<Integer, Integer> ACTIVE_FIELD;

    private List<Pair<Integer, Integer>> activeFieldsList = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.M)
    public ChessView(Context context) {
        super(context);
        init(null, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public ChessView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(attrs, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public ChessView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(attrs, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void init(AttributeSet attrs, int defStyle) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ChessView,
                0, 0);
        try {
            ACTIVE_FIELD_COLOR = a.getColor(R.styleable.ChessView_activeFieldColor, getContext().getColor(DEFAULT_ACTIVE_FIELD_COLOR));
            CHESS_FIELD_WIDTH = a.getInt(R.styleable.ChessView_fieldWidth, DEFAULT_CHESS_FIELD_WIDTH);
            CHESS_FIELD_HEIGHT = a.getInteger(R.styleable.ChessView_fieldHeight, DEFAULT_CHESS_FIELD_HEIGHT);
            MIN_CHESS_FIELD_HEIGHT = a.getInteger(R.styleable.ChessView_minFieldHeight, DEFAULT_MIN_CHESS_FIELD_HEIGHT);
            MIN_CHESS_FIELD_WIDTH = a.getInteger(R.styleable.ChessView_minFieldWidth, DEFAULT_MIN_CHESS_FIELD_WIDTH);
            MAX_CHESS_FIELD_HEIGHT = a.getInteger(R.styleable.ChessView_maxFieldHeight, DEFAULT_MAX_CHESS_FIELD_HEIGHT);
            MAX_CHESS_FIELD_WIDTH = a.getInteger(R.styleable.ChessView_maxFieldWidth, DEFAULT_MAX_CHESS_FIELD_WIDTH);
            DARK_FIELD_COLOR = a.getColor(R.styleable.ChessView_darkFieldColor, getContext().getColor(DEFAULT_DARK_FIELD_COLOR));
            LIGHT_FIELD_COLOR = a.getColor(R.styleable.ChessView_lightFieldColor, getContext().getColor(DEFAULT_LIGHT_FIELD_COLOR));
            VALUE_ANIMATOR_DURATION = a.getInt(R.styleable.ChessView_animationDuration, DEFAULT_VALUE_ANIMATOR_DURATION);
            DARK_PATH_FIELD_COLOR = a.getColor(R.styleable.ChessView_pathFieldDarkColor, DEFAULT_DARK_PATH_FIELD_COLOR);
            LIGHT_PATH_FIELD_COLOR = a.getColor(R.styleable.ChessView_pathFieldLightColor, DEFAULT_LIGHT_PATH_FIELD_COLOR);

            final int activeFieldX = a.getInteger(R.styleable.ChessView_activeFieldX, DEFAULT_ACTIVE_FIELD_POSITION.first);
            final int activeFieldY = a.getInteger(R.styleable.ChessView_activeFieldY, DEFAULT_ACTIVE_FIELD_POSITION.second);
            ACTIVE_FIELD = new Pair<Integer, Integer>(activeFieldX, activeFieldY);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final Paint chessFieldPaint = new Paint();
        for (int y = 0; y < CHESS_FIELD_HEIGHT; y++) {
            int counter = y;
            for (int x = 0; x < CHESS_FIELD_WIDTH; x++) {
                // IF ABOUT TO DRAW CURRENT FIELD THEN SET COLOR
                if (ACTIVE_FIELD.first == x && ACTIVE_FIELD.second == y) {
                    chessFieldPaint.setColor(ANIMATED_ACTIVE_FIELD_COLOR);
                    counter++;
                }
                // IF ABOUT TO DRAW PAIR SET COLOR
                else if (activeFieldsList.contains(new Pair<Integer, Integer>(x, y)))
                    chessFieldPaint.setColor(counter++ % 2 == 1 ? DARK_PATH_FIELD_COLOR : LIGHT_PATH_FIELD_COLOR);
                    // IF ABOUT TO DRAW BLANK FIELD THEN SET COLOR
                else
                    chessFieldPaint.setColor(counter++ % 2 == 1 ? DARK_FIELD_COLOR : LIGHT_FIELD_COLOR);
                Rect currentRect = getRectByCoord(x, y);
                canvas.drawRect(currentRect, chessFieldPaint);
            }
        }
    }

    private int getTopCoordinate(int x, int y) {
        final int height_fraction = getHeight() / CHESS_FIELD_HEIGHT;
        if (y == 0)
            return height_fraction * y;
        else return height_fraction * y;
    }

    private int getBottomCoordinate(int x, int y) {
        final int height_fraction = getHeight() / CHESS_FIELD_HEIGHT;
        return height_fraction * (y + 1);
    }

    private int getLeftCoordinate(int x, int y) {
        final int width_fraction = getWidth() / CHESS_FIELD_WIDTH;
        return width_fraction * x;
    }

    private int getRightCoordinate(int x, int y) {
        final int width_fraction = getWidth() / CHESS_FIELD_WIDTH;
        return width_fraction * (x + 1);
    }

    private Rect getRectByCoord(int x, int y) {
        final int top = getTopCoordinate(x, y);
        final int bottom = getBottomCoordinate(x, y);
        final int left = getLeftCoordinate(x, y);
        final int right = getRightCoordinate(x, y);

        Rect rect = new Rect(left, top, right, bottom);
        return rect;
    }

    public void setActiveField(int x, int y) {
        ACTIVE_FIELD = new Pair<Integer, Integer>(x, y);
        try {
            checkIfCanFit(ACTIVE_FIELD);
        } catch (OutOfChessFieldException e) {
            Log.e(TAG, e.getMessage());
        }
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(LIGHT_FIELD_COLOR, ACTIVE_FIELD_COLOR);
        valueAnimator.setEvaluator(new ArgbEvaluator());

        valueAnimator.setDuration(200);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ANIMATED_ACTIVE_FIELD_COLOR = (int) animation.getAnimatedValue();
                invalidate();
                requestLayout();
            }
        });

        valueAnimator.start();
    }

    private boolean checkIfFitsBoundaries(Pair<Integer, Integer> boundaries) throws ChessView.OutOfChessFieldBoundariesException {
        final int widthBoundary = boundaries.first;
        final int heightBoundary = boundaries.second;

        boolean fitsWidthBoundary = false;
        boolean fitsHeightBoundary = false;
        if (widthBoundary >= MIN_CHESS_FIELD_WIDTH && widthBoundary <= MAX_CHESS_FIELD_WIDTH)
            fitsWidthBoundary = true;

        if (heightBoundary >= MIN_CHESS_FIELD_HEIGHT && heightBoundary <= MIN_CHESS_FIELD_HEIGHT)
            fitsHeightBoundary = true;

        if (fitsHeightBoundary && fitsWidthBoundary)
            return true;
        else
            throw new ChessView.OutOfChessFieldBoundariesException();
    }

    public void setChessFieldWidth(int width) {
        final Pair<Integer, Integer> targetSize = new Pair<Integer, Integer>(width, CHESS_FIELD_HEIGHT);
        try {
            checkIfFitsBoundaries(targetSize);
        } catch (OutOfChessFieldBoundariesException e) {
            Log.e(TAG, e.getMessage());
        }
        CHESS_FIELD_WIDTH = width;
        invalidate();
        requestLayout();
    }

    public void setChessFieldHeight(int height) {
        final Pair<Integer, Integer> targetSize = new Pair<Integer, Integer>(CHESS_FIELD_WIDTH, height);
        try {
            checkIfFitsBoundaries(targetSize);
        } catch (OutOfChessFieldBoundariesException e) {
            Log.e(TAG, e.getMessage());
        }
        CHESS_FIELD_HEIGHT = height;
        invalidate();
        requestLayout();
    }

    public void setLightFieldColor(int color) {
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(LIGHT_FIELD_COLOR, color);
        valueAnimator.setEvaluator(new ArgbEvaluator());
        valueAnimator.setDuration(VALUE_ANIMATOR_DURATION);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LIGHT_FIELD_COLOR = (int) animation.getAnimatedValue();
                invalidate();
                requestLayout();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setLightFieldColorResource(int id) {
        final int targetColor = getContext().getColor(id);
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(LIGHT_FIELD_COLOR, targetColor);
        valueAnimator.setEvaluator(new ArgbEvaluator());
        valueAnimator.setDuration(VALUE_ANIMATOR_DURATION);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LIGHT_FIELD_COLOR = (int) animation.getAnimatedValue();
                invalidate();
                requestLayout();
            }
        });

        valueAnimator.start();
    }

    public void setDarkFieldColor(int color) {
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(DARK_FIELD_COLOR, color);
        valueAnimator.setEvaluator(new ArgbEvaluator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                DARK_FIELD_COLOR = (int) animation.getAnimatedValue();
                invalidate();
                requestLayout();
            }
        });
        valueAnimator.setDuration(VALUE_ANIMATOR_DURATION);
        valueAnimator.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setDarkFieldColorResource(int id) {
        final int targetColor = getContext().getColor(id);
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(DARK_FIELD_COLOR, targetColor);
        valueAnimator.setEvaluator(new ArgbEvaluator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                DARK_FIELD_COLOR = (int) animation.getAnimatedValue();
                invalidate();
                requestLayout();
            }
        });

        valueAnimator.setDuration(VALUE_ANIMATOR_DURATION);
        valueAnimator.start();
    }

    public int getChessFieldWidth() {
        return CHESS_FIELD_WIDTH;
    }

    public int getChessFieldHeight() {
        return CHESS_FIELD_HEIGHT;
    }

    public int getActiveFieldX() {
        return ACTIVE_FIELD.first;
    }

    public int getActiveFieldY() {
        return ACTIVE_FIELD.second;
    }

    public void setActiveFieldsList(List<Pair<Integer, Integer>> fields) {
        this.activeFieldsList = fields;
        invalidate();
        requestLayout();
    }

    private boolean checkIfCanFit(Pair<Integer, Integer> coord) throws ChessView.OutOfChessFieldException {
        final int xCoord = coord.first;
        final int yCoord = coord.second;

        boolean xCoordCanFit = false;
        boolean yCoordCanFit = false;

        if (xCoord >= 0 && xCoord < CHESS_FIELD_WIDTH) {
            xCoordCanFit = true;
        }

        if (yCoord >= 0 && yCoord < CHESS_FIELD_HEIGHT) {
            yCoordCanFit = true;
        }

        if (xCoordCanFit && yCoordCanFit)
            return true;
        else {
            throw new ChessView.OutOfChessFieldException();
        }
    }

    public void addActiveField(Pair<Integer, Integer> position) {
        if (!this.activeFieldsList.contains(position)) {
            this.activeFieldsList.add(position);
        }
        invalidate();
        requestLayout();
    }

    public int getMinChessFieldHeight() {
        return MIN_CHESS_FIELD_HEIGHT;
    }

    public int getMinChessFieldWidth() {
        return MIN_CHESS_FIELD_WIDTH;
    }

    public int getMaxChessFieldHeight() {
        return MAX_CHESS_FIELD_HEIGHT;
    }

    public int getMaxChessFieldWidth() {
        return MAX_CHESS_FIELD_HEIGHT;
    }

    public void setMinChessFieldHeight(int height) {
        this.MIN_CHESS_FIELD_HEIGHT = height;
    }

    public void setMinChessFieldWidth(int width) {
        this.MIN_CHESS_FIELD_WIDTH = width;
    }

    public void setMaxChessFieldHeight(int height) {
        this.MAX_CHESS_FIELD_WIDTH = height;
    }

    public void setMaxChessFieldWidth(int width) {
        this.MAX_CHESS_FIELD_WIDTH = width;
    }

    // EXCEPTIONS
    private class OutOfChessFieldException extends Exception {
        private static final String MESSAGE = "You've run out of ChessField";

        public OutOfChessFieldException() {
            super(MESSAGE);
        }
    }

    private class OutOfChessFieldBoundariesException extends Exception {
        private static final String MESSAGE = "Can't set ChessField size!";

        public OutOfChessFieldBoundariesException() {
            super(MESSAGE);
        }
    }
}
