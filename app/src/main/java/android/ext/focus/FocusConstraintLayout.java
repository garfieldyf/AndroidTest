package android.ext.focus;

import android.content.Context;
import android.graphics.Canvas;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;

/**
 * Class FocusConstraintLayout
 * <h3>Usage</h3>
 * <p>Here is a resource example:</p><pre>
 * &lt;xxx.focus.FocusConstraintLayout
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     xmlns:app="http://schemas.android.com/apk/res-auto"
 *     app:focus="@drawable/focused_image"
 *     ... ... &gt;
 *     ... ...
 * &lt;xxx.focus.FocusConstraintLayout /&gt;</pre>
 * @author Garfield
 */
public class FocusConstraintLayout extends ConstraintLayout {
    private final FocusDrawable mDrawable;

    public FocusConstraintLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FocusConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public FocusConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
