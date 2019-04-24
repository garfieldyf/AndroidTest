package android.ext.image;

import java.util.concurrent.Executor;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageItemInfo;
import android.ext.cache.Cache;
import android.ext.util.PackageUtils;
import android.graphics.drawable.Drawable;
import android.util.Pair;

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
     * @see #PackageIconLoader(Context, Executor, Cache)
     */
    public PackageIconLoader(Context context, Executor executor) {
        super(context, executor);
    }

    /**
     * Constructor
     * @param context The {@link Context}.
     * @param executor The <tt>Executor</tt> to executing load task.
     * @param cache The {@link Cache} to store the loaded icons.
     * @see #PackageIconLoader(Context, Executor)
     */
    public PackageIconLoader(Context context, Executor executor, Cache<String, Pair<Drawable, CharSequence>> cache) {
        super(context, executor, cache);
    }

    @Override
    protected Pair<Drawable, CharSequence> loadInBackground(Task<?, ?> task, String key, PackageItemInfo[] infos, int flags) {
        return PackageUtils.loadPackageIcon(mContext, (ApplicationInfo)infos[0]);
    }
}
