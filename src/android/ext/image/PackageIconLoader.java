package android.ext.image;

import java.util.concurrent.Executor;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.ext.cache.Cache;
import android.ext.cache.LruCache;
import android.ext.content.AsyncLoader;
import android.ext.util.PackageUtils;
import android.ext.util.PackageUtils.PackageItemIcon;
import android.util.Printer;

/**
 * Class <tt>PackageIconLoader</tt> allows to load a package archive file's
 * icon and label on a background thread and bind it to target on the UI thread.
 * @see PackageUtils#loadPackageArchiveIcon(Context, ApplicationInfo)
 * @author Garfield
 */
public class PackageIconLoader extends AsyncLoader<String, ApplicationInfo, PackageItemIcon> {
    /**
     * The application <tt>Context</tt>.
     */
    public final Context mContext;

    /**
     * Constructor
     * @param context The {@link Context}.
     * @param executor The <tt>Executor</tt> to executing load task.
     * @param maxSize The maximum number of icons to allow in the internal cache.
     * @see #PackageIconLoader(Context, Executor, Cache)
     */
    public PackageIconLoader(Context context, Executor executor, int maxSize) {
        this(context, executor, new LruCache<String, PackageItemIcon>(maxSize));
    }

    /**
     * Constructor
     * @param context The {@link Context}.
     * @param executor The <tt>Executor</tt> to executing load task.
     * @param cache The {@link Cache} to store the loaded icons and labels.
     * @see #PackageIconLoader(Context, Executor, int)
     */
    public PackageIconLoader(Context context, Executor executor, Cache<String, PackageItemIcon> cache) {
        super(executor, cache);
        mContext = context.getApplicationContext();
    }

    /**
     * Equivalent to calling <tt>load(info.packageName, target, 0, binder, info)</tt>.
     * @see AsyncLoader#load(Key, Object, int, Binder, Params[])
     */
    public final void loadIcon(ApplicationInfo info, Object target, Binder<String, ApplicationInfo, PackageItemIcon> binder) {
        load(info.packageName, target, 0, binder, info);
    }

    @Override
    public void dump(Context context, Printer printer) {
        super.dump(context, printer);
        IconLoader.dumpCache(context, getCache(), printer);
    }

    @Override
    protected PackageItemIcon loadInBackground(Task<?, ?> task, String key, ApplicationInfo[] params, int flags) {
        return PackageUtils.loadPackageArchiveIcon(mContext, params[0]);
    }
}
