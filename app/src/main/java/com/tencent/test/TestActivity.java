package com.tencent.test;

import android.app.Activity;
import android.ext.graphics.drawable.InvertedBitmapDrawable;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

public class TestActivity extends Activity {
    private ImageView mImageLeft;
    private ImageView mImageTop;
    private ImageView mImageRight;
    private ImageView mImageBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reflected);

        mImageLeft = (ImageView)findViewById(R.id.image_left);
        mImageTop = (ImageView)findViewById(R.id.image_top);
        mImageRight = (ImageView)findViewById(R.id.image_right);
        mImageBottom = (ImageView)findViewById(R.id.image_bottom);

        final ImageView center = (ImageView)findViewById(R.id.image_center);
        final Bitmap bitmap = ((BitmapDrawable)center.getDrawable()).getBitmap();
        final View view = findViewById(R.id.image_frame);

        center.post(new Runnable() {
            @Override
            public void run() {
                final int alpha = 225;
                final float percent = 0.9f;
                mImageLeft.setImageDrawable(new InvertedBitmapDrawable(center, alpha, percent, Gravity.LEFT));
                mImageTop.setImageDrawable(new InvertedBitmapDrawable(center, alpha, percent, Gravity.TOP));
                mImageRight.setImageDrawable(new InvertedBitmapDrawable(center, alpha, percent, Gravity.RIGHT));
                mImageBottom.setImageDrawable(new InvertedBitmapDrawable(center, alpha, percent, Gravity.BOTTOM));
            }
        });

//        final View view = findViewById(R.id.image_frame);
//        view.post(new Runnable() {
//            @Override
//            public void run() {
//                mImageBottom.setImageDrawable(new InvertedBitmapDrawable(view, alpha, percent, Gravity.BOTTOM));
//            }
//        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            //final ImageView center = (ImageView)findViewById(R.id.image_center);
            //center.setImageResource(R.drawable.medal_five);
            reset(mImageLeft);
            reset(mImageTop);
            reset(mImageRight);
            reset(mImageBottom);
        }

        return super.onKeyDown(keyCode, event);
    }

    private void reset(ImageView imageView) {
        final View view = findViewById(R.id.image_frame);
        ((InvertedBitmapDrawable)imageView.getDrawable()).refresh(view);
    }
}
