package android.ext.temp;

import java.util.concurrent.Executor;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.ext.cache.ArrayMapCache;
import android.ext.cache.Cache;
import android.ext.content.AsyncLoader;
import android.ext.util.PackageUtils;
import android.graphics.drawable.Drawable;
import android.util.Pair;

public final class PackageItemLoader extends AsyncLoader<String, PackageItemInfo, Pair<CharSequence, Drawable>> {
    private final Loader mLoader;

    public PackageItemLoader(Context context, Executor executor, boolean fromArchiveFile) {
        this(context, executor, fromArchiveFile, new ArrayMapCache<String, Pair<CharSequence, Drawable>>());
    }

    public PackageItemLoader(Context context, Executor executor, boolean fromArchiveFile, Cache<String, Pair<CharSequence, Drawable>> cache) {
        super(executor, cache);
        mLoader = (fromArchiveFile ? new ArchiveFileLoader(context) : new Loader(context));
    }

    public final void load(PackageItemInfo info, Object target, Binder<String, PackageItemInfo, Pair<CharSequence, Drawable>> binder) {
        load(mLoader.getItemName(info), target, 0, binder, info);
    }

    public final void remove(String itemName) {
        getCache().remove(itemName);
    }

    public final void remove(PackageItemInfo info) {
        getCache().remove(mLoader.getItemName(info));
    }

    @Override
    protected Pair<CharSequence, Drawable> loadInBackground(Task<?, ?> task, String key, PackageItemInfo[] params, int flags) {
        return mLoader.load(params[0]);
    }

    private static class Loader {
        public final Context mContext;

        public Loader(Context context) {
            mContext = context.getApplicationContext();
        }

        public String getItemName(PackageItemInfo info) {
            return info.name;
        }

        public Pair<CharSequence, Drawable> load(PackageItemInfo info) {
            final PackageManager pm = mContext.getPackageManager();
            return new Pair<CharSequence, Drawable>(info.loadLabel(pm), info.loadIcon(pm));
        }
    }

    private static final class ArchiveFileLoader extends Loader {
        public ArchiveFileLoader(Context context) {
            super(context);
        }

        @Override
        public String getItemName(PackageItemInfo info) {
            return info.packageName;
        }

        @Override
        public Pair<CharSequence, Drawable> load(PackageItemInfo info) {
            return PackageUtils.loadApplicationResources(mContext, (ApplicationInfo)info);
        }
    }
}
