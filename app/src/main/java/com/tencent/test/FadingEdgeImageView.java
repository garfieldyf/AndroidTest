package com.tencent.test;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class FadingEdgeImageView extends ImageView {

    public FadingEdgeImageView(Context context) {
        super(context);
    }

    public FadingEdgeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FadingEdgeImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected float getTopFadingEdgeStrength() {
        return 1.0f;
    }

    @Override
    protected float getBottomFadingEdgeStrength() {
        return 1.0f;
    }

    @Override
    protected float getLeftFadingEdgeStrength() {
        return 1.0f;
    }

    @Override
    protected float getRightFadingEdgeStrength() {
        return 1.0f;
    }
}
