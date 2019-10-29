package com.tencent.test.ui;

import android.app.Activity;
import android.ext.graphics.drawable.RoundedBitmapDrawable;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import com.tencent.test.R;

public class WebViewActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        RoundedBitmapDrawable drawable = new RoundedBitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.personal_card));
        drawable.setCornerRadii(30);
        ((ImageView)findViewById(R.id.image)).setImageDrawable(drawable);
    }
}
