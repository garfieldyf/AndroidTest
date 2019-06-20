package android.ext.image;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executor;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.ext.cache.Cache;
import android.ext.cache.LruCache;
import android.ext.cache.SimpleLruCache;
import android.ext.content.AsyncLoader;
import android.ext.util.DebugUtils;
import android.ext.util.PackageUtils.PackageItemIcon;
import android.util.Printer;

/**
 * Class <tt>IconLoader</tt> allows to load the {@link ResolveInfo}'s icon
 * and label on a background thread and bind it to target on the UI thread.
 * @author Garfield
 */
public class IconLoader extends AsyncLoader<String, ResolveInfo, PackageItemIcon> {
    private final PackageManager mPackageManager;

    /**
     * Constructor
     * @param context The {@link Context}.
     * @param executor The <tt>Executor</tt> to executing load task.
     * @param maxSize The maximum number of icons to allow in the internal cache.
     * @see #IconLoader(Context, Executor, Cache)
     */
    public IconLoader(Context context, Executor executor, int maxSize) {
        this(context, executor, new LruCache<String, PackageItemIcon>(maxSize));
    }

    /**
     * Constructor
     * @param context The {@link Context}.
     * @param executor The <tt>Executor</tt> to executing load task.
     * @param cache The {@link Cache} to store the loaded icons and labels.
     * @see #IconLoader(Context, Executor, int)
     */
    public IconLoader(Context context, Executor executor, Cache<String, PackageItemIcon> cache) {
        super(executor, cache);
        mPackageManager = context.getPackageManager();
    }

    @Override
    public void dump(Context context, Printer printer) {
        super.dump(context, printer);
        dumpCache(context, getCache(), printer);
    }

    @Override
    protected PackageItemIcon loadInBackground(Task<?, ?> task, String key, ResolveInfo[] params, int flags) {
        return new PackageItemIcon(mPackageManager, params[0]);
    }

    /* package */ static void dumpCache(Context context, Cache<String, PackageItemIcon> cache, Printer printer) {
        final Set<Entry<String, PackageItemIcon>> entries = cache.entries().entrySet();
        final StringBuilder result = new StringBuilder(256);
        if (cache instanceof SimpleLruCache) {
            DebugUtils.dumpSummary(printer, result, 130, " Dumping %s [ size = %d, maxSize = %d ] ", cache.getClass().getSimpleName(), entries.size(), ((SimpleLruCache<?, ?>)cache).maxSize());
        } else {
            DebugUtils.dumpSummary(printer, result, 130, " Dumping %s [ size = %d ] ", cache.getClass().getSimpleName(), entries.size());
        }

        for (Entry<String, PackageItemIcon> entry : entries) {
            result.setLength(0);
            printer.println(entry.getValue().dump(result.append("  ").append(entry.getKey()).append(" ==> ")).toString());
        }
    }
}
