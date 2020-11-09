package com.tencent.test;

import android.app.Activity;
import android.ext.graphics.BitmapUtils;
import android.ext.graphics.GIFImage;
import android.ext.graphics.drawable.GIFBaseDrawable;
import android.ext.graphics.drawable.GIFBaseDrawable.AnimationCallback;
import android.ext.graphics.drawable.RoundedGIFDrawable;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class DrawActivity extends Activity implements AnimationCallback, Runnable {
    private ImageView mImageView;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_test);

        final GIFBaseDrawable<?> drawable = new RoundedGIFDrawable(GIFImage.decode(getResources(), R.drawable.bbb), 20);
//        final GIFBaseDrawable<?> drawable = new GIFDrawable(GIFImage.decode(getResources(), R.drawable.bbb));
        drawable.setAnimationCallback(this);
        mImageView = (ImageView)findViewById(R.id.image1);
        mImageView.setImageDrawable(drawable);
        mTextView = (TextView)findViewById(R.id.start);
        //mImageView.postDelayed(this, 100);

        final Options opts = new Options();
        opts.inMutable = true;
        opts.inPreferredConfig = Config.ARGB_8888;

        ((ImageView)findViewById(R.id.image)).setImageResource(R.drawable.video);
        ImageView view = findViewById(R.id.mirror_h);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.video, opts);
        BitmapUtils.mirrorBitmap(bitmap, true);
        view.setImageBitmap(bitmap);

        view = findViewById(R.id.mirror_v);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.video, opts);
        BitmapUtils.mirrorBitmap(bitmap, false);
        view.setImageBitmap(bitmap);
    }

    @Override
    public void run() {
        mImageView.invalidate();
        mImageView.postDelayed(this, 100);
    }

    public void onPlayClicked(View view) {
        final GIFBaseDrawable<?> drawable = (GIFBaseDrawable<?>)mImageView.getDrawable();
        if (drawable.isRunning()) {
            drawable.stop();
            ((TextView)view).setText("Play");
        } else {
            drawable.start();
            ((TextView)view).setText("Stop");
        }
    }

    public void onOneShotClicked(View view) {
        final GIFBaseDrawable<?> drawable = (GIFBaseDrawable<?>)mImageView.getDrawable();
        if (drawable.isOneShot()) {
            drawable.setOneShot(false);
            ((TextView)view).setText("OneShot");
        } else {
            drawable.setOneShot(true);
            ((TextView)view).setText("Repeat");
        }
    }

    @Override
    public void onAnimationStart(GIFBaseDrawable<?> drawable) {
        Log.d("yf", "onAnimationStart");
    }

    @Override
    public void onAnimationEnd(GIFBaseDrawable<?> drawable) {
        mTextView.setText("Play");
    }
}
