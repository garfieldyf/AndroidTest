package com.tencent.test.ui;

import android.app.Activity;
import android.ext.graphics.drawable.RoundedBitmapDrawable;
import android.ext.text.style.ImageSpan;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.widget.ImageView;
import android.widget.TextView;
import com.tencent.test.R;

public class VideoActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setSpanTextContent();
        setVideoContent();
    }

    private void setVideoContent() {
        setContentView(R.layout.activity_video);

        final ImageView view = (ImageView)findViewById(R.id.image);
        final RoundedBitmapDrawable drawable = new RoundedBitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.image), 30);
        view.setImageDrawable(drawable);
    }

    private void setSpanTextContent() {
        setContentView(R.layout.activity_span_text_view);
        String s = "中国，你好";
        setText(R.id.text1, s);
        setText(R.id.text2, s + s + s + s + s + s);
        setText(R.id.text3, s + s + s + s + s + s);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setText(int id, String text) {
        final TextView view = (TextView)findViewById(id);
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        int start = builder.length();
        builder.append('@');
        ImageSpan span = new ImageSpan(getResources(), R.drawable.playing_7);
        builder.setSpan(span, start, builder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        view.setText(builder);
    }
}
