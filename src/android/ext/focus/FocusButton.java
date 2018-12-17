package android.ext.focus;

import android.content.Context;
import android.ext.graphics.drawable.FrameDrawable;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Class FocusButton
 * @author Garfield
 */
public class FocusButton extends Button {
    private final FrameDrawable mDrawable;

    public FocusButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FocusButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDrawable = new FrameDrawable(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mDrawable.draw(canvas, this, ENABLED_FOCUSED_STATE_SET);
    }
}
