package android.ext.reference;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

/**
 * Class ReferenceDrawable
 * @author Garfield
 */
public class ReferenceDrawable extends BitmapDrawable implements Referenceable {
    private final AtomicInteger mRefCount = new AtomicInteger();

    public ReferenceDrawable(Resources res, Bitmap bitmap) {
        super(res, bitmap);
    }

    public ReferenceDrawable(Resources res, String filePath) {
        super(res, filePath);
    }

    public ReferenceDrawable(Resources res, InputStream is) {
        super(res, is);
    }

    @Override
    public void draw(Canvas canvas) {
        if (isValid()) {
            super.draw(canvas);
        }
    }

    public void addRef() {
        mRefCount.incrementAndGet();
    }

    public void release() {
        if (mRefCount.decrementAndGet() <= 0 && isValid()) {
            getBitmap().recycle();
        }
    }

    public int referenceCount() {
        return mRefCount.get();
    }

    public int sizeOf() {
        return (isValid() ? getBitmap().getByteCount() : 0);
    }

    public synchronized boolean isValid() {
        final Bitmap bitmap = getBitmap();
        return (bitmap != null && !bitmap.isRecycled());
    }
}
