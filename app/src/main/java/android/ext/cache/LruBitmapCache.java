package android.ext.cache;

import android.content.Context;
import android.ext.graphics.BitmapUtils;
import android.ext.util.DeviceUtils;
import android.ext.util.FileUtils;
import android.graphics.Bitmap;
import android.util.Printer;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Class <tt>LruBitmapCache</tt> is an implementation of a {@link LruCache}.
 * @author Garfield
 */
public class LruBitmapCache<K> extends LruCache<K, Bitmap> {
    private final BitmapPool mBitmapPool;

    /**
     * Constructor
     * @param maxSize The maximum the number of bytes to allow in this cache.
     * @param bitmapPool May be <tt>null</tt>. The {@link BitmapPool} to recycle
     * the evicted bitmap from this cache.
     */
    public LruBitmapCache(int maxSize, BitmapPool bitmapPool) {
        super(maxSize);
        mBitmapPool = bitmapPool;
    }

    /**
     * Returns the {@link BitmapPool} associated with this cache.
     * @return The <tt>BitmapPool</tt> or <tt>null</tt>.
     */
    public final BitmapPool getBitmapPool() {
        return mBitmapPool;
    }

    @Override
    protected int sizeOf(K key, Bitmap value) {
        return value.getAllocationByteCount();
    }

    @Override
    protected void entryRemoved(boolean evicted, K key, Bitmap oldValue, Bitmap newValue) {
        if (mBitmapPool != null && !evicted && oldValue != newValue) {
            mBitmapPool.put(oldValue);
        }
    }

    @Override
    public void dump(Context context, Printer printer) {
        final Set<Entry<K, Bitmap>> entries = snapshot().entrySet();
        final StringBuilder result = new StringBuilder(384);

        DeviceUtils.dumpSummary(printer, result, 130, " Dumping %s [ count = %d, size = %s, maxSize = %s, appMaxSize = %s ] ", getClass().getSimpleName(), entries.size(), FileUtils.formatFileSize(size()), FileUtils.formatFileSize(maxSize()), FileUtils.formatFileSize(Runtime.getRuntime().maxMemory()));
        for (Entry<K, Bitmap> entry : entries) {
            result.setLength(0);
            final Bitmap value = entry.getValue();
            printer.println(BitmapUtils.dumpBitmap(context, result.append("  ").append(entry.getKey()).append(" ==> ").append(value), value).toString());
        }
    }
}
