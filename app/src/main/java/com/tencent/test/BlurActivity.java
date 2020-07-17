package com.tencent.test;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.ext.graphics.BitmapUtils;
import android.ext.graphics.drawable.RoundedBitmapDrawable;
import android.ext.renderscript.RenderScriptBlur;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import com.tencent.temp.AsyncImageTask;

public class BlurActivity extends Activity implements OnClickListener {
    private ImageView mImageView;
    private RenderScriptBlur mBlur;
    private RoundedBitmapDrawable mDrawable;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blur);

        mBlur = new RenderScriptBlur(this);
        final int radius = 20;
//        final Printer printer = new LogPrinter(Log.DEBUG, "yf");
        final Options opts = new Options();
        opts.inMutable = true;
        opts.inSampleSize = 2;
        opts.inPreferredConfig = Config.ARGB_8888;
        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.abc, opts);
        Bitmap bitmap2 = bitmap1.copy(bitmap1.getConfig(), true);
        Bitmap bitmap3 = bitmap1.copy(bitmap1.getConfig(), true);

        DebugUtils.startMethodTracing();
        bitmap1 = FastBlur.doBlur(bitmap1, radius, true);
        DebugUtils.stopMethodTracing("yf", "FastBlur.doBlur");
        ((ImageView)findViewById(R.id.imageView1)).setImageBitmap(bitmap1);

        DebugUtils.startMethodTracing();
        BitmapUtils.blurBitmap(bitmap2, radius);
        DebugUtils.stopMethodTracing("yf", "BitmapUtils.blurBitmap");
        ((ImageView)findViewById(R.id.imageView2)).setImageBitmap(bitmap2);

        DebugUtils.startMethodTracing();
        mBlur.blur(bitmap3, radius);
        DebugUtils.stopMethodTracing("yf", "RenderScript blurBitmap");
        ((ImageView)findViewById(R.id.imageView3)).setImageBitmap(bitmap3);

        mImageView = (ImageView)findViewById(R.id.imageView4);
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.abc);
        Bitmap bmp = BitmapUtils.createScaledBitmap(bitmap, 0.5f, 0.5f, Config.RGB_565, null);
        mImageView.setImageBitmap(bmp);
        //((ImageView)findViewById(R.id.imageView1)).setImageBitmap(bitmap);

//        mImageView = (ImageView)findViewById(R.id.imageView4);
//        mDrawable = new RoundedBitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.ticket_exchange));
//        mDrawable.setCornerRadius(20);
//        mImageView.setImageDrawable(mDrawable);

        findViewById(R.id.btn_fill).setOnClickListener(this);
        findViewById(R.id.btn_left).setOnClickListener(this);
        findViewById(R.id.btn_right).setOnClickListener(this);
        findViewById(R.id.btn_center).setOnClickListener(this);

        final String url1 = "http://img.funshion.com/sdw?oid=9922513929ba871e30a05d18f9a97c80&w=360&h=504";
        new ImageTask(this)
            .setParameters(R.xml.scaled_params)
            .executeOnExecutor(MainApplication.sThreadPool, url1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBlur.close();
    }
    
    @SuppressLint("NewApi")
    private Bitmap reconfigure() {
        final Options opts = new Options();
        opts.inMutable = true;
        opts.inPreferredConfig = Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.abc, opts);

        final int width  = bitmap.getWidth();
        final int height = bitmap.getHeight();
        bitmap.reconfigure(height, width, bitmap.getConfig());
        return bitmap;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.btn_fill:
            //mDrawable.setCornerRadius(40);
            //mDrawable.setGravity(Gravity.FILL);
            break;

        case R.id.btn_left:
//            mDrawable.setCornerRadius(0);
//            mDrawable.setGravity(Gravity.START);
            break;

        case R.id.btn_right:
            mDrawable.setCornerRadii(50, 50, 50, 50);
//            mDrawable.setGravity(Gravity.END);
            break;

        case R.id.btn_center:
            mDrawable.setCornerRadii(10, 10, 10, 10);
//            mDrawable.setGravity(Gravity.CENTER);
            break;
        }
    }

    private static final class ImageTask extends AsyncImageTask<String> {
        public ImageTask(Activity ownerActivity) {
            super(ownerActivity);
        }

        @Override
        protected void onPostExecute(Object[] results) {
            final BlurActivity activity = getOwnerActivity();
            if (activity != null) {
                activity.mImageView.setImageBitmap((Bitmap)results[0]);
            }
        }
    }
}
