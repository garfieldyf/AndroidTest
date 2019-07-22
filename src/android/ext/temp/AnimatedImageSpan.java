package android.ext.temp;

import java.lang.ref.WeakReference;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;

public final class AnimatedImageSpan extends DrawableSpan implements Runnable {
    private int mFrameIndex;
    private boolean mScheduleNext;
    private final AnimationDrawable mDrawable;
    private final WeakReference<View> mViewRef;

    @SuppressWarnings("deprecation")
    public AnimatedImageSpan(View view, int id) {
        mViewRef  = new WeakReference<View>(view);
        mDrawable = (AnimationDrawable)view.getResources().getDrawable(id);
    }

    @Override
    public void run() {
        final View view = getView();
        if (view != null) {
            mFrameIndex = (mFrameIndex + 1) % mDrawable.getNumberOfFrames();
            mScheduleNext = false;
            view.invalidate();
        }
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        draw(canvas, mDrawable.getFrame(mFrameIndex), (int)x, top, bottom);
        final View view = getView();
        if (view != null && !mScheduleNext) {
            mScheduleNext = true;
            view.postDelayed(this, mDrawable.getDuration(mFrameIndex));
        }
    }

    private View getView() {
        final View view = mViewRef.get();
        return (view != null && view.getParent() != null ? view : null);
    }
}
