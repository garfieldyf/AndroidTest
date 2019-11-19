package com.tencent.temp;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.util.AttributeSet;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class AnimatorDrawable extends Drawable implements Animatable, Callback {
    private static final int[] ANIMATOR_DRAWABLE_ATTRS = { android.R.attr.drawable, android.R.attr.animation, };

    private static final int FLAG_SCALE_X = 0x01;
    private static final int FLAG_SCALE_Y = 0x02;
    private static final int FLAG_PIVOT_X = 0x04;
    private static final int FLAG_PIVOT_Y = 0x08;
    private static final int FLAG_ROTATION = 0x10;
    private static final int FLAG_TRANSLATION_X = 0x20;
    private static final int FLAG_TRANSLATION_Y = 0x40;
    private static final int FLAG_MASK = FLAG_PIVOT_X | FLAG_PIVOT_Y | FLAG_ROTATION | FLAG_SCALE_X | FLAG_SCALE_Y | FLAG_TRANSLATION_X | FLAG_TRANSLATION_Y;

    private int mFlags;
    private AnimatorState mState;

    public AnimatorDrawable() {
        mState = new AnimatorState(null, null);
    }

    public AnimatorDrawable(Drawable drawable, Animator animator) {
        mState = new AnimatorState(drawable, animator);
        animator.setTarget(this);
        drawable.setCallback(this);
    }

    @SuppressWarnings("deprecation")
    public AnimatorDrawable(Context context, int drawableId, int animatorId) {
        this(context.getResources().getDrawable(drawableId), AnimatorInflater.loadAnimator(context, animatorId));
    }

    public final Drawable getDrawable() {
        return mState.mDrawable;
    }

    public float getRotation() {
        return mState.mDegrees;
    }

    public void setRotation(float degrees) {
        if (mState.mDegrees != degrees) {
            mState.mDegrees = degrees;
            mFlags |= FLAG_ROTATION;
            invalidateSelf();
        }
    }

    public float getScaleX() {
        return mState.mScaleX;
    }

    public void setScaleX(float scaleX) {
        if (mState.mScaleX != scaleX) {
            mState.mScaleX = scaleX;
            mFlags |= FLAG_SCALE_X;
            invalidateSelf();
        }
    }

    public float getScaleY() {
        return mState.mScaleY;
    }

    public void setScaleY(float scaleY) {
        if (mState.mScaleY != scaleY) {
            mState.mScaleY = scaleY;
            mFlags |= FLAG_SCALE_Y;
            invalidateSelf();
        }
    }

    public float getPivotX() {
        return mState.mPivotX;
    }

    public void setPivotX(float pivotX) {
        if (mState.mPivotX != pivotX) {
            mState.mPivotX = pivotX;
            mFlags |= FLAG_PIVOT_X;
            invalidateSelf();
        }
    }

    public float getPivotY() {
        return mState.mPivotY;
    }

    public void setPivotY(float pivotY) {
        if (mState.mPivotY != pivotY) {
            mState.mPivotY = pivotY;
            mFlags |= FLAG_PIVOT_Y;
            invalidateSelf();
        }
    }

    public void setTranslationX(float translationX) {
    }

    public void setTranslationY(float translationY) {
    }

    @Override
    public void setAlpha(int alpha) {
        mState.mDrawable.setAlpha(alpha);
    }

    @Override
    public int getAlpha() {
        return mState.mDrawable.getAlpha();
    }

    @Override
    public void setDither(boolean dither) {
        mState.mDrawable.setDither(dither);
    }

    @Override
    public void setColorFilter(ColorFilter filter) {
        mState.mDrawable.setColorFilter(filter);
    }

    @Override
    public int getOpacity() {
        return mState.mDrawable.getOpacity();
    }

    @Override
    public ConstantState getConstantState() {
        return mState;
    }

    @Override
    public int getIntrinsicWidth() {
        return mState.mDrawable.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return mState.mDrawable.getIntrinsicHeight();
    }

    @Override
    public void start() {
        mState.mAnimator.start();
    }

    @Override
    public void stop() {
        mState.mAnimator.cancel();
    }

    @Override
    public boolean isRunning() {
        return mState.mAnimator.isRunning();
    }

    @Override
    public void invalidateDrawable(Drawable who) {
        final Callback callback = getCallback();
        if (callback != null) {
            callback.invalidateDrawable(this);
        }
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        final Callback callback = getCallback();
        if (callback != null) {
            callback.scheduleDrawable(this, what, when);
        }
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        final Callback callback = getCallback();
        if (callback != null) {
            callback.unscheduleDrawable(this, what);
        }
    }

    @Override
    public boolean isStateful() {
        return mState.mDrawable.isStateful();
    }

    @Override
    public boolean getPadding(Rect padding) {
        return mState.mDrawable.getPadding(padding);
    }

    @Override
    public int getChangingConfigurations() {
        return (super.getChangingConfigurations() | mState.mDrawable.getChangingConfigurations());
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        return super.setVisible(visible, restart);
    }

    @Override
    public void draw(Canvas canvas) {
        if ((mFlags & FLAG_MASK) == 0) {
            mState.mDrawable.draw(canvas);
        } else {
            final Rect bounds = mState.mDrawable.getBounds();
            // bounds.offset(dx, dy)

            final float px = ((mFlags & FLAG_PIVOT_X) != 0 ? mState.mPivotX : bounds.centerX());
            final float py = ((mFlags & FLAG_PIVOT_Y) != 0 ? mState.mPivotY : bounds.centerY());

            final int saveCount = canvas.save();
            if ((mFlags & FLAG_ROTATION) != 0) {
                canvas.rotate(mState.mDegrees, px, py);
            }

            if ((mFlags & (FLAG_SCALE_X | FLAG_SCALE_Y)) != 0) {
                canvas.scale(mState.mScaleX, mState.mScaleY, px, py);
            } else if ((mFlags & FLAG_SCALE_X) != 0) {
                canvas.scale(mState.mScaleX, 1.0f, px, py);
            } else if ((mFlags & FLAG_SCALE_Y) != 0) {
                canvas.scale(1.0f, mState.mScaleY, px, py);
            }

            mFlags &= ~FLAG_MASK;
            mState.mDrawable.draw(canvas);
            canvas.restoreToCount(saveCount);
        }
    }

    @SuppressLint("NewApi")
    public void inflate(Resources res, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        final TypedArray a = res.obtainAttributes(attrs, ANIMATOR_DRAWABLE_ATTRS);
        mState.mDrawable = a.getDrawable(0 /* android.R.attr.drawable */);
        mState.mDrawable.setCallback(this);
        mState.mAnimator = AnimatorInflater.loadAnimator(null, 1 /* android.R.attr.animation */);
        mState.mAnimator.setTarget(this);
        a.recycle();

        //super.inflate(res, parser, attrs, theme);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        mState.mDrawable.setBounds(bounds);
    }

    @Override
    protected boolean onStateChange(int[] state) {
        return mState.mDrawable.setState(state);
    }

    @Override
    protected boolean onLevelChange(int level) {
        return mState.mDrawable.setLevel(level);
    }

    public static class AnimatorState extends ConstantState {
        protected float mPivotX;
        protected float mPivotY;
        protected float mScaleX;
        protected float mScaleY;
        protected float mDegrees;
        protected Drawable mDrawable;
        protected Animator mAnimator;

        public AnimatorState(Drawable drawable, Animator animator) {
            mDrawable = drawable;
            mAnimator = animator;
        }

        public AnimatorState(AnimatorState state) {
            mAnimator = state.mAnimator.clone();
            mDrawable = state.mDrawable.getConstantState().newDrawable();
        }

        @Override
        public Drawable newDrawable() {
            return null;
        }

        @Override
        public int getChangingConfigurations() {
            return 0;
        }
    }
}
