package android.ext.focus;

import android.content.Context;
import android.ext.graphics.drawable.FrameDrawable;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Class FocusRelativeLayout
 * @author Garfield
 * @version 1.0
 */
public class FocusRelativeLayout extends RelativeLayout {
    private final FrameDrawable mDrawable;

    public FocusRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FocusRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDrawable = new FrameDrawable(context, attrs);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        mDrawable.draw(canvas, this, ENABLED_FOCUSED_STATE_SET);
    }
}
