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
import android.graphics.drawable.Drawable;
import android.util.Pair;
import android.util.Printer;

/**
 * Class <tt>IconLoader</tt> allows to load the {@link PackageItemInfo} icon
 * and label on a background thread and bind it to target on the UI thread.
 * @author Garfield
 */
public class IconLoader extends AsyncLoader<String, PackageItemInfo, Pair<Drawable, CharSequence>> {
    /**
     * The application <tt>Context</tt>.
     */
    public final Context mContext;

    /**
     * Constructor
     * @param context The {@link Context}.
     * @param executor The <tt>Executor</tt> to executing load task.
     * @see #IconLoader(Context, Executor, Cache)
     */
    public IconLoader(Context context, Executor executor) {
        this(context, executor, new LruCache<String, Pair<Drawable, CharSequence>>(100));
    }

    /**
     * Constructor
     * @param context The {@link Context}.
     * @param executor The <tt>Executor</tt> to executing load task.
     * @param cache The {@link Cache} to store the loaded icons.
     * @see #IconLoader(Context, Executor)
     */
    public IconLoader(Context context, Executor executor, Cache<String, Pair<Drawable, CharSequence>> cache) {
        super(executor, cache);
        mContext = context.getApplicationContext();
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

    public static void dumpIconCache(Context context, Cache<String, Pair<Drawable, CharSequence>> cache, Printer printer) {
        final Set<Entry<String, Pair<Drawable, CharSequence>>> entries = cache.entries().entrySet();
        final StringBuilder result = new StringBuilder(256);
        if (cache instanceof SimpleLruCache) {
            DebugUtils.dumpSummary(printer, result, 130, " Dumping IconCache [ size = %d, maxSize = %d ] ", entries.size(), ((SimpleLruCache<?, ?>)cache).maxSize());
        } else {
            DebugUtils.dumpSummary(printer, result, 130, " Dumping IconCache [ size = %d ] ", entries.size());
        }

        for (Entry<String, Pair<Drawable, CharSequence>> entry : entries) {
            result.setLength(0);
            final Pair<Drawable, CharSequence> value = entry.getValue();
            printer.println(result.append("  ").append(entry.getKey()).append(" ==> { lable = ").append(value.second).append(", icon = ").append(value.first).append(" }").toString());
        }
    }

    @Override
    protected Pair<Drawable, CharSequence> loadInBackground(Task<?, ?> task, String key, PackageItemInfo[] infos, int flags) {
        final PackageItemInfo info = infos[0];
        final PackageManager pm = mContext.getPackageManager();
        return new Pair<Drawable, CharSequence>(info.loadIcon(pm), info.loadLabel(pm));
    }
}
