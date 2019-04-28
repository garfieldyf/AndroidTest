package android.ext.image;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executor;
import android.content.Context;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.ext.cache.Cache;
import android.ext.cache.LruCache;
import android.ext.cache.SimpleLruCache;
import android.ext.content.AsyncLoader;
import android.ext.util.DebugUtils;
import android.ext.util.PackageUtils.IconResult;
import android.util.Printer;

/**
 * Class <tt>IconLoader</tt> allows to load the {@link PackageItemInfo} icon
 * and label on a background thread and bind it to target on the UI thread.
 * @author Garfield
 */
public class IconLoader extends AsyncLoader<String, PackageItemInfo, IconResult> {
    /**
     * The application <tt>Context</tt>.
     */
    public final Context mContext;

    /**
     * Constructor
     * @param context The {@link Context}.
     * @param executor The <tt>Executor</tt> to executing load task.
     * @param maxSize The maximum number of icons to allow in the internal cache.
     * @see #IconLoader(Context, Executor, Cache)
     */
    public IconLoader(Context context, Executor executor, int maxSize) {
        this(context, executor, new LruCache<String, IconResult>(maxSize));
    }

    /**
     * Constructor
     * @param context The {@link Context}.
     * @param executor The <tt>Executor</tt> to executing load task.
     * @param cache The {@link Cache} to store the loaded icons.
     * @see #IconLoader(Context, Executor, int)
     */
    public IconLoader(Context context, Executor executor, Cache<String, IconResult> cache) {
        super(executor, cache);
        mContext = context.getApplicationContext();
    }

    /**
     * Equivalent to calling <tt>load(info.name, target, 0, binder, info)</tt>.
     * @see AsyncLoader#load(Key, Object, int, Binder, Params[])
     */
    public final void loadIcon(PackageItemInfo info, Object target, Binder<String, PackageItemInfo, IconResult> binder) {
        load(info.name, target, 0, binder, info);
    }

    /**
     * Removes the icon from the specified <em>key</em>.
     * @param key The key to find.
     */
    public final void remove(String key) {
        getCache().remove(key);
    }

    @Override
    public void dump(Context context, Printer printer) {
        super.dump(context, printer);
        dumpIconCache(context, getCache(), printer);
    }

    public static void dumpIconCache(Context context, Cache<String, IconResult> cache, Printer printer) {
        final Set<Entry<String, IconResult>> entries = cache.entries().entrySet();
        final StringBuilder result = new StringBuilder(256);
        if (cache instanceof SimpleLruCache) {
            DebugUtils.dumpSummary(printer, result, 130, " Dumping %s [ size = %d, maxSize = %d ] ", cache.getClass().getSimpleName(), entries.size(), ((SimpleLruCache<?, ?>)cache).maxSize());
        } else {
            DebugUtils.dumpSummary(printer, result, 130, " Dumping %s [ size = %d ] ", cache.getClass().getSimpleName(), entries.size());
        }

        for (Entry<String, IconResult> entry : entries) {
            result.setLength(0);
            printer.println(entry.getValue().dump(result.append("  ").append(entry.getKey()).append(" ==> ")).toString());
        }
    }

    @Override
    protected IconResult loadInBackground(Task<?, ?> task, String key, PackageItemInfo[] params, int flags) {
        final PackageItemInfo info = params[0];
        final PackageManager pm = mContext.getPackageManager();
        return new IconResult(info.loadIcon(pm), info.loadLabel(pm));
    }
}
