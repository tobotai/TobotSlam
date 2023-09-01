package com.tobot.map.module.main.action;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.tobot.map.R;

/**
 * 自定义方向的view
 *
 * @author houdeming
 * @date 2023/06/03
 */
public class DirectionView extends View implements View.OnTouchListener {
    private int sideStrokeWidth;
    private int sideColor;
    private int bg;
    private int rockerStrokeWidth;
    private boolean isRockerStyleFill;
    private int rockerRadius;
    private int rockerMargin;
    private int rockerBg;
    private int arrowStrokeWidth;
    private int arrowColor;
    private int arrowPressedColor;
    private int halfWidth;
    private int halfHeight;
    private int arrowMargin;
    private int arrowOffset;
    private int arrowHeight;
    private Paint paintSide;
    private Paint paintBg;
    private Paint paintRocker;
    private Paint paintArrow;
    private int pressDirectionH = 0;
    private int pressDirectionV = 0;
    private int smallCircleCenterX = -1;
    private int smallCircleCenterY = -1;
    private boolean isTouchCenter;
    private BlurMaskFilter blurMaskFilter;
    private final Path path = new Path();
    private DirectionMode mDirectionMode;
    private OnShakeListener mOnShakeListener;

    public DirectionView(Context context) {
        this(context, null);
    }

    public DirectionView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DirectionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = getSize(widthMode, widthMeasureSpec);
        int height = getSize(heightMode, heightMeasureSpec);
        halfWidth = (int) (width / 2.0f);
        halfHeight = (int) (height / 2.0f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(halfWidth, halfHeight, halfWidth - sideStrokeWidth, paintBg);
        canvas.drawCircle(halfWidth, halfHeight, halfWidth - sideStrokeWidth / 2.0f, paintSide);
        if (this.smallCircleCenterY != -1) {
//            paintRocker.setMaskFilter(blurMaskFilter);
            canvas.drawCircle(smallCircleCenterX, smallCircleCenterY, rockerRadius, paintRocker);
        } else {
//            paintRocker.setMaskFilter(null);
            canvas.drawCircle(halfWidth, halfHeight, rockerRadius, paintRocker);
        }

        int arrowSpace = arrowMargin + arrowHeight;
        // 上
        paintArrow.setColor(pressDirectionV == Direction.UP ? arrowPressedColor : arrowColor);
        path.reset();
        path.moveTo(halfWidth - arrowOffset, arrowSpace);
        path.lineTo(halfWidth, arrowMargin);
        path.lineTo(halfWidth + arrowOffset, arrowSpace);
        canvas.drawPath(path, paintArrow);
        // 右
        paintArrow.setColor(pressDirectionH == Direction.RIGHT ? arrowPressedColor : arrowColor);
        int width = halfWidth * 2;
        path.reset();
        path.moveTo(width - arrowSpace, halfHeight - arrowOffset);
        path.lineTo(width - arrowMargin, halfHeight);
        path.lineTo(width - arrowSpace, halfHeight + arrowOffset);
        canvas.drawPath(path, paintArrow);
        // 下
        paintArrow.setColor(pressDirectionV == Direction.DOWN ? arrowPressedColor : arrowColor);
        int height = halfHeight * 2;
        path.reset();
        path.moveTo(halfWidth - arrowOffset, height - arrowSpace);
        path.lineTo(halfWidth, height - arrowMargin);
        path.lineTo(halfWidth + arrowOffset, height - arrowSpace);
        canvas.drawPath(path, paintArrow);
        // 左
        paintArrow.setColor(pressDirectionH == Direction.LEFT ? arrowPressedColor : arrowColor);
        path.reset();
        path.moveTo(arrowSpace, halfHeight - arrowOffset);
        path.lineTo(arrowMargin, halfHeight);
        path.lineTo(arrowSpace, halfHeight + arrowOffset);
        canvas.drawPath(path, paintArrow);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        try {
            int action = event.getAction();
            int x = (int) event.getX();
            int y = (int) event.getY();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    if (mOnShakeListener != null) {
                        mOnShakeListener.onStart();
                    }

                    // 判断按下的是不是中间
                    if (x >= halfWidth - rockerRadius && x <= halfWidth + rockerRadius && y >= halfHeight - rockerRadius && y <= halfHeight + rockerRadius) {
                        isTouchCenter = true;
                        this.smallCircleCenterX = x;
                        this.smallCircleCenterY = y;
                        return true;
                    }

                    isTouchCenter = false;
                    // 上
                    if (y < halfHeight - rockerRadius) {
                        this.smallCircleCenterX = halfWidth;
                        this.smallCircleCenterY = rockerMargin + rockerRadius;
                        setDirection(Direction.NONE, Direction.UP);
                        break;
                    }

                    // 下
                    if (y > halfHeight + rockerRadius) {
                        this.smallCircleCenterX = halfWidth;
                        this.smallCircleCenterY = halfHeight * 2 - rockerMargin - rockerRadius;
                        setDirection(Direction.NONE, Direction.DOWN);
                        break;
                    }

                    // 左
                    if (x < halfWidth - rockerRadius) {
                        this.smallCircleCenterX = rockerMargin + rockerRadius;
                        this.smallCircleCenterY = halfHeight;
                        setDirection(Direction.LEFT, Direction.NONE);
                        break;
                    }

                    // 右
                    if (x > halfWidth + rockerRadius) {
                        this.smallCircleCenterX = halfWidth * 2 - rockerMargin - rockerRadius;
                        this.smallCircleCenterY = halfHeight;
                        setDirection(Direction.RIGHT, Direction.NONE);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    // 说明按下的是中间小圆
                    if (isTouchCenter) {
                        // 小圆拖动时圆心所在的圆的最大半径
                        int maxRadius = halfHeight - rockerRadius - rockerMargin;
                        int minRadius = rockerRadius + rockerMargin;
                        int deltaX = x - halfWidth;
                        int deltaY = y - halfHeight;
                        int length = (int) Math.hypot(deltaX, deltaY);
                        if (length > maxRadius) {
                            this.smallCircleCenterX = halfWidth + maxRadius * deltaX / length;
                            this.smallCircleCenterY = halfHeight + maxRadius * deltaY / length;
                        } else {
                            this.smallCircleCenterX = x;
                            this.smallCircleCenterY = y;
                            if (length < minRadius) {
                                setDirection(Direction.NONE, Direction.NONE);
                                invalidate();
                                return true;
                            }
                        }

                        double aTan = Math.atan((this.smallCircleCenterY - halfHeight) * 1.0 / (this.smallCircleCenterX - halfWidth));
                        double degree = Math.toDegrees(aTan);
                        handleDegree(degree);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    setDirection(Direction.NONE, Direction.NONE);
                    this.smallCircleCenterX = -1;
                    this.smallCircleCenterY = -1;
                    isTouchCenter = false;
                    if (mOnShakeListener != null) {
                        mOnShakeListener.onFinish();
                    }
                    break;
                default:
                    break;
            }

            invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 一定要return true, 否则UP事件不会被监听到。
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            initAttr(attrs);
        }

        // 描边
        paintSide = createPaint(sideStrokeWidth, sideColor, Paint.Style.STROKE);
        // 背景色
        paintBg = createPaint(2, bg, Paint.Style.FILL);
        // 中心的遥杆
        paintRocker = createPaint(rockerStrokeWidth, rockerBg, isRockerStyleFill ? Paint.Style.FILL : Paint.Style.STROKE);
        // 箭头
        paintArrow = createPaint(arrowStrokeWidth, arrowColor, Paint.Style.STROKE);
        // 阴影
        blurMaskFilter = new BlurMaskFilter(20, BlurMaskFilter.Blur.SOLID);
        setOnTouchListener(this);
    }

    private void initAttr(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.DirectionView);
        if (array.hasValue(R.styleable.DirectionView_directionSideStrokeWidth)) {
            sideStrokeWidth = array.getInt(R.styleable.DirectionView_directionSideStrokeWidth, 2);
        }

        if (array.hasValue(R.styleable.DirectionView_directionSideColor)) {
            sideColor = array.getColor(R.styleable.DirectionView_directionSideColor, Color.WHITE);
        }

        if (array.hasValue(R.styleable.DirectionView_directionBg)) {
            bg = array.getColor(R.styleable.DirectionView_directionBg, Color.WHITE);
        }

        if (array.hasValue(R.styleable.DirectionView_directionRockerStrokeWidth)) {
            rockerStrokeWidth = array.getInt(R.styleable.DirectionView_directionRockerStrokeWidth, 1);
        }

        if (array.hasValue(R.styleable.DirectionView_directionRockerStyleFill)) {
            isRockerStyleFill = array.getBoolean(R.styleable.DirectionView_directionRockerStyleFill, false);
        }

        if (array.hasValue(R.styleable.DirectionView_directionRockerRadius)) {
            rockerRadius = array.getInt(R.styleable.DirectionView_directionRockerRadius, 4);
        }

        if (array.hasValue(R.styleable.DirectionView_directionRockerMargin)) {
            rockerMargin = array.getInt(R.styleable.DirectionView_directionRockerMargin, 8);
        }

        if (array.hasValue(R.styleable.DirectionView_directionRockerBg)) {
            rockerBg = array.getColor(R.styleable.DirectionView_directionRockerBg, Color.WHITE);
        }

        if (array.hasValue(R.styleable.DirectionView_directionArrowStrokeWidth)) {
            arrowStrokeWidth = array.getInt(R.styleable.DirectionView_directionArrowStrokeWidth, 2);
        }

        if (array.hasValue(R.styleable.DirectionView_directionArrowColor)) {
            arrowColor = array.getColor(R.styleable.DirectionView_directionArrowColor, Color.WHITE);
        }

        if (array.hasValue(R.styleable.DirectionView_directionArrowPressedColor)) {
            arrowPressedColor = array.getColor(R.styleable.DirectionView_directionArrowPressedColor, Color.WHITE);
        }

        if (array.hasValue(R.styleable.DirectionView_directionArrowMargin)) {
            arrowMargin = array.getInt(R.styleable.DirectionView_directionArrowMargin, 10);
        }

        if (array.hasValue(R.styleable.DirectionView_directionArrowOffset)) {
            arrowOffset = array.getInt(R.styleable.DirectionView_directionArrowOffset, 8);
        }

        if (array.hasValue(R.styleable.DirectionView_directionArrowHeight)) {
            arrowHeight = array.getInt(R.styleable.DirectionView_directionArrowHeight, 6);
        }

        array.recycle();
    }

    private Paint createPaint(int strokeWidth, int color, Paint.Style style) {
        Paint paint = new Paint();
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(color);
        paint.setStyle(style);
        paint.setAntiAlias(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        return paint;
    }

    private int getSize(int mode, int sizeMeasureSpec) {
        if (mode == MeasureSpec.EXACTLY || mode == MeasureSpec.AT_MOST) {
            return MeasureSpec.getSize(sizeMeasureSpec);
        }

        if (mode == MeasureSpec.UNSPECIFIED) {
            return sizeMeasureSpec;
        }

        return 0;
    }

    private void handleDegree(double degree) {
        if (this.smallCircleCenterX < halfWidth) {
            if (degree > 60) {
                setDirection(Direction.NONE, Direction.UP);
            } else if (degree > 30) {
                setDirection(Direction.LEFT, Direction.UP);
            } else if (degree > -30) {
                setDirection(Direction.LEFT, Direction.NONE);
            } else if (degree > -60) {
                setDirection(Direction.LEFT, Direction.DOWN);
            } else {
                setDirection(Direction.NONE, Direction.DOWN);
            }
        } else {
            if (degree > 60) {
                setDirection(Direction.NONE, Direction.DOWN);
            } else if (degree > 30) {
                setDirection(Direction.RIGHT, Direction.DOWN);
            } else if (degree > -30) {
                setDirection(Direction.RIGHT, Direction.NONE);
            } else if (degree > -60) {
                setDirection(Direction.RIGHT, Direction.UP);
            } else {
                setDirection(Direction.NONE, Direction.UP);
            }
        }
    }

    private void setDirection(int directionH, int directionV) {
        this.pressDirectionH = directionH;
        this.pressDirectionV = directionV;
        if (mOnShakeListener == null) {
            return;
        }

        switch (directionH) {
            case Direction.NONE:
                switch (directionV) {
                    case Direction.UP:
                        mOnShakeListener.onDirection(DirectionEnum.DIRECTION_UP);
                        break;
                    case Direction.DOWN:
                        mOnShakeListener.onDirection(DirectionEnum.DIRECTION_DOWN);
                        break;
                    default:
                        break;
                }
                break;
            case Direction.LEFT:
                switch (directionV) {
                    case Direction.NONE:
                        mOnShakeListener.onDirection(DirectionEnum.DIRECTION_LEFT);
                        break;
                    case Direction.UP:
                        mOnShakeListener.onDirection(mDirectionMode == DirectionMode.DIRECTION_8 ? DirectionEnum.DIRECTION_UP_LEFT : DirectionEnum.DIRECTION_LEFT);
                        break;
                    case Direction.DOWN:
                        mOnShakeListener.onDirection(mDirectionMode == DirectionMode.DIRECTION_8 ? DirectionEnum.DIRECTION_DOWN_LEFT : DirectionEnum.DIRECTION_LEFT);
                        break;
                    default:
                        break;
                }
                break;
            case Direction.RIGHT:
                switch (directionV) {
                    case Direction.NONE:
                        mOnShakeListener.onDirection(DirectionEnum.DIRECTION_RIGHT);
                        break;
                    case Direction.UP:
                        mOnShakeListener.onDirection(mDirectionMode == DirectionMode.DIRECTION_8 ? DirectionEnum.DIRECTION_UP_RIGHT : DirectionEnum.DIRECTION_RIGHT);
                        break;
                    case Direction.DOWN:
                        mOnShakeListener.onDirection(mDirectionMode == DirectionMode.DIRECTION_8 ? DirectionEnum.DIRECTION_DOWN_RIGHT : DirectionEnum.DIRECTION_RIGHT);
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    public void setDirectionMode(DirectionMode mode) {
        mDirectionMode = mode;
    }

    public void setOnShakeListener(OnShakeListener listener) {
        mOnShakeListener = listener;
    }

    private static class Direction {
        public static final int NONE = 0;
        public static final int LEFT = 1;
        public static final int RIGHT = 2;
        public static final int UP = 3;
        public static final int DOWN = 4;
    }

    /**
     * 摇动方向监听接口
     */
    public interface OnShakeListener {
        /**
         * 开始
         */
        void onStart();

        /**
         * 摇动方向
         *
         * @param direction 方向
         */
        void onDirection(DirectionEnum direction);

        /**
         * 结束
         */
        void onFinish();
    }

    /**
     * 方向
     */
    public enum DirectionEnum {
        /**
         * 左
         */
        DIRECTION_LEFT,
        /**
         * 右
         */
        DIRECTION_RIGHT,
        /**
         * 上
         */
        DIRECTION_UP,
        /**
         * 下
         */
        DIRECTION_DOWN,
        /**
         * 左上
         */
        DIRECTION_UP_LEFT,
        /**
         * 右上
         */
        DIRECTION_UP_RIGHT,
        /**
         * 左下
         */
        DIRECTION_DOWN_LEFT,
        /**
         * 右下
         */
        DIRECTION_DOWN_RIGHT,
    }

    /**
     * 摇杆支持几个方向
     */
    public enum DirectionMode {
        /**
         * 四个方向 旋转45度
         */
        DIRECTION_4,
        /**
         * 八个方向
         */
        DIRECTION_8
    }
}
