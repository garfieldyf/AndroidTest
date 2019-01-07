package android.ext.graphics.drawable;

import android.content.Context;
import android.content.res.TypedArray;
import android.ext.graphics.DrawUtils;
import android.ext.util.ClassUtils;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Class FrameDrawable
 * @author Garfield
 */
public class FrameDrawable {
    private Drawable mFrame;

    /**
     * Constructor
     * @see #FrameDrawable(Drawable)
     * @see #FrameDrawable(Context, AttributeSet)
     */
    public FrameDrawable() {
    }

    /**
     * Constructor
     * @param frame The <tt>Drawable</tt>.
     * @see #FrameDrawable()
     * @see #FrameDrawable(Context, AttributeSet)
     */
    public FrameDrawable(Drawable frame) {
        mFrame = frame;
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The base set of attribute values.
     * @see #FrameDrawable()
     * @see #FrameDrawable(Drawable)
     */
    public FrameDrawable(Context context, AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, (int[])ClassUtils.getAttributeValue(context, "FrameDrawable"));
        mFrame = a.getDrawable(0 /* R.styleable.FrameDrawable_frame */);
        a.recycle();
    }

    /**
     * Returns the frame image associated with this object.
     * @return The <tt>Drawable</tt> or <tt>null</tt>.
     * @see #setFrame(Drawable)
     */
    public Drawable getFrame() {
        return mFrame;
    }

    /**
     * Sets the frame image to this drawable.
     * @param frame The <tt>Drawable</tt>.
     * @see #getFrame()
     */
    public void setFrame(Drawable frame) {
        mFrame = frame;
    }

    /**
     * Draw this frame drawable with the specified the {@link View} states.
     * @param canvas The canvas to draw into.
     * @param view The <tt>View</tt> obtains the current states.
     * @param stateSpec An array of required {@link View} states. If this
     * frame drawable is state full, this parameter will be ignored.
     * @see #draw(Canvas, int[], int[], int, int, int, int)
     */
    public void draw(Canvas canvas, View view, int[] stateSpec) {
        if (mFrame != null) {
            DrawUtils.drawDrawable(canvas, mFrame, view.getDrawableState(), stateSpec, 0, 0, view.getWidth(), view.getHeight());
        }
    }

    /**
     * Draw this frame drawable with the specified the <tt>View</tt> states.
     * @param canvas The canvas to draw into.
     * @param stateSet An array of the <tt>View</tt> current states.
     * @param stateSpec An array of required <tt>View</tt> states. If this frame drawable is
     * state full, this parameter will be ignored.
     * @param left The specified a bounding rectangle left coordinate for this frame drawable.
     * @param top The specified a bounding rectangle top coordinate for this frame drawable.
     * @param right The specified a bounding rectangle right coordinate for this frame drawable.
     * @param bottom The specified a bounding rectangle bottom coordinate for this frame drawable.
     * @see #draw(Canvas, View, int[])
     */
    public void draw(Canvas canvas, int[] stateSet, int[] stateSpec, int left, int top, int right, int bottom) {
        if (mFrame != null) {
            DrawUtils.drawDrawable(canvas, mFrame, stateSet, stateSpec, left, top, right, bottom);
        }
    }
}
