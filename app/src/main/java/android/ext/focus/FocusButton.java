package android.ext.focus;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Class <tt>FocusButton</tt>
 * <h3>Usage</h3>
 * <p>Here is a resource example:</p><pre>
 * &lt;xxx.focus.FocusButton
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     xmlns:app="http://schemas.android.com/apk/res-auto"
 *     &lt;!-- android:singleLine="false" must be false --&gt;
 *     app:focus="@drawable/focused_image"
 *     ... ... /&gt;</pre>
 * @author Garfield
 */
public class FocusButton extends Button {
    private final FocusDrawable mDrawable;

    public FocusButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FocusButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDrawable = new FocusDrawable(context, attrs);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        mDrawable.drawableStateChanged(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mDrawable.draw(canvas, this, ENABLED_FOCUSED_STATE_SET);
    }
}
