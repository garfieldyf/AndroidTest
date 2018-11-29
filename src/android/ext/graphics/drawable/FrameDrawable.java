package android.ext.graphics.drawable;

import android.content.Context;
import android.content.res.TypedArray;
import android.ext.graphics.DrawUtils;
import android.ext.util.DebugUtils;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Class FrameDrawable
 * @author Garfield
 */
public class FrameDrawable {
    private static int[] FRAME_DRAWABLE_ATTRS;
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
        DebugUtils.__checkError(FRAME_DRAWABLE_ATTRS == null, "The " + getClass().getName() + " did not call FrameDrawable.initAttrs()");
        final TypedArray a = context.obtainStyledAttributes(attrs, FRAME_DRAWABLE_ATTRS);
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
     * @param stateSpec An array of required {@link View} states. If the
     * <em>drawable</em> is state full, this parameter will be ignored.
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
     * @param stateSpec An array of required <tt>View</tt> states. If the <em>drawable</em> is
     * state full, this parameter will be ignored.
     * @param left The specified a bounding rectangle left coordinate for the <em>drawable</em>.
     * @param top The specified a bounding rectangle top coordinate for the <em>drawable</em>.
     * @param right The specified a bounding rectangle right coordinate for the <em>drawable</em>.
     * @param bottom The specified a bounding rectangle bottom coordinate for the <em>drawable</em>.
     * @see #draw(Canvas, View, int[])
     */
    public void draw(Canvas canvas, int[] stateSet, int[] stateSpec, int left, int top, int right, int bottom) {
        if (mFrame != null) {
            DrawUtils.drawDrawable(canvas, mFrame, stateSet, stateSpec, left, top, right, bottom);
        }
    }

    /**
     * Initialize the {@link FrameDrawable} styleable. <p>Note: This method recommended call in the
     * <tt>Application's</tt> static constructor.</p>
     * <p>Includes the following attributes:
     * <table><colgroup align="left" /><colgroup align="left" /><colgroup align="center" />
     * <tr><th>Attribute</th><th>Type</th><th>Index</th></tr>
     * <tr><td><tt>frame</tt></td><td>reference</td><td>0</td></tr>
     * </table></p>
     * @param attrs The <tt>R.styleable.FrameDrawable</tt> styleable, as generated by the aapt tool.
     */
    public static void initAttrs(int[] attrs) {
        FRAME_DRAWABLE_ATTRS = attrs;
    }
}
