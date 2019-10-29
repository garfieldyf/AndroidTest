package com.tencent.test;

import android.app.Activity;
import android.ext.graphics.drawable.GIFBaseDrawable;
import android.ext.graphics.drawable.GIFBaseDrawable.AnimationCallback;
import android.ext.graphics.drawable.GIFDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class DrawActivity extends Activity implements AnimationCallback {
    private ImageView mImageView;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_test);

//        final GIFDrawable drawable = GIFDrawable.decode(getResources(), R.raw.bbb);
//        drawable.setAnimationCallback(this);
        mImageView = (ImageView)findViewById(R.id.image1);
//        mImageView.setImageDrawable(drawable);
        mTextView = (TextView)findViewById(R.id.start);
    }

    public void onPlayClicked(View view) {
        final GIFDrawable drawable = (GIFDrawable)mImageView.getDrawable();
        if (drawable.isRunning()) {
            drawable.stop();
            ((TextView)view).setText("Play");
        } else {
            drawable.start();
            ((TextView)view).setText("Stop");
        }
    }

    public void onOneShotClicked(View view) {
        final GIFDrawable drawable = (GIFDrawable)mImageView.getDrawable();
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

//        final List<FileEntry> entries = new ArrayList<FileEntry>();
//        FileUtils.scanFiles("", new ScanCallback() {
//            @Keep
//            @Override
//            @SuppressWarnings("unchecked")
//            public int onScanFile(String path, int type, Object cookie) {
//                ((List<FileEntry>)cookie).add(new FileEntry(path, type));
//                return SC_CONTINUE;
//            }
//        }, 0, entries);
    }

//    private static final class FileEntry {
//        private final Dirent dirent;
//
//        public FileEntry(String path, int type) {
//            dirent = new Dirent(path, type);
//        }
//    }
}
