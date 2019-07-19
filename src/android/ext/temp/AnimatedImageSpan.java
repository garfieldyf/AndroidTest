package android.ext.temp;

import java.lang.ref.WeakReference;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;

public final class AnimatedImageSpan extends DrawableSpan {
    private int mFrameIndex;
    private final AnimationDrawable mDrawable;
    private final WeakReference<View> mViewRef;

    @SuppressWarnings("deprecation")
    public AnimatedImageSpan(View view, int id) {
        mViewRef  = new WeakReference<View>(view);
        mDrawable = (AnimationDrawable)view.getResources().getDrawable(id);
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        draw(canvas, mDrawable.getFrame(mFrameIndex), (int)x, top, bottom);
        final View view = mViewRef.get();
        if (view != null && view.getParent() != null) {
            view.postInvalidateDelayed(mDrawable.getDuration(mFrameIndex));
            mFrameIndex = (mFrameIndex + 1) % mDrawable.getNumberOfFrames();
        }
    }
}
