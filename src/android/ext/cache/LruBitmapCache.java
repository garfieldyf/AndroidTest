package android.ext.cache;

import java.util.Map.Entry;
import java.util.Set;
import android.content.Context;
import android.ext.graphics.BitmapUtils;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.text.format.Formatter;
import android.util.Printer;

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
    }

    @Override
    protected int sizeOf(K key, Bitmap value) {
        return value.getAllocationByteCount();
    }

    @Override
    /* package */ void dump(Context context, Printer printer) {
        final Set<Entry<K, Bitmap>> entries = snapshot().entrySet();
        final StringBuilder result = new StringBuilder(384);

        DebugUtils.dumpSummary(printer, result, 130, " Dumping %s [ count = %d, size = %s, maxSize = %s, appMaxSize = %s ] ", getClass().getSimpleName(), entries.size(), Formatter.formatFileSize(context, size()), Formatter.formatFileSize(context, maxSize()), Formatter.formatFileSize(context, Runtime.getRuntime().maxMemory()));
        for (Entry<K, Bitmap> entry : entries) {
            result.setLength(0);
            final Bitmap value = entry.getValue();
            printer.println(BitmapUtils.dumpBitmap(context, result.append("  ").append(entry.getKey()).append(" ==> ").append(value), value).toString());
        }
    }
}
