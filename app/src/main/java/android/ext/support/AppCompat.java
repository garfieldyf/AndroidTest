package android.ext.support;

import android.annotation.TargetApi;
import android.ext.graphics.BitmapUtils;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.os.Build;
import java.util.Map;

/**
 * Class AppCompat
 * @author Garfield
 */
public final class AppCompat {
    public static void clearForRecycle(Options opts) {
        IMPL.clearForRecycle(opts);
    }

    public static int getBytesPerPixel(Options opts) {
        return IMPL.getBytesPerPixel(opts);
    }

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
            opts.inMutable = false;
            opts.inSampleSize  = 0;
            opts.outMimeType   = null;
            opts.inTempStorage = null;
            opts.inTargetDensity = 0;
            opts.inScreenDensity = 0;
            opts.inJustDecodeBounds = false;
            opts.inPreferredConfig  = Config.ARGB_8888;
        }

        public int getBytesPerPixel(Options opts) {
            return BitmapUtils.getBytesPerPixel(opts.inPreferredConfig);
        }

        public void remove(Map<?, ?> map, Object key, Object value) {
            if (map.get(key) == value) {
                map.remove(key);
            }
        }
    }

    /**
     * Class <tt>AppCompatApi24</tt>
     */
    @TargetApi(Build.VERSION_CODES.N)
    /* package */ static class AppCompatApi24 extends AppCompatImpl {
        @Override
        public void remove(Map<?, ?> map, Object key, Object value) {
            map.remove(key, value);
        }
    }

    /**
     * Class <tt>AppCompatApi26</tt>
     */
    @TargetApi(Build.VERSION_CODES.O)
    /* package */ static final class AppCompatApi26 extends AppCompatApi24 {
        @Override
        public void clearForRecycle(Options opts) {
            super.clearForRecycle(opts);
            opts.outConfig     = null;
            opts.outColorSpace = null;
            opts.inPreferredColorSpace = null;
        }

        @Override
        public int getBytesPerPixel(Options opts) {
            return BitmapUtils.getBytesPerPixel(opts.outConfig != null ? opts.outConfig : opts.inPreferredConfig);
        }
    }

    private static final AppCompatImpl IMPL;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            IMPL = new AppCompatApi26();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
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
