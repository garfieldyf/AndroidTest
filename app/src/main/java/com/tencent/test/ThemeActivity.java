package com.tencent.test;

import android.app.Activity;
import android.content.res.Resources;
import android.ext.graphics.drawable.RingBitmapDrawable;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class ThemeActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);

        final Resources res = getResources();
        ImageView src = (ImageView)findViewById(R.id.image_src);
        ImageView bin = (ImageView)findViewById(R.id.image_bin);

//        final Drawable drawable = new BitmapDrawable(res, BitmapFactory.decodeResource(res, R.drawable.personal_card));
//        src.setImageDrawable(drawable);
//        bin.setImageDrawable(drawable);

        final float innerRadius = 30;
        src.setImageDrawable(new RingBitmapDrawable(BitmapFactory.decodeResource(res, R.drawable.personal_card), innerRadius));
        bin.setImageDrawable(new RingBitmapDrawable(BitmapFactory.decodeResource(res, R.drawable.personal_card), innerRadius));
    }

    public void onBlackClicked(View view) {
    }

    public void onWhiteClicked(View view) {
    }
}
