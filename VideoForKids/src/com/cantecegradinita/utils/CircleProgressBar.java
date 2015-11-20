package com.cantecegradinita.utils;

import com.cantecegradinita.ro.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Leone on 2014/7/10.
 *
 * @author Administrator
 * @version 1.0.0
 * @date 2014/7/10
 */
public class CircleProgressBar extends View {

    /** ç”»Progresså�„ä¸ªç»„ä»¶æ‰€éœ€è¦�çš„ç”»ç¬” **/
    private Paint mPaint;
    /** è¿›åº¦åœ†çŽ¯çš„å®½åº¦ **/
    private float mRingWidth;
    /** è¿›åº¦åœ†çŽ¯çš„ä¸€èˆ¬çŠ¶æ€�é¢œè‰² **/
    private int mRingNormalColor;
    /** è¿›åº¦åœ†çŽ¯çš„è¿›åº¦é¢œè‰² **/
    private int mRingProgressColor;
    /** è¿›åº¦æ–‡å­—çš„å¤§å°� **/
    private float mProgressTextSize;
    /** è¿›åº¦æ–‡å­—çš„é¢œè‰² **/
    private int mProgressTextColor;
    /** è¿›åº¦æ–‡å­—çš„æ˜¾ç¤ºä¸Žå�¦ **/
    private boolean mProgressTextVisibility;
    /** ç»„ä»¶æ˜¾ç¤ºæˆ�å¡«å……ç±»åž‹ **/
    private static final int STYLE_FILL = 1;
    /** ç»„ä»¶æ˜¾ç¤ºæˆ�ç”»ç¬”ç±»åž‹ **/
    private static final int STYLE_STAROKE = 0;
    /** ç»„ä»¶æ‰€éœ€æ˜¾ç¤ºçš„ç±»åž‹ï¼šfillæˆ–è€…stroke **/
    private int mProgressBarStyle;
    /** é»˜è®¤çš„è¿›åº¦å­—ä½“å¤§å°� **/
    private static final int DEFAULT_PROGRESS_TEXT_SIZE = 16;
    /** é»˜è®¤çš„è¿›åº¦åœˆå®½åº¦ **/
    private static final int DEFAULT_RING_CIRCLE_WIDTH = 8;
    /** è¿›åº¦ **/
    private int mProgress;
    /** è®¾ç½®æœ€å¤§è¿›åº¦ **/
    private int mMaxProgress;
    /** é»˜è®¤æœ€å¤§è¿›åº¦ **/
    private static final int DEFAULT_MAX_PROGRESS = 100;
    /** åœ†çš„åº¦æ•° 360åº¦ **/
    private static final int CIRCLE_ANGLE = 360;

    /**
     * CircleProgressBar
     * @param context context
     * @param attrs attrs
     */
    public CircleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * CircleProgressBar
     * @param context context
     * @param attrs attrs
     * @param defStyle defStyle
     */
    public CircleProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        float density = getResources().getDisplayMetrics().density;
        TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar);
        mProgressBarStyle = typeArray.getInt(R.styleable.CircleProgressBar_circleStyle, STYLE_STAROKE);
        mProgressTextColor = typeArray.getColor(R.styleable.CircleProgressBar_progressTextColor, Color.BLACK);
        mProgressTextSize = typeArray.getDimension(R.styleable.CircleProgressBar_progressTextSize, (int) (DEFAULT_PROGRESS_TEXT_SIZE * density + 0.5f));
        mProgressTextVisibility = typeArray.getBoolean(R.styleable.CircleProgressBar_progressTextVisibility, true);
        mRingWidth = typeArray.getDimension(R.styleable.CircleProgressBar_ringWidth, (int) (DEFAULT_RING_CIRCLE_WIDTH * density));
        mRingNormalColor = typeArray.getColor(R.styleable.CircleProgressBar_ringNormalColor, Color.GRAY);
        mRingProgressColor = typeArray.getColor(R.styleable.CircleProgressBar_ringProgressColor, Color.YELLOW);
        mMaxProgress = typeArray.getInt(R.styleable.CircleProgressBar_maxProgress, DEFAULT_MAX_PROGRESS);
        mRingWidth = mProgressBarStyle == STYLE_FILL ? 0 : mRingWidth;
        typeArray.recycle();
        mPaint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // é¦–å…ˆç”»å¤–å›´è¿›åº¦åœˆ
        int center = getWidth() / 2;
        int radius = (int) (center - mRingWidth / 2);
        mPaint.setColor(mRingNormalColor);
        mPaint.setStyle(mProgressBarStyle == STYLE_FILL ? Paint.Style.FILL : Paint.Style.STROKE);
        mPaint.setStrokeWidth(mRingWidth);
        mPaint.setAntiAlias(true);
        canvas.drawCircle(center, center, radius, mPaint);

        // ç”»æ–‡å­—è¿›åº¦ï¼Œè®¾ç½®åˆ°è¿›åº¦åœˆä¸­é—´
        if (mProgressTextVisibility && mProgress >= 0 && mProgressBarStyle != STYLE_FILL) {
            mPaint.setStrokeWidth(0);
            mPaint.setColor(mProgressTextColor);
            mPaint.setTextSize(mProgressTextSize);
            // è®¾ç½®å­—ä½“
            mPaint.setTypeface(Typeface.DEFAULT_BOLD);
            // ä¸­é—´çš„è¿›åº¦ç™¾åˆ†æ¯”ï¼Œå…ˆè½¬æ�¢æˆ�floatåœ¨è¿›è¡Œé™¤æ³•è¿�ç®—ï¼Œä¸�ç„¶éƒ½ä¸º0
            int percent = (int) ((mProgress * 1.0 / mMaxProgress) * 100);
            // æµ‹é‡�å­—ä½“å®½åº¦ï¼Œæˆ‘ä»¬éœ€è¦�æ ¹æ�®å­—ä½“çš„å®½åº¦è®¾ç½®åœ¨åœ†çŽ¯ä¸­é—´
            float textWidth = mPaint.measureText(percent + "%");
            canvas.drawText(percent + "%", center - textWidth / 2, center + mProgressTextSize / 2, mPaint);
        }

        // ç”»åœ†çŽ¯è¿›åº¦åœˆ
        mPaint.setStrokeWidth(mRingWidth);
        mPaint.setColor(mRingProgressColor);
        RectF rectF = new RectF(center - radius, center - radius, center + radius, center + radius);
        switch (mProgressBarStyle) {
            case STYLE_FILL:
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawArc(rectF, -90, CIRCLE_ANGLE * mProgress / mMaxProgress , true, mPaint);
                break;
            case STYLE_STAROKE:
            default:
                mPaint.setStyle(Paint.Style.STROKE);
                canvas.drawArc(rectF, -90, CIRCLE_ANGLE * mProgress / mMaxProgress , false, mPaint);
                break;
        }
    }


    /**
     * è®¾ç½®è¿›åº¦
     * @param progress progress
     */
    public synchronized void setProgress(int progress) {
        if (progress < 0) {
            mProgress = 0;

        } else if (progress > mMaxProgress) {
            mProgress = mMaxProgress;
        } else {
            mProgress = progress;
        }
        postInvalidate();
    }

    /**
     * èŽ·å�–è¿›åº¦
     * @return è¿›åº¦
     */
    public synchronized int getProgress() {
        return mProgress;
    }

    /**
     * èŽ·å�–æœ€å¤§è¿›åº¦
     * @return maxProgress
     */
    public synchronized int getMaxProgress() {
        return mMaxProgress;
    }
}
