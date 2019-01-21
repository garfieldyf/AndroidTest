package android.ext.focus;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Class FocusRelativeLayout
 * @author Garfield
 */
public class FocusRelativeLayout extends RelativeLayout {
    private final FocusDrawable mDrawable;

    public FocusRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FocusRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDrawable = new FocusDrawable(context, attrs);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        mDrawable.draw(canvas, this, ENABLED_FOCUSED_STATE_SET);
    }
}
