package android.ext.focus;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Class FocusLinearLayout
 * <h3>Usage</h3>
 * <p>Here is a resource example:</p><pre>
 * &lt;xxx.focus.FocusLinearLayout
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     xmlns:app="http://schemas.android.com/apk/res-auto"
 *     app:focus="@drawable/focused_image"
 *     ... ... &gt;
 *     ... ...
 * &lt;xxx.focus.FocusLinearLayout /&gt;</pre>
 * @author Garfield
 */
public class FocusLinearLayout extends LinearLayout {
    private final FocusDrawable mDrawable;

    public FocusLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FocusLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDrawable = new FocusDrawable(context, attrs);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        mDrawable.drawableStateChanged(this);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        mDrawable.draw(canvas, this, ENABLED_FOCUSED_STATE_SET);
    }
}
