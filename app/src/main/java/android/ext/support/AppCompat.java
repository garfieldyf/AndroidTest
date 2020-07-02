package android.ext.support;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.ext.graphics.BitmapUtils;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import java.io.File;
import java.util.Map;

/**
 * Class AppCompat
 * @author Garfield
 */
public final class AppCompat {
    /**
     * Install a package with the specified <em>packageFile</em>.
     * @param context The <tt>Context</tt>.
     * @param authority The authority of a {@link FileProvider} defined
     * in a <tt>&lt;provider&gt;</tt> element in your app's manifest.
     * @param packageFile The location of the package file to install.
     */
    public static void installPackage(Context context, String authority, File packageFile) {
        IMPL.installPackage(context, authority, packageFile);
    }

    /**
     * Called on the <tt>AbsImageDecoder</tt> internal, do not call this method directly.
     * @hide
     */
    public static void clearForRecycle(Options opts) {
        IMPL.clearForRecycle(opts);
    }

    /**
     * Called on the <tt>Parameters</tt> internal, do not call this method directly.
     * @hide
     */
    public static int getBytesPerPixel(Options opts) {
        return IMPL.getBytesPerPixel(opts);
    }

    /**
     * Called on the <tt>Loader</tt> internal, do not call this method directly.
     * @hide
     */
    public static void remove(Map<?, ?> map, Object key, Object value) {
        IMPL.remove(map, key, value);
    }

    /**
     * Class <tt>AppCompatImpl</tt>
     */
    /* package */ static class AppCompatImpl {
        public void clearForRecycle(Options opts) {
            opts.inBitmap  = null;
            opts.inDensity = 0;
            opts.outWidth  = 0;
            opts.outHeight = 0;
            opts.inScaled  = true;
            opts.inSampleSize  = 0;
            opts.outMimeType   = null;
            opts.inTempStorage = null;
            opts.inTargetDensity = 0;
            opts.inScreenDensity = 0;
            opts.inJustDecodeBounds = false;
            opts.inPreferredConfig  = Config.ARGB_8888;
        }

        public int getBytesPerPixel(Options opts) {
            DebugUtils.__checkError(opts.inPreferredConfig == null, "opts.inPreferredConfig == null");
            return BitmapUtils.getBytesPerPixel(opts.inPreferredConfig);
        }

        public void remove(Map<?, ?> map, Object key, Object value) {
            if (map.get(key) == value) {
                map.remove(key);
            }
        }

        public void installPackage(Context context, String authority, File packageFile) {
            DebugUtils.__checkError(packageFile == null, "packageFile == null");
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(packageFile), "application/vnd.android.package-archive");
            context.startActivity(intent);
        }
    }

    /**
     * Class <tt>AppCompatApi24</tt>
     */
    @TargetApi(24)
    /* package */ static class AppCompatApi24 extends AppCompatImpl {
        @Override
        public void remove(Map<?, ?> map, Object key, Object value) {
            map.remove(key, value);
        }

        @Override
        public void installPackage(Context context, String authority, File packageFile) {
            DebugUtils.__checkError(packageFile == null, "packageFile == null");
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(FileProvider.getUriForFile(context, authority, packageFile), "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        }
    }

    /**
     * Class <tt>AppCompatApi26</tt>
     */
    @TargetApi(26)
    /* package */ static final class AppCompatApi26 extends AppCompatApi24 {
        @Override
        public void clearForRecycle(Options opts) {
            super.clearForRecycle(opts);
            opts.outConfig = null;
            opts.outColorSpace = null;
            opts.inPreferredColorSpace = null;
        }

        @Override
        public int getBytesPerPixel(Options opts) {
            DebugUtils.__checkError(opts.outConfig == null && opts.inPreferredConfig == null, "opts.outConfig == null && opts.inPreferredConfig == null");
            DebugUtils.__checkDebug(true, "AppCompat", "getBytesPerPixel - outConfig = " + opts.outConfig);
            return BitmapUtils.getBytesPerPixel(opts.outConfig != null ? opts.outConfig : opts.inPreferredConfig);
        }
    }

    private static final AppCompatImpl IMPL;

    static {
        if (Build.VERSION.SDK_INT >= 26) {
            IMPL = new AppCompatApi26();
        } else if (Build.VERSION.SDK_INT >= 24) {
            IMPL = new AppCompatApi24();
        } else {
            IMPL = new AppCompatImpl();
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private AppCompat() {
    }
}