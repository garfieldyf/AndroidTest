package android.ext.image.decoder;

import android.content.Context;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.ext.image.ImageLoader;
import android.ext.util.PackageUtils.PackageItemIcon;

/**
 * Class <tt>IconDecoder</tt> used to decode the {@link PackageItemInfo} icon and label.
 * @author Garfield
 */
public final class IconDecoder implements ImageLoader.ImageDecoder<PackageItemIcon> {
    private final PackageManager mPackageManager;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     */
    public IconDecoder(Context context) {
        mPackageManager = context.getPackageManager();
    }

    @Override
    public PackageItemIcon decodeImage(Object uri, Object target, Object[] params, int flags, byte[] tempStorage) {
        return new PackageItemIcon(mPackageManager, (PackageItemInfo)params[0]);
    }
}
