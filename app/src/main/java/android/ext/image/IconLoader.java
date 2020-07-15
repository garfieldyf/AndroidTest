package android.ext.image;

import static android.ext.image.ImageModule.PARAMETERS;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.ext.cache.Cache;
import android.ext.cache.LruCache;

/**
 * Class <tt>IconLoader</tt> allows to load the icon associated with the
 * {@link ResolveInfo} on a background thread and bind it to target on
 * the UI thread.
 * <h3>Usage</h3>
 * <p>Here is an example:</p><pre>
 * &lt;[ IconLoader | loader ]
 *      xmlns:app="http://schemas.android.com/apk/res-auto"
 *      class="classFullName"
 *      app:flags="[ none | noMemoryCache ]" /&gt;
 *
 * module.load(R.xml.icon_loader, resolveInfo.activityInfo.name)
 *       .placeholder(R.drawable.ic_placeholder)
 *       .parameters(resolveInfo)
 *       .into(imageView);</pre>
 * @author Garfield
 */
public final class IconLoader<URI> extends ImageLoader<URI, Object> {
    private final PackageManager mPackageManager;

    /**
     * Constructor
     * @param module The {@link ImageModule}.
     * @see #IconLoader(ImageModule, Cache)
     */
    public IconLoader(ImageModule<?, ?> module) {
        this(module, new LruCache<URI, Object>(64));
    }

    /**
     * Constructor
     * @param module The {@link ImageModule}.
     * @param iconCache May be <tt>null</tt>. The {@link Cache} to store the loaded icon.
     * @see #IconLoader(ImageModule)
     */
    public IconLoader(ImageModule<?, ?> module, Cache<URI, Object> iconCache) {
        super(module, iconCache);
        mPackageManager = module.mContext.getPackageManager();
    }

    @Override
    protected Object loadInBackground(Task task, URI uri, Object[] params, int flags) {
        return ((ResolveInfo)params[PARAMETERS]).loadIcon(mPackageManager);
    }
}
