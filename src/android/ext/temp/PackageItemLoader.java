package android.ext.temp;

import java.util.concurrent.Executor;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.ext.cache.Cache;
import android.ext.cache.LruCache;
import android.ext.content.AsyncLoader;
import android.ext.util.PackageUtils;
import android.graphics.drawable.Drawable;
import android.util.Pair;

public final class PackageItemLoader extends AsyncLoader<String, PackageItemInfo, Pair<Drawable, CharSequence>> {
    public static final int FLAG_APPLICATION_INFO = 0;
    public static final int FLAG_ACTIVITY_INFO = 1;
    public static final int FLAG_PACKAGE_ARCHIVE_INFO = 2;
    private final Loader mLoader;

    public PackageItemLoader(Context context, Executor executor, int flags) {
        this(context, executor, flags, new LruCache<String, Pair<Drawable, CharSequence>>(100));
    }

    public PackageItemLoader(Context context, Executor executor, int flags, Cache<String, Pair<Drawable, CharSequence>> cache) {
        super(executor, cache);
        switch (flags) {
        case FLAG_ACTIVITY_INFO:
            mLoader = new ActivityLoader(context);
            break;

        case FLAG_PACKAGE_ARCHIVE_INFO:
            mLoader = new PackageArchiveLoader(context);
            break;

        default:
            mLoader = new Loader(context);
        }
    }

    public final void load(PackageItemInfo info, Object target, Binder<String, PackageItemInfo, Pair<Drawable, CharSequence>> binder) {
        load(mLoader.getItemName(info), target, 0, binder, info);
    }

    public final void remove(String itemName) {
        getCache().remove(itemName);
    }

    public final void remove(PackageItemInfo info) {
        getCache().remove(mLoader.getItemName(info));
    }

    public final Context getContext() {
        return mLoader.mContext;
    }

    @Override
    protected Pair<Drawable, CharSequence> loadInBackground(Task<?, ?> task, String key, PackageItemInfo[] params, int flags) {
        return mLoader.load(params[0]);
    }

    private static class Loader {
        public final Context mContext;

        public Loader(Context context) {
            mContext = context.getApplicationContext();
        }

        public String getItemName(PackageItemInfo info) {
            return info.packageName;
        }

        public Pair<Drawable, CharSequence> load(PackageItemInfo info) {
            final PackageManager pm = mContext.getPackageManager();
            return new Pair<Drawable, CharSequence>(info.loadIcon(pm), info.loadLabel(pm));
        }
    }

    private static final class ActivityLoader extends Loader {
        public ActivityLoader(Context context) {
            super(context);
        }

        @Override
        public String getItemName(PackageItemInfo info) {
            return info.name;
        }
    }

    private static final class PackageArchiveLoader extends Loader {
        public PackageArchiveLoader(Context context) {
            super(context);
        }

        @Override
        public Pair<Drawable, CharSequence> load(PackageItemInfo info) {
            return PackageUtils.loadApplicationIcon(mContext, (ApplicationInfo)info);
        }
    }
}