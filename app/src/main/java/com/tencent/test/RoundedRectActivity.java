package com.tencent.test;

import android.app.Activity;
import android.ext.graphics.BitmapUtils;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.widget.ImageView;

public class RoundedRectActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        testColorMatrix();
    }

    private void testRoundedRect() {
        setContentView(R.layout.acitivity_rounded_rect);
    }

    private void testRotate() {
        setContentView(R.layout.activity_rotate);
        //findViewById(R.id.loading_view).setBackground(new AnimatedRotateDrawable(getResources(), R.drawable.spinner_black_76));
    }

    private void testColorMatrix() {
        setContentView(R.layout.activity_color_matrix);

        setViewImage(R.id.gray_rgb_view, R.drawable.medal_five);
        setViewImage(R.id.gray_argb_view, R.drawable.personal_card);
    }

    private void setViewImage(int viewId, int resId) {
        final Options opts = new Options();
        opts.inPreferredConfig = Config.ARGB_8888;
        opts.inMutable = true;

        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId, opts);
        DebugUtils.startMethodTracing();
        BitmapUtils.grayBitmap(bitmap);
        DebugUtils.stopMethodTracing("yf", "grayBitmap");

        final ImageView view = (ImageView)findViewById(viewId);
        view.setImageBitmap(bitmap);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
