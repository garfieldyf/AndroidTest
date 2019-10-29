package com.tencent.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.View;

public class CustomView extends View {
    private Paint mPaint;
    private Bitmap mBitmap;

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // setLayerType(LAYER_TYPE_SOFTWARE, null);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

        mBitmap = Bitmap.createBitmap(900, 200, Config.ARGB_8888);
        final Canvas canvas = new Canvas(mBitmap);
        canvas.drawColor(Color.BLUE);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        paint.setShader(new LinearGradient(0, 50, 0, 200, Color.argb(255, 255, 255, 255), Color.TRANSPARENT, TileMode.CLAMP));
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
        canvas.drawRect(0, 0, 900, 200, paint);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, null);
        // DrawUtils.drawReflectedBitmap(canvas, mBitmap, 128, 0.8f,
        // DrawUtils.BOTTOM, mPaint);
    }
}
