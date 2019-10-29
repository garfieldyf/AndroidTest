package com.tencent.test;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class FadingEdgeView extends FrameLayout {

    public FadingEdgeView(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    public FadingEdgeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    public FadingEdgeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);
    }

    @Override
    protected float getLeftFadingEdgeStrength() {
        return 1.0f;
    }

    @Override
    protected float getRightFadingEdgeStrength() {
        return 1.0f;
    }

    @Override
    protected float getTopFadingEdgeStrength() {
        return 1.0f;
    }

    @Override
    protected float getBottomFadingEdgeStrength() {
        return 1.0f;
    }
}
