package android.ext.focus;

import android.content.Context;
import android.content.res.TypedArray;
import android.ext.graphics.DrawUtils;
import android.ext.util.ClassUtils;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Class FocusDrawable
 * @author Garfield
 */
public class FocusDrawable {
    private final Drawable mFocusDrawable;

    /**
     * Constructor
     * @param focus The <tt>Drawable</tt>.
     * @see #FocusDrawable(Context, AttributeSet)
     */
    public FocusDrawable(Drawable focus) {
        mFocusDrawable = focus;
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The base set of attribute values.
     * @see #FocusDrawable(Drawable)
     */
    public FocusDrawable(Context context, AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, (int[])ClassUtils.getAttributeValue(context, "FocusDrawable"));
        mFocusDrawable = a.getDrawable(0 /* R.styleable.FocusDrawable_foucs */);
        a.recycle();
    }

    /**
     * Draw this focus drawable with the specified the {@link View} states.
     * @param canvas The canvas to draw into.
     * @param view The <tt>View</tt> obtains the current states.
     * @param stateSpec An array of required {@link View} states. If this
     * frame drawable is state full, this parameter will be ignored.
     */
    public void draw(Canvas canvas, View view, int[] stateSpec) {
        DrawUtils.drawFrameDrawable(canvas, mFocusDrawable, view, stateSpec);
    }
}
