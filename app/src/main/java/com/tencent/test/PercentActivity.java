package com.tencent.test;

import java.util.Locale;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.ext.graphics.BitmapUtils;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

public class PercentActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_precent);

        final Options opts = new Options();
        opts.inMutable = true;
        opts.inSampleSize = 16;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.device, opts);
        BitmapUtils.dumpBitmap(this, "yf", bitmap);

        DebugUtils.startMethodTracing();
        BitmapUtils.blurBitmap(bitmap, 10);
        DebugUtils.stopMethodTracing("yf", "blur");

        final FrameLayout contentView = new FrameLayout(this);
        contentView.setBackground(new BitmapDrawable(getResources(), bitmap));
        setContentView(contentView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("yf", "onDestroy");
    }

    public void onLanguageChanged(View view) {
        if (Locale.getDefault() == Locale.SIMPLIFIED_CHINESE) {
            update(Locale.US);
        } else {
            update(Locale.SIMPLIFIED_CHINESE);
        }
    }

    private void update(Locale locale) {
        final Resources res = getResources();
        final Configuration config = res.getConfiguration();
        config.setLocale(locale);
        Locale.setDefault(locale);
        res.updateConfiguration(config, res.getDisplayMetrics());

        final Intent intent = new Intent(this, PercentActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
//        recreate();
    }
}
