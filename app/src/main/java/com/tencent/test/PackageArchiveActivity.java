package com.tencent.test;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.ext.cache.LruCache;
import android.ext.content.AbsAsyncTask;
import android.ext.content.AsyncLoader.Binder;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.PackageUtils.PackageItemIcon;
import android.ext.util.PackageUtils.PackageParser;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.tencent.temp.BaseListAdapter;
import com.tencent.temp.PackageIconLoader;
import com.tencent.test.PackageActivity.ViewHolder;
import java.util.List;

public class PackageArchiveActivity extends Activity {
    /* package */ ListView mPackageList;
    /* package */ PackageAdapter mAdapter;
    /* package */ PackageIconLoader<String> mIconLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DebugUtils.startMethodTracing();
        setContentView(R.layout.activity_packages);
        DebugUtils.stopMethodTracing("yf", "setContentView");

        DebugUtils.startMethodTracing();
        mIconLoader = new PackageIconLoader<String>(MainApplication.sInstance.getImageModule(), new LruCache<String, Object>(64));
        mPackageList = (ListView)findViewById(R.id.packages);
        mAdapter = new PackageAdapter();
        mPackageList.setAdapter(mAdapter);
        new LoadTask(this).executeOnExecutor(MainApplication.sThreadPool, "/mnt/usb/sda1");
        DebugUtils.stopMethodTracing("yf", "onCreate");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIconLoader.shutdown();
    }

    private final class PackageAdapter extends BaseListAdapter<PackageInfo> implements Binder<String, Object, PackageItemIcon> {
        public PackageAdapter() {
            super(null);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            final View itemView = getLayoutInflater().inflate(R.layout.package_item, parent, false);
            itemView.setTag(new ViewHolder(itemView));
            return itemView;
        }

        @Override
        protected void bindView(PackageInfo pi, int position, View view) {
            final ViewHolder holder = (ViewHolder)view.getTag();
            holder.packageName.setText(pi.packageName);
            mIconLoader.load(pi.packageName)
                .parameters(pi.applicationInfo)
                .placeholder(R.drawable.ic_placeholder)
                .binder(this)
                .into(holder);
        }

        @Override
        public void bindValue(String key, Object[] params, Object target, PackageItemIcon value, int state) {
            final ViewHolder holder = (ViewHolder)target;
            if (value != null) {
                holder.name.setText(value.label);
                holder.icon.setImageDrawable(value.icon);
            } else {
                holder.name.setText(null);
                holder.icon.setImageResource(R.drawable.ic_placeholder);
            }
        }
    }

    private static final class LoadTask extends AbsAsyncTask<String, Object, List<PackageInfo>> {
        public LoadTask(PackageArchiveActivity activity) {
            super(activity);
        }

        @Override
        protected List<PackageInfo> doInBackground(String... params) {
            DebugUtils.startMethodTracing();
            List<PackageInfo> result = new PackageParser(MainApplication.sInstance)
                .addScanFlags(FileUtils.FLAG_IGNORE_HIDDEN_FILE | FileUtils.FLAG_SCAN_FOR_DESCENDENTS)
                .setCancelable(this)
                .parse(params);
            DebugUtils.stopMethodTracing("yf", "parsePackages");

            return result;
        }

        @Override
        protected void onPostExecute(List<PackageInfo> result) {
            final PackageArchiveActivity activity = getOwnerActivity();
            if (activity != null) {
                ((TextView)activity.findViewById(R.id.title)).setText("可安装的应用: " + ArrayUtils.getSize(result));
                activity.mAdapter.changeData(result);
            }
        }
    }
}
