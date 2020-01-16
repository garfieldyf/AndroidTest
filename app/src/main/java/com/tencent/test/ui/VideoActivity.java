package com.tencent.test.ui;

import android.app.Activity;
import android.ext.graphics.BitmapUtils;
import android.ext.graphics.GIFImage;
import android.ext.graphics.drawable.GIFDrawable;
import android.ext.graphics.drawable.RoundedBitmapDrawable;
import android.ext.text.style.AnimatedImageSpan;
import android.ext.text.style.ImageSpan;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.TextView;
import com.tencent.test.R;

public class VideoActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setSurfaceContent();
        setSpanTextContent();
//        setVideoContent();
    }

    private void setVideoContent() {
        setContentView(R.layout.activity_video);

//        final GridImageView view = (GridImageView)findViewById(R.id.image);
//        final Resources res = getResources();
//        view.addDrawable(res.getDrawable(R.drawable.video));
//        view.addDrawable(res.getDrawable(R.drawable.ticket_exchange));
//        view.addDrawable(res.getDrawable(R.drawable.personal_card));
//        view.addDrawable(new RoundedBitmapDrawable(BitmapFactory.decodeResource(res, R.drawable.image), 10));
//        view.addDrawable(new OvalBitmapDrawable(BitmapFactory.decodeResource(res, R.drawable.ic_image)));
//        view.addDrawable(res.getDrawable(R.drawable.personal));
//        view.addDrawable(res.getDrawable(R.drawable.tool_local));
//        view.addDrawable(res.getDrawable(R.drawable.indicator_area_green));

        //android.R.attr.content
//        final RoundedBitmapDrawable drawable = new RoundedBitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.image), 30);
//        view.setImageDrawable(drawable);
    }

    private void setSpanTextContent() {
        setContentView(R.layout.activity_span_text_view);
        ImageView view = (ImageView)findViewById(R.id.image);
        RoundedBitmapDrawable drawable = new RoundedBitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.image), 40);
        view.setImageDrawable(drawable);

        setGIFText(R.id.face, "欢迎光临！");
    }

    private void setSurfaceContent() {
        setContentView(R.layout.activity_surface_view);
        final SurfaceView view = (SurfaceView)findViewById(R.id.surface_view);
        final SurfaceHolder holder = view.getHolder();
        holder.setFormat(PixelFormat.RGBA_8888);
        holder.setFixedSize(401, 200);

        view.postDelayed(() -> {
            Log.i("abcd", "vw = " + view.getWidth() + ", vh = " + view.getHeight());
//            GIFImage.nativeDraw(holder.getSurface());
        }, 500);

        ImageView imageView = (ImageView)findViewById(R.id.gray_image);
        final Options opts = new Options();
        opts.inMutable = true;
        opts.inPreferredConfig = Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rgb, opts);
        BitmapUtils.grayBitmap(bitmap);
        imageView.setImageBitmap(bitmap);
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

    private void setGIFText(int id, String text) {
        final TextView view = (TextView)findViewById(id);
        SpannableStringBuilder builder = new SpannableStringBuilder("@").append(text);

        AnimatedImageSpan span = AnimatedImageSpan.newGIFImageSpan(view, R.drawable.mood);
        span.setPadding(10, 10);
        builder.setSpan(span, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        view.setText(builder);
    }
}
