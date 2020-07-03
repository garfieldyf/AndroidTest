package com.tencent.test;

import android.ext.image.params.Parameters;
import java.io.Closeable;
import android.content.Context;
import android.ext.cache.BitmapPool;
import android.ext.image.decoder.BitmapDecoder;
import android.ext.renderscript.RenderScriptBlur;
import android.ext.util.Pools.Pool;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;

public class BlurBitmapDecoder extends BitmapDecoder<Bitmap> implements Closeable {
    private RenderScriptBlur mRenderScript;

    public BlurBitmapDecoder(Context context, Pool<Options> optionsPool, BitmapPool bitmapPool) {
        super(context, optionsPool, bitmapPool);
        mRenderScript = new RenderScriptBlur(mContext);
    }

    @Override
    public void close() {
        mRenderScript.close();
    }

//    @Override
//    protected Bitmap decodeImage(Object uri, LoadParams<Object> params, Options opts) throws Exception {
//        final Bitmap bitmap = super.decodeImage(uri, params, opts);
//        mRenderScript.blur(bitmap, 10);
//        return bitmap;
//    }

    @Override
    protected Bitmap decodeImage(Object uri, Object target, Parameters parameters, int flags, Options opts) throws Exception {
        final Bitmap bitmap = super.decodeImage(uri, target, parameters, flags, opts);
        mRenderScript.blur(bitmap, 10);
        return bitmap;
    }
}
