package android.ext.image.transformer;

import android.ext.graphics.drawable.OvalBitmapDrawable;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Class <tt>OvalTransformer</tt> used to transforms a {@link Bitmap} to a {@link OvalBitmapDrawable}.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;OvalTransformer /&gt;</pre>
 * @author Garfield
 */
public final class OvalTransformer implements Transformer<Object, Bitmap> {
    private static final OvalTransformer sInstance = new OvalTransformer();

    /**
     * Constructor
     */
    private OvalTransformer() {
    }

    /**
     * Returns a type-safe {@link Transformer} to transforms a {@link Bitmap} to an {@link OvalBitmapDrawable}.
     * @return The <tt>Transformer</tt>.
     */
    @SuppressWarnings("unchecked")
    public static <URI> Transformer<URI, Bitmap> getInstance() {
        return (Transformer<URI, Bitmap>)sInstance;
    }

    @Override
    public Drawable transform(Object uri, Bitmap bitmap) {
        return new OvalBitmapDrawable(bitmap);
    }
}
