package fiitstu.gulis.cmsimulator.views;

import android.animation.ArgbEvaluator;
import android.animation.IntEvaluator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.renderscript.Sampler;
import android.support.annotation.IntegerRes;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import fiitstu.gulis.cmsimulator.R;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

public class ChessView extends View {
    private static final String TAG = "ChessView";

    // DEFAULT VALUES
    private static final int DEFAULT_ACTIVE_FIELD_COLOR = R.color.current_position;
    private static final int DEFAULT_CHESS_FIELD_HEIGHT = 8;
    private static final int DEFAULT_CHESS_FIELD_WIDTH = 8;
    private static final int DEFAULT_MAX_CHESS_FIELD_HEIGHT = 20;
    private static final int DEFAULT_MAX_CHESS_FIELD_WIDTH = 20;
    private static final int DEFAULT_MIN_CHESS_FIELD_HEIGHT = 5;
    private static final int DEFAULT_MIN_CHESS_FIELD_WIDTH = 5;
    private static final Pair<Integer, Integer> DEFAULT_ACTIVE_FIELD_POSITION = new Pair<Integer, Integer>(-1, -1);
    private static final int DEFAULT_DARK_FIELD_COLOR = R.color.colorPrimaryDark;
    private static final int DEFAULT_LIGHT_FIELD_COLOR = R.color.primary_color;
    private static final int DEFAULT_VALUE_ANIMATOR_DURATION = 1000;
    private static final int DEFAULT_LIGHT_PATH_FIELD_COLOR = Color.LTGRAY;
    private static final int DEFAULT_DARK_PATH_FIELD_COLOR = Color.DKGRAY;
    private static final int DEFAULT_DARK_FINISH_FIELD_COLOR = R.color.finish_dark;
    private static final int DEFAULT_LIGHT_FINISH_FIELD_COLOR = R.color.finish_light;
    private static final int DEFAULT_DARK_START_FIELD_COLOR = R.color.start_dark;
    private static final int DEFAULT_LIGHT_START_FIELD_COLOR = R.color.start_light;
    private static final int DEFAULT_FINISH_FIELD_X = -1;
    private static final int DEFAULT_FINISH_FIELD_Y = -1;
    private static final int DEFAULT_START_FIELD_X = -1;
    private static final int DEFAULT_START_FIELD_Y = -1;
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
    private Pair<Integer, Integer> START_FIELD;
    private Pair<Integer, Integer> FINISH_FIELD;
    private int START_FIELD_LIGHT_COLOR;
    private int START_FIELD_DARK_COLOR;
    private int FINISH_FIELD_LIGHT_COLOR;
    private int FINISH_FIELD_DARK_COLOR;

    private List<Pair<Integer, Integer>> activeFieldsList = new ArrayList<>();

    public enum FIELD_TYPE {
        BLANK,
        START,
        FINISH,
        PATH,
        CURRENT
    }

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

            final int startFieldX = a.getInteger(R.styleable.ChessView_startFieldX, DEFAULT_START_FIELD_X);
            final int startFieldY = a.getInteger(R.styleable.ChessView_startFieldY, DEFAULT_START_FIELD_Y);
            START_FIELD = new Pair<Integer, Integer>(startFieldX, startFieldY);

            final int finishFieldX = a.getInteger(R.styleable.ChessView_finishFieldX, DEFAULT_FINISH_FIELD_X);
            final int finishFieldY = a.getInteger(R.styleable.ChessView_activeFieldY, DEFAULT_FINISH_FIELD_Y);
            FINISH_FIELD = new Pair<Integer, Integer>(finishFieldX, finishFieldY);

            START_FIELD_LIGHT_COLOR = a.getColor(R.styleable.ChessView_startFieldLightColor, getContext().getColor(DEFAULT_LIGHT_START_FIELD_COLOR));
            FINISH_FIELD_LIGHT_COLOR = a.getColor(R.styleable.ChessView_finishFieldLightColor, getContext().getColor(DEFAULT_LIGHT_FINISH_FIELD_COLOR));
            START_FIELD_DARK_COLOR = a.getColor(R.styleable.ChessView_startFieldDarkColor, getContext().getColor(DEFAULT_DARK_START_FIELD_COLOR));
            FINISH_FIELD_DARK_COLOR = a.getColor(R.styleable.ChessView_finishFieldDarkColor, getContext().getColor(DEFAULT_DARK_FINISH_FIELD_COLOR));


            ANIMATED_ACTIVE_FIELD_COLOR = ACTIVE_FIELD_COLOR;
        } finally {
            a.recycle();
        }
    }

    private FIELD_TYPE getFieldType(int x, int y) {
        if (ACTIVE_FIELD.first == x && ACTIVE_FIELD.second == y)
            return FIELD_TYPE.CURRENT;
        else if (START_FIELD.first == x && START_FIELD.second == y)
            return FIELD_TYPE.START;
        else if (FINISH_FIELD.first == x && FINISH_FIELD.second == y)
            return FIELD_TYPE.FINISH;
        else if (activeFieldsList.contains(new Pair<Integer, Integer>(x, y)))
            return FIELD_TYPE.PATH;
        else return FIELD_TYPE.BLANK;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final Paint chessFieldPaint = new Paint();
        for (int y = 0; y < CHESS_FIELD_HEIGHT; y++) {
            int counter = y;
            for (int x = 0; x < CHESS_FIELD_WIDTH; x++) {
                final FIELD_TYPE field_type = getFieldType(x, y);
                switch (field_type) {
                    default:
                    case BLANK:
                        chessFieldPaint.setColor((counter & 1) == 1 ? DARK_FIELD_COLOR : LIGHT_FIELD_COLOR);
                        break;
                    case START:
                        chessFieldPaint.setColor((counter & 1) == 1 ? START_FIELD_DARK_COLOR : START_FIELD_LIGHT_COLOR);
                        break;
                    case FINISH:
                        chessFieldPaint.setColor((counter & 1) == 1 ? FINISH_FIELD_DARK_COLOR : FINISH_FIELD_LIGHT_COLOR);
                        break;
                    case PATH:
                        chessFieldPaint.setColor((counter & 1) == 1 ? DARK_PATH_FIELD_COLOR : LIGHT_PATH_FIELD_COLOR);
                        break;
                    case CURRENT:
                        chessFieldPaint.setColor(ANIMATED_ACTIVE_FIELD_COLOR);
                        break;
                }
                counter++;
                Rect currentRect = getRect(x, y);
                canvas.drawRect(currentRect, chessFieldPaint);
            }
        }
    }

    private Rect getRect(int x, int y) {
        final int rectWidth = getRectWidth();
        final int rectHeight = getRectHeight();

        final Rect rect = new Rect(x * (rectWidth), y * (rectHeight), (x + 1) * (rectWidth), (y + 1) * (rectHeight));
        return rect;
    }

    private int getRectWidth() {
        final float width = getWidth() / CHESS_FIELD_WIDTH;
        return (int) width;
    }

    private int getRectHeight() {
        final float height = getHeight() / CHESS_FIELD_HEIGHT;
        return (int) height;
    }

    private int getTopCoordinate(int x, int y) {
        final float height_fraction = getHeight() / CHESS_FIELD_HEIGHT;
        return (int) height_fraction * y;
    }

    private int getBottomCoordinate(int x, int y) {
        final float height_fraction = getHeight() / CHESS_FIELD_HEIGHT;
        return (int) height_fraction * (y + 1);
    }

    private int getLeftCoordinate(int x, int y) {
        final float width_fraction = getWidth() / CHESS_FIELD_WIDTH;
        return (int) width_fraction * x;
    }

    private int getRightCoordinate(int x, int y) {
        final float width_fraction = getWidth() / CHESS_FIELD_WIDTH;
        return (int) width_fraction * (x + 1);
    }

    private Rect getRectByCoord(int x, int y) {
        final int top = getTopCoordinate(x, y);
        final int bottom = getBottomCoordinate(x, y);
        final int left = getLeftCoordinate(x, y);
        final int right = getRightCoordinate(x, y);

        Rect rect = new Rect(left, top, right, bottom);
        return rect;
    }

    public void setActiveField(int x, int y) throws OutOfChessFieldException {
        ACTIVE_FIELD = new Pair<Integer, Integer>(x, y);
        if (checkIfCanFit(ACTIVE_FIELD)) {
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
    }

    private boolean checkIfFitsBoundaries(Pair<Integer, Integer> boundaries) throws ChessView.OutOfChessFieldBoundariesException {
        final int widthBoundary = boundaries.first;
        final int heightBoundary = boundaries.second;

        boolean fitsWidthBoundary = false;
        boolean fitsHeightBoundary = false;
        if (widthBoundary >= MIN_CHESS_FIELD_WIDTH && widthBoundary <= MAX_CHESS_FIELD_WIDTH)
            fitsWidthBoundary = true;

        if (heightBoundary >= MIN_CHESS_FIELD_HEIGHT && heightBoundary <= MAX_CHESS_FIELD_HEIGHT)
            fitsHeightBoundary = true;

        if (fitsHeightBoundary && fitsWidthBoundary)
            return true;
        else
            throw new ChessView.OutOfChessFieldBoundariesException();
    }

    public void setChessFieldWidth(final int width) throws OutOfChessFieldBoundariesException {
        final Pair<Integer, Integer> targetSize = new Pair<Integer, Integer>(width, CHESS_FIELD_HEIGHT);
        if (checkIfFitsBoundaries(targetSize)) {
            CHESS_FIELD_WIDTH = width;
            invalidate();
            requestLayout();
        }
    }

    public void setChessFieldHeight(int height) throws OutOfChessFieldBoundariesException {
        final Pair<Integer, Integer> targetSize = new Pair<Integer, Integer>(CHESS_FIELD_WIDTH, height);
        if (checkIfFitsBoundaries(targetSize)) {
            CHESS_FIELD_HEIGHT = height;
            invalidate();
            requestLayout();
        }
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

    public void setStartField(Pair<Integer, Integer> field) throws OutOfChessFieldException {
        if (checkIfCanFit(field)) {
            if (this.FINISH_FIELD == field)
                this.FINISH_FIELD = new Pair<>(DEFAULT_FINISH_FIELD_X, DEFAULT_FINISH_FIELD_X);
            if (this.START_FIELD == field)
                this.START_FIELD = new Pair<>(DEFAULT_START_FIELD_X, DEFAULT_START_FIELD_Y);
            else
                this.START_FIELD = field;
            invalidate();
            requestLayout();
        }
    }

    public void setFinishField(Pair<Integer, Integer> field) throws OutOfChessFieldException {
        if (checkIfCanFit(field)) {
            if (this.START_FIELD == field)
                this.START_FIELD = new Pair<>(DEFAULT_START_FIELD_X, DEFAULT_START_FIELD_Y);
            if (this.FINISH_FIELD == field)
                this.FINISH_FIELD = new Pair<>(DEFAULT_FINISH_FIELD_X, DEFAULT_FINISH_FIELD_Y);
            else
                this.FINISH_FIELD = field;
            invalidate();
            requestLayout();
        }
    }

    public Pair<Integer, Integer> getStartField() {
        return START_FIELD;
    }

    public Pair<Integer, Integer> getFinishField() {
        return FINISH_FIELD;
    }

    public Pair<Integer, Integer> getRectCoordForTouchCoord(float x, float y) {
        final float rectWidth = getWidth() / CHESS_FIELD_WIDTH;
        final float rectHeight = getHeight() / CHESS_FIELD_HEIGHT;

        final int rectX = (int) (x / rectWidth);
        final int rectY = (int) (y / rectHeight);

        final Pair<Integer, Integer> rectCoord = new Pair<>(rectX, rectY);

        return rectCoord;
    }

    public List<Pair<Integer, Integer>> getPath() {
        return this.activeFieldsList;
    }

    public void addFieldToPath(Pair<Integer, Integer> field) throws OutOfChessFieldException {
        if (checkIfCanFit(field) && !activeFieldsList.contains(field)) {
            if (START_FIELD != field && FINISH_FIELD != field)
                activeFieldsList.add(field);
        }

        invalidate();
        requestLayout();
    }

    public void removeFieldFromPath(Pair<Integer, Integer> field) throws OutOfChessFieldException {
        if (checkIfCanFit(field) && activeFieldsList.contains(field)) {
            activeFieldsList.remove(field);
        }

        invalidate();
        requestLayout();
    }

    public boolean isFieldInPath(Pair<Integer, Integer> field) throws OutOfChessFieldException {
        if (checkIfCanFit(field)) {
            return activeFieldsList.contains(field);
        }

        return false;
    }

    public Pair<Integer, Integer> getFieldSize(){
        return new Pair<>(CHESS_FIELD_WIDTH,CHESS_FIELD_HEIGHT);
    }

    // EXCEPTIONS
    public class OutOfChessFieldException extends Exception {

        private static final String MESSAGE = "You've run out of ChessField";

        public OutOfChessFieldException() {
            super(MESSAGE);
        }

    }

    public class OutOfChessFieldBoundariesException extends Exception {

        private static final String MESSAGE = "Can't set ChessField size!";

        public OutOfChessFieldBoundariesException() {
            super(MESSAGE);
        }

    }


}
