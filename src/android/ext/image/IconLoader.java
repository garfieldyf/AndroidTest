package android.ext.image;

import java.util.concurrent.Executor;
import android.content.Context;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.ext.cache.Cache;
import android.ext.cache.LruCache;
import android.ext.content.AsyncLoader;
import android.graphics.drawable.Drawable;
import android.util.Pair;

/**
 * Class <tt>IconLoader</tt> allows to load the {@link PackageItemInfo} icon
 * and label on a background thread and bind it to target on the UI thread.
 * @author Garfield
 */
public final class IconLoader extends AsyncLoader<String, PackageItemInfo, Pair<Drawable, CharSequence>> {
    private final PackageManager mPackageManager;

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
        mPackageManager = context.getPackageManager();
    }

    /**
     * Removes the icon from the specified <em>key</em>.
     * @param key The key to find.
     */
    public final void remove(String key) {
        getCache().remove(key);
    }

    @Override
    protected Pair<Drawable, CharSequence> loadInBackground(Task<?, ?> task, String key, PackageItemInfo[] infos, int flags) {
        final PackageItemInfo info = infos[0];
        return new Pair<Drawable, CharSequence>(info.loadIcon(mPackageManager), info.loadLabel(mPackageManager));
    }
}
