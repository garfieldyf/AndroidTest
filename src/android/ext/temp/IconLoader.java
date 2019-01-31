package android.ext.temp;

import java.util.concurrent.Executor;
import android.content.Context;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.ext.cache.LruCache;
import android.ext.content.AsyncLoader;
import android.ext.content.AsyncLoader.Binder;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * Class <tt>IconLoader</tt> allows to load the icon from the {@link PackageItemInfo}
 * on a background thread and bind it to target on the UI thread.
 * @author Garfield
 */
public class IconLoader extends AsyncLoader<String, PackageItemInfo, Drawable> implements Binder<String, PackageItemInfo, Drawable> {
    protected final Drawable mDefaultImage;
    protected final PackageManager mPackageManager;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param executor The {@link Executor} to execute load task.
     * @param defaultImage May be <tt>null</tt>. The <tt>Drawable</tt> to be used when the icon is loading.
     * @see #IconLoader(Context, Executor, Drawable, int)
     */
    public IconLoader(Context context, Executor executor, Drawable defaultImage) {
        this(context, executor, defaultImage, 64);
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param executor The {@link Executor} to execute load task.
     * @param defaultImage May be <tt>null</tt>. The <tt>Drawable</tt> to be used when the icon is loading.
     * @param maxIconSize The maximum number of icons to allow in the internal cache.
     * @see #IconLoader(Context, Executor, Drawable)
     */
    public IconLoader(Context context, Executor executor, Drawable defaultImage, int maxIconSize) {
        super(executor, new LruCache<String, Drawable>(maxIconSize));
        mDefaultImage = defaultImage;
        mPackageManager = context.getPackageManager();
    }

    /**
     * Equivalent to calling <tt>loadIcon(info, target, this)</tt>.
     * @param info The {@link PackageItemInfo} to load.
     * @param target The <tt>Object</tt> to bind.
     * @see #loadIcon(PackageItemInfo, Object, Binder)
     */
    public final void loadIcon(PackageItemInfo info, Object target) {
        load(info.name, target, 0, this, info);
    }

    /**
     * Loads the icon from the specified <em>info</em>, bind it to the target. If the icon
     * is already cached, it is bind immediately. Otherwise loads the icon on a background
     * thread. <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param info The {@link PackageItemInfo} to load.
     * @param target The <tt>Object</tt> to bind.
     * @param binder The {@link Binder} used to bind the icon to <em>target</em>.
     * @see #loadIcon(PackageItemInfo, Object)
     */
    public final void loadIcon(PackageItemInfo info, Object target, Binder<String, PackageItemInfo, Drawable> binder) {
        load(info.name, target, 0, binder, info);
    }

    /**
     * Returns the default image associated with this loader.
     * @return The <tt>Drawable</tt> or <tt>null</tt>.
     */
    public final Drawable getDefaultImage() {
        return mDefaultImage;
    }

    @Override
    public void bindValue(String name, PackageItemInfo[] infos, Object target, Drawable value, int state) {
        ((ImageView)target).setImageDrawable(value != null ? value : mDefaultImage);
    }

    @Override
    protected Drawable loadInBackground(Task<?, ?> task, String name, PackageItemInfo[] infos, int flags) {
        return infos[0].loadIcon(mPackageManager);
    }
}
