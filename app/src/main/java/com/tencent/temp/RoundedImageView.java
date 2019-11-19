package com.tencent.temp;

import android.content.Context;
import android.ext.content.res.XmlResources;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.tencent.temp.ShapeLayer.RoundedRectLayer;

public class RoundedImageView extends ImageView {
    private final ShapeLayer mLayer;

    public RoundedImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLayer = new RoundedRectLayer(XmlResources.loadCornerRadii(context.getResources(), attrs));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int saveCount = mLayer.save(canvas, this);
        super.onDraw(canvas);
        mLayer.restore(canvas, saveCount);
    }
}
