package android.ext.image;

import static android.ext.image.ImageModule.PARAMETERS;
import android.content.pm.ResolveInfo;
import android.ext.cache.Cache;

/**
 * Class <tt>IconLoader</tt> allows to load the icon from the URI
 * on a background thread and bind it to target on the UI thread.
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
public class IconLoader<URI> extends ImageLoader<URI, Object> {
    /**
     * Constructor
     * @param module The {@link ImageModule}.
     * @param iconCache May be <tt>null</tt>. The {@link Cache} to store the loaded icon.
     */
    public IconLoader(ImageModule module, Cache iconCache) {
        super(module, iconCache);
    }

    @Override
    protected Object loadInBackground(Task task, URI uri, Object[] params, int flags) {
        return ((ResolveInfo)params[PARAMETERS]).loadIcon(mModule.mContext.getPackageManager());
    }
}
