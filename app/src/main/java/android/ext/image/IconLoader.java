package android.ext.image;

import static android.ext.image.ImageModule.PARAMETERS;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.ext.cache.Cache;
import android.ext.content.Task;
import android.ext.util.DebugUtils;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * Class <tt>IconLoader</tt> allows to load the icon associated with the {@link ResolveInfo}
 * or {@link PackageItemInfo} on a background thread and bind it to target on the UI thread.
 * <h3>Usage</h3>
 * <p>Here is an example:</p><pre>
 * &lt;[ IconLoader | loader ]
 *     xmlns:app="http://schemas.android.com/apk/res-auto"
 *     class="classFullName"
 *     app:flags="[ none | noMemoryCache ]" /&gt;
 *
 * ImageModule.getInstance(context)
 *     .load(R.xml.icon_loader, [ resolveInfo.activityInfo.name | packageItemInfo.packageName ])
 *     .placeholder(R.drawable.ic_placeholder)
 *     .parameters([ resolveInfo | packageItemInfo ])
 *     .into(imageView);</pre>
 * @author Garfield
 */
public class IconLoader extends ImageLoader<Object> {
    protected final PackageManager mPackageManager;

    /**
     * Constructor
     * @param module The {@link ImageModule}.
     * @param iconCache May be <tt>null</tt>. The {@link Cache} to store the loaded icon.
     */
    protected IconLoader(ImageModule module, Cache<Object, Object> iconCache) {
        super(module, iconCache);
        mPackageManager = module.mContext.getPackageManager();
    }

    @Override
    public void bindValue(Object uri, Object[] params, Object target, Object value, int state) {
        final ImageView view = (ImageView)target;
        if (value != null) {
            view.setImageDrawable((Drawable)value);
        } else if ((state & STATE_LOAD_FROM_BACKGROUND) == 0) {
            ImageModule.setPlaceholder(view, params);
        }
    }

    @Override
    protected Object loadInBackground(Task task, Object uri, Object[] params, int flags) {
        final Object param = params[PARAMETERS];
        DebugUtils.__checkError(param == null, "Invalid parameter - param == null");
        if (param instanceof ResolveInfo) {
            return ((ResolveInfo)param).loadIcon(mPackageManager);
        } else if (param instanceof PackageItemInfo) {
            return ((PackageItemInfo)param).loadIcon(mPackageManager);
        } else {
            return mPackageManager.getDefaultActivityIcon();
        }
    }
}
