package com.tencent.test;

import android.app.Activity;
import android.content.pm.PackageItemInfo;
import android.content.pm.ResolveInfo;
import android.ext.content.AsyncTask;
import android.ext.content.AsyncLoader.Binder;
import android.ext.image.ImageLoader.ImageDecoder;
import android.ext.image.ImageModule;
import android.ext.util.ArrayUtils.Filter;
import android.ext.util.DebugUtils;
import android.ext.util.PackageUtils;
import android.ext.util.PackageUtils.PackageItemIcon;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.tencent.temp.BaseListAdapter;
import java.util.List;

public class PackageActivity extends Activity {
    /* package */ ListView mAppList;
    /* package */ AppAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_packages);
        mAppList = (ListView)findViewById(R.id.packages);
        mAdapter = new AppAdapter();
        mAppList.setAdapter(mAdapter);
        new LoadTask(this).execute((Object[])null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private final class AppAdapter extends BaseListAdapter<ResolveInfo> implements Binder<String, Object, PackageItemIcon> {
        public AppAdapter() {
            super(null);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            final View itemView = getLayoutInflater().inflate(R.layout.package_item, parent, false);
            itemView.setTag(new ViewHolder(itemView));
            return itemView;
        }

        @Override
        protected void bindView(ResolveInfo info, int position, View view) {
            final ViewHolder holder = (ViewHolder)view.getTag();
            holder.packageName.setText(PackageUtils.getPackageName(info));
            holder.name.setText(info.loadLabel(getPackageManager()));

            ImageModule.with(PackageActivity.this).load(R.xml.icon_loader, info.activityInfo.name)
                .placeholder(R.drawable.ic_placeholder)
                .parameters(info)
//                .binder(this)
                .into(holder.icon);
//                .into(holder);
        }

        @Override
        public void bindValue(String key, Object[] params, Object target, PackageItemIcon value, int state) {
            final ViewHolder holder = (ViewHolder)target;
            if (value != null) {
                holder.name.setText(value.label);
                holder.icon.setImageDrawable(value.icon);
            } else {
                holder.name.setText(null);
                ImageModule.setPlaceholder(holder.icon, params);
            }
        }
    }

    public static final class ViewHolder {
        public final ImageView icon;
        public final TextView name;
        public final TextView packageName;

        public ViewHolder(View itemView) {
            this.icon = (ImageView)itemView.findViewById(R.id.app_icon);
            this.name = (TextView)itemView.findViewById(R.id.app_name);
            this.packageName = (TextView)itemView.findViewById(R.id.app_package);
        }
    }

    private static final class LoadTask extends AsyncTask<Object, Object, List<ResolveInfo>> implements Filter<ResolveInfo> {
        public LoadTask(PackageActivity activity) {
            super(activity);
        }

        @Override
        protected List<ResolveInfo> doInBackground(Object... params) {
            DebugUtils.startMethodTracing();
            List<ResolveInfo> result = PackageUtils.queryLauncherActivities(MainApplication.sInstance.getPackageManager(), 0, null);
            DebugUtils.stopMethodTracing("yf", "queryLauncherActivities");

            return result;
        }

        @Override
        public boolean accept(ResolveInfo info) {
            return !(MainApplication.sInstance.getPackageName().equals(PackageUtils.getPackageName(info)));
        }

        @Override
        protected void onPostExecute(Object[] params, List<ResolveInfo> result) {
            final PackageActivity activity = getOwner();
            if (activity != null) {
                ((TextView)activity.findViewById(R.id.title)).setText("已安装的应用: " + result.size());
                activity.mAdapter.changeData(result);
            }
        }
    }

    /* package */ final class IconDecoder implements ImageDecoder<Bitmap> {
        @Override
        public Bitmap decodeImage(Object uri, Object target, Object[] params, int flags, byte[] tempStorage) {
            final Drawable icon = ((PackageItemInfo)params[0]).loadIcon(getPackageManager());
            return ((BitmapDrawable)icon).getBitmap();
        }
    }
}
