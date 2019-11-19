package com.tencent.test;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import com.tencent.temp.AnimatorDrawable;

public class WaveActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(200, 200);
        // params.gravity = Gravity.CENTER;
        // WaterWaveView view = new WaterWaveView(this);
        // setContentView(view, params);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        setContentView(new ClipView(this));

//        mDrawable = new AnimatorDrawable(this, R.drawable.onkeyopt_scanning, R.animator.rotate);
//        mDrawable.start();
//
//        final ImageView view = new ImageView(this);
//        view.setScaleType(ScaleType.CENTER_INSIDE);
//        view.setImageDrawable(mDrawable);
//        setContentView(view);

        setContentView(new AnimatorView(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mDrawable.stop();
    }

//    AnimatorDrawable mDrawable;

    public static class AnimatorView extends View {
        AnimatorDrawable mDrawable;

        public AnimatorView(Context context) {
            super(context);
            mDrawable = new AnimatorDrawable(context, R.drawable.onkeyopt_scanning, R.animator.rotate);
            mDrawable.setCallback(this);
            mDrawable.start();
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            mDrawable.stop();
        }

        @Override
        protected boolean verifyDrawable(Drawable who) {
            return (who == mDrawable || super.verifyDrawable(who));
        }

        @Override
        public void draw(Canvas canvas) {
            final int width = getWidth();
            final int height = getHeight();
            final int w = mDrawable.getIntrinsicWidth();
            final int h = mDrawable.getIntrinsicHeight();

            mDrawable.setBounds((width - w) / 2, (height - h) / 2, (width + w) / 2, (height + h) / 2);
            mDrawable.draw(canvas);
        }
    }

    public static class ClipView extends View {
        private Paint mPaint;
        private Rect mBounds;

        public ClipView(Context context) {
            super(context);

            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(Color.RED);
            mBounds = new Rect(786, 216, 1434, 864);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawRect(mBounds, mPaint);
        }
    }

    /**
     * 水波进度效果.
     */
    public static class WaterWaveView extends View {
        private static final float RADIUS = 0.05f;
        private static final float MAX_VALUE = 200;

        /**
         * 进度条最大值和当前进度值
         */
        private float mValue;

        private Paint mPaint;
        private float mRadius;
        private float mOffsetX;
        private int mCircleX, mCircleY;

        private Path mPath;
        private RectF mBounds;

        // 每一个像素对应的弧度数

        public WaterWaveView(Context context) {
            super(context);
            init();
        }

        public WaterWaveView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public WaterWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        // 初始化
        private void init() {
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(Color.RED);
            mPath = new Path();
        }

        @Override
        @SuppressLint("DrawAllocation")
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            final int width = getWidth(), height = getHeight();
            // 计算圆弧半径和圆心点
            int circleRadius = Math.min(width, height) >> 1;

            mCircleX = width / 2;
            mCircleY = height / 2;

            mRadius = circleRadius;
            mBounds = new RectF(mCircleX - mRadius, mCircleY - mRadius, mCircleX + mRadius, mCircleY + mRadius);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // 绘制水波曲线
            mPath.rewind();
            computePath();
            canvas.drawPath(mPath, mPaint);

            mOffsetX += (mRadius / 16.0f);
            mValue = (mValue + 1) % MAX_VALUE;
            postInvalidate();
        }

        private void computePath() {
            float x, y, distance, startX = 0, startY = 0, endX = 0, endY = 0;
            for (int i = 0, end = (int)mRadius * 2; i <= end; i += 2) {
                x = mCircleX - mRadius + i;
                y = (float)(mCircleY + mRadius * (1.0f + RADIUS) * 2 * (0.5f - mValue / MAX_VALUE) + mRadius * RADIUS * Math.sin((mOffsetX + i) * (Math.PI / mRadius)));

                distance = (float)Math.sqrt(Math.pow(x - mCircleX, 2) + Math.pow(y - mCircleY, 2));
                if (distance > mRadius) {
                    if (x < mCircleX) {
                        continue;
                    } else {
                        break;
                    }
                }

                if (mPath.isEmpty()) {
                    startX = x;
                    startY = y;
                    mPath.moveTo(x, y);
                } else {
                    mPath.lineTo(x, y);
                }

                endX = x;
                endY = y;
            }

            if (mPath.isEmpty()) {
                if (mValue / MAX_VALUE >= 0.5f) {
                    mPath.moveTo(mCircleX, mCircleY - mRadius);
                    mPath.addCircle(mCircleX, mCircleY, mRadius, Path.Direction.CW);
                }
            } else {
                final float degree = computeDegree(endX, endY);
                mPath.arcTo(mBounds, degree - 360, computeDegree(startX, startY) - (degree - 360));
            }
        }

        private float computeDegree(float x, float y) {
            float result = (float)(Math.atan(1.0f * (mCircleX - x) / (y - mCircleY)) / Math.PI * 180);
            if (y < mCircleY) {
                result += 180;
            } else if (y > mCircleY && x > mCircleX) {
                result += 360;
            }

            return result + 90;
        }

        // 直接设置进度值（同步）
        public void setProgressSync(float progress) {
            this.mValue = progress;
            invalidate();
        }
    }
}
