package com.tencent.test;

import android.ext.image.ImageModule;
import android.ext.image.decoder.BitmapDecoder;
import android.ext.image.params.Parameters;
import android.ext.renderscript.RenderScriptBlur;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import java.io.Closeable;

public class BlurBitmapDecoder extends BitmapDecoder<Bitmap> implements Closeable {
    private RenderScriptBlur mRenderScript;

    public BlurBitmapDecoder(ImageModule module) {
        super(module);
        mRenderScript = new RenderScriptBlur(module.mContext);
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
