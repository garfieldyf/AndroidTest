package android.ext.focus;

import android.content.Context;
import android.content.res.TypedArray;
import android.ext.util.DebugUtils;
import android.ext.util.Pools.RectPool;
import android.ext.util.ReflectUtils;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.StateSet;
import android.view.View;

/**
 * Class FocusDrawable
 * @author Garfield
 */
public final class FocusDrawable {
    private final Drawable mDrawable;

    /**
     * Constructor
     * @param focus The focus <tt>Drawable</tt>.
     * @see #FocusDrawable(Context, AttributeSet)
     */
    public FocusDrawable(Drawable focus) {
        mDrawable = focus;
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The base set of attribute values.
     * @see #FocusDrawable(Drawable)
     */
    public FocusDrawable(Context context, AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, ReflectUtils.getAttributes(context.getPackageName(), "FocusDrawable"));
        mDrawable = a.getDrawable(0 /* R.styleable.FocusDrawable_focus */);
        DebugUtils.__checkError(mDrawable == null, "Requires a valid 'focus' attribute");
        a.recycle();
    }

    /**
     * Callback method to be invoked when the view's state has been changed.
     */
    public final void drawableStateChanged(View view) {
        if (mDrawable.isStateful() && mDrawable.setState(view.getDrawableState())) {
            view.invalidate();
        }
    }

    /**
     * Draw this focus drawable with the specified the {@link View} state.
     * @param canvas The canvas to draw into.
     * @param view The <tt>View</tt> obtains the current state.
     * @param stateSpec An array of required {@link View} state. If this
     * focus drawable is state full, This parameter will be ignored.
     */
    public final void draw(Canvas canvas, View view, int[] stateSpec) {
        if (mDrawable.isStateful()) {
            final Drawable drawable = mDrawable.getCurrent();
            if (drawable != null) {
                draw(canvas, view, drawable);
            }
        } else if (StateSet.stateSetMatches(stateSpec, view.getDrawableState())) {
            draw(canvas, view, mDrawable);
        }
    }

    private static void draw(Canvas canvas, View view, Drawable drawable) {
        int left = 0, top = 0, right = view.getWidth(), bottom = view.getHeight();
        final Rect padding = RectPool.sInstance.obtain();
        if (drawable.getPadding(padding)) {
            left   -= padding.left;
            top    -= padding.top;
            right  += padding.right;
            bottom += padding.bottom;
        }

        RectPool.sInstance.recycle(padding);
        drawable.setBounds(left, top, right, bottom);
        drawable.draw(canvas);
    }
}
