package com.kingpei.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * AzimuthCircle for IR Control or others
 */
public class AzimuthCircle extends ViewGroup {
    public static final int LEFT_PRESS = 1;
    public static final int TOP_PRESS = 2;
    public static final int RIGHT_PRESS = 3;
    public static final int BOTTOM_PRESS = 4;
    public static final int NONE_PRESS = -1;

    private int pressDirection = NONE_PRESS;

    public int getPressDirection() {
        return pressDirection;
    }

    private static final String TAG = AzimuthCircle.class.getSimpleName();

    private int circleColor = -1;
    private int shadowColor = -1;

    private Drawable drawLeft = null;
    private Drawable drawRight = null;
    private Drawable drawTop = null;
    private Drawable drawBottom = null;

    public AzimuthCircle(Context context) {
        super(context);

        initWork();
    }

    private void initWork() {
        if(getBackground() == null){
            setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }

        defaultParentSize = dp2px(getContext(), 100);
        defaultChildSize = dp2px(getContext(), 50);
    }


    public AzimuthCircle(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        getAzimuthCircleAttrs(attrs);
       initWork();
    }

    private void getAzimuthCircleAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.AzimuthCircle);
        circleColor = typedArray.getResourceId(R.styleable.AzimuthCircle_circleColor, -1);
        shadowColor = typedArray.getResourceId(R.styleable.AzimuthCircle_shadowColor, -1);
        drawLeft = typedArray.getDrawable(R.styleable.AzimuthCircle_drawLeft);
        drawRight = typedArray.getDrawable(R.styleable.AzimuthCircle_drawRight);
        drawTop = typedArray.getDrawable(R.styleable.AzimuthCircle_drawTop);
        drawBottom = typedArray.getDrawable(R.styleable.AzimuthCircle_drawBottom);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutChildren(l, t, r, b);
    }

    /**
     * make child center inside
     */
    void layoutChildren(int left, int top, int right, int bottom) {
        if (getChildCount() > 0) {
            View view = getChildAt(0);
            int width = view.getMeasuredWidth();
            int height = view.getMeasuredHeight();

            int parentWidth = right - left;
            int parentHeight = bottom - top;

            int childLeft = (parentWidth - width) / 2;
            int childTop = (parentHeight - height) / 2;
//            LogAs.i(TAG, "childLeft:" + childLeft + " -- childTop:" + childTop + " -- width:" + width + " -- height:" + height);
            view.layout(childLeft, childTop,  childLeft + width,   childTop + height);
        }
    }

    private int defaultParentSize;
    private int defaultChildSize;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //parent size
        int maxParentSize = defaultParentSize;

        maxParentSize = Math.max(maxParentSize, getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec));
        setMeasuredDimension(maxParentSize, maxParentSize);

//        LogAs.i(TAG, "maxParentSize:" + maxParentSize);

        //child size
        int maxChildSize = defaultChildSize;
        if(getChildCount() > 0){
            View child = getChildAt(0);
            LayoutParams lp = child.getLayoutParams();
            if(lp.width != LayoutParams.MATCH_PARENT  && lp.width != LayoutParams.WRAP_CONTENT){
                    maxChildSize = Math.max(maxChildSize, lp.width);
            }

            int measureSpec = MeasureSpec.makeMeasureSpec(maxChildSize, MeasureSpec.EXACTLY);
            child.measure(measureSpec, measureSpec);

//            LogAs.i(TAG, "maxChildSize:" + maxChildSize);
        }
    }

    @Override
    public void addView(View child) {
        //make sure have only one child
        removeAllViews();
        super.addView(child);
    }

    private boolean consumeTouchEvent;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        Log.i(TAG, "event:" + event.getAction());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pressDirection = getPressDirection(event.getX(), event.getY());
                consumeTouchEvent = shouldIntercept(event.getX(), event.getY());
                Log.i(TAG, "pressDirection:" + pressDirection);
                if(consumeTouchEvent){
                    invalidate();
                }


                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                //some times it won't performClick, so should post unClick
                if(consumeTouchEvent){
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            unClick();
                        }
                    }, 50);
                }

                break;
        }

        Log.i(TAG, "consumeTouchEvent:" + consumeTouchEvent);
//        LogAs.i(TAG, "onTouch:" + onTouch);

        return consumeTouchEvent;
    }

    private void unClick(){
        pressDirection = NONE_PRESS;
        invalidate();
    }

    @Override
    public boolean performClick() {
        boolean result = super.performClick();
        unClick();
        return result;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        boolean inter = (super.onInterceptTouchEvent(ev) || shouldIntercept(ev.getX(), ev.getY()));
        Log.i(TAG, "action:" + action + " -- inter:" +inter);
        return inter;
    }

    private boolean shouldIntercept(float x, float y) {
        int centerPoint = getMeasuredWidth() / 2;
        double minSqr = 0;

        float calculateX = x - centerPoint;
        float calculateY = y - centerPoint;

        Log.i(TAG, "centerPoint：" + centerPoint + " -- calculateX:" + calculateX + "calculateY:" + calculateY);

        if (getChildCount() > 0) {
            View view = getChildAt(0);
            minSqr = view.getMeasuredWidth() / 2;
        }

        double calculateSqr = Math.sqrt(Math.pow(calculateX, 2) + Math.pow(calculateY, 2));
        double centerSqr = centerPoint;

        Log.i(TAG, "calculateSqr：" + calculateSqr + " -- centerSqr:" + centerSqr + "minSqr:" + minSqr);

        return !(calculateSqr >= centerSqr || calculateSqr <= minSqr);
    }

    //measure press area
    private int getPressDirection(float x, float y) {
        int centerPoint = getMeasuredWidth() / 2;

        float calculateX = x - centerPoint;
        float calculateY = y - centerPoint;

        if (!shouldIntercept(x, y)) {
            return NONE_PRESS;
        }

        double targetTan = (double) calculateY / calculateX;
        double targetDegree = Math.toDegrees(Math.atan(targetTan));

        if (calculateX > 0 && calculateY < 0) {
            targetDegree += 360;
        } else if (calculateX < 0) {
            targetDegree += 180;
        }

//        LogAs.i(TAG, "targetTan：" + targetTan + " -- targetDegree:" + targetDegree);
        if (targetDegree < 225 && targetDegree >= 135) {
            return LEFT_PRESS;
        } else if (targetDegree < 315 && targetDegree >= 225) {
            return TOP_PRESS;
        } else if (targetDegree > 315) {
            return RIGHT_PRESS;
        } else if (targetDegree < 135 && targetDegree >= 45) {
            return BOTTOM_PRESS;
        } else {
            return RIGHT_PRESS;
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        drawDirectImage(canvas);
        drawCircle(canvas);
        drawPressArea(canvas);
        super.onDraw(canvas);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        return super.drawChild(canvas, child, drawingTime);
    }

    private void drawDirectImage(Canvas canvas) {
        int painDistance = dp2px(getContext(), 12);

        if(drawLeft != null){
            drawLeft.setBounds(painDistance, getMeasuredWidth()/2 - drawLeft.getIntrinsicHeight() /2, drawLeft.getIntrinsicWidth() + painDistance, getMeasuredWidth()/2 + drawLeft.getIntrinsicHeight()/2 );
            drawLeft.draw(canvas);
        }

        if(drawRight != null){
            drawRight.setBounds(getMeasuredWidth() - painDistance - drawRight.getIntrinsicWidth(), getMeasuredWidth()/2 - drawRight.getIntrinsicHeight() /2,getMeasuredWidth() - painDistance, getMeasuredWidth()/2 + drawRight.getIntrinsicHeight()/2 );
            drawRight.draw(canvas);
        }

        if(drawTop != null){
            drawTop.setBounds(getMeasuredWidth() /2 - drawTop.getIntrinsicWidth() / 2, painDistance, getMeasuredWidth()/2+drawTop.getIntrinsicWidth()/2, painDistance+ drawTop.getIntrinsicHeight());
            drawTop.draw(canvas);
        }

        if(drawBottom != null){
            drawBottom.setBounds(getMeasuredWidth()/2-drawBottom.getIntrinsicWidth()/2, getMeasuredWidth() - drawBottom.getIntrinsicHeight() -painDistance, getMeasuredWidth()/2+drawBottom.getIntrinsicHeight()/2, getMeasuredWidth() -painDistance);
            drawBottom.draw(canvas);
        }
    }

    private void drawPressArea(Canvas canvas) {
        if (circleColor <= 0 || shadowColor <= 0 || pressDirection == NONE_PRESS) {
            return;
        }

        RectF rectF;

        float startAngle = (float) getStartAngle();
        int paintWidth = dp2px(getContext(), 1);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(paintWidth);
        paint.setStyle(Paint.Style.STROKE);

        for (int i = 0; i < 8; i++) {
            int paintDistance = (paintWidth * (i + 1));
            float maxDistance = getMeasuredWidth();
            float halfDistance = getMeasuredWidth() /2;
            float minDistance = (float) (Math.sqrt(2.0) / 2 * getMeasuredWidth());
            float pieceDistance = (maxDistance - minDistance)/2;
            rectF = new RectF(paintDistance, paintDistance, maxDistance -paintDistance, maxDistance -paintDistance);

            int startColor;
            int endColor;

            if (i < 3) {
                startColor = makeAlpha(i * 16, getResources().getColor(shadowColor));
                endColor = makeAlpha((i + 1) * 16, getResources().getColor(shadowColor));
            } else {
//                startColor = makeAlpha(i * 16, getResources().getColor(shadowColor));
                startColor = makeAlpha((2 * (i - 3)) * 16, getResources().getColor(circleColor));
                endColor = makeAlpha((4 + 2 * (i - 3)) * 16, getResources().getColor(circleColor));
            }

            if(pressDirection == LEFT_PRESS){
                paint.setShader(new LinearGradient(paintDistance, halfDistance, pieceDistance + paintDistance, halfDistance, endColor, startColor, Shader.TileMode.CLAMP));
                canvas.drawArc(rectF, startAngle, 90, false, paint);
            }else if(pressDirection == TOP_PRESS){
                paint.setShader(new LinearGradient(halfDistance,  paintDistance, halfDistance, pieceDistance + paintDistance, endColor, startColor, Shader.TileMode.CLAMP));
                canvas.drawArc(rectF, startAngle, 90, false, paint);
            }else if(pressDirection == RIGHT_PRESS){
                paint.setShader(new LinearGradient(maxDistance - pieceDistance - paintDistance, halfDistance, maxDistance - paintDistance, halfDistance, startColor, endColor, Shader.TileMode.CLAMP));
                canvas.drawArc(rectF, startAngle, 90, false, paint);
            }else if(pressDirection == BOTTOM_PRESS){
                paint.setShader(new LinearGradient(halfDistance, maxDistance - pieceDistance - paintDistance, halfDistance, maxDistance - paintDistance, startColor, endColor, Shader.TileMode.CLAMP));
                canvas.drawArc(rectF, startAngle, 90, false, paint);
            }
        }
    }

    public int makeAlpha(int alpha, @ColorInt int color){
        int blue = Color.blue(color);
        int green = Color.green(color);
        int red = Color.red(color);
        int resultColor;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            resultColor = Color.argb(alpha, red, green, blue);
        }else{
            resultColor = (alpha << 24) | (red << 16) | (green << 8) | blue;
        }

        return resultColor;
    }

    private double getStartAngle() {
        if (pressDirection == LEFT_PRESS) {
            return 135;
        } else if (pressDirection == TOP_PRESS) {
            return 225;
        } else if (pressDirection == RIGHT_PRESS) {
            return 315;
        } else if (pressDirection == BOTTOM_PRESS) {
            return 45;
        } else {
            return 0;
        }
    }

    private double getBgStartAngle() {
        if (pressDirection == LEFT_PRESS) {
            return 225;
        } else if (pressDirection == TOP_PRESS) {
            return 315;
        } else if (pressDirection == RIGHT_PRESS) {
            return 45;
        } else if (pressDirection == BOTTOM_PRESS) {
            return 135;
        } else {
            return 0;
        }
    }

    public int dp2px(Context context, int dp) {
        return (int) (context.getResources().getDisplayMetrics().density * dp + 0.5f);
    }

    private void drawCircle(Canvas canvas) {
        if (circleColor <= 0 || shadowColor <= 0) {
            return;
        }

        int paintWidth = dp2px(getContext(), 1);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(paintWidth);
        paint.setStyle(Paint.Style.STROKE);
        int centerPoint = getMeasuredWidth() / 2;

        for (int i = 0; i < 8; i++) {

            int distance = (paintWidth * (i + 1));
            if (i < 4) {
                paint.setColor(makeAlpha(0, getResources().getColor(shadowColor)));
            } else if (i == 4 || i == 5) {
                paint.setColor(makeAlpha(16 * (i - 3), getResources().getColor(shadowColor)));
            } else {
                paint.setColor(makeAlpha(3 * 16 + 2 * 16 * (i - 6), getResources().getColor(circleColor)));
            }

            if (pressDirection == NONE_PRESS) {
                canvas.drawCircle(centerPoint, centerPoint, centerPoint - distance, paint);
            } else {
                RectF rectF = new RectF(distance, distance, getMeasuredWidth() - distance, getMeasuredWidth() - distance);
                float startAngle = (float) getBgStartAngle();
                canvas.drawArc(rectF, startAngle, 270, false, paint);
            }
        }
    }
}
