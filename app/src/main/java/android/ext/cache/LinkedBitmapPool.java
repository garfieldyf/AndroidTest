package android.ext.cache;

import android.content.Context;
import android.ext.graphics.BitmapUtils;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.util.Printer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Class <tt>LinkedBitmapPool</tt> is an implementation of a {@link BitmapPool}.
 * @author Garfield
 */
public class LinkedBitmapPool implements BitmapPool, Comparator<Bitmap> {
    private final int mMaxSize;
    private final LinkedList<Bitmap> mBitmaps;

    /**
     * Constructor
     * @param maxSize The maximum number of bitmaps to allow in this pool.
     */
    public LinkedBitmapPool(int maxSize) {
        DebugUtils.__checkError(maxSize <= 0, "maxSize <= 0");
        mMaxSize = maxSize;
        mBitmaps = new LinkedList<Bitmap>();
    }

    @Override
    public synchronized void clear() {
        mBitmaps.clear();
    }

    @Override
    public synchronized Bitmap get(int size) {
        final Iterator<Bitmap> itor = mBitmaps.iterator();
        while (itor.hasNext()) {
            final Bitmap bitmap = itor.next();
            if (bitmap.getAllocationByteCount() >= size) {
                itor.remove();
                return bitmap;
            }
        }

        return null;
    }

    @Override
    public synchronized void put(Bitmap bitmap) {
        DebugUtils.__checkError(bitmap == null, "bitmap == null");
        DebugUtils.__checkWarning(bitmap.isRecycled(), "LinkedBitmapPool", "The bitmap is recycled, couldn't to reused.");
        DebugUtils.__checkWarning(!bitmap.isMutable(), "LinkedBitmapPool", "The bitmap is immutable, couldn't to reused.");
        if (bitmap.isMutable() && !bitmap.isRecycled()) {
            // Inserts the bitmap into the mBitmaps at the appropriate position.
            ArrayUtils.insert(mBitmaps, bitmap, this);

            // Removes the smallest bitmaps until the mBitmaps.size() is less the mMaxSize.
            while (mBitmaps.size() > mMaxSize) {
                mBitmaps.removeFirst();
            }
        }
    }

    @Override
    public int compare(Bitmap one, Bitmap another) {
        return (one.getAllocationByteCount() - another.getAllocationByteCount());
    }

    private synchronized List<Bitmap> snapshot() {
        return new ArrayList<Bitmap>(mBitmaps);
    }

    /* package */ final void dump(Context context, Printer printer) {
        final List<Bitmap> bitmaps = snapshot();
        final int size = bitmaps.size();
        final StringBuilder result = new StringBuilder(288);
        DebugUtils.dumpSummary(printer, result, 130, " Dumping %s [ size = %d, maxSize = %d ] ", getClass().getSimpleName(), size, mMaxSize);
        for (int i = 0; i < size; ++i) {
            result.setLength(0);
            final Bitmap bitmap = bitmaps.get(i);
            printer.println(BitmapUtils.dumpBitmap(context, result.append("  ").append(bitmap), bitmap).toString());
        }
    }
}
