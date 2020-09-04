package com.tencent.test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.ext.util.FileUtils;
import android.ext.util.FileUtils.Dirent;
import android.ext.util.FileUtils.Stat;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.tencent.temp.BaseListAdapter;
import java.net.HttpURLConnection;

public class MainActivity extends Activity {
    private ListView mListView;
    private int mCount;
    private FileAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.leakback);

//        Options opts = new Options();
//        opts.inPreferredConfig = Config.RGB_565;
//        opts.inMutable = true;
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.abc, opts);
//        BlurUtils.blurBitmap(this, bitmap, 5.0f);
//        ((ImageView)findViewById(R.id.image)).setImageBitmap(bitmap);

        final String dir = Environment.getExternalStorageDirectory().getPath();
        mListView = (ListView)findViewById(R.id.list);
        mAdapter = new FileAdapter(this, dir);
        mListView.setAdapter(mAdapter);
    }

    public void onOptClicked(View view) {
        final Intent intent = new Intent("com.funshion.android.intent.action.FUN_TV_CC_BUTTON");
        sendBroadcast(intent);
    }

    public void onMemoryClicked(View view) {
        final Intent intent = new Intent();
        intent.setClassName("tv.fun.master", "tv.fun.master.ui.activity.MemoryClearActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void onCleanClicked(View view) {
        final Intent intent = new Intent();
        intent.setClassName("tv.fun.master", "tv.fun.master.ui.activity.StorageCleanActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void onTestClicked(View view) {
        final Intent intent = new Intent();
        intent.setClassName("tv.fun.master", "tv.fun.master.ui.activity.NetworkTestActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void onSelfClicked(View view) {
        final Intent intent = new Intent();
        intent.setClassName("tv.fun.master", "tv.fun.master.ui.activity.SelfStartManageActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void onRebootClicked(View view) {
        final Intent intent = new Intent();
        intent.setClassName("tv.fun.master", "tv.fun.master.ui.activity.RebootActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private static final class FileEntry2 {
        private Stat mStat;
        private Dirent mDirent;

        private FileEntry2(String path, int type) {
            mDirent = new Dirent(path, type);
//            Log.i("yf", "FileEntry2 ctor");
        }

        public Stat stat(String parent) {
            if (mStat == null) {
                mStat = FileUtils.stat(mDirent.path);
            }

            return mStat;
        }

        public boolean isHidden() {
            return mDirent.isHidden();
        }

        public String getExtension() {
            return mDirent.getExtension();
        }

        public String getMimeType() {
            return mDirent.getMimeType();
        }

        public boolean isDirectory() {
            return mDirent.isDirectory();
        }

        public String getPath() {
            return mDirent.path;
        }
    }

    private static final class FileEntry1 {
        private Stat mStat;
        private String mPath;
        private int mType;

        private FileEntry1(String path, int type) {
            mPath = path;
            mType = type;
//            Log.i("yf", "FileEntry1 ctor");
        }

        public Stat stat(String parent) {
            if (mStat == null) {
                mStat = FileUtils.stat(parent + "/" + mPath);
            }

            return mStat;
        }

        public boolean isHidden() {
            return (mPath.charAt(0) != '/' && mPath.charAt(mPath.lastIndexOf('/') + 1) == '.');
        }

        public String getExtension() {
            if (mType != Dirent.DT_DIR) {
                final int index = FileUtils.findFileExtension(mPath);
                if (index != -1) {
                    return mPath.substring(index);
                }
            }

            return null;
        }

        public String getMimeType() {
            return (mType != Dirent.DT_DIR ? HttpURLConnection.getFileNameMap().getContentTypeFor(mPath) : null);
        }

        public boolean isDirectory() {
            return (mType == Dirent.DT_DIR);
        }

        public String getPath() {
            return mPath;
        }
    }

    private static final class FileEntry {
    }

    private final class FileAdapter extends BaseListAdapter<FileEntry> {
        private final String path;

        public FileAdapter(Context context, String path) {
            super(null);
            this.path = path;
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            return View.inflate(MainActivity.this, android.R.layout.simple_list_item_2, null);
        }

        @Override
        protected void bindView(FileEntry entry, int position, View view) {
//            final StringBuilder text = new StringBuilder(entry.toString())
//                .append('\n').append(entry.stat().toString())
//                .append('\n').append("hide = ").append(entry.isHidden())
//                             .append(", ext = ").append(entry.getExtension())
//                             .append(", mime = ").append(entry.getMimeType());
//
//            if (entry.isDirectory()) {
//                final String[] files = new File(path + '/' + entry.getPath()).list();
//                text.append(", files = ").append(ArrayUtils.getSize(files));
//            }

//            ((TextView)view.findViewById(android.R.id.text1)).setText(entry.getPath());
//            ((TextView)view.findViewById(android.R.id.text2)).setText(text);
        }
    }
}
