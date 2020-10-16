package com.tencent.test;

import android.content.res.TypedArray;
import android.ext.image.ImageModule;
import android.ext.image.decoder.BitmapDecoder;
import android.ext.image.params.Parameters;
import android.ext.renderscript.RenderScriptBlur;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.util.AttributeSet;
import android.util.Log;
import java.io.Closeable;

public class BlurBitmapDecoder extends BitmapDecoder<Bitmap> implements Closeable {
    private RenderScriptBlur mRenderScript;

    public BlurBitmapDecoder(ImageModule module, AttributeSet attrs) {
        super(module);
        mRenderScript = new RenderScriptBlur(module.mContext);
        final TypedArray a = module.mContext.obtainStyledAttributes(attrs, R.styleable.GridImageView);
        int hmargin = a.getDimensionPixelOffset(R.styleable.GridImageView_horizontalMargin, -1);
        int count = a.getInt(R.styleable.GridImageView_android_columnCount, -1);
        int vmargin = a.getDimensionPixelOffset(R.styleable.GridImageView_verticalMargin, -1);
        int id = a.getResourceId(R.styleable.GridImageView_drawables, -1);
        a.recycle();
        Log.d("ImageModule", "hmargin = " + hmargin + ", vmargin = " + vmargin + ", count = " + count + ", id = " + id);
    }

    @Override
    public void close() {
        mRenderScript.close();
    }

    @Override
    protected Bitmap decodeImage(Object uri, Object target, Parameters parameters, int flags, Options opts) throws Exception {
        final Bitmap bitmap = super.decodeImage(uri, target, parameters, flags, opts);
        mRenderScript.blur(bitmap, 10);
        return bitmap;
    }
}
