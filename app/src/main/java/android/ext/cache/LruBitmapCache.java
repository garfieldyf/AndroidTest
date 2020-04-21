package android.ext.cache;

import android.content.Context;
import android.ext.graphics.BitmapUtils;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.graphics.Bitmap;
import android.support.v4.graphics.BitmapCompat;
import android.util.Printer;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Class <tt>LruBitmapCache</tt> is an implementation of a {@link LruCache}.
 * @author Garfield
 */
public class LruBitmapCache<K> extends LruCache<K, Bitmap> {
    /**
     * Constructor
     * @param maxSize The maximum the number of bytes to allow in this cache.
     * @see #LruBitmapCache(float)
     */
    public LruBitmapCache(int maxSize) {
        super(maxSize);
    }

    /**
     * Constructor
     * @param scaleMemory The scale of memory, expressed as a percentage
     * of this application maximum memory of the current device.
     * @see #LruBitmapCache(int)
     */
    public LruBitmapCache(float scaleMemory) {
        super((int)(Runtime.getRuntime().maxMemory() * scaleMemory + 0.5f));
        DebugUtils.__checkError(Float.compare(scaleMemory, +1.0f) >= 0, "scaleMemory >= 1.0f");
    }

    @Override
    protected int sizeOf(K key, Bitmap value) {
        return BitmapCompat.getAllocationByteCount(value);
    }

    @Override
    public void dump(Context context, Printer printer) {
        final Set<Entry<K, Bitmap>> entries = snapshot().entrySet();
        final StringBuilder result = new StringBuilder(384);

        DebugUtils.dumpSummary(printer, result, 130, " Dumping %s [ count = %d, size = %s, maxSize = %s, appMaxSize = %s ] ", getClass().getSimpleName(), entries.size(), FileUtils.formatFileSize(size()), FileUtils.formatFileSize(maxSize()), FileUtils.formatFileSize(Runtime.getRuntime().maxMemory()));
        for (Entry<K, Bitmap> entry : entries) {
            result.setLength(0);
            final Bitmap value = entry.getValue();
            printer.println(BitmapUtils.dumpBitmap(context, result.append("  ").append(entry.getKey()).append(" ==> ").append(value), value).toString());
        }
    }
}
