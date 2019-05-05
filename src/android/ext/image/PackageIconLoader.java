package android.ext.image;

import java.util.concurrent.Executor;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageItemInfo;
import android.ext.cache.Cache;
import android.ext.content.AsyncLoader;
import android.ext.util.PackageUtils;
import android.ext.util.PackageUtils.IconResult;

/**
 * Class <tt>PackageIconLoader</tt> allows to load a package archive file's application
 * icon and label on a background thread and bind it to target on the UI thread.
 * @see PackageUtils#loadPackageIcon(Context, ApplicationInfo)
 * @author Garfield
 */
public class PackageIconLoader extends IconLoader {
    /**
     * Constructor
     * @param context The {@link Context}.
     * @param executor The <tt>Executor</tt> to executing load task.
     * @param maxSize The maximum number of icons to allow in the internal cache.
     * @see #PackageIconLoader(Context, Executor, Cache)
     */
    public PackageIconLoader(Context context, Executor executor, int maxSize) {
        super(context, executor, maxSize);
    }

    /**
     * Constructor
     * @param context The {@link Context}.
     * @param executor The <tt>Executor</tt> to executing load task.
     * @param cache The {@link Cache} to store the loaded icons.
     * @see #PackageIconLoader(Context, Executor, int)
     */
    public PackageIconLoader(Context context, Executor executor, Cache<String, IconResult> cache) {
        super(context, executor, cache);
    }

    /**
     * Equivalent to calling <tt>load(info.packageName, target, 0, binder, info)</tt>.
     * @see AsyncLoader#load(Key, Object, int, Binder, Params[])
     */
    public final void loadIcon(ApplicationInfo info, Object target, Binder<String, PackageItemInfo, IconResult> binder) {
        load(info.packageName, target, 0, binder, info);
    }

    @Override
    protected IconResult loadInBackground(Task<?, ?, ?> task, String key, PackageItemInfo[] params, int flags) {
        return PackageUtils.loadPackageIcon(mContext, (ApplicationInfo)params[0]);
    }
}
