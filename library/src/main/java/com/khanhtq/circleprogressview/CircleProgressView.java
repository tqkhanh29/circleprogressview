package com.khanhtq.circleprogressview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.khanhtq.circleprogressview.util.ScreenUtil;

/**
 * Created by khanhtq on 16/06/2017.
 */

public class CircleProgressView extends View {
    private static final int DEFAULT_AVAILABLE_BPOINT_START_GRADIENT_COLOR = 0xfff6d365;
    private static final int DEFAULT_AVAILABLE_BPOINT_END_GRADIENT_COLOR = 0xfffda085;
    private static final String EMPTY_TEXT = "";
    private static final int DEFAULT_TEXT_COLOR = Color.WHITE;
    private static final int FRAME_PER_SECOND = 60;

    private Paint mAvailablePaint;
    private Paint mUsedPaint;
    private Paint mInnerCirclePaint;
    private Paint mValueTextPaint;
    private Paint mLabelTextPaint;

    private Typeface mOpenSanTypeface;

    private RectF mAvailableRectF = new RectF();
    private RectF mUsedRectF = new RectF();

    private float mValueTextSize;
    private float mLabelTextSize;
    private int mLabelTextColor;
    private int mValueTextColor;
    private final float mUsedStrokeWidth;
    private final float mDefaultStrokeWidth;
    private final int mMinSize;

    private float mStartAngle;

    private int mCurrentValue;
    private int mMaxValue;

    private long mAnimationDuration;
    private long mStartTime;

    private String mValueText;
    private String mLabelText;

    public CircleProgressView(Context context) {
        this(context, null);
    }

    public CircleProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mUsedStrokeWidth = ScreenUtil.dp2px(getResources(), 2);
        mDefaultStrokeWidth = ScreenUtil.dp2px(getResources(), 9);
        mMinSize = (int) ScreenUtil.dp2px(getResources(), 100);
        mOpenSanTypeface = Typeface.createFromAsset(context.getAssets(), "font/OpenSans-Regular.ttf");
        final TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircleProgressView, defStyleAttr, 0);
        initAttributes(attributes);
        attributes.recycle();
        initPainter();
        mStartTime = System.currentTimeMillis();
        postInvalidate();
    }

    private void initAttributes(TypedArray attrs) {
        mValueTextColor = attrs.getColor(R.styleable.CircleProgressView_value_text_color, DEFAULT_TEXT_COLOR);
        mLabelTextColor = attrs.getColor(R.styleable.CircleProgressView_label_text_color, DEFAULT_TEXT_COLOR);
        mValueTextSize = attrs.getDimension(R.styleable.CircleProgressView_value_text_size, 28);
        mLabelTextSize = attrs.getDimension(R.styleable.CircleProgressView_label_text_size, 22);
        mValueText = attrs.getString(R.styleable.CircleProgressView_value_text);
        mLabelText = attrs.getString(R.styleable.CircleProgressView_label_text);
        mMaxValue = attrs.getInteger(R.styleable.CircleProgressView_max_value, 1500000);
        mCurrentValue = attrs.getInteger(R.styleable.CircleProgressView_current_value, 0);
        mStartAngle = attrs.getFloat(R.styleable.CircleProgressView_start_angle, 270.0f);
        mAnimationDuration = attrs.getInteger(R.styleable.CircleProgressView_animation_duration, 10000);
    }

    private void initPainter() {
        mValueTextPaint = new TextPaint();
        mValueTextPaint.setColor(mValueTextColor);
        mValueTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mValueTextPaint.setTextSize(mValueTextSize);
        mValueTextPaint.setAntiAlias(true);

        mLabelTextPaint = new TextPaint();
        mLabelTextPaint.setColor(mLabelTextColor);
        mLabelTextPaint.setTypeface(mOpenSanTypeface);
        mLabelTextPaint.setTextSize(mLabelTextSize);
        mLabelTextPaint.setAntiAlias(true);

        RadialGradient availableGradient = new RadialGradient(0, getHeight(), 190, DEFAULT_AVAILABLE_BPOINT_START_GRADIENT_COLOR, DEFAULT_AVAILABLE_BPOINT_END_GRADIENT_COLOR, Shader.TileMode.MIRROR);

        mAvailablePaint = new Paint();
        mAvailablePaint.setStyle(Paint.Style.STROKE);
        mAvailablePaint.setAntiAlias(true);
        mAvailablePaint.setDither(true);
        mAvailablePaint.setShader(availableGradient);
        mAvailablePaint.setStrokeCap(Paint.Cap.ROUND);
        mAvailablePaint.setStrokeWidth(mDefaultStrokeWidth);

        mUsedPaint = new Paint();
        mUsedPaint.setColor(0x55000000);
        mUsedPaint.setStyle(Paint.Style.STROKE);
        mUsedPaint.setAntiAlias(true);
        mUsedPaint.setStrokeCap(Paint.Cap.BUTT);
        mUsedPaint.setStrokeWidth(mUsedStrokeWidth);

        mInnerCirclePaint = new Paint();
        mInnerCirclePaint.setColor(Color.TRANSPARENT);
        mInnerCirclePaint.setAntiAlias(true);
    }

    public void setMaxBPoint(int maxBPoint) {
        mMaxValue = maxBPoint;
        invalidate();
    }


    public void setCurrentBPoint(int currentBPoint) {
        mCurrentValue = currentBPoint;
        invalidate();
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public int getCurrentValue() {
        return mCurrentValue;
    }

    public float getProgressAngle() {
        return getProgress() * 3.60f;
    }

    public float getProgress() {
        return getCurrentValue() * 100f / mMaxValue;
    }

    public float getStartAngle() {
        return mStartAngle;
    }

    private float getBonusAngle() {
        float innerRadius = (getWidth() - mDefaultStrokeWidth) / 2f;
        return mCurrentValue == 0 ? 0 : (float) ((mDefaultStrokeWidth / 2.0f) / (2f * Math.PI * innerRadius) * 360f);
    }

    @Override
    public void invalidate() {
        initPainter();
        super.invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec), measure(heightMeasureSpec));
    }

    private int measure(int measureSpec) {
        int result;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = mMinSize;
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(result, size);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long elapsedTime = System.currentTimeMillis() - mStartTime;
        float delta = Math.max(mDefaultStrokeWidth, mUsedStrokeWidth);
        mAvailableRectF.set(delta, delta, getWidth() - delta, getHeight() - delta);
        mUsedRectF.set(delta, delta, getWidth() - delta, getHeight() - delta);
        float innerCircleRadius = (getWidth() - Math.min(mDefaultStrokeWidth, mUsedStrokeWidth) + Math.abs(mDefaultStrokeWidth - mUsedStrokeWidth)) / 2f;
        canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, innerCircleRadius, mInnerCirclePaint);
        canvas.drawArc(mUsedRectF, getStartAngle() + getProgressAngle() + getBonusAngle(), 360f - getProgressAngle() - 2 * getBonusAngle(), false, mUsedPaint);
        canvas.drawArc(mAvailableRectF, getStartAngle(), getProgressAngle(), false, mAvailablePaint);

        // Draw text value.
        if (mValueText == null) {
            mValueText = String.valueOf(getCurrentValue());
        }
        canvas.drawText(mValueText, (getWidth() - mValueTextPaint.measureText(mValueText)) / 2.0f, getHeight() / 2.0f, mValueTextPaint);

        // Draw label unit text.
        if (!TextUtils.isEmpty(mLabelText)) {
            float bottomTextHeight = mLabelTextPaint.descent() + mLabelTextPaint.ascent();
            canvas.drawText(mLabelText, (getWidth() - mLabelTextPaint.measureText(mLabelText)) / 2.0f, 9 * getHeight() / 16.0f - bottomTextHeight, mLabelTextPaint);
        }
        if (elapsedTime < mAnimationDuration) {
            postInvalidateDelayed(1000 / FRAME_PER_SECOND);
        }
    }
}
