package android.ext.image;

import static android.ext.image.ImageModule.PARAMETERS;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.ext.cache.Cache;

/**
 * Class <tt>IconLoader</tt> allows to load the icon associated with the
 * {@link ResolveInfo} or {@link PackageItemInfo} on a background thread
 * and bind it to target on the UI thread.
 * <h3>Usage</h3>
 * <p>Here is an example:</p><pre>
 * &lt;[ IconLoader | loader ]
 *      xmlns:app="http://schemas.android.com/apk/res-auto"
 *      class="classFullName"
 *      app:flags="[ none | noMemoryCache ]" /&gt;
 *
 * module.load(R.xml.icon_loader, resolveInfo.activityInfo.name or packageItemInfo.name)
 *       .placeholder(R.drawable.ic_placeholder)
 *       .parameters(resolveInfo or packageItemInfo)
 *       .into(imageView);</pre>
 * @author Garfield
 */
public class IconLoader<URI> extends ImageLoader<URI, Object> {
    protected final PackageManager mPackageManager;

    /**
     * Constructor
     * @param module The {@link ImageModule}.
     * @param iconCache May be <tt>null</tt>. The {@link Cache} to store the loaded icon.
     */
    public IconLoader(ImageModule module, Cache iconCache) {
        super(module, iconCache);
        mPackageManager = module.mContext.getPackageManager();
    }

    @Override
    protected Object loadInBackground(Task task, URI uri, Object[] params, int flags) {
        final Object param = params[PARAMETERS];
        if (param instanceof ResolveInfo) {
            return ((ResolveInfo)param).loadIcon(mPackageManager);
        } else if (param instanceof PackageItemInfo) {
            return ((PackageItemInfo)param).loadIcon(mPackageManager);
        } else {
            return null;
        }
    }
}
