package com.tencent.test;

import android.app.Activity;
import android.ext.graphics.BitmapUtils;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.os.Debug;
import android.view.View;
import android.widget.ImageView;

public class ThemeActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);

        ImageView src = (ImageView)findViewById(R.id.image_src);
        ImageView bin = (ImageView)findViewById(R.id.image_bin);

        final Options opts = new Options();
        opts.inMutable = true;
        opts.inPreferredConfig = Config.ARGB_8888;
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.abc, opts);
        src.setImageBitmap(bitmap);

//        Bitmap b = BitmapUtils.convert(bitmap);
//        BitmapUtils.dumpBitmap(this, "yf", b);
//        bin.setImageBitmap(b);
    }

    public void onBlackClicked(View view) {
    }

    public void onWhiteClicked(View view) {
    }
}
