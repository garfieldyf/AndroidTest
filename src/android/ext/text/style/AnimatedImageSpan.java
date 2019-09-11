package android.ext.text.style;

import java.lang.ref.WeakReference;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;

/**
 * Class AnimatedImageSpan
 * @author Garfield
 */
public class AnimatedImageSpan extends ImageSpan implements Runnable {
    private int mFrameIndex;
    private boolean mScheduleNext;
    private final WeakReference<View> mViewRef;

    /**
     * Constructor
     * @param view The {@link View}.
     * @param id The resource id of the {@link AnimationDrawable}.
     * @see #AnimatedImageSpan(View, AnimationDrawable)
     */
    @SuppressWarnings("deprecation")
    public AnimatedImageSpan(View view, int id) {
        this(view, (AnimationDrawable)view.getResources().getDrawable(id));
    }

    /**
     * Constructor
     * @param view The {@link View}.
     * @param drawable The {@link AnimationDrawable}.
     * @see #AnimatedImageSpan(View, int)
     */
    public AnimatedImageSpan(View view, AnimationDrawable drawable) {
        super(drawable);
        mViewRef = new WeakReference<View>(view);
    }

    @Override
    public void run() {
        final View view = getView();
        if (view != null) {
            mFrameIndex = (mFrameIndex + 1) % ((AnimationDrawable)mDrawable).getNumberOfFrames();
            mScheduleNext = false;
            view.invalidate();
        }
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        final AnimationDrawable drawable = (AnimationDrawable)mDrawable;
        draw(canvas, drawable.getFrame(mFrameIndex), (int)x, top, bottom);
        final View view = getView();
        if (view != null && !mScheduleNext) {
            mScheduleNext = true;
            view.postDelayed(this, drawable.getDuration(mFrameIndex));
        }
    }

    private View getView() {
        final View view = mViewRef.get();
        return (view != null && view.getParent() != null ? view : null);
    }
}
