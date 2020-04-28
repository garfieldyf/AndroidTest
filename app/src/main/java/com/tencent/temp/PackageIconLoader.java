package com.tencent.temp;

import android.ext.cache.Cache;
import android.ext.image.IconLoader;
import android.ext.image.ImageModule;
import android.ext.util.PackageUtils.PackageItemIcon;

/**
 * Class <tt>PackageIconLoader</tt> allows to load the icon and label from
 * the package archive file's application info on a background thread and
 * bind it to target on the UI thread.
 * <h3>Usage</h3>
 * <p>Here is an example:</p><pre>
 * &lt;xxx.xxx.PackageIconLoader | loader ]
 *      xmlns:app="http://schemas.android.com/apk/res-auto"
 *      class="classFullName"
 *      app:flags="[ none | noMemoryCache ]" /&gt;
 *
 * module.load(R.xml.package_icon_loader, applicationInfo.packageName)
 *       .placeholder(R.drawable.ic_placeholder)
 *       .parameters(applicationInfo)
 *       .binder(binder)
 *       .into(imageView);</pre>
 * @author Garfield
 */
public final class PackageIconLoader<URI> extends IconLoader<URI> {
    /**
     * Constructor
     * @param module The {@link ImageModule}.
     * @param iconCache May be <tt>null</tt>. The {@link Cache} to store the loaded icon.
     */
    public PackageIconLoader(ImageModule module, Cache iconCache) {
        super(module, iconCache);
    }

    @Override
    protected Object loadInBackground(Task task, URI uri, Object[] params, int flags) {
        return new PackageItemIcon(mModule.mContext, ImageModule.getParameters(params));
    }
}
